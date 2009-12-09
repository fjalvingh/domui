package to.etc.domui.caches.images;

import javax.annotation.concurrent.*;

/**
 * The base for a cached image thingerydoo. This is maintained by
 * the ImageCache.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 9, 2009
 */
class CachedImageFragment {
	/** The root descriptor of the image's base */
	private final ImageRoot m_imageRoot;

	/** An unique string describing the permutation of the original that this contains. When "" (empty string) this is the ORIGINAL image. */
	private final String m_permutation;

	/** The versionLong of the source for this image at the time it was created. */
	@GuardedBy("getRoot()")
	private long m_sourceVersionLong;

	/**
	 * The LRU pointers for the cache's LRU list. These are locked and maintained by the ImageCache itself; access to these is "verboten" from self.
	 */
	@GuardedBy("cache()")
	ImageInstance m_lruPrev, m_lruNext;

	@GuardedBy("cache()")
	InstanceCacheState m_cacheState;

	/** The current actual memory size taken by this entry. */
	@GuardedBy("getRoot()")
	private long m_memoryCacheSize;

	CachedImageFragment(final ImageRoot root, final String perm, long sourceVersionLong) {
		m_imageRoot = root;
		m_permutation = perm;
		m_sourceVersionLong = sourceVersionLong;
	}

	/**
	 * Return the image root cache entry.
	 * @return
	 */
	final ImageRoot getRoot() {
		return m_imageRoot;
	}

	final public String getPermutation() {
		return m_permutation;
	}

	final public boolean isOriginal() {
		return m_permutation.length() == 0;
	}

	final public ImageCache cache() {
		return getRoot().getCache();
	}

	public long getMemoryCacheSize() {
		return m_memoryCacheSize;
	}

	public void setMemoryCacheSize(long memoryCacheSize) {
		m_memoryCacheSize = memoryCacheSize;
	}
}
