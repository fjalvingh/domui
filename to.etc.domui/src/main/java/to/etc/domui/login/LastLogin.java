package to.etc.domui.login;

/**
 * Keeps a timestamp and a failed login count for a user.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 10-11-17.
 */
final class LastLogin {
	private final String m_userId;

	private int m_failCount;

	final private long m_firstAttempt;

	public LastLogin(String userId) {
		m_userId = userId;
		m_firstAttempt = System.currentTimeMillis();
	}

	public String getUserId() {
		return m_userId;
	}

	public int getFailCount() {
		return m_failCount;
	}

	public void setFailCount(int failCount) {
		m_failCount = failCount;
	}

	public long getFirstAttempt() {
		return m_firstAttempt;
	}
}
