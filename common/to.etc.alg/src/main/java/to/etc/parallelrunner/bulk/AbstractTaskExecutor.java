package to.etc.parallelrunner.bulk;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.util.WrappedException;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 09-05-22.
 */
@NonNullByDefault
abstract public class AbstractTaskExecutor<T> extends Thread {
	private final BulkTaskRunner<T> m_runner;

	@Nullable
	private T m_nextTask;

	private boolean m_finished;

	abstract protected void initialize() throws Exception;

	abstract protected void terminate() throws Exception;

	abstract protected void executeOnce(T taskInfo);

	public AbstractTaskExecutor(BulkTaskRunner<T> runner) {
		m_runner = runner;
	}

	@Override
	final public void run() {
		try {
			initialize();
		} catch(Exception x) {
			m_runner.startFailed(x);
			return;
		}

		try {
			runLoop();
		} finally {
			try {
				terminate();
			} catch(Exception x) {
				x.printStackTrace();
			}
		}
	}

	/**
	 * In a loop: accept tasks to do, execute them, then wait for another task
	 * until we're signalled as finished.
	 */
	private void runLoop() {
		for(;;) {
			T task = waitForTask();
			if(null == task)
				break;
			try {
				executeOnce(task);
			} catch(Exception | Error x) {
				m_runner.taskFailed(this, x);
			} finally {
				m_runner.taskFinished(this);
			}
		}
	}

	@Nullable
	private T waitForTask() {
		m_runner.taskFree(this);
		synchronized(this) {
			for(;;) {
				//-- FIRST check for a task before we check for termination, or we might miss the last task
				T task = m_nextTask;
				if(null != task) {
					m_nextTask = null;
					return task;
				}

				//-- Are we finished?
				if(m_finished)
					return null;

				//-- Wait for work to arrive
				try {
					wait();
				} catch(Exception x) {
					throw WrappedException.wrap(x);
				}
			}
		}
	}

	/**
	 * This will cause the task to terminate EXCEPT if there is still a task present(!). So it cannot be used to ABORT!
	 */
	synchronized void setFinished() {
		m_finished = true;
		notifyAll();
	}

	synchronized void setTask(T task) {
		if(m_nextTask != null)
			throw new IllegalStateException("?? Task already set!!");
		m_nextTask = task;
		notifyAll();
	}
}
