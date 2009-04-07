package to.etc.webapp.eventmanager;

/**
 * A base class for VP based record events. This extends the base
 * with a change type and a primary key ID field.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 14, 2006
 */
public class AppEvent extends AppEventBase {
	private final long m_key;

	private final ChangeType m_type;

	public AppEvent(final ChangeType type, final long key) {
		m_type = type;
		m_key = key;
	}

	public long getKey() {
		return m_key;
	}

	public ChangeType getType() {
		return m_type;
	}
}
