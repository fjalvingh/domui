package to.etc.server.vfs;

final public class VfsDependency {
	private Object	m_key;

	private long	m_timestamp;

	/**
	 * @param key
	 * @param timestamp
	 */
	public VfsDependency(Object key, long timestamp) {
		m_key = key;
		m_timestamp = timestamp;
	}

	/**
	 * @return the key
	 */
	public final Object getKey() {
		return m_key;
	}

	/**
	 * @return the timestamp
	 */
	public final long getTimestamp() {
		return m_timestamp;
	}
}
