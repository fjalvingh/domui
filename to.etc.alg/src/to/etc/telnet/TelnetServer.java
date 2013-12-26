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

import to.etc.util.*;

/**
 * Implements a Telnet server for log and debugging tasks. Each session started
 * can send and receive data. Each session will get a separate thread(!) so this
 * is NOT meant as a serious implementation!
 * To get a working server call the static method createServer; this will
 * create a server structure and it's associated thread, called the listener
 * thread. The only task of this thread is to listen for new session requests
 * and to spawn new sessions when these occur.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 *
 */
public class TelnetServer extends TelnetStateThing implements Runnable {
	/// This server's debug port
	private int				m_port;

	/// The server socket for this server
	private ServerSocket	m_server_socket;

	/// The thread for this server.
	private Thread			m_thread;

	/// The current #of ajacent accept errors...
	private int				m_error_count;

	/// The currently active sessions.
	private List<TelnetSession>	m_sessions	= new ArrayList<TelnetSession>();

	/// The command handler list.
	private List<ITelnetCommandHandler>	m_command_v;

	private TelnetServer(int port) {
		m_port = port;
	}


	/**
	 *	Creates a new Telnet server.
	 */
	static public TelnetServer createServer(int port) throws Exception {
		TelnetServer ts = new TelnetServer(port); // Create'un,

		//-- Do the thing's static init,
		try {
			ts.init(); // Allocate socket and the like,
		} finally {
			if(!ts.inState(tsRUN) && !ts.inState(tsINITING))
				ts.releaseResources();
		}

		return ts;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Basic initialization & termination code...			*/
	/*--------------------------------------------------------------*/
	/**
	 *	Initializes by allocating a socket and starting the thread belonging
	 *  to this server.
	 */
	private void init() throws Exception {
		setState(tsINITING);
		m_command_v = new ArrayList<ITelnetCommandHandler>();
		m_server_socket = new ServerSocket(m_port, 10);

		//-- Now start the thread thing,
		m_thread = new Thread(this);
		m_thread.setDaemon(true);
		m_thread.setName("Telnet@" + m_port);
		m_thread.start();
	}


	private void releaseResources() {
		setState(tsDOWN);
		try {
			if(m_server_socket != null)
				m_server_socket.close();
		} catch(Exception x) {}
		m_server_socket = null;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Telnet command handler interface.					*/
	/*--------------------------------------------------------------*/
	/**
	 *	Adds a command handler.
	 */
	public void addCommandHandler(ITelnetCommandHandler tch) {
		synchronized(m_command_v) {
			m_command_v.add(tch);
		}
	}


	/**
	 *	Calls a command handler for a given (session, command) to get it
	 *  executed.
	 */
	protected void executeTelnetCommand(TelnetPrintWriter tpw, String command) {
		CmdStringDecoder csd = new CmdStringDecoder(command);

		//-- Is it a TN command?
		if(csd.currIs("bye") || csd.currIs("close")) {
			//-- Command for THIS!
			tpw.println("Closing telnet session. Bye and thanks for all the fish!");
			TelnetSession ts = tpw.getSession();
			ts.close();
			return;
		}
		if(csd.currIs("stdout")) {
			if(csd.hasMore()) {
				String n = csd.getNext();
				if("on".equalsIgnoreCase(n) || "true".equalsIgnoreCase(n) || "capture".equals(n))
					setCapture(true);
				else
					setCapture(false);
			}
			tpw.println("Stdout state is: " + (m_capturing_stdout ? "capturing stdout" : "not capturing stdout"));
			return;
		}


		ITelnetCommandHandler[] ar;
		synchronized(m_command_v) {
			ar = m_command_v.toArray(new ITelnetCommandHandler[m_command_v.size()]);
		}

		boolean handled = false;

		for(int i = 0; i < ar.length; i++) {
			if(executeTelnetCommand(tpw, ar[i], csd))
				handled = true;
		}
		if(!handled) {
			tpw.write("TS: Unrecognized command " + command + "\r\n");
		}
	}


	private boolean executeTelnetCommand(TelnetPrintWriter tpw, ITelnetCommandHandler tch, CmdStringDecoder cmd) {
		try {
			cmd.reset();
			return tch.executeTelnetCommand(tpw, cmd);
		} catch(Exception x) {
			tpw.println("Exception in command: " + x.toString());
			x.printStackTrace(tpw);
		}
		return false;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Accept and new session spawning code...				*/
	/*--------------------------------------------------------------*/
	/**
	 *	This is the server's thread. It blocks on accept() on the server socket
	 *  and accepts new connections. When a new connection is established a new
	 *  session is started for that connection (using a new thread) and we
	 *  loop again. On error the server will enter down state.
	 */
	public void run() {
		System.out.println("TelnetServer: listener thread started OK.");
		setState(tsRUN);
		try {
			m_error_count = 0;
			while(m_error_count < 10)
				acceptListen();

			//-- Too many errors!!! Close all then exit,
			System.out.println("TelnetServer: listener died due to excessive errors");
		} catch(Exception x) {
			System.out.println("TelnetServer: listener fatal exception.");
			x.printStackTrace();
		} finally {
			setState(tsDOWN);
		}
		System.out.println("TelnetServer: terminated.");
	}


	/**
	 *	Blocks on the accept() call to accept new sessions.
	 */
	private void acceptListen() throws Exception {
		if(!inState(tsRUN))
			throw new Exception("TelnetServer is not in RUN state: cannot accept connections");

		Socket s = null;
		try {
			s = m_server_socket.accept(); // Accept incoming,

			//-- New connection! Start a new session,
			createSession(s);
			s = null;
			m_error_count = 0;
		} catch(Exception x) {
			x.printStackTrace();
			m_error_count++;
		} finally {
			try {
				if(s != null)
					s.close();
			} catch(Exception x) {}
		}
	}


	/**
	 *	Creates a session with the connecting client. This creates a reader
	 *  thread and a session structure. The reader thread will accept commands
	 *  and will call a command handler.
	 */
	private void createSession(Socket s) throws Exception {
		TelnetSession ts = new TelnetSession(this, s); // Create the session,
		ts.init(); // Initialize & create thread,
		synchronized(this) {
			m_sessions.add(ts); // And add to session list
		}
		pumpShit(ts);
	}

	/**
	 *	Called to remove a session that was closed (due to error or normal
	 *  circumstances) from this server's tables.
	 */
	protected void sessionClosed(TelnetSession ts) {
		synchronized(this) {
			m_sessions.remove(ts);
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Writing to ALL stuff....							*/
	/*--------------------------------------------------------------*/
	/// The list of "last written all strings". Used to present new terminals with data.
	private LinkedList<String>	m_string_cache	= new LinkedList<String>();


	/**
	 *	Appends a string to the storage cache.
	 */
	private void appendToCache(String v) {
		synchronized(m_string_cache) {
			m_string_cache.addLast(v);
			if(m_string_cache.size() > 200)
				m_string_cache.removeFirst();
		}
	}

	/**
	 *	Pumps the current contents of the "all" cache to a session.
	 */
	private void pumpShit(TelnetSession ts) {
		try {
			synchronized(m_string_cache) {
				for(String v : m_string_cache) {
					ts.write(v);
				}
			}
		} catch(Exception x) {
			x.printStackTrace();
		}
	}

	/**
	 *	Sends a single string to ALL clients. The string gets locally buffered
	 *  if required.
	 */
	public void wall(String msg) {
		//-- 1. Get a list of ALL sessions,
		TelnetSession[] ar;
		appendToCache(msg);
		synchronized(this) {
			int ns = m_sessions.size();
			if(ns == 0)
				return; // No sessions...

			ar = m_sessions.toArray(new TelnetSession[ns]);
		}

		//-- Now wall to all sessions,
		for(int i = 0; i < ar.length; i++) {
			try {
				ar[i].write(msg);
			} catch(Exception x) {
				x.printStackTrace();
			}
		}
	}

	/**
	 *	Called to dump data from outputstream and such..
	 */
	public void _write(int ch) {
		char[] c = new char[1];
		c[0] = (char) ch;

		wall(new String(c));
	}

	public void _write(byte[] ar, int off, int len) {
		String s = new String(ar, off, len);
		wall(s);
	}



	/*--------------------------------------------------------------*/
	/*	CODING:	Main, test subroutine...							*/
	/*--------------------------------------------------------------*/


	static public void main(String[] args) {
		try {
			TelnetServer t = TelnetServer.createServer(7171);

			int ct = 0;
			while(true) {
				try {
					Thread.sleep(2000);
					//					System.out.println("WALL: Doing it NOW..");
					t.wall("Testing, one, two.." + ct + "\n\r");
					ct++;
					Runtime.getRuntime().gc();

				} catch(Exception x) {}
			}

		} catch(Exception x) {
			System.out.println("EXCEPTION: " + x.toString());
			x.printStackTrace();
		}
	}

	/// The Telnet server
	static private TelnetServer	m_telnet_server;

	private static PrintStream	m_orig_stdout;

	private static PrintStream	m_orig_stderr;

	static private boolean		m_capturing_stdout;

	static private void setCapture(boolean on) {
		if(m_capturing_stdout == on)
			return;
		synchronized(System.class) {
			if(on) {
				TelnetSysoutMirrorStream tsms = new TelnetSysoutMirrorStream(m_telnet_server, System.out);
				PrintStream ps = new PrintStream(tsms);
				System.setOut(ps);
				System.setErr(ps);
			} else {
				System.setOut(m_orig_stdout);
				System.setErr(m_orig_stderr);
			}
			m_capturing_stdout = on;
		}

	}

	/**
	 *	Called to start the telnet server. If the server has already
	 *  started this returns false.
	 */
	static public void startTelnetServer(int port) {
		synchronized(TelnetServer.class) {
			if(m_telnet_server != null)
				return;
			System.out.println("Starting telnet thingy on port " + port);
			try {
				m_telnet_server = TelnetServer.createServer(port);

				//				//-- Add the logmaster command handler.
				//				ITelnetCommandHandler lh = new ITelnetCommandHandler() {
				//					public boolean executeTelnetCommand(TelnetPrintWriter tpw, CmdStringDecoder commandline) throws Exception {
				//						return executeTnCommand(tpw, commandline);
				//					}
				//				};
				//				m_telnet_server.addCommandHandler(lh);

				//-- Add system.out handler...
				m_orig_stdout = System.out;
				m_orig_stderr = System.err;

				m_capturing_stdout = true;
				TelnetSysoutMirrorStream tsms = new TelnetSysoutMirrorStream(m_telnet_server, System.out);
				PrintStream ps = new PrintStream(tsms);
				System.setOut(ps);
				System.setErr(ps);
			} catch(Exception x) {
				System.err.println("LogMaster: startTelnet failed, " + x.toString());
				x.printStackTrace();
			}
		}
	}

	static public void registerTelnetCommand(ITelnetCommandHandler tch) {
		synchronized(TelnetServer.class) {
			if(m_telnet_server == null)
				return;
			m_telnet_server.addCommandHandler(tch);
		}
	}


}
