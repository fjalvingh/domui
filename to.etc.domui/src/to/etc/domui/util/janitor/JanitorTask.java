/*
 * DomUI Java User Interface library
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
package to.etc.domui.util.janitor;

import java.util.*;


/**
 *	Holds a single Janitor request. It holds the administrative details also,
 *  like the (next) start time and the interval time.
 */
abstract public class JanitorTask {
	/** The janitor.. */
	protected Janitor m_j;

	/** The interval time, in seconds, to run this again, */
	protected int m_t_interval;

	/** The NEXT time this task has to run, or -1 when it (still) runs.. */
	protected long m_t_next;

	/** The time this task ran last, */
	protected long m_t_lastrun;

	/** This-task's key (id) in the task vector */
	protected int m_key;

	/** This-task's display string */
	protected String m_taskname;

	/** If this is running, the slot number it runs in, or -1 if not running */
	protected int m_run_slot = -1;

	/** T if this task was removed */
	protected boolean m_deleted = false;

	/** T if this task is unrunnable (too many exceptions) */
	protected boolean m_unrunnable = false;

	public JanitorTask() {}

	/** The function-to-override. */
	public abstract void run() throws Exception;


	/**
	 *	Returns the NEXT start time for this request, depending on the current
	 *  time. The next start time must be somewhere in the future or it can be
	 *  NOW (0). This function can be overridden for more complex timing
	 *  requirements.
	 */
	public long getNextStartTime() {
		//-- By default take the current time, add the interval and return that..
		if(m_t_interval == -1)
			throw new RuntimeException("JanitorTask " + m_taskname + ": attempt to reschedule a run-once");
		long dt = m_j.getTime(); // Get current time,
		dt += 1000 * m_t_interval; // Add the interval, in millis
		return dt;
	}


	/**
	 *	Called by the janitor to make this task set it's next start time.
	 */
	protected void calcNextStartTime() {
		m_t_next = getNextStartTime();
	}

	/**
	 *	Sets a fixed start date for the task.
	 */
	protected void setNextTime(Date dt) {
		m_t_next = dt.getTime(); // Get the UTC time
	}

}
