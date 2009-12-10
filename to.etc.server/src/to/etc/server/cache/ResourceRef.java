package to.etc.server.cache;

/**
 * A reference to a specific <i>version</i> of a resource in the
 * cache. When a resource gets created the resource is kept in an
 * instance of ResourceVersion. If some source that the resource
 * depended on during initialization changed then a new resource
 * is to be created using a new version.
 *
 * @author jal
 * Created on Dec 8, 2005
 */
final public class ResourceRef {
	/** The proxy this is a given release of. */
	private ResourceProxy		m_proxy;

	/** The cached object. */
	private Object				m_cachedThingy;

	/** The current LRU link state of this object. */
	private ResourceLinkState	m_linkStatus	= ResourceLinkState.lsNONE;

	/**
	 * The actual dependency set for this resource, i.e. all of the items that
	 * we depend on. This is read-only after ref construction.
	 */
	private CacheDependencies	m_dependencies;

	private CacheListEntry[]	m_entry_ar;

	ResourceRef(ResourceProxy p) {
		m_proxy = p;
	}

	ResourceProxy getProxy() {
		return m_proxy;
	}

	/**
	 * Returns the dependencies of this ref. These are read-only after construction so
	 * they do not need to be synchronized.
	 * @return
	 */
	CacheDependencies getDependencies() {
		return m_dependencies;
	}

	void setDependencies(DependencySet depset) {
		if(m_dependencies != null)
			throw new IllegalStateException("Deps already set");
		m_dependencies = depset.getDependencies();
	}

	/**
	 * Returns all of the LRU lists that this resource should be linked in. This
	 * is read-only after construction so does not need synchronisation.
	 * @return
	 */
	CacheListEntry[] getListHeads() {
		if(m_entry_ar == null)
			throw new IllegalStateException("The resource created by the CacheObjectFactory did not specify it's cached size(s)! You ust call ref.registerXxxxSize() from the factory!");
		return m_entry_ar;
	}

	final public CacheStats getStats() {
		return m_proxy.stat();
	}

	public ResourceLinkState getLinkState() {
		synchronized(getCache()) {
			return m_linkStatus;
		}
	}

	void setLinkStatus(ResourceLinkState state) {
		if(state == null)
			throw new NullPointerException("Link state cannot be null dude");
		synchronized(getCache()) {
			m_linkStatus = state;
		}
	}

	private ResourceCache getCache() {
		return m_proxy.getCache();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Size registration of the cached thingy.				*/
	/*--------------------------------------------------------------*/
	/**
	 * <p>Registers the size-used in the specified cache LRU list. This may
	 * only be called while the resource ref is not in the cache, i.e. at
	 * build time. After creation we will link this resource into all of
	 * the caches it has specified.</p>
	 * <p>This may be called multiple times for a resource; each call replaces
	 * the size set by an earlier call. This call can be used with a zero
	 * size although this is unreasonable. It would cause the item to be removed
	 * when it is an LRU item in the spec'd list.</p>
	 *
	 * @param lh
	 * @param size
	 */
	public void registerSize(CacheListHeader lh, int size) {
		if(getLinkState() != ResourceLinkState.lsNONE)
			throw new IllegalStateException("This can only be called for an initializing resource! The current state is " + getLinkState());

		//-- Find out if we already used this header.
		int at = -1;
		if(m_entry_ar != null) {
			for(int i = m_entry_ar.length; --i >= 0;) {
				CacheListEntry ce = m_entry_ar[i];
				if(ce.getListHead() == lh) {
					//-- Adjust this-item's size and be done.
					ce.setSize(size);
					return;
				}
			}

			//-- New entry. Grow the array, copy,
			at = m_entry_ar.length;
			CacheListEntry[] ar = new CacheListEntry[at + 1];
			System.arraycopy(m_entry_ar, 0, ar, 0, at);
			m_entry_ar = ar;
		} else {
			m_entry_ar = new CacheListEntry[1]; // Create a new thingy to hold this
			at = 0;
		}
		CacheListEntry ce = new CacheListEntry(lh, this); // Make a new dude
		ce.setSize(size);
		m_entry_ar[at] = ce;
	}

	public void registerMemorySize(int size) {
		registerSize(getCache().cacheGetMemoryHeader(), size);
	}

	public void registerFileSize(int size) {
		registerSize(getCache().cacheGetFileHeader(), size);
	}

	public void registerCodeSize(int size) {
		registerSize(getCache().cacheGetCodeHeader(), size);
	}

	final public Object getObject() {
		return m_cachedThingy;
	}

	final void setCachedObject(Object thingy) {
		m_cachedThingy = thingy;
	}
}
