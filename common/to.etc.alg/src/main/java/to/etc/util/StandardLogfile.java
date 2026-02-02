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
package to.etc.util;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * Non-rotating logfile implementing ILogSink.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 25, 2010
 */
public class StandardLogfile implements ILogSink {
	/** The date/time formatter. */
	private DateFormat	m_df;

	/** The current log file name */
	private File		m_path_f;

	/** The requested path & name for the log */
	private String		m_logname;

	/** The printwriter to use. If null we're initializing */
	private PrintWriter	m_outWriter;


	public StandardLogfile() {
		m_df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
	}

	public StandardLogfile(String name) {
		this();
		setName(name);
	}

	public void setName(String name) {
		m_logname = name;
	}

	public String getName() {
		return m_logname;
	}

	public File getFile() {
		return m_path_f;
	}

	/**
	 * Sets a new file as the logfile. If the logging was open the old file
	 * is closed and the new one opened.
	 * @param f
	 */
	public void setFile(File f) {
		Date dt = new Date();
		if(getOutWriter() != null) {
			getOutWriter().println("This log was closed on " + m_df.format(dt));
			getOutWriter().println("The new log file is " + f);
			getOutWriter().println("Bye!");
			getOutWriter().close();
			setOutWriter(null);
		}

		try {
			setOutWriter(new PrintWriter(new FileWriter(f, true)));
			getOutWriter().println("************************ Log start ********************************");
			getOutWriter().println("This log file was started on " + m_df.format(dt));
			if(m_path_f != null)
				getOutWriter().println("It was continued from the file " + m_path_f);
			m_path_f = f;
		} catch(IOException x) {
			x.printStackTrace();
			if(getOutWriter() != null) {
				try {
					getOutWriter().close();
					setOutWriter(null);
				} catch(Exception xx) {}

			}
		}
	}

	/**
	 * Called when a new line is to be logged. This returns a new timestamp OR
	 * null if the logfile is closed due to errors.
	 * @return
	 */
	protected Date checkCycle() {
		if(m_path_f == null)
			setFile(new File(m_logname));
		return m_outWriter == null ? null : new Date();
	}

	/**
	 * @param outWriter The outWriter to set.
	 */
	protected void setOutWriter(PrintWriter outWriter) {
		m_outWriter = outWriter;
	}

	/**
	 * @return Returns the outWriter.
	 */
	protected PrintWriter getOutWriter() {
		return m_outWriter;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	LogSink interface implementation						*/
	/*--------------------------------------------------------------*/
	private Calendar	m_cal	= new GregorianCalendar();

	private synchronized String dateStr(Date dt) {
		m_cal.setTime(dt);
		StringBuffer sb = new StringBuffer();
		sb.append(m_cal.get(Calendar.HOUR_OF_DAY));
		sb.append(':');
		StringTool.strAddIntFixed(sb, m_cal.get(Calendar.MINUTE), 10, 2);
		sb.append(':');
		StringTool.strAddIntFixed(sb, m_cal.get(Calendar.SECOND), 10, 2);
		return sb.toString();
	}

	/**
	 * Logs a line to the stream.
	 */
	public synchronized void log(String msg) {
		Date d = checkCycle();
		if(d == null)
			return;
		getOutWriter().print(dateStr(d));
		getOutWriter().print(" ");
		getOutWriter().println(msg);
		getOutWriter().flush();
	}

	/**
	 * Logs an exception to the stream.
	 */
	public synchronized void exception(Throwable t, String msg) {
		Date d = checkCycle();
		if(d == null)
			return;
		getOutWriter().print(dateStr(d));
		getOutWriter().print(" Exception ");
		getOutWriter().println(t.toString());
		getOutWriter().print("Message: ");
		getOutWriter().println(msg);
		getOutWriter().println("----- Stack dump ------");
		t.printStackTrace(getOutWriter());
		getOutWriter().println("----- End of stack ----");
		getOutWriter().flush();
	}

	public synchronized void rawlog(String s) {
		getOutWriter().print(s);
	}

	public void flush() {
		if(m_outWriter != null)
			m_outWriter.flush();
	}


}
