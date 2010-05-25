package to.etc.webapp.pendingoperations;

import java.util.*;

/**
 * Queue containing jobs to execute. When jobs are added here the polling task
 * provider will be signalled.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 4, 2009
 */
public class PolledActionQueue implements IPollQueueTaskProvider {
	private PollingWorkerQueue m_executor;

	private Queue<Runnable> m_queue = new LinkedList<Runnable>();

	/**
	 * Adds a job to the execution queue. If a free thread is available the job gets
	 * run immediately.
	 * @param run
	 */
	public void schedule(Runnable run) {
		synchronized(m_executor) {
			if(m_queue.size() > 1000)
				throw new IllegalStateException("FATAL: The background execution queue is FULL (more than 1000 waiting jobs)");
			m_queue.add(run);
			m_executor.checkProvider(this);
		}
	}

	public void initializeOnRegistration(PollingWorkerQueue pwq) throws Exception {
		m_executor = pwq;
	}

	public Runnable getRunnableTask() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
