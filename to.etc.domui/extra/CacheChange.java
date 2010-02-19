package to.etc.domui.util.images.cache;

import java.util.*;

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
	/** When this action has caused extra memory to be used this contains the #bytes that the MEMORY cacheload has increased */
	private long m_extraMemoryUsed;

	/** When this action has caused extra file(s) to be used this contains the #bytes that the FILE cacheload has increased */
	private long m_extraFilespaceUsed;

	/** Contains every ImageInstance that was used (and not deleted) in this task. Each of these will be marked as recently-used when the action returns to the cache. */
	private List<ImageInstance> m_instancesUsed = new ArrayList<ImageInstance>();

	/** Contains invalidated image instances, for instance because the source image has changed. These need to be subtracted from the cache and it's use count needs to be decremented. */
	private List<ImageInstance> m_imagesDiscarded = new ArrayList<ImageInstance>();

	public void addMemoryLoad(long load) {
		m_extraMemoryUsed += load;
	}

	public void addFileLoad(long load) {
		m_extraFilespaceUsed += load;
	}

	/**
	 * Register an image as recently-used. The cache will relink it as used. Cannot be
	 * called for deleted images.
	 * @param ii
	 */
	public void addUsedImage(ImageInstance ii) {
		if(m_imagesDiscarded.contains(ii))
			throw new IllegalStateException("Trying to use an image that is marked as deleted: " + ii);
		m_instancesUsed.add(ii);
	}

	public void addDeletedImage(ImageInstance ii) {
		m_instancesUsed.remove(ii); // If it was used earlier remove from there
		m_imagesDiscarded.add(ii);

		//-- Reduce cache loads by the released image's sizes,
		m_extraFilespaceUsed -= ii.getFileCacheSize();
		m_extraMemoryUsed -= ii.getFileCacheSize();
		ii.setFileCacheSize(0); // Make very certain they are not counted again.
		ii.setMemoryCacheSize(0);
	}
}
