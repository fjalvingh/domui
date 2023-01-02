package to.etc.domui.component.delayed;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 02-01-23.
 */
public class DelayedActivitiesExecutor {
	private State m_state = State.TERMINATED;

	private ThreadPoolExecutor m_executor;

	public synchronized void initialize(int maxThreads) {
		if(m_state == State.INITIALIZED)
			return;
		ThreadFactory factory = r -> {
			Thread t = new Thread(r);
			t.setDaemon(true);
			t.setName("delayedExecutor");
			return t;
		};

		PriorityBlockingQueue<Runnable> queue = new PriorityBlockingQueue<>(256, (a, b) -> {
			int pa = (a instanceof DelayedActivityInfo) ? ((DelayedActivityInfo) a).getPriority() : 0;
			int pb = (b instanceof DelayedActivityInfo) ? ((DelayedActivityInfo) b).getPriority() : 0;
			return - Integer.compare(pa, pb);
		});
		m_executor = new ThreadPoolExecutor(maxThreads, maxThreads, 60, TimeUnit.SECONDS, queue, factory);
		m_state = State.INITIALIZED;
	}

	public void terminate() {
		ThreadPoolExecutor executor;
		synchronized(this) {
			if(m_state == State.TERMINATED)
				return;
			m_state = State.TERMINATED;
			executor = m_executor;
			m_executor = null;
		}
		if(null != executor)
			executor.shutdownNow();
	}

	public synchronized void schedule(DelayedActivityInfo dai) {
		if(m_state != State.INITIALIZED)
			throw new IllegalStateException("Delayed executor has invalid state: " + m_state);
		m_executor.execute(dai);
	}

	public void remove(DelayedActivityInfo dai) {
		m_executor.remove(dai);
	}

	public enum State {
		TERMINATED, INITIALIZED
	}
}
