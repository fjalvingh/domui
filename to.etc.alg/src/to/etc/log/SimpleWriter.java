package to.etc.log;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * Simple logfile writer, writes text files.
 *
 * <p>Title: Mumble Global Libraries - Non-database tools</p>
 * <p>Description: Small tools for Java programs</p>
 * <p>Copyright: Copyright (c) 2002 Frits Jalvingh; released under the LGPL licence.</p>
 * <p>Website <a href="http://www.mumble.to/">Mumble</a></p>
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 * @version 1.0
 */
public class SimpleWriter implements iLogWriter {
	protected PrintStream	m_pw;

	protected PrintStream	m_sw;		// Screen stream if applicable

	protected boolean		m_isfile;

	public SimpleWriter() {
	}

	public void initialize(LogMaster l, Properties p) throws Exception {
		System.out.println("SimpleWriter: logging initialized");
		m_isfile = false;

		String fn = p.getProperty("log.file");
		if(fn.compareToIgnoreCase("stderr") == 0)
			m_pw = System.err;
		else if(fn.compareToIgnoreCase("stdout") == 0)
			m_pw = System.out;
		else {
			//** Writing a file. Must we dump to the screen also?
			String st = p.getProperty("log.screen");
			if(st != null && st.equalsIgnoreCase("yes"))
				m_sw = System.out;

			String ap = p.getProperty("log.mode");
			boolean append = ap.compareToIgnoreCase("append") == 0;
			m_pw = new PrintStream(new FileOutputStream(fn, append));
			m_isfile = true;

			//** For a file: save the current date/time,
			m_pw.println("\n\nLogging started at " + DateFormat.getDateTimeInstance().format(new Date()));
		}
	}

	public void write(Object l, Category c1, Category c2, Category c3, Category c4, String msg, Exception x) throws Exception {
		StringBuffer s = new StringBuffer(100);

		if(m_isfile) {
			//** File logs are preceded by date & time
			s.append(DateFormat.getDateTimeInstance().format(new Date()));
			s.append(" ");
		}

		s.append("[");
		s.append(Thread.currentThread().getName());
		s.append("@");
		s.append(l.getClass().getName());
		s.append("] ");
		s.append(msg);
		s.append("\n");
		if(x != null) {
			s.append(" --- Exception: ");
			s.append(x.toString());
			ByteArrayOutputStream bo = new ByteArrayOutputStream(1024);
			PrintWriter pw = new PrintWriter(bo);
			x.printStackTrace(pw);
			pw.flush();
			s.append("\nStack trace:\n");
			s.append(bo.toString());
		}
		synchronized(m_pw) {
			m_pw.print(s);

			if(m_sw != null)
				m_sw.print(s);
		}
	}

}
