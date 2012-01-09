package to.etc.log;

/**
 *	WARNING:	THIS CLASS IS CURRENTLY NOT USED.
 */

import java.util.*;


/**
 *	This class implements a multi-threaded generic logging class that can be used
 *  by daemon processes to generate a logfile. The main methods can be called
 *  by several threads at a time, and the order of entry will be reflected in the
 *  log.
 *  To allow easy access to this log a TELNET server can be instantiated
 *  by calling the startServer() entrypoint. This telnet server will accept
 *  sessions; sessions will receive all data from the logger and can issue
 *  commands to enable certain logging options.
 *
 * <p>Title: Mumble Global Libraries - Non-database tools</p>
 * <p>Description: Small tools for Java programs</p>
 * <p>Copyright: Copyright (c) 2002 Frits Jalvingh; released under the LGPL licence.</p>
 * <p>Website <a href="http://www.mumble.to/">Mumble</a></p>
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 * @version 1.0
 */
public class MTLog {
	/// The set of log writers associated.
	protected HashSet		m_writers_s;

	/// The queue of pending writes.
	protected LinkedList	m_pending_events;

	/// T if the output system is currently busy (writing)
	protected boolean		m_am_writing;

	/// The telnet server for the log things.
	protected TelnetServer	m_telnet_server;

	static private MTLog	m_instance	= new MTLog();

	private MTLog() {
		m_pending_events = new LinkedList();
		m_writers_s = new HashSet();
		m_am_writing = false;
	}

	public MTLog getInstance() {
		return m_instance;
	}

	/**
	 *	Called to start a Telnet server to allow easy access to log and debug
	 *  information. Only one server will ever be started; once some class calls
	 *  this function the server gets created and will not be destroyed. The
	 *  server will be created at port 7171.
	 */
	static public void startTelnetServer() throws Exception {
		synchronized(m_instance) {
			if(m_instance.m_telnet_server != null)
				return;

			m_instance.m_telnet_server = TelnetServer.createServer(7171);
			m_instance.registerWriter(m_instance.m_telnet_server);
		}
	}


	/**
	 *	Registers an output writer.
	 */
	public void registerWriter(iLogEventWriter lw) {
		synchronized(m_writers_s) {
			m_writers_s.add(lw);
		}
	}


	/**
	 *	Writes a single log event to all of the registered output servers. If
	 *  the output system is free the message gets written in the context of the
	 *  calling thread. If the output system is busy the calling thread will
	 *  queue the message.
	 */
	protected void logEvent(LogRecord l) {
		//-- Lock: is the writer busy?
		boolean mustblock = false;
		boolean mustwrite = false;

		//-- 1. Queue or allocate the writer,
		synchronized(m_pending_events) {
			if(m_am_writing) {
				//-- The writer is busy. Queue.
				m_pending_events.addLast(l); // Add to queue,
				if(m_pending_events.size() > 200)
					mustblock = true;
			} else {
				//-- We will write!
				mustwrite = true;
				m_am_writing = true;
			}
		}

		//-- Outside the synced block check what to do
		if(!mustwrite) {
			if(mustblock) {
				System.out.println("MTLog: Too many queued messages; blocking to temper...");
				try {
					Thread.sleep(2000);
				} catch(Exception x) {}


			}
			return;
		}


		//-- Ok: we have to write this message (1st in queue)
		for(;;) {
			writeReally(l); // Do the actual write

			//-- Ok. Are we done or is something queued?
			synchronized(m_pending_events) {
				if(m_pending_events.isEmpty()) // Do we have queued stuff?
				{
					//-- Nothing more! Unlock the writer and be done.
					m_am_writing = false;
					return;
				}

				//-- Something was queued and we have to output it.
				l = (LogRecord) m_pending_events.removeFirst();
			}
		}
	}


	/**
	 *	Handles the actual writing of a Log record. When called we don't need
	 *  to sync anymore because we're already sure we're called from only one
	 *  thread at a time (programmatic lock m_am_writing).
	 */
	private void writeReally(LogRecord l) {
		//-- Call all entries in the writers thing one by one.
		synchronized(m_writers_s) {
			Iterator i = m_writers_s.iterator();
			while(i.hasNext()) {
				iLogEventWriter ew = (iLogEventWriter) i.next();

				ew.writeEventRecord(l);
			}
		}
	}


}
