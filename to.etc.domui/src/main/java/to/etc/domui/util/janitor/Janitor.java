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

import org.slf4j.*;


/**
 *	The Janitor class handles all housekeeping chores for NEMA and NEMA-derived
 *  applications. The janitor is started as a separate thread (by the
 *  ResourceBroker). It maintains a list of things-to-do, where each thing will
 *  be scheduled at a specified interval.
 *  Each janitor task will be scheduled in a separate thread. The max. number
 *  of threads (janitor jobs) can be specified.
 *  As soon a job finishes it is rescheduled (if required) at the interval
 *  specified. Since rescheduling will only take place after the task has
 *  finished there's no risk for running the same task twice.
 *
 *
 *  Internals:
 *  The janitor has a #of job slots. Each slot can contain a single thread. All
 *  Janitor threads derive from JanitorThread, a local class. The janitor's task
 *  list contains task entries, containing an interval and a reference to some
 *  class and method.
 *
 * 	The scheduler keeps all things to-do ordered by time-of-next-execution.
 */
public class Janitor implements Runnable {
	static private final Logger LOG = LoggerFactory.getLogger(Janitor.class);

	/// Start phase: not started, not running
	static private final int jspNONE = 0;

	/// Start phase: started but not yet running
	static private final int jspSTART = 1;

	/// Start phase: running.
	static private final int jspRUN = 2;

	/// T if the janitor itself has started.
	private int m_start_phase;

	/// The max #of janitor jobs running at a time (size of slot table)
	private int m_n_maxjobs;

	/// The next task number to assign.
	private int m_tasknr = 1;

	/// T if someone requested janitor termination.
	private boolean m_termination_requested = false;

	/// The CURRENTLY RUNNING job queue
	private JanitorThread[] m_job_ar;

	/// The current #of RUNNING jobs
	private int m_n_running;

	/// Last time the SCHEDULER has run (jobs are running too long) in SECONDS
	private long m_t_last_sched_run;

	/// The last time that a thread slot was free IN SECONDS
	private long m_t_freeslot;

	/// The list of assigned tasks, ordered on next-execution time.
	private ArrayList<JanitorTask> m_task_v = new ArrayList<JanitorTask>();

	/// Set by the janitor thread as soon as it starts running.
	private boolean m_thread_isrunning = false;

	public Janitor(int maxjobs) {
		m_n_maxjobs = maxjobs;
		m_job_ar = new JanitorThread[maxjobs];
	}


	/**
	 *	Starts this janitor as a new thread; it waits till the thread starts!
	 */
	public void start() {
		synchronized(this) {
			if(m_start_phase != jspNONE)
				throw new RuntimeException("This instance has ALREADY been started!");
			m_start_phase = jspSTART;
		}
		Thread t = new Thread(this, "Janitor");
		t.setDaemon(true);
		t.start();

		int ct = 10;
		synchronized(this) {
			while(!m_thread_isrunning && ct-- > 0) {
				try {
					wait(5000);
				} catch(Exception x) {}
			}
		}
		if(ct <= 0)
			throw new RuntimeException("FATAL Janitor: thread did not start!?");
		LOG.debug("Janitor: thread seems to run, life's good ;-) " + ct + ", " + m_thread_isrunning);
	}


	/**
	 *	Returns the current time, in milliseconds, as returned from System.currentTimeMillis()
	 */
	public long getTime() {
		return System.currentTimeMillis();

		//		return (new Date()).getTime() / 1000;
	}


	/**
	 *	Returns the current scheduler run's time.
	 */
	private long getSchedTime() {
		return m_t_last_sched_run;
	}


	protected synchronized void logTask(JanitorThread jt, String msg) {
		synchronized(jt) {
			LOG.debug("T" + jt.m_slot + ":" + jt.m_jt.m_taskname + "- " + msg);
		}
	}


	/**
	 *	Add a new task to the janitor's tables. No duplicate task checking is
	 *  done! The returned ID can be used to remove the task from the tables.
	 * @param interval		interval in seconds
	 * @param once			TRUE if this is a single-shot event
	 * @param name			The name of the dude for info pps
	 * @param jt			The task functor to call at time zero.
	 */
	public int addTask(int interval, boolean once, String name, JanitorTask jt) throws Exception {
		jt.m_j = this; // Set janitor,
		jt.m_taskname = name;
		if(once) {
			jt.m_t_next = System.currentTimeMillis() + interval * 1000;
			jt.m_t_interval = -1;
		} else {
			jt.m_t_interval = interval; // Interval, in seconds.
			jt.calcNextStartTime(); // Calculate next time-to-start,
		}

		insertOrderedTask(jt);

		return jt.m_key; // Return the ID
	}

