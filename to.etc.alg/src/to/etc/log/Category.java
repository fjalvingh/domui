package to.etc.log;

import java.util.*;


/**
 *	Contains a single category. Each category has an unique number and a name;
 *	this record also stores if the category gets logged at runtime.
 *
 * <p>Title: Mumble Global Libraries - Non-database tools</p>
 * <p>Description: Small tools for Java programs</p>
 * <p>Copyright: Copyright (c) 2002 Frits Jalvingh; released under the LGPL licence.</p>
 * <p>Website <a href="http://www.mumble.to/">Mumble</a></p>
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 * @version 1.0
 */
public class Category {
	/// True if this-category events are logged.
	protected boolean	m_on;

	/// T if this ALWAYS has to call the log sink.
	private boolean		m_logsink;

	/// This-category's dotted name (unique)
	private String		m_name;

	/// This category's event listeners.
	private Vector		m_listener_v;

	public boolean		m_screen;		// REMOVE


	/**
	 *	Called by the LogMaster to create a new, unique category.
	 */
	protected Category(String name) {
		m_name = name;
	}

	@Override
	public String toString() {
		return m_name + ":" + m_on;
	}

	public String getName() {
		return m_name;
	}

	public boolean isOn() {
		return m_on;
	}


	/**
	 *	Add a listener for this category. If the listener already exists it
	 *  is kept. A single listener is added only once.
	 */
	protected void addListener(iLogEventListener il) {
		synchronized(this) {
			if(m_listener_v == null)
				m_listener_v = new Vector(4);
			if(m_listener_v.contains(il))
				return;
			m_listener_v.add(il);
			m_on = true; // Always need to dump a message,
		}
	}

	/**
	 *	Removes a listener.
	 */
	protected void removeListener(iLogEventListener il) {
		synchronized(this) {
			if(m_listener_v != null) {
				m_listener_v.remove(il);
				if(m_listener_v.size() == 0 && !m_logsink) // Anything handled at all?
					m_on = false;
			}
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Chain sender...										*/
	/*--------------------------------------------------------------*/
	/**
	 *	Calls a single event listener's method, and handles errors. If the
	 *  event handler throws an exception then the event handler gets removed
	 *  from all lists.
	 */
	private void callListenerLogger(iLogEventListener lel, LogRecord lr) {
		try {
			lel.logEvent(lr);
		} catch(Throwable t) {
			//-- !! Error!!!!!
			System.out.println("LogMaster: unexpected LogEventListener exception " + t.toString());
			t.printStackTrace();

			//-- Remove this!!
			LogMaster.removeListener(lel);
		}
	}


	/**
	 *	Sends a logrecord to all registered listeners. When called we're
	 *  already synchronized on the logmaster's category table.
	 */
	protected void sendThruChain(LogRecord lr) {
		synchronized(this) {
			if(m_listener_v == null)
				return;

			Enumeration e = m_listener_v.elements();
			while(e.hasMoreElements()) {
				iLogEventListener lel = (iLogEventListener) e.nextElement();
				callListenerLogger(lel, lr);
			}
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Message functions...								*/
	/*--------------------------------------------------------------*/
	/**
	 *	The main message entrypoint, logging as much as possible by creating
	 *  a LogRecord and dumping that thru the event chains and the logsink.
	 */
	public void msg(Object cls, String msg, Throwable x, byte[] data) {
		if(!m_on)
			return; // Exit immediately if not logged,
		LogRecord lr = new LogRecord(this);
		lr.m_class = cls;
		lr.m_x = x;
		lr.m_msg = msg;
		lr.m_data = data;
		LogMaster.log(lr);
	}


	public void msg(String s) {
		if(!m_on)
			return; // Exit immediately if not logged,
		LogRecord lr = new LogRecord(this);
		lr.m_msg = s;
		LogMaster.log(lr);
	}

	public void msg(Object o, String s) {
		if(!m_on)
			return; // Exit immediately if not logged,
		LogRecord lr = new LogRecord(this);
		lr.m_msg = s;
		lr.m_class = o;
		LogMaster.log(lr);
	}

	public void exception(Object cl, String msg, Throwable x) {
		if(!m_on)
			return; // Exit immediately if not logged,
		LogRecord lr = new LogRecord(this);
		lr.m_msg = msg;
		lr.m_class = cl;
		lr.m_x = x;
		LogMaster.log(lr);
	}

	public void exception(String msg, Throwable x) {
		if(!m_on)
			return; // Exit immediately if not logged,
		LogRecord lr = new LogRecord(this);
		lr.m_msg = msg;
		lr.m_x = x;
		LogMaster.log(lr);
	}

	public void dump(Object cl, String msg, byte[] buf) {
		if(!m_on)
			return; // Exit immediately if not logged,
		LogRecord lr = new LogRecord(this);
		lr.m_msg = msg;
		lr.m_class = cl;
		lr.m_data = buf;
		LogMaster.log(lr);
	}
}
