package to.etc.server.janitor;

import java.util.*;

import javax.sql.*;

import to.etc.log.*;
import to.etc.server.syslogger.*;
import to.etc.util.*;

/**
 * This class manages events that are posted in a
 * database table. It allows multiple databases to
 * be scanned for events. It is a generalized version
 * of the NEMA UpdateChecker.
 *
 * @author jal
 * Created on Jan 22, 2005
 */
public class UpdateEventManager implements Runnable {
	static private final Category		MSG					= LogMaster.getCategory("jan.upman", "msg");

	static private final Category		JAN					= LogMaster.getCategory("jan.upman", "janitor");

	/** The locking thread, if locked */
	private Thread						m_lock_t;

	/** The thread for the runner process */
	private Thread						m_thread;

	/** The event handler hashmap. */
	private Hashtable					m_registered_map	= new Hashtable();

	/** The set of databases to listen for events on */
	private Hashtable					m_db_al				= new Hashtable();

	static private UpdateEventManager	m_update_manager	= new UpdateEventManager();

	public UpdateEventManager() {
	}

	static public UpdateEventManager getInstance() {
		return m_update_manager;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Registering databases and handlers.					*/
	/*--------------------------------------------------------------*/
	/**
	 * Adds a new checked connection and table to the set. This does not
	 * start the checks for this connection until an event handler gets
	 * registered.
	 */
	public synchronized UpdateSource addDatabase(DataSource dbc, String tbl, String sequence) throws Exception {
		UpdateSource e = new UpdateSource(this, dbc, tbl, sequence);
		UpdateSource o = (UpdateSource) m_db_al.get(e);
		if(o != null)
			return o;
		m_db_al.put(e, e);

		//-- If the handler has initialized already initialize this handler now too
		if(hasInitialized())
			e.init();
		return e;
	}

	public UpdateSource addDatabase(DataSource dbc) throws Exception {
		return addDatabase(dbc, null, null);
	}

	/**
	 * Registers a handler for the event specified..
	 * @param evcode
	 * @param h
	 * @throws Exception
	 */
	public void registerHandler(String evcode, UpdateListener h) {
		synchronized(this) {
			m_registered_map.put(evcode.toLowerCase(), h);
			init();
		}
	}

	/**
	 * Returns T if the event handler has initialized, i.e. the task manager
	 * is running.
	 * @return
	 */
	private synchronized boolean hasInitialized() {
		return m_thread != null;
	}

	/**
	 * Called when this checker initializes. It creates the checker thread.
	 *
	 * @param dbc
	 * @throws Exception
	 */
	private synchronized void init() {
		//-- Register janitor task
		//		if(JANITOR)
		//		{
		//			NemaBroker.getJanitor().addTask(60, false, "Updates@"+m_poolid, new JanitorTask()
		//			{
		//				public void run() throws Exception
		//				{
		//					jCheckUpdates();
		//				}
		//			});
		//		}
		//		else
		{
			if(m_thread == null) {
				Thread t = new Thread(this, "NemaUpdateHandler");
				t.setDaemon(true);
				t.start();
				m_thread = t;

				//-- Now initialize all of the handlers.
				for(Enumeration e = m_db_al.elements(); e.hasMoreElements();) {
					UpdateSource us = (UpdateSource) e.nextElement();
					try {
						us.init();
					} catch(Exception x) {
						Panicker.logUnexpected(x, "In initializing the update handler for " + us);
						x.printStackTrace();
					}
				}
			}
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Thread code.										*/
	/*--------------------------------------------------------------*/
	/**
	 * The thread method if the update checker is run in a thread.
	 */
	public void run() {
		for(;;) {
			jCheckUpdates();
			try {
				Thread.sleep(4 * 1000); // Sleep x secs before rerun
			} catch(Exception ex) {}
		}
	}

	private void jCheckUpdates() {
		long t = PrecisionTimer.getTime();
		try {
			JAN.msg("Checking for updates"); //: "+Thread.currentThread());
			ArrayList al = new ArrayList();
			checkUpdates(al);
			for(int i = 0; i < al.size(); i++)
				handleUpdate((UpdateEvent) al.get(i));
		} catch(Exception ex) {
			ex.printStackTrace();
			Panicker.logUnexpected(ex, "In UpdateEventManager");
		} finally {
			t = PrecisionTimer.getTime() - t;
			JAN.msg("Update check completed in " + t + " us");
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Checker entrypoint...								*/
	/*--------------------------------------------------------------*/
	/**
	 * Checks for updates on this checker, and handles them if found. To prevent
	 * multiple threads from handling updates this locks the instance.
	 * @return T if this handled updates, F if another thread is handling them
	 * 			currently.
	 */
	private boolean checkUpdates(ArrayList al) throws Exception {
		synchronized(this) {
			if(m_lock_t != null)
				return false;
			m_lock_t = Thread.currentThread();
		}

		al.clear();
		for(Enumeration e = m_db_al.elements(); e.hasMoreElements();) {
			UpdateSource um = (UpdateSource) e.nextElement();
			try {
				um.checkUpdates(al);
			} catch(Exception x) {
				Panicker.panic("UpdateHandler: exception in READING updates for db=" + um.toString() + ": " + x, StringTool.strStacktrace(x));
			}
		}
		synchronized(this) {
			m_lock_t = null;
		}
		return true;
	}

	private void handleUpdate(UpdateEvent e) {
		try {
			UpdateListener h = null;
			synchronized(this) {
				h = (UpdateListener) m_registered_map.get(e.getEventName());
				if(h == null) {
					System.out.println("nema.uh: no handler for event ID=" + e.getEventName());
					return;
				}
			}
			//			System.out.println("nema.uh: calling handler for event ID="+ev);
			MSG.msg("Calling update handler for " + e.getEventName());
			h.DatabaseUpdateEvent(e);
		} catch(Exception ex) {
			Panicker.panic("In UPDATE-EVENT handler for event " + e.getEventName(), ex);
			System.out.println("Update-event: exception " + ex + " in update event " + e.getEventName());
			ex.printStackTrace();
		}
	}
}
