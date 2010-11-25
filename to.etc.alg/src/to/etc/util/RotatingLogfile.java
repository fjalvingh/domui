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
import java.util.*;

/**
 * Encapsulates a rotating logfile. It contains methods to write log records to
 * a file; the file will rotate to a new name every day round midnight. By
 * default it will clear out files older than 7 days when rotating.
 * Before using the logfile you MUST have called setName, or you must have used
 * the constructor which passed a name. The name is the full path to a logfile
 * including a filename. The system will insert a date part BEFORE the filename
 * extension for every logfile generated. So when entering a name like
 * /var/log/serverinfo/mailer.log a generated name could be something like
 * /var/log/serverinfo/mailer2001-12-31.log
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class RotatingLogfile extends StandardLogfile {
	/** The last time a rotate check was done. */
	private long				m_check_ts;

	/** The #of days a logfile must be kept. */
	private int					m_log_days	= 7;

	/** The current logfile's date/time stamp, */
	private GregorianCalendar	m_currfile_cal;

	public RotatingLogfile() {
	}

	public RotatingLogfile(String name) {
		super(name);
	}

	/**
	 * Checks to see if the log must be opened/rotated. The log is opened when
	 * the printwriter is null; it gets rotated if the day number from the
	 * calendar changes.
	 */
	@Override
	protected synchronized Date checkCycle() {
		Date d = new Date();
		if(d.getTime() < m_check_ts)
			return getOutWriter() != null ? d : null;
		m_check_ts = d.getTime() + 1 * 60 * 1000; // New check time.

		//-- Need to check. Get the current date/time...
		GregorianCalendar cal = new GregorianCalendar();
		File path = null;
		File f = null;
		if(getOutWriter() != null) // ARE we currently open?
		{
			if(cal.get(Calendar.DAY_OF_YEAR) == m_currfile_cal.get(Calendar.DAY_OF_YEAR))
				return getOutWriter() != null ? d : null; // On same day still-> exit

			//-- We need to roll over to a new file, and we have to delete the file of a week earlier.
			path = makeCalendarFile(cal); // Make filename for now;
			GregorianCalendar cold = (GregorianCalendar) cal.clone();
			cold.add(Calendar.DATE, -m_log_days); // Get (7) days earlier.
			f = makeCalendarFile(cold); // Make a filename from that,
			f.delete(); // And drop it;

			//-- Now: start a new file..
			getOutWriter().println("\n\n********************* Log rotation ******************************");
		} else
			path = makeCalendarFile(cal); // Make filename for now;

		//-- Open a new rotlog. If it fails pw stays null.
		setFile(path);
		m_currfile_cal = cal;
		try {
			// m_pw	= new PrintWriter(new FileWriter(m_path_f)); mbp, nov 2004
			if(f == null)
				getOutWriter().println("This logfile is NOT a start of a rotated log file.");
			else
				getOutWriter().println("This is the continuation of the rotated logfile " + f);

			getOutWriter().println("----------");
			return d;
		} catch(Exception x) {
			x.printStackTrace();
			setOutWriter(null);
		}
		return null;
	}


	/**
	 * Creates a new filename from the filename pattern.
	 */
	private File makeCalendarFile(Calendar c) {
		//-- 1. Extract file path and extension.
		String base, ext;
		int pos = FileTool.findFilenameExtension(getName());
		if(pos == -1) {
			base = getName();
			ext = ".log";
		} else {
			base = getName().substring(0, pos);
			ext = getName().substring(pos);
		}

		//-- 2. Make a name part using date components yyyy/mm/dd
		int dd = c.get(Calendar.DAY_OF_MONTH);
		int mm = c.get(Calendar.MONTH) + 1; // Bloody assholes!
		int yy = c.get(Calendar.YEAR);

		StringBuffer sb = new StringBuffer(128);
		sb.append(base);
		sb.append(StringTool.intToStr(yy, 10, 4));
		sb.append('-');
		sb.append(StringTool.intToStr(mm, 10, 2));
		sb.append('-');
		sb.append(StringTool.intToStr(dd, 10, 2));
		sb.append(ext);

		return new File(sb.toString());
	}

	@Override
	public void setFile(File f) {
		super.setFile(f);
		m_currfile_cal = new GregorianCalendar();
	}

}
