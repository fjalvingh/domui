package to.etc.server.cache;

import to.etc.util.*;

/**
 * <p>This is a header for a cache lru list. Each cache keeps a list of entries
 * that refer to the impl's allocating resources in that cache. Three cache lists
 * exist: the buffer cache (memory), the code cache (compiled classes) and the
 * file cache.
 *
 * <p>resource impl's are linked to their cache AFTER initialization, lazily. Cache
 * overflow is checked AFTER a resource has initialized. This means that caches
 * can (and will) overflow their allocation. The reason has to do with optimal
 * locking. A resource only knows it's allocation load AFTER it has completely
 * initialized. Initialization means allocating resources from the cache. We do
 * not want to check for cache overflow with every allocation from it, so we
 * check once AFTER all initializations have been done.
 *
 * <p>This means that an impl can have the following states in the cache:
 * <dl>
 * 	<dt>Uninitialized<dd>The impl has not allocated any space, and is not linked
 *  	to the cache. Discarding the resource has no effect on the cache load.
 *	<dt>Unlinked<dd>The impl has allocated resources, and they are accounted for
 *  	in the cache header's 'allocated' fields. The resource is not yet or no more
 *      linked in the cache's used fields. This occurs when the impl is initializing.
 *      If the init fails then the resources are released and the item is never
 *      linked.
 *      It also occurs when the item has been removed because the cache has overflown;
 *      then it means that the resource has been deleted but the impl is still
 *      being used. As soon as the impl is no longer used it will be deleted and
 *      the allocated data will be released.
 *	<dt>Linked<dd>The resource is linked in the LRU list, and it's size is
 *  	accounted in 'allocated' and 'committed' counters.
 * </dl>
 *
 *
 */
public class CacheListHeader {
	/** The resource manager manipulating this cache. Used for locking. */
	private ResourceCache	m_rm;

	/** The first LRU entry in the cache, or NULL if the cache is empty. */
	private CacheListEntry	m_first_ce;

	/** The committed (items are in LRU chain) size */
	private long			m_sz_committed;

	/** The #of committed items (#items in the LRU chain) */
	private int				m_n_committed;

	/** The max. allowed committed size of this cache, in cache units. */
	private long			m_sz_committed_max;

	/** The max. amount of storage ever used in the cache */
	private long			m_sz_peak;

	/** The #of times an object was dropped from this cache because it overflowed the cache */
	private long			m_n_lru_cachereleases;

	/** The name of this cache, for debugging purposes. */
	private final String	m_name;

	public CacheListHeader(ResourceCache rm, String name) {
		m_rm = rm;
		m_name = name;
	}

	public CacheListHeader(ResourceCache rm, String name, long max) {
		m_rm = rm;
		m_name = name;
		m_sz_committed_max = max;
	}

	final public void setSzMax(long max) {
		m_sz_committed_max = max;
	}

	void incCacheReleases() {
		m_n_lru_cachereleases++;
	}

	@Override
	public String toString() {
		return "LruCache:" + m_name;
	}

