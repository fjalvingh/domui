package to.etc.domui.component.delayed;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

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

	public synchronized Job schedule(String name, IAsyncRunnable runnable, IAsyncCompletionListener onComplete) {
		return schedule(name, runnable, onComplete, 0);
	}

	public synchronized Job schedule(String name, IAsyncRunnable runnable, IAsyncCompletionListener onComplete, int priority) {
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
		return job;
	}

	int queueIndex(Job job) {
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

}
