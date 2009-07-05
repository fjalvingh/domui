package to.etc.domui.util.janitor;

import java.util.*;

/**
 *	Encapsulates a single job spawned by the janitor.
 */
public class JanitorThread implements Runnable {
	/** Idle and available for a job */
	public static final int jtfIDLE = 0;

	/** Started but thread has not yet started */
	public static final int jtfASSIGN = 1;

	/** Currently running */
	public static final int jtfRUNNING = 2;

	/** Terminated but not yet released */
	public static final int jtfTERM = 3;

	/** Terminated and released. */
	public static final int jtfDONE = 4;


	/** The janitor that spawned this */
	private Janitor m_j;

	/** The janitor task executing in this thread */
	protected JanitorTask m_jt;

	/** The time the current task was assigned/ started. */
	private long m_t_start;

	/** The slot number for this Janitor Thread  */
	protected int m_slot;

	/** This thread's status. */
	private int m_state;


	protected synchronized boolean hasState(int state) {
		return m_state == state;
	}


	protected synchronized void setState(int state) {
		m_state = state;
	}

	/// The start time for this thread

	public JanitorThread(Janitor j, int slot) {
		m_j = j;
		m_slot = slot;
	}

	public synchronized void assignTask(JanitorTask jt) {
		m_jt = jt;
		m_t_start = System.currentTimeMillis();
		m_state = jtfASSIGN; // Assigned but not running
		jt.m_run_slot = m_slot; // Set runslot,
		jt.m_t_lastrun = m_t_start; // Task last run,
	}


	/**
	 *	The main thread entry for the job...
	 */
	public void run() {
		m_j.logTask(this, "Task started..");
		setState(jtfRUNNING);

		try {
			Date d = new Date();
			m_jt.run(); // Run the task,
			long dt = d.getTime() - m_t_start;
			m_j.logTask(this, "Task completed in " + Long.toString(dt) + " ms");
		} catch(Exception x) {
			m_j.logTask(this, "Task EXCEPTION: " + x.getMessage());
			//			Panicker.logUnexpected(x, "Janitor task " + m_jt.m_taskname);
		} finally {
			//-- Discard any connections used by this thread
			//			PoolManager.getInstance().closeThreadConnections();
		}

		//-- Task terminated.
		setState(jtfTERM);
	}


}
