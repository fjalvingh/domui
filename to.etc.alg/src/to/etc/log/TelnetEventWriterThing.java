package to.etc.log;

import java.text.*;
import java.util.*;

import to.etc.log.*;
import to.etc.telnet.*;


/**
 *	Interface between the logging modules and a Telnet session.
 *
 * <p>Title: Mumble Global Libraries - Non-database tools</p>
 * <p>Description: Small tools for Java programs</p>
 * <p>Copyright: Copyright (c) 2002 Frits Jalvingh; released under the LGPL licence.</p>
 * <p>Website <a href="http://www.mumble.to/">Mumble</a></p>
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 * @version 1.0
 */
public class TelnetEventWriterThing implements iLogEventListener, ITelnetClientEvent {
	/// The telnet session receiving these events.
	private TelnetPrintWriter	m_pw;

	///	The filter,
	private LinkedList			m_filter	= new LinkedList();

	public TelnetEventWriterThing(TelnetPrintWriter tpw) {
		m_pw = tpw;
	}

	public void logEvent(LogRecord lr) throws Exception {
		//-- Now log whatever we know..
		DateFormat df = DateFormat.getTimeInstance();
		m_pw.print(df.format(lr.m_ts));
		m_pw.print(" ");
		if(lr.m_cat != null) {
			m_pw.print("{");
			m_pw.print(lr.m_cat.getName());
			m_pw.print("} ");
		}
		m_pw.println(lr.m_msg);

		//-- Now the stack dump && the like
		if(lr.m_x != null) {
			m_pw.println("Exception: " + lr.m_x.toString());
			lr.m_x.printStackTrace(m_pw);
		}
	}


	public boolean isInterestedIn(Category c) {
		return LogMaster.checkInterestedIn(m_filter, c.getName());
	}


	/**
	 *	Called by the TelnetSession when the session is CLOSED for whatever
	 *  reason. This removes this thing from the logger's listener lists.
	 */
	public void sessionClosed(TelnetSession ts) {
		LogMaster.removeListener(this);
	}


	/**
	 *	Adds a filter string for this session.
	 */
	protected void addFilter(String f) {
		if(!m_filter.contains(f))
			m_filter.add(f);
	}


	/**
	 *	Removes a filter string for this session.
	 */
	protected void removeFilter(String f) {
		Iterator i = m_filter.iterator();
		while(i.hasNext()) {
			String s = (String) i.next();
			if(f.equalsIgnoreCase(s)) {
				i.remove();
				m_pw.println("Filter removed.");
				return;
			}
		}
	}

	/**
	 *	Removes a filter string for this session.
	 */
	protected void removeFilters() {
		m_filter.clear();
	}

	protected void printFilters() {
		Iterator i = m_filter.iterator();
		while(i.hasNext()) {
			String s = (String) i.next();
			m_pw.println("Filter: " + s);
		}
	}


}
