package to.etc.domui.util.images.cache;

import java.io.*;

import javax.annotation.concurrent.*;

import to.etc.domui.util.images.converters.*;
import to.etc.domui.util.images.machines.*;
import to.etc.util.*;

/**
 * Some permutation of an image that was recently used. Once created, an ImageInstance is immutable
 * and will be discarded fully when changes to it are necessary, for instance because a newer
 * version of the source is available. The immutable character means that when ImageInstances have
 * to change existing "readers" of an instance may continue to use that instance; when they are
 * done using it the garbage collector will release it's resources automatically.
 *
 * <p>An ImageInstance always contains a cachefile containing the datastream for the resource, and
 * optionally a bufferset containing a memory-cached copy. The amount of memory used by an ImageEntry
 * determines it's <i>cache load</i>, and is used to add to the cache's actual size. This is the only
 * metric used to determine that the cache is full causing LRU removal of entries.</p>
 *
 * <p>The "immutableness" of an ImageSource also extends to it's backing file in the filecache. This
 * means that different "versions" of the ImageSource need different cachefiles, and in no way may
 * an existing cachefile be "overwritten" with a different version - because clients can be reading
 * it. This means that all cachefiles for a given ImageInstance use the source's version number in
 * their filename.</p>
 *
 * <p>Because filenames are versioned we can reuse existing files on the file system when encountered.
 * This can be after a server restart where the memory cache is empty; when a resource is accessed and
 * it's cachefiles are present and valid there is no need to get them again from the db; this means that
 * only a "version number ping" is done to the db. Another way is when a resource is evicted from the
 * memory cache because the cache was full and it was the least-recently-used (LRU) object: in that
 * case the memory copy is removed but the files remain. When the object is used again it will pick
 * up it's datafiles when possible, making for speedy recovery of a cache miss.</p>
 *
 * <h3>Exceptions to the immutableness</h3>
 * <p>Although all user-required fields are immutable, this object contains some fields like the LRU
 * pointers and the cache state which are changed and maintained by the ImageCache instance itself,
 * and that change during the object's lifecycle.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 2, 2008
 */
final public class ImageInstance {
	static private final int FRAGSZ = 32768;

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


	/** If this is cached on the file system this contains the cachefile for it. */
	private File m_cacheFile;

	/** If this is cached in memory the data of this image. */
	private byte[][] m_buffers;

	/** The number of bytes of data in the above buffers. Also the cache load. */
	private int m_size;

	private OriginalImageData m_imageData;

	/** The mime type of the data stored in this data block (original mime or mime of permutation) */
	private String m_mimeType;

	ImageInstance(final ImageRoot root, final String perm, long sourceVersionLong) {
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

	/**
	 * Set the value as a bufferset.
	 * @param data
	 * @param size
	 */
	void initBuffers(byte[][] data, int size) {
		m_buffers = data;
		m_size = size;
	}

	/**
	 * Set the value as a cachefile.
	 * @param data
	 * @param size
	 */
	void initFile(File data, int size) {
		m_cacheFile = data;
		m_size = size;
	}

	void initImageData(OriginalImageData oid) {
		m_imageData = oid;
	}

	/**
	 * Return the size in bytes of the cached data.
	 * @return
	 */
	public int getSize() {
		return m_size;
	}

	/**
	 * Returns the current backing file or null if not present.
	 * @return
	 */
	File getCacheFile() {
		return m_cacheFile;
	}

	void setCachedFile(File f) {
		m_cacheFile = f;
	}

	/**
	 * This retrieves the ImageData for this image. If the data is not yet known this instance gets locked and
	 * a new Identify action is done.
	 * @return
	 */
	public synchronized OriginalImageData getImageData() throws Exception {
		if(m_imageData == null) {
			//-- Create a file containing the thingy.
			File tmp = File.createTempFile("imgi", ".tmp");
			try {
				FileTool.save(tmp, m_buffers);
				m_imageData = ImageConverterRegistry.identify(m_mimeType, tmp);
			} finally {
				try {
					tmp.delete();
				} catch(Exception x) {}
			}
		}
		return m_imageData;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Reference counting code.							*/
	/*--------------------------------------------------------------*/
	/**
	 * LOCKS ROOT: Increment the use count by 1. If the use count is <= 0 we die.
	 */
	@GuardedBy("getRoot()")
	protected void use() {
		synchronized(getRoot()) {
			if(m_useCount <= 0)
				throw new IllegalStateException("Access to destroyed ImageInstance: usecount=" + m_useCount);
			m_useCount++;
		}
	}

	/**
	 * LOCKS ROOT: This decrements the use count by 1. If the use count becomes 0 it's resources are destroyed.
	 */
	@GuardedBy("getRoot()")
	protected void release() {
		synchronized(getRoot()) {
			if(m_useCount <= 0)
				throw new IllegalStateException("Access to destroyed ImageInstance: usecount=" + m_useCount);
			if(--m_useCount > 0)
				return;
			m_buffers = null;
			m_size = 0;
			m_mimeType = null;
			m_sourceVersionLong = -1;
		}

		if(m_cacheFile != null) {
			File d = m_cacheFile;
			m_cacheFile = null;
			d.delete();
		}
	}

	//	boolean remove() {
	//		m_cacheState = InstanceCacheState.DISCARD;
	//		return getRoot().unregisterInstance(this);
	//	}


	public byte[][] getBuffers() {
		return m_buffers;
	}

	public long getMemoryCacheSize() {
		return m_memoryCacheSize;
	}

	public void setMemoryCacheSize(long memoryCacheSize) {
		m_memoryCacheSize = memoryCacheSize;
	}

	public long getFileCacheSize() {
		return m_fileCacheSize;
	}

	public void setFileCacheSize(long fileCacheSize) {
		m_fileCacheSize = fileCacheSize;
	}
}
