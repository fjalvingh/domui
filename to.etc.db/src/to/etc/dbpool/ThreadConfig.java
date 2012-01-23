package to.etc.dbpool;


/**
 * Maintains per-thread configuration for a pool. Used to control things like
 * post-commit listeners and commit inhibition (for creating JUnit tests that
 * do not change the database).
 * <p>Although this structure is shared, it does not need thread protection
 * because it is only ever accessed by a single thread through a {@link ThreadLocal}
 * variable.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 12, 2011
 */
class ThreadConfig {
	private boolean m_disableCommit;

	public boolean isDisableCommit() {
		return m_disableCommit;
	}

	public void setDisableCommit(boolean disableCommit) {
		m_disableCommit = disableCommit;
	}
}
