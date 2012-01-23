/*
 * DomUI Java User Interface - shared code
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.telnet;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Encapsulates a single session. A session consists of the socket used to talk
 * with the peer, a thread used to read() the socket and to accept input, and
 * an instance of this class.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class TelnetSession extends TelnetStateThing implements Runnable {
	/// The telnet server
	private TelnetServer		m_server;

	/// The socket for this session, connected to peer,
	private Socket				m_s;

	/// The thread used for reading this socket.
	private Thread				m_reader_thread;

	/// The inputstream containing data sent by my peer,
	private InputStream			m_is;

	/// The outputstream to send data to
	private OutputStream		m_os;

	/// The outputstream writer to use.
	private PrintWriter			m_pw;

	/// The name of this connection (it's IP address and port)
	private String				m_name;

	/// The command received from the peer as a string.
	private StringBuffer		m_cmd_sb;

	/// This-sessions writer thing.
	private TelnetPrintWriter	m_tpw;


	/*--------------------------------------------------------------*/
	/*	CODING:	Initialization of a session.						*/
	/*--------------------------------------------------------------*/
	/**
	 *	This constructor will only be called by TelnetServer.
	 */
	protected TelnetSession(TelnetServer srv, Socket s) {
		m_s = s;
		m_server = srv;
		StringBuffer sb = new StringBuffer(40);
		sb.append(m_s.getInetAddress().toString());
		sb.append(":");
		sb.append(Integer.toString(m_s.getPort()));
		m_name = sb.toString();
		m_cmd_sb = new StringBuffer(80);
		m_tpw = new TelnetPrintWriter(new TelnetWriter(this));
	}


	/**
	 *	Returns a session name from the IP address and the port.
	 */
	public String getName() {
		return m_name;
	}

	/**
	 *	Called after construction before this session is fully accepted, this
	 *  retrieves all data, opens the streams and starts the reader thread. If
	 *  all works then the session will be accepted.
	 */
	public void init() throws Exception {
		//-- 1. Get the input- and outputstreams. On error close the session.
		System.out.println("TelnetServer: accepted session with " + getName());

		try {
			m_is = m_s.getInputStream();
			m_os = m_s.getOutputStream();
			m_pw = new PrintWriter(m_os);

			//-- Allocate a reader thread and go,
			m_reader_thread = new Thread(this);
			m_reader_thread.setDaemon(true);
			m_reader_thread.setName("TelnetClient:" + getName());
			setState(tsINITING);
			m_reader_thread.start();
		} finally {
			if(!inState(tsRUN) && !inState(tsINITING)) {
				releaseResources();
			}
		}
	}

	private void releaseResources() {
		setState(tsDOWN);
		try {
			if(m_is != null)
				m_is.close();
		} catch(Exception x) {}
		try {
			if(m_os != null)
				m_os.close();
		} catch(Exception x) {}
		try {
			m_s.close();
		} catch(Exception x) {}

		m_is = null;
		m_os = null;
		m_s = null;
		releaseAllData();
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Writing to the session.								*/
	/*--------------------------------------------------------------*/
	/**
	 *	The screen attached to the session can be in INPUT mode or in NORMAL
	 *  mode. In NORMAL mode no input is visible, and all output is written
	 *  directly to the terminal.
	 *  As soon as a user inputs some data we switch to INPUT mode. While in
	 *  INPUT mode all output will be buffered in the "pending" list, and only
	 *  input characters are echoed and edited back to the console. This mode
	 *  continues until input mode is done, usually by pressing the ENTER
	 *  key.
	 *  If, in input mode, a user does not press a key for one minute then
	 *  the system will switch back to output mode as soon as output is
	 *  generated. The input will not be lost; the next keypress will re-enter
	 *  input mode and will show the last message buffer anew.
	 *
	 * 	All I/O code is synchronized on the m_pending_output_ll object(!)
	 */
	/// The current screen "mode": normal(0) or input(1)...
	private int			m_screen_mode;

	/// When did we switch to input mode?
	private long		m_ts_input;

	/// The list of cached strings to be output (while in input mode)
	private LinkedList<String>	m_pending_output_ll	= new LinkedList<String>();


	/**
	 *	Switches back to NORMAL mode. If already in normal mode nothing is done.
	 *  Else it writes all data in the buffers, then it returns the mode to
	 *  NORMAL.
	 */
	private void toNormalMode() throws Exception {
		synchronized(m_pending_output_ll) {
			if(m_screen_mode == 0)
				return; // Already in NORMAL mode.

			//-- Write all buffers...
			try {
				_write("\r\n");
				while(!m_pending_output_ll.isEmpty()) {
					String s = m_pending_output_ll.removeFirst();
					_write(s);
				}
			} finally {
				m_pending_output_ll.clear(); // Make sure all's deleted,
				m_screen_mode = 0; // Back to normal now!
			}
		}
	}


	/**
	 *	Switches to INPUT mode. Stops the output of data, displays the mode
	 *  prompt AND all current data in the input buffer, and continues.
	 */
	private void toInputMode() throws Exception {
		synchronized(m_pending_output_ll) {
			if(m_screen_mode != 0)
				return;

			//-- We have to switch. Do it NOW!!
			_write("\r\nWhat? >");
			_write(m_cmd_sb.toString());
			m_ts_input = System.currentTimeMillis() + 10 * 1000;

			m_screen_mode = 1;
		}
	}


	/**
	 *	Writes a string to this terminal. If the mode is not NORMAL then the
	 *  lines are cached.
	 */
	public void write(String s) throws Exception {
		synchronized(m_pending_output_ll) {
			if(m_screen_mode == 0) {
				_write(s);
				return;
			}

			//-- Shit- we have to cache....
			m_pending_output_ll.addLast(s); // Append new data at end
			if(m_pending_output_ll.size() > 300) // Too many saved?
				m_pending_output_ll.removeFirst(); // ..Then remove start!

			//-- How long ago did we switch to another mode?
			long t = System.currentTimeMillis();
			if(t > m_ts_input) // Very long ago?
				toNormalMode(); // Switch back to normal mode NOW!
		}
	}

	//	public void writeln(String s) throws Exception
	//	{
	//		write(s+"\r\n");
	//	}


	/**
	 *	Writes a stream of characters to the stream. If the write causes an
	 *  error this session gets closed and will be released. This call is
	 *  synchronized. If the session is not in running state the call returns
	 *  silently.
	 *  This call does NOT respect input mode and the like!
	 */
	public void _write(String s) throws Exception {
		if(!inState(tsRUN) && !inState(tsINITING))
			return;

		//-- Probably OK...
		try {
			synchronized(m_pending_output_ll) {
				int ix = 0, sl = s.length();
				while(ix < sl) {
					int pos = s.indexOf('\n', ix);
					if(pos == -1) {
						m_pw.print(s.substring(ix));
						break;
					}
					m_pw.print(s.substring(ix, pos));
					m_pw.print("\r\n");
					ix = pos + 1;
				}

				m_pw.flush();
			}
		} catch(Exception x) {
			handleFatalError(x);
		}
	}


	/**
	 *	Called for a fatal error, this prints the error, closes the connection,
	 *  releases all resources and the like.
	 */
	private void handleFatalError(Exception x) throws Exception {
		setState(tsDOWN); // Do not allow more IO
		x.printStackTrace(); // Print a stack dump,
		releaseResources(); // Close everything; causes reader thread to die.

		//-- Remove from server's table...
		m_server.sessionClosed(this); // ... And be done...
	}


	/**
	 *	Terminate a connection normally.
	 */
	private void normalClose() throws Exception {
		setState(tsDOWN); // Do not allow more IO
		releaseResources(); // Close everything; causes reader thread to die.
		m_server.sessionClosed(this); // ... And be done...
		System.out.println("Telnet " + getName() + ": connection terminated normally");
	}

	/**
	 *	Called when the session MUST close.
	 */
	public void close() {
		try {
			normalClose();
		} catch(Exception x) {}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Reader stuff....									*/
	/*--------------------------------------------------------------*/
	/**
	 *	The actual reader thread's main code. Basically reads data till EOF.
	 */
	public void run() {
		System.out.println("Telnet " + getName() + ": starting reader thread..");
		setState(tsRUN);

		try {
			write("Welcome to the Java Loggger Telnet Server. Enter ? to get a list of commands.\r\n");
		} catch(Exception x) {}


		try {
			//-- Read input and handle it till error....
			for(;;) {
				if(!readInputAndHandleIt()) {
					//-- End of file - stream closed. Terminate normally.
					normalClose();
					break;
				}
			}
		} catch(Exception x) {
			x.printStackTrace();
		} finally {
			setState(tsDOWN);
		}
		System.out.println("Telnet " + getName() + ": reader thread TERMINATED..");
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Input string editing and command history...			*/
	/*--------------------------------------------------------------*/
	/// The last input character, used to detect CRLF...
	private int				m_last_inchar;

	/// When non-zero in length, contains part of an escape sequence. Includes the ESCAPE.
	private StringBuffer	m_esc_sb	= new StringBuffer(8);


	/**
	 *	Reads the inputstream, and handles all commands received from there.
	 */
	private boolean readInputAndHandleIt() throws Exception {
		if(!inState(tsRUN))
			return false; // End of thing.
		int c = m_is.read();
		if(c == -1)
			return false; // End of stream!
		appendCommand(c);
		return true;
	}


	/**
	 *	Appends a received character to the current command. If the ENTER
	 *  key is received then execute the command. This part handles editing
	 *  of commands.
	 *  This accepts VTxxx function keys for the cursor keys and the like.
	 */
	private void appendCommand(int c) {
		if(m_esc_sb.length() > 0) {
			if(keyEscapeSequence(c)) // Part of escape sequence?
				return; // Yes-> be done,
		}

		try {
			if(c == '\r' || c == '\n') {
				if(m_last_inchar == '\r' && c == '\n') // CRLF -> command already executed,
					return;
				if(m_last_inchar == '\n' && c == '\r')
					return;

				//-- Not CRLF -> is return
				keyReturn();
				return;
			}

			toInputMode();

			if(c == '\u001b') {
				keyEscapeSequence(c);
				return;
			}

			if(c == '\u0008') {
				keyBackspace();
				return;
			}
			if(c == '\u007f') {
				keyDelete();
				return;
			}


			m_cmd_sb.append((char) c);
			_write(m_cmd_sb.toString().substring(m_cmd_sb.length() - 1));
		} catch(Exception x) {
			x.printStackTrace();
		} finally {
			m_last_inchar = c;
		}
	}


	/**
	 *	Adds a literal character at the current cursor position, then rerenders
	 *  the screen copy.
	 */


	/**
	 *	Called when a RETURN is received, terminating the current command.
	 */
	private void keyReturn() {
		try {
			toNormalMode();
		} catch(Exception x) {}
		String cmd = m_cmd_sb.toString().trim();
		if(cmd.length() > 0) {
			//			System.out.println("COMMAND: "+cmd);

			m_server.executeTelnetCommand(m_tpw, cmd);
			m_tpw.flush();
		}
		m_cmd_sb.setLength(0);
	}


	/**
	 *	Called when a keypress is part of an escape sequence.
	 */
	private boolean keyEscapeSequence(int ch) {
		m_esc_sb.append((char) ch);
		if(m_esc_sb.length() >= 3) {
			//			System.out.println("ESCAPE SEQUENCE: "+m_esc_sb.toString());
			m_esc_sb.setLength(0);
		}
		return true;
	}


	/**
	 *	Called to backspace. Deletes the char before the cursor.
	 */
	private void keyBackspace() throws Exception {
		int cl = m_cmd_sb.length();
		if(cl <= 0)
			return; // Cannot do that dodo

		m_cmd_sb.setLength(cl - 1);
		_write("\u0008 \u0008");
	}


	/**
	 *	Called to delete. Deletes the char under the cursor.
	 */
	private void keyDelete() throws Exception {
		int cl = m_cmd_sb.length();
		if(cl <= 0)
			return; // Cannot do that dodo

		m_cmd_sb.setLength(cl - 1);
		_write("\u0008 \u0008");
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Client structures.									*/
	/*--------------------------------------------------------------*/
	/** The attached Telnet Client data things. */
	private Map<String, Object>	m_clientdata_ht	= new HashMap<String, Object>(11);


	/**
	 *	Adds a client data thing to this-session's session data.
	 */
	public synchronized void put(String n, Object o) {
		if(inState(tsDOWN) || inState(tsSHUT))
			return;
		m_clientdata_ht.put(n, o);
	}


	/**
	 *	Removes a client data thing from this-session's session data.
	 */
	public synchronized Object get(String name) {
		return m_clientdata_ht.get(name);
	}


	/**
	 *	Called when this session terminates, it calls the terminated()
	 *  handler for all objects in the clientdata table that implement the
	 *  iTelnetClientEvent interface.
	 */
	private synchronized void releaseAllData() {
		for(Object o : m_clientdata_ht.values()) {
			if(o instanceof ITelnetClientEvent)
				((ITelnetClientEvent) o).sessionClosed(this);
		}
		m_clientdata_ht.clear();
	}
}
