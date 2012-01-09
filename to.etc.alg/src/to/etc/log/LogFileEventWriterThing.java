package to.etc.log;

import java.io.*;
import java.util.*;

/**
 * Implements log event listener.
 *
 * <p>Title: Mumble Global Libraries - Non-database tools</p>
 * <p>Description: Small tools for Java programs</p>
 * <p>Copyright: Copyright (c) 2002 Frits Jalvingh; released under the LGPL licence.</p>
 * <p>Website <a href="http://www.mumble.to/">Mumble</a></p>
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 * @version 1.0
 */
public class LogFileEventWriterThing implements iLogEventListener {
	/// The base filename to write to,
	private String		m_fn;

	/// The current filename,
	private String		m_curr_fn;

	/// T if this log wraps every day
	private boolean		m_wrap;

	/// T if this needs to overwrite the first logfile.
	private boolean		m_over;

	/// The filter
	private LinkedList	m_filter;

	/// The current file number, if wrapping,
	private int			m_filenr	= -1;

	/// The next time we need to check for log wrap.
	private long		m_ts_check;

	/// A file.
	private PrintWriter	m_pw;

	public LogFileEventWriterThing(String fn, boolean over, boolean wrap, LinkedList filter) {
		m_over = over;
		m_wrap = wrap;
		m_fn = fn;
		m_filter = filter;
	}


	/**
	 *	Called to create a new file name. Returns T if the filename changed
	 *  from last time.
	 */
	private boolean calcFileName() {
		//-- Set new check time,
		m_ts_check = System.currentTimeMillis() + 60000;
		if(!m_wrap) {
			m_curr_fn = m_fn;
			return false;
		}

		//-- We're wrapping. Create a day #,
		Calendar c = Calendar.getInstance();
		int oldday = m_filenr;
		m_filenr = c.get(Calendar.DAY_OF_WEEK);
		m_curr_fn = m_fn + Integer.toString(m_filenr);
		return m_filenr != oldday;
	}


	/**
	 *	Called to initialize.
	 */
	private synchronized void init() throws Exception {
		if(m_pw != null)
			return;
		calcFileName();
		m_pw = new PrintWriter(new FileOutputStream(m_curr_fn, !m_over));
		m_over = false;
	}


	/**
	 *	Called when the thing needs initialization...
	 */
	private synchronized void checkWrap() throws Exception {
		init();
		if(!m_wrap)
			return;

		//-- 1. Time to check again?
		if(System.currentTimeMillis() < m_ts_check)
			return;

		if(!calcFileName())
			return; // No new name needed,

		//-- Ok: close the current one and start the next,
		try {
			m_pw.close();
		} catch(Exception x) {}
		m_pw = null;
		m_pw = new PrintWriter(new FileOutputStream(m_curr_fn));
		m_pw.println("Log file wrapped, opened at " + (new Date()).toString());
	}


	public void logEvent(LogRecord lr) throws Exception {
		//		System.out.println(getClass().getName()+" logrecord to write recvd");
		checkWrap();

		//-- Now log whatever we know..
		m_pw.print(lr.m_ts.toString());
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
		m_pw.flush();
	}


	public boolean isInterestedIn(Category c) {
		if(LogMaster.logAllEnabled())
			return true;

		if(LogMaster.checkInterestedIn(m_filter, c.getName())) {
			//			System.out.println("Is interested in "+c.getName());
			return true;
		}
		//		System.out.println("NOT interested in "+c.getName());
		return false;
	}
}
