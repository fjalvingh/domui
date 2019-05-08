package to.etc.parallelrunner;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.util.CancelledException;
import to.etc.util.Progress;
import to.etc.util.WrappedException;

import java.util.Objects;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-1-19.
 */
public final class Job implements Runnable {
	private final AsyncWorker m_asyncWorker;

	private final String m_jobId;

	@Nullable
	private IAsyncRunnable m_runner;

	final private IAsyncCompletionListener m_listener;

	final private long m_submitTs;

	private long m_executeTs;

	private long m_finishTs;

	final private int m_priority;

	@Nullable
	private Throwable m_exception;

	private final Progress m_progress;

	public Job(AsyncWorker asyncWorker, String jobId, IAsyncRunnable runner, IAsyncCompletionListener listener, String name, int priority) {
		m_asyncWorker = asyncWorker;
		m_jobId = jobId;
		m_runner = runner;
		m_listener = listener;
		m_submitTs = System.currentTimeMillis();
		m_progress = new Progress(name);
		m_priority = priority;
	}

	public String getJobId() {
		return m_jobId;
	}

	public int getPriority() {
		return m_priority;
	}

	IAsyncRunnable getRunner() {
		return Objects.requireNonNull(m_runner);
	}

	IAsyncCompletionListener getListener() {
		return m_listener;
	}

	public long getSubmitTs() {
		return m_submitTs;
	}

	public synchronized long getFinishTs() {
		return m_finishTs;
	}

	private synchronized void setFinishTs(long finishTs) {
		m_finishTs = finishTs;
	}

	@Nullable
	public synchronized Throwable getException() {
		return m_exception;
	}

	private synchronized void setException(Throwable exception) {
		m_exception = exception;
	}

	public boolean isFinished() {
		return getFinishTs() != 0;
	}

	public boolean isError() {
		return getException() != null;
	}

	public synchronized boolean isRunning() {
		return getFinishTs() == 0 && getExecuteTs() != 0;
	}

	public synchronized long getExecuteTs() {
		return m_executeTs;
	}

	public synchronized void setExecuteTs(long executeTs) {
		m_executeTs = executeTs;
	}

	public Progress getProgress() {
		return m_progress;
	}

	@Override public void run() {
		boolean cancelled = false;
		try {
			setExecuteTs(System.currentTimeMillis());
			getRunner().run(getProgress());
		} catch(CancelledException cx) {
			setException(cx);
			cancelled = true;
		} catch(Exception x) {
			m_exception = x;
		} catch(Error x) {
			setException(x);
			throw x;
		} finally {
			m_progress.complete();
			m_runner = null;                                    // Release any resources
			setFinishTs(System.currentTimeMillis());

			//-- Call the termination listener.
			IAsyncCompletionListener listener = m_listener;
			if(null != listener) {
				try {
					Throwable exception = getException();
					if(exception instanceof Exception || exception == null) {
						listener.onCompleted(cancelled, (Exception) exception);
					} else {
						listener.onCompleted(cancelled, new WrappedException(exception));
					}
				} catch(Exception x) {
					x.printStackTrace();
				}
			}
		}
	}

	/**
	 * Return an indication of where in the queue the job is. This returns:
	 * <ul>
	 * <li>-1 if the job has already finished</li>
	 * <li>0 if the job is executing</li>
	 * <li>Any higher number is the index of the job in the queue</li>
	 * </ul>
	 */
	public int getPosition() {
		if(isFinished())
			return -1;
		if(getExecuteTs() != 0)
			return 0;
		return m_asyncWorker.queueIndex(this) + 1;
	}

	public synchronized String getInfo() {
		StringBuilder sb = new StringBuilder();
		if(isFinished()) {
			sb.append("Finished");
			Throwable exception = getException();
			if(null != exception) {
				sb.append(" with error: ");
				String message = exception.getMessage();
				if(null == message || message.length() == 0)
					sb.append(exception.toString());
				else
					sb.append(message);
				//if(exception instanceof CodeException) {
				//	sb.append(exception.getMessage());
				//} else {
				//	sb.append(exception.toString());
				//}
			}
			return sb.toString();
		}

		if(isRunning()) {
			sb.append("Running, ");
			sb.append(getProgress().getActionPath(3));
			return sb.toString();
		}

		int position = getPosition();
		if(position > 0) {
			sb.append("Waiting for a processor, ").append(position).append(" before me");
			return sb.toString();
		}
		return "?? position " + position;
	}

	@Override public String toString() {
		return "Job#" + m_jobId;
	}
}
