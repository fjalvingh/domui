package to.etc.domui.caches.images;

import java.util.*;

import javax.annotation.*;

/**
 * This is an accounting record for a cache change in progress. While individual
 * images are manipulated the main cache structures must remain unlocked, but the
 * actual load of images on the cache (their cache size in bytes) is available
 * long after they are added to the cache. To accomodate we lock the cache twice:
 * at the start of an image retrieve to obtain/create the ImageRoot, and at the
 * end of ANY action to do all of the change accounting in the cache for the
 * items that changed. When the code executing the change returns the root
 * cache is again locked and the accounting changes are done quickly.
 *
 * <h2>Locking strategy</h2>
 * This object itself is always used by <i>one</i> thread at a time; ownership passes
 * protected by locks that are not present on this object. While the data in this
 * object changes the ImageRoot object for which this data changes is locked.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 8, 2009
 */
class CacheChange {
	/** Contains every CachedImageData that was used (and not deleted) in this task. Each of these will be marked as recently-used when the action returns to the cache. */
	private List<CachedImageFragment> m_usedFragmentList = new ArrayList<CachedImageFragment>();

	/** Contains invalidated image instances, for instance because the source image has changed. These need to be subtracted from the cache and it's use count needs to be decremented. */
	private List<CachedImageFragment> m_deletedFragmentList = new ArrayList<CachedImageFragment>();

	/**
	 * Register an image as recently-used. The cache will relink it as used. Cannot be
	 * called for deleted images.
	 * @param ii
	 */
	public void addUsedFragment(@Nonnull CachedImageFragment ii) {
		if(ii == null)
			throw new IllegalArgumentException("Cannot pass null");
		if(m_deletedFragmentList.contains(ii))
			throw new IllegalStateException("Trying to use an image that is marked as deleted: " + ii);
		m_usedFragmentList.add(ii);
	}

	public void addDeletedFragment(@Nonnull CachedImageFragment ii) {
		if(ii == null)
			throw new IllegalArgumentException("Cannot pass null");
		m_usedFragmentList.remove(ii); // If it was used earlier remove from there
		m_deletedFragmentList.add(ii);
	}

	@Nonnull
	public List<CachedImageFragment> getUsedFragmentList() {
		return m_usedFragmentList;
	}

	@Nonnull
	public List<CachedImageFragment> getDeletedFragmentList() {
		return m_deletedFragmentList;
	}
}
