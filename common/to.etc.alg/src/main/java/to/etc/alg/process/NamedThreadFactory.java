package to.etc.alg.process;

import java.util.concurrent.ThreadFactory;

/**
 * This is what Java's ThreadFactory should be, sigh.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 05-02-2024.
 */
public class NamedThreadFactory implements ThreadFactory {
	private final String m_poolName;

	private int m_threadCounter;

	private boolean m_asDaemon;

	public NamedThreadFactory(String poolName) {
		this(poolName, true);
	}

	public NamedThreadFactory(String poolName, boolean asDaemon) {
		m_poolName = poolName;
		m_asDaemon = asDaemon;
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread t = new Thread(r, m_poolName + "-" + nextId());
		t.setDaemon(m_asDaemon);
		return t;
	}

	private synchronized int nextId() {
		return ++m_threadCounter;
	}
}
