package to.etc.server.janitor;

/**
 * The event from the database.
 *
 * @author jal
 * Created on Jan 23, 2005
 */
public class UpdateEvent {
	private long	m_upid;

	private long	m_i1, m_i2, m_i3;

	private String	m_s1, m_s2;

	private String	m_key;


	/**
	 * @param upid
	 * @param key
	 * @param s1
	 * @param s2
	 * @param i1
	 * @param i2
	 * @param i3
	 */
	public UpdateEvent(long upid, String key, String s1, String s2, long i1, long i2, long i3) {
		m_upid = upid;
		m_key = key.toLowerCase();
		m_s1 = s1;
		m_s2 = s2;
		m_i1 = i1;
		m_i2 = i2;
		m_i3 = i3;
	}

	/**
	 * @return Returns the i1.
	 */
	public long getI1() {
		return m_i1;
	}

	/**
	 * @return Returns the i2.
	 */
	public long getI2() {
		return m_i2;
	}

	/**
	 * @return Returns the i3.
	 */
	public long getI3() {
		return m_i3;
	}

	/**
	 * @return Returns the event name.
	 */
	public String getEventName() {
		return m_key;
	}

	/**
	 * @return Returns the s1.
	 */
	public String getS1() {
		return m_s1;
	}

	/**
	 * @return Returns the s2.
	 */
	public String getS2() {
		return m_s2;
	}

	/**
	 * @return Returns the upid.
	 */
	public long getUPID() {
		return m_upid;
	}
}
