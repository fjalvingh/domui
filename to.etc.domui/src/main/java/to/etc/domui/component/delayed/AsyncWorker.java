package to.etc.domui.component.delayed;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.util.CancelledException;
import to.etc.util.Progress;
import to.etc.util.WrappedException;
import to.etc.webapp.nls.CodeException;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A worker implementation that can be used to run async tasks
 * using a fixed number of execution workers.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 18-5-18.
 */
@NonNullByDefault
public class AsyncWorker {
	private long m_idCount = 1;

	static private AsyncWorker m_instance = new AsyncWorker();

	private boolean m_initialized;

	private boolean m_terminated;

	@Nullable
	private ThreadPoolExecutor m_executor;

	final private Map<String, Reference<Job>> m_jobMap = new HashMap<>();

	public static AsyncWorker getInstance() {
		return m_instance;
	}

	public synchronized void initialize(int maxThreads) {
		if(m_initialized)
			return;
		ThreadFactory factory = r -> {
			Thread t = new Thread(r);
			t.setDaemon(true);
			t.setName("asyncWorker");
			return t;
		};

		PriorityBlockingQueue<Runnable> queue = new PriorityBlockingQueue<>(256, (a, b) -> {
			int pa = (a instanceof Job) ? ((Job) a).getPriority() : 0;
			int pb = (b instanceof Job) ? ((Job) b).getPriority() : 0;
			return - Integer.compare(pa, pb);
		});
		//BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(8192, true);
		m_executor = new ThreadPoolExecutor(maxThreads, maxThreads, 60, TimeUnit.SECONDS, queue, factory);
		m_initialized = true;
	}

	public void terminate() {
		ThreadPoolExecutor executor;
		synchronized(this) {
			if(! m_initialized || m_terminated)
				return;
			m_terminated = true;
			executor = m_executor;
		}
		if(null != executor)
			executor.shutdownNow();
	}

	private synchronized String nextID() {
		return "J#" + (m_idCount++);
	}

	public synchronized String schedule(String name, IAsyncRunnable runnable, IAsyncCompletionListener onComplete) {
		return schedule(name, runnable, onComplete, 0);
	}

	public synchronized String schedule(String name, IAsyncRunnable runnable, IAsyncCompletionListener onComplete, int priority) {
		if(! m_initialized)
			throw new IllegalStateException("Not initialized");
		if(m_terminated)
			throw new IllegalStateException("The executor has been terminated - system shutdown in progress");
		String id = nextID();
		Job job = new Job(this, id, runnable, onComplete, name, priority);
		ThreadPoolExecutor executor = m_executor;
		if(null == executor)
			throw new IllegalStateException();
		executor.execute(job);
		m_jobMap.put(id, new WeakReference<>(job));
		if(m_idCount % 5 == 0)
			pruneJobs();
		return id;
	}

	private int queueIndex(Job job) {
		ThreadPoolExecutor executor = m_executor;
		if(null == executor)
			return -1;
		ArrayBlockingQueue<Runnable> aq = (ArrayBlockingQueue<Runnable>) executor.getQueue();

		//-- For some reason the assholes that made the above joke have no indexOf.
		int assholes = 0;
		for(Runnable runnable : aq) {
			if(runnable == job)
				return assholes;
			assholes++;
		}
		return -1;
	}

	private synchronized void pruneJobs() {
		Iterator<Reference<Job>> iterator = m_jobMap.values().iterator();
		long ets = System.currentTimeMillis() - 30 * 60 * 1000;
		while(iterator.hasNext()) {
			Reference<Job> r = iterator.next();
			Job job = r.get();
			if(null == job) {
				iterator.remove();
			} else {
				if(job.getFinishTs() > 0 && job.getFinishTs() < ets) {
					iterator.remove();
				}
			}
		}
	}

	@Nullable
	public synchronized Job findJob(String jobId) {
		pruneJobs();
		Reference<Job> r = m_jobMap.get(jobId);
		if(null == r)
			return null;
		Job job = r.get();
		if(null == job) {
			m_jobMap.remove(jobId);
			return null;
		}
		return job;
	}

	public Executor getExecutor() {
		ThreadPoolExecutor executor = m_executor;
		if(null == executor)
			throw new IllegalStateException("You must call initialize() before you can use the executor.");
		return executor;
	}

	public final class Job implements Runnable {
		private final AsyncWorker m_asyncWorker;

		private final String m_jobId;

		final private IAsyncRunnable m_runner;

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

		public IAsyncRunnable getRunner() {
			return m_runner;
		}

		public IAsyncCompletionListener getListener() {
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
				m_runner.run(getProgress());
			} catch(CancelledException cx) {
				setException(cx);
				cancelled = true;
			} catch(Exception x) {
				m_exception = x;
			} catch(Error x) {
				setException(x);
				throw x;
			} finally {
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
		 *     <li>-1 if the job has already finished</li>
		 *     <li>0 if the job is executing</li>
		 *     <li>Any higher number is the index of the job in the queue</li>
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
					if(exception instanceof CodeException) {
						sb.append(exception.getMessage());
					} else {
						sb.append(exception.toString());
					}
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
}
