package to.etc.domui.util.resources;


/**
 * Holds the last-modified timestamp for some source "file" used in some production at the time
 * it was used; plus a reference to that file so it's /original/ change time can be determined.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 22, 2008
 */
final public class ResourceTimestamp {
	private IModifyableResource m_ref;

	private long m_ts;

	public ResourceTimestamp(IModifyableResource ref, long ts) {
		m_ref = ref;
		m_ts = ts;
	}

	public boolean isModified() {
		try {
			return m_ref.getLastModified() != m_ts;
		} catch(Exception x) {
			return true;
		}
	}

	public IModifyableResource getRef() {
		return m_ref;
	}

	@Override
	public String toString() {
		return m_ref.toString();
	}
}
