package to.etc.log;

import java.util.*;

/**
 * Contains log data.
 *
 * <p>Title: Mumble Global Libraries - Non-database tools</p>
 * <p>Description: Small tools for Java programs</p>
 * <p>Copyright: Copyright (c) 2002 Frits Jalvingh; released under the LGPL licence.</p>
 * <p>Website <a href="http://www.mumble.to/">Mumble</a></p>
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 * @version 1.0
 */
public class LogRecord {
	public Date			m_ts;

	//	public String	m_c1, m_c2, m_c3, m_c4;
	public Category		m_cat;

	public String		m_thread;

	public Object		m_class;

	public String		m_msg;

	//	public String	m_ex;
	//	public String	m_stack;
	public Throwable	m_x;

	public byte[]		m_data;

	public LogRecord(Category c) {
		m_cat = c;
		m_ts = new Date();
	}

	public LogRecord() {
		m_ts = new Date();
	}
}
