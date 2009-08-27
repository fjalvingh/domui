package to.etc.server.cache;

/**
 * This is an entry in one of the cache lists, referencing a resource proxy and a
 * resource impl using space in one of the caches.
 */
final public class CacheListEntry {
	/** The cache header this belongs to */
	private CacheListHeader	m_ch;

	/** The size (bytes, or cache units) this resource is using */
	private int				m_sz_cached;

	/** The resource implementation using this amount of space there, */
	ResourceRef				m_ri;

	/** The previous thingy (or self) */
	CacheListEntry			m_prev_ce;

	/** The next cache entry (of self) */
	CacheListEntry			m_next_ce;

	public CacheListEntry(CacheListHeader clh, ResourceRef ri) {
		m_ch = clh;
		m_ri = ri;
	}

	public CacheListHeader getListHead() {
		return m_ch;
	}

	public int getSize() {
		return m_sz_cached;
	}

	public void setSize(int sz) {
		m_sz_cached = sz;
	}

	/**
	 * Returns T if this entry IS linked in the appropriate list.
	 * @return
	 */
	final public boolean isLinked() {
		return m_prev_ce != null;
	}

	/**
	 * Link or relink this entry to the cache. If this item is unlinked it gets
	 * linked at the head of the cache AND the commit size for this item is
	 * added to the "committed" counters. If this is already linked then it is
	 * moved to the top (MRU) position.
	 */
	final public void link() {
		m_ch.link(this);
	}

	/**
	 * Unlinks this resource from the cache. If the thing is already unlinked
	 * nothing happens. If the thing is linked then the cache's committed size
	 * is reduced by the allocated size of this item and the thing is removed
	 * from the LRU chain. The allocated size is left alone until setSize() is
	 * called.
	 */
	final public void unlink() {
		m_ch.unlink(this);
	}
}