	public String getInfoString() {
		StringBuffer sb = new StringBuffer(80);
		sb.append("Cache ");
		sb.append(m_name);
		sb.append(": ");
		sb.append("" + m_n_committed);
		sb.append(" objects, ");
		sb.append(StringTool.strSize(m_sz_committed));
		sb.append(" [");
		sb.append(StringTool.strCommad(m_sz_committed));
		sb.append(" bytes]");
		return sb.toString();
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Linking into the cache list...						*/
	/*--------------------------------------------------------------*/
	/**
	 *	Links the resource specified at the start (MRU) of the LRU list. The
	 *	resource MAY NOT be linked already!
	 */
	final private void _link(CacheListEntry ce) {
		if(ce.m_next_ce != null)
			throw new IllegalStateException("kickJal: relinking already linked cache entry!?");

		//-- Special case: the list is empty,
		if(m_first_ce == null) {
			m_first_ce = ce;
			ce.m_next_ce = ce;
			ce.m_prev_ce = ce;
			return;
		}

		//-- Not empty: link this AS the head of the list.
		ce.m_prev_ce = m_first_ce; // New one's previous is current HEAD
		ce.m_next_ce = m_first_ce.m_next_ce; // Current one's NEXT is now my NEXT
		m_first_ce.m_next_ce = ce; // Current head's NEXT is new one,
		ce.m_next_ce.m_prev_ce = ce;

		//-- And last but not least: the HEAD is now the new record!
		m_first_ce = ce;
	}


	/**
	 *	Unlink the resource from the LRU list.
	 */
	private void _unlink(CacheListEntry ce) {
		if(ce.m_next_ce == null)
			throw new IllegalStateException("kickJal: unlinking already unlinked cache entry!?");

		//-- 1. Is this the ONLY record?
		if(ce.m_next_ce == ce) {
			//-- Create an empty list.
			m_first_ce = null;
		} else {
			//-- Is this the head? If so make the previous one head
			if(m_first_ce == ce)
				m_first_ce = ce.m_prev_ce;

			//-- Now: remove this from the chain,
			ce.m_prev_ce.m_next_ce = ce.m_next_ce; // next of PREV is my next
			ce.m_next_ce.m_prev_ce = ce.m_prev_ce;
		}
		ce.m_next_ce = null;
		ce.m_prev_ce = null;
	}


	/**
	 * Unlink the resource from the LRU list. If the thing is already unlinked
	 * nothing happens. If the item is linked then it is removed, and the
	 * committed size is reduced with the size allocated by the item.
	 */
	final void unlink(CacheListEntry ce) {
		if(ce.m_next_ce == null)
			return; // Not linked -> no change
		m_sz_committed -= ce.getSize(); // Reduce commit size,
		m_n_committed--; // And cache entries
		_unlink(ce); // Actually unlink the dude,
	}

	final void link(CacheListEntry ce) {
		//-- If we're already linked then just relink at the top.
		if(ce.m_next_ce != null) // Already linked?
		{
			if(ce == m_first_ce)
				return; // Already at the top -> exit
			_unlink(ce); // ..remove..
			_link(ce); // ..and relink at top
			return;
		}

		//-- Initial link: add to cache committed load and exit
		m_sz_committed += ce.getSize();
		if(m_sz_committed > m_sz_peak)
			m_sz_peak = m_sz_committed;
		m_n_committed++;
		_link(ce);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Info functions.										*/
	/*--------------------------------------------------------------*/
	final public String getName() {
		return m_name;
	}

	final public long getSzCommited() {
		synchronized(m_rm) {
			return m_sz_committed;
		}
	}

	final public long getNumCommited() {
		synchronized(m_rm) {
			return m_n_committed;
		}
	}

	final public long getSzMaxCommited() {
		synchronized(this) {
			return m_sz_committed_max;
		}
	}

	final public long getSzPeak() {
		synchronized(this) {
			return m_sz_peak;
		}
	}

	final public boolean isOverCommited() {
		synchronized(m_rm) {
			return m_sz_committed > m_sz_committed_max;
		}
	}

	final public long getNumLruCacheReleases() {
		synchronized(this) {
			return m_n_lru_cachereleases;
		}
	}

	final ResourceRef getLruItem() {
		if(m_first_ce == null)
			return null; // Empty list!
		CacheListEntry ce = m_first_ce.m_prev_ce; // Get LRU item,
		return ce.m_ri;
	}

	//	/**
	//	 * Remove the LRU item from the chain.
	//	 * @return
	//	 */
	//	final public ResourceRef	unlinkLRU()
	//	{
	//		if(m_first_ce == null) return null;				// Empty list!
	//
	//		//-- Now- unlink
	//		CacheListEntry	ce =  m_first_ce.m_prev_ce;		// Get LRU item,
	//		ce.m_ri.unlink();								// Ask the thing to unlink;
	//		return ce.m_ri;
	//	}

	//	/**
	//	 * For info functions only(!) not to be used for ANY OTHER FUNCTION! This
	//	 * is a performance problem. This function creates an array containing all
	//	 * resources it has in it's list, together with some data pertaining to the
	//	 * resource. Its only use is the NEMA web pages.
	//	 * @return
	//	 */
	//	final public CacheResourceInfo[]	getResources()
	//	{
	//		synchronized(m_rm)
	//		{
	//			CacheResourceInfo[]	ar	= new CacheResourceInfo[ m_n_committed ];
	//			CacheListEntry	ce	= m_first_ce;
	//			if(ce == null) return ar;					// List is empty.
	//
	//			int	ix	= 0;
	//			do
	//			{
	//				ar[ix]	= new CacheResourceInfo(ce.m_sz_cached, ce.m_ri.getProxy(), ce.m_ri);
	//				ix++;
	//				ce	= ce.m_next_ce;
	//			} while(ce != m_first_ce);
	//			return ar;
	//		}
	//	}
}
