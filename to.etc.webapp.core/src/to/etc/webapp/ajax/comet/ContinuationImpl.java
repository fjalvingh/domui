package to.etc.webapp.ajax.comet;

class ContinuationImpl implements Continuation {
	private boolean m_continued;

	private long m_timeout = -1;

	public void resume() {
		synchronized(this) {
			if(m_continued)
				return;
			m_continued = true;
			notifyAll();
		}
	}

	public void setTimeout(long ms) {
		m_timeout = ms;
	}

	long getTimeout() {
		return m_timeout;
	}

	boolean hasCompleted() {
		return m_continued;
	}
}