	/**
	 *	Add a new task to the janitor's tables, that first executes after given offset, and repeats in given interval.
	 *	No duplicate task checking is done! The returned ID can be used to remove the task from the tables.
	 * @param offset		offset interval in seconds, before task runs for first time
	 * @param interval		interval in seconds
	 * @param name			The name of the dude for info pps
	 * @param jt			The task functor to call at time zero.
	 */
	public int addTask(int offset, int interval, String name, JanitorTask jt) throws Exception {
		jt.m_j = this; // Set janitor,
		jt.m_taskname = name;
		jt.m_t_next = getTime() + offset * 1000;
		jt.m_t_interval = interval;

		insertOrderedTask(jt);

		return jt.m_key; // Return the ID
	}

	private synchronized void insertOrderedTask(JanitorTask jt) throws JanitorException {
		if(m_task_v.contains(jt)) // Already in tables?
			throw new JanitorException(jt, "This task was ALREADY scheduled!!");
		jt.m_key = m_tasknr++; // Assign an unique task number
		insertOrdered(jt); // Insert into vector, ordered by execution time,
	}

	/**
	 *	Adds a task to the janitor's table. The task is to start at a given
	 *  date/time(!) and runs only once. The date/time passed is only as
	 *  accurate as the janitor's main loop allows!
	 *  If the time that the task has to start is expired then the task IS
	 *  posted and will execute the next time the janitor's main loop sees it.
	 */
	public int addTaskAt(Date attime, String name, JanitorTask jt) throws Exception {
		jt.m_j = this; // Set janitor,
		jt.m_taskname = name;
		jt.m_t_interval = -1; // Interval, in seconds.

		//-- Calculate the time this task has to awaken,
		//		System.out.println("TaskAT " + name + " runs at " + attime.toString());
		jt.setNextTime(attime);

		insertOrderedTask(jt);

		return jt.m_key; // Return the ID
	}

