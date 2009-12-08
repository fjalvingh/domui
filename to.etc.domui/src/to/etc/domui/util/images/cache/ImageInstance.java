package to.etc.domui.util.images.cache;

import java.io.*;

import javax.annotation.concurrent.*;

import to.etc.domui.util.images.converters.*;
import to.etc.domui.util.images.machines.*;
import to.etc.util.*;

/**
 * Some permutation of an image that was recently used. An ImageInstance always contains a data stream
 * and a details info stream. After initialization ImageInstances are mostly immutable: the data they
 * contain will not change, only the lru counters and use count information will change. When the cached
 * source object changes this will cause an existing ImageInstance to be removed and it's use count
 * decremented; when that use count reaches zero it's resources are released.
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

	/**
	 * The use count determines when an immutable ImageInstance can finally be destroyed. Every ImageInstance that
	 * is still reachable by the cache (i.e. present in the root's imagelist) <i>always</i> has a use count of at
	 * least 1 (meaning the ImageRoot "uses" it's instances).
	 * As soon as any kind of reference is created to an ImageInstance this will increase the use count by 1; it
	 * will be reduced by 1 as soon as the reference is closed().
	 * <p>When the use count reaches zero the object will be physically destroyed: meaning it's data buffers will
	 * be released and any caching file will be deleted. Accessing data after this will result in invalid data.</p>
	 * When an instance is removed from the cache, either because newer versions are available or because it's the
	 * one to go because the cache is full and it is LRU this is done by removing the object from the master cache,
	 * then it's count (which was 1 while it was in the cache) is reduced also. This reduction will cause it to be
	 * deleted as soon as the last client using it is done.
	 */
	@GuardedBy("getRoot()")
	private int m_useCount;

	/** The current actual memory size taken by this entry. */
	@GuardedBy("getRoot()")
	private long m_memoryCacheSize;

	/** The current actual size in file space taken by this entry. */
	@GuardedBy("getRoot()")
	private long m_fileCacheSize;

	//	boolean						m_discard;

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
		m_useCount = 1;
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
