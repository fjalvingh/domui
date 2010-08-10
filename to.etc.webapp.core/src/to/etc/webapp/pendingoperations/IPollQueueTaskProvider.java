package to.etc.webapp.pendingoperations;

/**
 * A thingy which gets polled regularly for new tasks to execute by the
 * polling queue provider.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 4, 2009
 */
public interface IPollQueueTaskProvider {
	void initializeOnRegistration(PollingWorkerQueue pwq) throws Exception;

	/**
	 * Must return the next task to run <i>without ever blocking</i>, except when executing code to
	 * find the next task.
	 * @return
	 * @throws Exception
	 */
	Runnable getRunnableTask() throws Exception;
}