	public boolean cancelJob(int id) {
		synchronized(this) {
			for(JanitorTask task : m_task_v) {
				if(task.m_key == id && !task.m_deleted) {
					task.m_deleted = true;
					m_task_v.remove(task);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 *	Inserts the specified task into the to-do vector, at a position which
	 *  represents its run time, so that vector(0) represents the FIRST task
	 *  to become eigible to run etc.
	 *  @todo inplement binary search/insert
	 */
	private synchronized void insertOrdered(JanitorTask jt) {
		//-- Locate an appropriate index (lazy - should use binary search)
		for(int i = 0; i < m_task_v.size(); i++) // Eval all elements,
		{
			JanitorTask tjt = m_task_v.get(i);
			if(jt.m_t_next < tjt.m_t_next) {
				//-- New one has to execure before current: insert!!
				m_task_v.add(i, jt); // Insert before CURRENT
				return;
			}
		}

		//-- Insert as the last'un
		m_task_v.add(jt); // Append at end
	}


	/**
	 *	Initialize the janitor.
	 */
	private void initialize() {}

	/**
	 *	Terminates the janitor.
	 */
	private void terminialize() {}

	public synchronized boolean mustTerminate() {
		return m_termination_requested;
	}

	/**
	 *	The main Janitor thread loop. It sleeps for a minute, then it runs
	 *  the scheduler.
	 */
	@Override
	public void run() {
		m_t_last_sched_run = getTime(); // Make sure time's correct
		m_t_freeslot = m_t_last_sched_run;

		synchronized(this) {
			m_start_phase = jspRUN;
			m_thread_isrunning = true;
			notify();
		}
		LOG.debug("Janitor thread started - initializing");

		try {
			initialize();

			while(!mustTerminate()) {
				runSinglePass(); // Run a single time.
				Thread.sleep(10000); // Sleep for a minute,
			}
			terminialize();
		} catch(Throwable t) {
			t.printStackTrace();
			LOG.error("FATAL exception in mainloop: " + t.getMessage(), t);
		} finally {
			m_thread_isrunning = false;
		}
		LOG.debug("Janitor thread stopped!?");
	}


	/**
	 *	Runs a single pass.
	 */
	private void runSinglePass() {
		try {
			m_t_last_sched_run = getTime(); // Get scheduler run time,

			if(!handleJobQueue()) // Check all finished stuff in the job queue.
			{
				//-- No slots... How long ago did the scheduler run?
				if(m_t_freeslot - getSchedTime() > 10 * 60 * 1000) {
					System.out.println("Janitor: No jobs have completed in 10 minutes; cannot schedule!!");
					LOG.info("WARNING: NO JANITOR JOBS COMPLETED IN 10 MINUTES!!");
				}
			} else
				m_t_freeslot = getSchedTime();
		} catch(Throwable x) {
			LOG.warn("ERROR! Janitor exception catched: " + x.toString());
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Job management										*/
	/*--------------------------------------------------------------*/
	/**
	 *	Checks all running jobs for completion. All completed jobs are removed
	 *  from the job table and -if required- are rescheduled. The routine returns
	 *  T if there are job slots free.
	 */
	private boolean handleJobQueue() {
		boolean hascompleted = false;

		for(int i = 0; i < m_n_maxjobs; i++) {
			if(handleJobSlot(i))
				hascompleted = true;
		}
		return hascompleted;
	}


	/**
	 *	Marks a job as complete, frees the job slot, and reschedules the task if
	 *  appropriate.
	 */
	private void jobCompleted(JanitorThread jtd) {
		synchronized(this) {
			m_n_running--;
			jtd.setState(JanitorThread.jtfIDLE); // Slot is free

			//-- Reschedule the task..
			JanitorTask jt = jtd.m_jt;
			jtd.m_jt = null; // Slot is FREE!
			jt.m_run_slot = -1; // Not running in slot!

			m_task_v.remove(jt); // Remove task from queue,
			if(!jt.m_deleted && jt.m_t_interval != -1) // Not removed and not single-pass?
			{
				jt.calcNextStartTime(); // Calculate next time-to-start,
				insertOrdered(jt); // Insert into vector, ordered by execution time,
			}
		}
	}


	/**
	 *	Checks to see if the given slot's job has completed, or is empty. This
	 *  reschedules completed tasks, and schedules a new task for empty slots.
	 *  The routine returns T if this slot was empty or complete.
	 */
	private boolean handleJobSlot(int sn) {
		//		System.out.println("SCHED: Handling job slot "+sn);

		JanitorThread jtd = m_job_ar[sn];
		if(jtd != null) // Is the slot occupied?
		{
			if(jtd.hasState(JanitorThread.jtfRUNNING))// Entry has completed?
				return false; // No - slot not available!

			//-- Ok: do we need to clean up?
			if(jtd.hasState(JanitorThread.jtfTERM)) // Has just terminated?
			{
				//-- This has completed. Make the slot available.
				jobCompleted(jtd); // Complete this
			}
		}

		//-- When here the current job slot is FREE- can a task be found to fill it?
		JanitorTask jt;
		synchronized(this) {
			jt = getRunnableTask(); // Find an available task
			if(jt == null)
				return true; // No task - exit with slot available

			//-- Schedule this task to run in this slot,
			if(jtd == null) // Get a job structure,
			{
				jtd = new JanitorThread(this, sn);
				m_job_ar[sn] = jtd;
			}

			//-- Assign.
			jtd.assignTask(jt); // Assign this to the thread,
			//			System.out.println("SCHEDULING: "+jt.m_taskname+" in slot "+sn);

			m_n_running++;
		}

		//-- When here we have to start a thread for the thing.
		Thread t = new Thread(jtd, "Jt:" + jt.m_taskname);
		t.start();
		return true;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Scheduler.											*/
	/*--------------------------------------------------------------*/
	/**
	 *	Scans the task table to find the 1st job to run. If no job is runnable
	 *  it returns null.
	 */
	private JanitorTask getRunnableTask() {
		long ct = getSchedTime(); // Get current scheduler runtime,

		synchronized(this) // While traversing task table
		{
			for(JanitorTask jt : m_task_v) {
				if(ct < jt.m_t_next) // This task must start LATER -> end!!!
					return null; // Nothing to start YET

				//-- This MAY run as far as time is concerned..
				if(!jt.m_deleted && !jt.m_unrunnable && jt.m_run_slot == -1) {
					//-- This is a runnable job AND it doesn't run already ;-)
					return jt;
				}
			}
		}
		return null;
	}

	/**
	 *	Returns the last scheduler's run timestamp. This is a VERY fast way of
	 *  obtaining time - without incurring the cost of a system call! This
	 *  time gets updated once a minute.
	 */
	public long getTimeStamp() {
		if(!m_thread_isrunning)
			throw new RuntimeException("Janitor: main thread is not running!?");
		return m_t_last_sched_run;
	}

	static private Janitor m_j;

	/**
	 *	Returns the Janitor, the class handling housekeeping chores.
	 */
	static public synchronized Janitor getJanitor() {
		if(m_j != null)
			return m_j;

		m_j = new Janitor(10);
		m_j.start();

		//		try {
		//			//			m_j.addTask(30*60, false, "SysHealth", new SystemHealthChecker());
		//		}
		//		catch(Exception x) {
		//			Panicker.logUnexpected(x, "in creating janitor thing");
		//		}
		return m_j;
	}


}
