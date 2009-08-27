package to.etc.server.vfs;

/**
 * Information passed to a VfsChangeListener about the resource 
 * that has changed.
 *
 * @author jal
 * Created on Dec 5, 2005
 */
final public class VfsChangeEvent {
	/**
	 * The resource that has changed.
	 */
	private VfsKey	m_key;

	public VfsChangeEvent(VfsKey key) {
		m_key = key;
	}

	/**
	 * @return  Returns the key.
	 */
	public VfsKey getKey() {
		return m_key;
	}
}
