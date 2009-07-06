package to.etc.domui.server.reloader;

/**
 * Holds the last-modified timestamp for a class thingy.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 22, 2008
 */
final public class ResourceTimestamp {
	private IResourceRef m_ref;

	private long m_ts;

	public ResourceTimestamp(IResourceRef ref, long ts) {
		m_ref = ref;
		m_ts = ts;
	}

	public boolean changed() {
		try {
			return m_ref.lastModified() != m_ts;
		} catch(Exception x) {
			return true;
		}
	}

	IResourceRef getRef() {
		return m_ref;
	}

	@Override
	public String toString() {
		return m_ref.toString();
	}
}
