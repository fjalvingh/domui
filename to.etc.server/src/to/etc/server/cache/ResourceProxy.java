package to.etc.server.cache;

/**
 * The core cache entry representing the resource identified by a given physical
 * key. This contains all of the cache related data.
 *
 * @author jal
 * Created on Dec 8, 2005
 */
final public class ResourceProxy {
	private ResourceCache		m_cache;

	/** The key which is the "root" of this thingy and which locates the thingy in the cache. */
	private Object				m_key;

	/**
	 * T when this proxy has been deleted. This gets
	 */
	private boolean				m_deleted;

	/**
	 * The item version that is currently cached. Protected by self.
	 */
	private ResourceRef			m_cachedRef;

	final private CacheStats	m_stats;

	ResourceProxy(ResourceCache rc, Object pk, CacheStats stats) {
		m_cache = rc;
		m_key = pk;
		m_stats = stats;
	}

	public ResourceCache getCache() {
		return m_cache;
	}

	final public CacheStats stat() {
		return m_stats;
	}

	public Object getVfsKey() {
		return m_key;
	}

	ResourceRef getCachedRef() {
		synchronized(this) {
			return m_cachedRef;
		}
	}

	synchronized void setCachedRef(ResourceRef ref) {
		m_cachedRef = ref;
	}

	synchronized void delete() {
		m_deleted = true;
	}

	synchronized boolean isDeleted() {
		return m_deleted;
	}
}
