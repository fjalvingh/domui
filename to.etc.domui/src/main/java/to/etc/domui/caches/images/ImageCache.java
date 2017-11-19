/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.caches.images;

import java.io.*;
import java.util.*;

import javax.annotation.*;

import to.etc.domui.caches.*;
import to.etc.domui.caches.filecache.*;
import to.etc.domui.parts.*;
import to.etc.domui.util.images.*;
import to.etc.domui.util.images.converters.*;
import to.etc.domui.util.images.machines.*;

/**
 * This cache handles images and transformations of images.
 *
 * <h1>Image and image transformation cache and handler.</h1>
 * <h2>Original image sources</h2>
 * <p>Image originals can come from many sources: database tables, file system files etc. The source never needs to be
 * "duplicated" (in long-term cache like the database) but can be obtained in some way. This code generalizes the retrieval
 * process for an original image into the {@link IImageRetriever} interface using a factory pattern. The factory (IIMageRetriever) to
 * use is defined by the retriever's name which is a small string. The image retriever, when asked, must deliver some basic information
 * on the image and a stream source to accesss it's content.</p>
 * <p>The Image Retriever retrieves a <i>specific</i> image using a retriever-specific key string called the <i>retriever key</i>. This
 * key string's value is only meaningful to the specific retriever it is meant for. The retriever decodes the string value into
 * a primary key, filename or whatnot and uses that to access the actual image. The combination of an IImageRetriever <i>and</i> a
 * <i>retriever key</i> uniquely identifies an original image. Internally this identity is maintained in {@link ImageKey}. For external
 * purposes the retriever itself can be specified as a string too: the retriever's name. This combination of retriever name and retriever
 * key can be used in URL's to access a given original image. This is the task of the {@link CachedImagePart} part.</p>
 *
 * <h2>Image transformations</h2>
 * <p>Getting an original image is nice but it's often not really needed - we usually need to have some specific transformed version
 * of the original, like:
 * <ul>
 *	<li>Page 7 of the original image (tiffs and the like)</li>
 *	<li>A resized copy of the original which fits the screen</li>
 *	<li>A thumbnail of the original</li>
 *	<li>A thumbnail of page 12 of the original...</li>
 * </ul>
 * This code allows you to retrieve an original image and add <i>permutations</i> to that image: operations to the image
 * that somehow transform it in another version of it. Adding permutations must be done in order: for a paged set you
 * must first add the "page select" permutation before you can add a "thumbnail" permutation. Each permutation has a "permutation
 * key" which is a short string representation of the transformation done and it's arguments. By concatenating all these strings
 * we get an "unique key" for that specific set of transformations done on that image. Combined with the ImageKey this means we
 * can cache the result of a permutation of an image too.</p>
 *
 * <h2>Caching</h2>
 * <p>We cache images for better performance for often-used images like thumbnail of the current set of properties that are available
 * for rent. We also cache metadata on images (format, size) to allow code to properly generate whatever HTML is needed to access
 * the image.</p>
 * <p>Since many transformations on an image are very expensive we cache the result of those transformations too. These transformations
 * are cached in database tables if so defined. They are always cached on the file system.</p>
 *
 * <h2>Retrieving images</h2>
 * <p>We distinguish between retrieving <i>image specifications</i> and the <i>image data</i>. The image specifications are things like
 * it's mime type, the number of pages (if paged) and it's size. These are all the result of the identification phase of the original
 * image or of an actual transformation process.</p>
 *
 * <h2>Multithreaded locking strategy</h2>
 * <p>We use a multi-level lock using the standard mechanism. The fast lock is the ImageCache instance itself; it protects the
 * registered factory list and the map from ImageKey to ImageRoot. It also handles LRU processing for the individual image
 * instances.</p>
 * <p>The 2nd level lock is a lock on ImageRoot which is locked for any access to whatever permutation of that image. This
 * second-level lock is a slow lock meaning that any time-consuming operation on any of the derivatives of the image will
 * keep this lock closed.
 * </p>
 *
 * <h2>Accessing image data</h2>
 * <p>Access to metadata of the image can be done by just requesting it. The data is cached but will never change, and if
 * the cache entry disappears while you are using that metadata nothing special happens - the data will be garbage collected
 * as soon as you release your reference to it.</p>
 *
 * <p>This is not the case for image data. This data is cached in memory as a set of buffers, an optional extra file and data
 * related to all this. While someone is using this data it cannot be released (the file cannot be deleted). To prevent
 * this image data has a <i>use count</i>. This use count is at least 1 as long as the data is accessible in the cache and
 * gets incremented for every reference to the data that is returned to the user. The user <i>must</i> release this data
 * explicitly with a call to close which decreases the use count. If the use count becomes zero then the resources for
 * image data will be released.</p>
 *
 * <h2>Locking path analysis</h2>
 * <h3>Get a new original image</h3>
 *
 *
 *
 *
 * </p>
 * <h2>Image retrieval process</h2>
 * <ul>
 * 	<li>Using the cacheKey, try to locate the image in the hashmap by finding the ImageRoot, then walking the CachedImageData list
 * 		to find a matching image. If we find one we return it's REF which prevents it's data from being GC'd. We also update
 * 		it's LRU location to most-recently-used.</li>
 *	<li>If not found AND the requested thing is not the ORIGINAL image we try to load it from the file system's cache location. We
 *		determine the filename that the copy would have then load it. If this load succeeds we enter it in the cache and return it's
 *		REF.</li>
 *	<li>If still not found we at the very least need the ORIGINAL (source). IF we are requesting a permutated original (not the
 *		original itself) we try to get that by doing another cache lookup. If we are locating the original itself another lookup
 *		is useless because the first lookup would have located it.</li>
 *	<li>If the original is NOT FOUND we use the factory provided to retrieve the image and it's data. The image is then entered
 *		in the cache (as the ORIGINAL) and it's REF is kept. If the original lookup <i>was</i> for the original image this REF
 *		is returned and we're done.</li>
 *	<li>Now we are sure to HAVE the original's REF, and we're sure we have to obtain some permutation of that original. Create the
 *		permutation of the original (resize, page) and store it in the file system at the appropriate location. Then add the
 *		permutation to the cache and return it's REF.
 * </ul>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 2, 2008
 */
public class ImageCache {
	static private ImageCache m_instance;

	/** The images file cache. */
	private FileCache m_fileCache;

	//	private long m_maxFileSize = 10l * 1024l * 1024l * 1024l;
	//
	//	private long m_currentFileSize = 0;


	//	/** Max. #bytes we may allocate on the file system for cached data; defaults to 1GB */
	//	private long				m_maxFileCacheSize = 1024l * 1024l * 1024l;
	//
	//	private long				m_currentFileCacheSize;

	/** The max. #bytes that this cache may use in memory; defaults to 32M */
	private long m_maxMemorySize = 32 * 1024 * 1024;

	private long m_currentMemorySize;

	/** The max. size in bytes that a result may be to be cacheable in memory (5MB default). Any stream larger will be cached on the file system. */
	private int m_memoryFenceSize = 5 * 1024 * 1024;

	/** The set of registered original image factories */
	private Map<String, IImageRetriever> m_factoryMap = new HashMap<String, IImageRetriever>();

	/** The map of keys to their image root */
	private Map<ImageKey, ImageRoot> m_cacheMap = new HashMap<ImageKey, ImageRoot>();

	private CachedImageFragment m_lruFirst, m_lruLast;

	//	/** File ID counters */
	//	private int[] m_counters = new int[4];

	private ImageCache(long maxsize, long maxfilesize, File cachedir) {
		m_fileCache = new FileCache(cachedir, maxfilesize);
		m_maxMemorySize = maxsize;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Initialization related code.						*/
	/*--------------------------------------------------------------*/
	/**
	 * Get the singleton image cache.
	 * @return
	 */
	static synchronized public ImageCache getInstance() {
		if(m_instance == null)
			throw new IllegalStateException("The image cache has not been initialized. Call ImageCache.initialize() before using the thing.");
		return m_instance;
	}

	static public synchronized void initialize(long maxsize, long maxfilesize, File cacheDir) throws Exception {
		m_instance = new ImageCache(maxsize, maxfilesize, cacheDir);
		m_instance.init();
	}

	private void init() throws Exception {
		m_fileCache.initialize();
		addRetriever(new FileImageRetriever());
	}

	/**
	 * Add a new image factory.
	 * @param r
	 */
	public synchronized void addRetriever(IImageRetriever r) {
		IImageRetriever old;
		if(null != (old = m_factoryMap.put(r.getRetrieverKey(), r)))
			throw new IllegalStateException("Duplicate image factory key: " + r.getRetrieverKey() + " for " + r + " and " + old);
	}

	public synchronized IImageRetriever findRetriever(String key) {
		return m_factoryMap.get(key);
	}

	public ImageKey createImageKey(String retrieverkey, String instancekey) {
		IImageRetriever ir = findRetriever(retrieverkey);
		if(ir == null)
			return null;
		return new ImageKey(ir, instancekey);
	}


	//	synchronized File createTemp() {
	//		int fnr = m_counters[m_counters.length - 1]++;
	//		if(fnr >= 1000) {
	//			fnr = 0;
	//			m_counters[m_counters.length] = 0;
	//			for(int i = m_counters.length - 1; --i >= 0;) {
	//				int v = m_counters[i]++;
	//				if(v >= 1000) {
	//					m_counters[i] = 0;
	//				} else
	//					break;
	//			}
	//		}
	//		StringBuilder sb = new StringBuilder(32);
	//		for(int i = 0; i < m_counters.length; i++) {
	//			if(i != 0)
	//				sb.append('/');
	//			sb.append(m_counters[i]);
	//		}
	//		sb.append(".cached");
	//		File tmpf = new File(sb.toString());
	//		if(fnr == 0)
	//			tmpf.getParentFile().mkdirs();
	//		return tmpf;
	//	}

	int getMemoryFence() {
		return m_memoryFenceSize;
	}

	FileCache getFileCache() {
		return m_fileCache;
	}

	FileCacheRef getFileRef(String path) {
		return getFileCache().getFile(path);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Image task initialization.							*/
	/*--------------------------------------------------------------*/
	/**
	 * <p>Create a new ImageTask for some action to pass through the cache. The ImageTask
	 * structure is used to maintain state and objects while some image or transformation
	 * is being passed through the image cache. It maintains proper state and locks and
	 * collects resources that need to be released when the task is done.</p>
	 * <p>Asking for an ImageTask is the first step in <i>any</i> cache action. This step
	 * does basic is-modified cache cleaning: it allocates an {@link IImageReference} for
	 * the key and asks it's modification date; if that one has changed since last time
	 * all cached resources are freed and retrieved anew.
	 *
	 * @param key	The key of the image.
	 * @return	The root entry to use for that instance. This entry can be mostly empty!
	 */
	@Nonnull
	private ImageTask getImageTask(ImageKey key) throws Exception {
		synchronized(this) {
			ImageRoot r = m_cacheMap.get(key);
			if(r == null) {
				r = new ImageRoot(this, key);
				m_cacheMap.put(key, r);
				r.m_cacheUseCount = 1;
			}
			ImageTask task = new ImageTask(key, r);
			return task;
		}
	}

	static void d(String s) {
		System.out.println(s);
	}

	private interface ISpecTask {
		Object executeTask(ImageTask task, Object args) throws Exception;
	}

	/**
	 * Handle ImageTask related actions. This does all of the cache-locking related work
	 * around an ImageTask, and handles all administrative details /after/ the task
	 * completes.
	 *
	 * @param key
	 * @param t
	 * @throws Exception
	 */
	private Object executeTask(ImageKey key, ISpecTask t, Object args) throws Exception {
		ImageTask it = getImageTask(key);
		try {
			synchronized(it.getRoot()) { // All of the task is executed with a locked root
				return t.executeTask(it, args);
			}
		} finally {
			try {
				it.close();
			} catch(Exception x) {}
			updateCacheDetails(it);
		}
	}

	private void updateCacheDetails(ImageTask it) {
		synchronized(this) {
			//-- Remove all fragments that were removed by this task and reduce the cacheload caused by them
			for(CachedImageFragment cif : it.getDeletedFragmentList()) {
				unlink(cif);
				cif.getRoot().m_cacheUseCount--;
			}

			//-- (re)link all new thingies in the cache.
			for(CachedImageFragment cif : it.getUsedFragmentList()) {
				registerAndLink(cif);
			}

			//-- If at this point the cache root has zero uses- discard it.
			if(it.getRoot().m_cacheUseCount == 0) {
				m_cacheMap.remove(it.getRoot().getKey());
			}
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	User accessable calls.								*/
	/*--------------------------------------------------------------*/
	/**
	 * Task to call getOriginalData().
	 */
	static private final ISpecTask C_GETORIGINALDATA = new ISpecTask() {
		@Override
		public Object executeTask(ImageTask task, Object args) throws Exception {
			return task.getOriginalData();
		}
	};

	static IImageStreamSource convert(CachedImageData cid) {
		if(cid.getBuffers() != null)
			return new MemoryImageSource(cid);
		else
			return new FileImageSource(cid);
	}

	/**
	 * Return a data reference to the original image's data.
	 * @param k
	 * @return
	 */
	public IImageStreamSource getOriginalData(ImageKey k) throws Exception {
		CachedImageData cid = (CachedImageData) executeTask(k, C_GETORIGINALDATA, null);
		return convert(cid);
	}

	static private final ISpecTask C_GETORIGINALINFO = new ISpecTask() {
		@Override
		public Object executeTask(ImageTask task, Object args) throws Exception {
			return task.getOriginalInfo();
		}
	};

	/**
	 * Return a data reference to the original image's info.
	 * @param k
	 * @return
	 */
	public ImageInfo getOriginalInfo(ImageKey k) throws Exception {
		return ((CachedImageInfo) executeTask(k, C_GETORIGINALINFO, null)).getImageInfo();
	}

	static private final ISpecTask C_GETCONVERTEDDATA = new ISpecTask() {
		@Override
		public Object executeTask(ImageTask task, Object args) throws Exception {
			return task.getImageData((List<IImageConversionSpecifier>) args);
		}
	};

	static private final ISpecTask C_GETCONVERTEDINFO = new ISpecTask() {
		@Override
		public Object executeTask(ImageTask task, Object args) throws Exception {
			return task.getImageInfo((List<IImageConversionSpecifier>) args);
		}
	};

	static private final ISpecTask C_GETFULLINFO = new ISpecTask() {
		@Override
		public Object executeTask(ImageTask task, Object args) throws Exception {
			return task.getFullImage((List<IImageConversionSpecifier>) args);
		}
	};

	/**
	 *
	 * @param k
	 * @param convlist
	 * @return
	 * @throws Exception
	 */
	public IImageStreamSource getImageData(ImageKey k, List<IImageConversionSpecifier> convlist) throws Exception {
		CachedImageData cid = (CachedImageData) executeTask(k, C_GETCONVERTEDDATA, convlist);
		return convert(cid);
	}

	public ImageInfo getImageInfo(ImageKey k, List<IImageConversionSpecifier> convlist) throws Exception {
		CachedImageInfo cid = (CachedImageInfo) executeTask(k, C_GETCONVERTEDINFO, convlist);
		return cid.getImageInfo();
	}


	/**
	 * Get full image data: both the data source AND it's info.
	 * @param k
	 * @param convlist
	 * @return
	 * @throws Exception
	 */
	public FullImage getFullImage(ImageKey k, List<IImageConversionSpecifier> convlist) throws Exception {
		return (FullImage) executeTask(k, C_GETFULLINFO, convlist);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Administration.										*/
	/*--------------------------------------------------------------*/
	/**
	 * Registers an instance. When called it is implied that initialization WORKED and that the
	 * thingy contains a valid resource.
	 * This must take care of the race conditions caused by the double-lock initialization.
	 * @param ii
	 */
	private void registerAndLink(CachedImageFragment ii) {
		//-- Atomically add in new cache load, and if it exceeds the maximum reap the thingies to remove.
		List<CachedImageFragment> dellist = null;
		synchronized(this) {
			if(ii.m_cacheState != InstanceCacheState.NONE) // Already linked (cannot happen) or discarded (can happen if 2nd init works && race)
				return;
			link(ii); // Link as 1st
			m_currentMemorySize += ii.getMemoryCacheSize(); // Add cache load, in bytes.
			ii.m_cacheState = InstanceCacheState.LINKED; // Properly linked and accounted.

			if(m_currentMemorySize <= m_maxMemorySize) // If we're not overdrawn...
				return;

			//-- Determine the oldest pages && discard 'm
			dellist = new ArrayList<CachedImageFragment>();
			int count = 0;
			long size = 0;
			while(m_lruLast != m_lruFirst && m_currentMemorySize > m_maxMemorySize) {
				//-- Discard from all metadata
				CachedImageFragment itd = m_lruLast;
				unlink(itd); // Discard thingy from LRU chain;
				dellist.add(itd);

				m_currentMemorySize -= itd.getMemoryCacheSize(); // Reduce cache load with this-item's size;
				size += itd.getMemoryCacheSize();
				count++;

				//-- Decrement the root's use count; if it becomes zero we'll discard this root.
				ImageRoot ir = itd.getRoot();
				if(ir.m_cacheUseCount > 0) { // Not already removed sometimes before?
					if(--ir.m_cacheUseCount == 0) {
						m_cacheMap.remove(ir.getKey()); // Drop from cache @ this point.
					}
				}
			}
			System.out.println("ImageCache: reaped " + count + " image instances totalling " + size + " bytes");
		}

		//-- Now, while we're out of the lock make the ImageRoot's discard their data.
		for(CachedImageFragment cif : dellist) {
			cif.getRoot().lruInstanceDeleted(cif);
		}
	}

	/**
	 * Return the amount of memory currently used in the cache system.
	 * @return
	 */
	public synchronized long getUsedMemory() {
		return m_currentMemorySize;
	}

	public long getUsedFilespace() {
		return m_fileCache.getCurrentFileSize();
	}

	/**
	 * Links the entry at the most recently used position of the LRU chain.
	 * @param e
	 */
	private void link(CachedImageFragment e) {
		unlink(e); // Make sure we're unlinked
		if(m_lruFirst == null) { // Empty initial list?
			m_lruFirst = e;
			m_lruLast = e;
			e.m_lruNext = e;
			e.m_lruPrev = e;
			return;
		}
		e.m_lruPrev = m_lruFirst; // Previous first is my previous
		e.m_lruNext = m_lruLast; // After me I wrap back to the end
		m_lruLast.m_lruPrev = e;
		m_lruFirst.m_lruNext = e; // I'm his next
		m_lruFirst = e; // I'm the 1st one now;
	}

	private void unlink(CachedImageFragment e) {
		if(e.m_lruNext == null) // Already unlinked?
			return;
		if(e.m_lruNext == e.m_lruPrev) { // I'm the only one?
			m_lruFirst = null;
			m_lruLast = null;
			e.m_lruNext = null;
			e.m_lruPrev = null;
			return;
		}

		if(m_lruFirst == e)
			m_lruFirst = e.m_lruPrev;
		if(m_lruLast == e)
			m_lruLast = e.m_lruNext;

		e.m_lruPrev.m_lruNext = e.m_lruNext;
		e.m_lruNext.m_lruPrev = e.m_lruPrev;
		e.m_lruNext = e.m_lruPrev = null;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Test code.											*/
	/*--------------------------------------------------------------*/

	//	public static void main(String[] args) {
	//		try {
	//			IImageRetriever golden_retriever = new IImageRetriever() {
	//				public IStreamingImageInfo loadImage(Object key) throws Exception {
	//					final File src = new File(new File("/home/jal"), (String) key);
	//					final String mime = ServerTools.getExtMimeType(FileTool.getFileExtension((String) key));
	//
	//					return new IStreamingImageInfo() {
	//						public String getMimeType() throws Exception {
	//							return mime;
	//						}
	//
	//						public InputStream getInputStream() throws Exception {
	//							return new FileInputStream(src);
	//						}
	//
	//						public ImageData getImageData() throws Exception {
	//							return null;
	//						}
	//					};
	//				}
	//
	//				public String keyAsFilenameString(Object s) {
	//					return s.toString().replace('/', '$').replace('\\', '$');
	//				}
	//			};
	//
	//			ImageCache ic = new ImageCache();
	//
	//			String key = "img_5589.jpg";
	//			for(int i = 0; i < 5; i++) {
	//				CachedImageData ii = ic.getOriginal(golden_retriever, key);
	//				System.out.println("Instance: " + ii);
	//			}
	//
	//			//-- Get a cached, converted result.
	//			System.out.println("Getting a thumbnailed thingy 5x.");
	//			for(int i = 0; i < 5; i++) {
	//				CachedImageData ii = ic.getImage(golden_retriever, key, new IImageConversionSpecifier[]{new ImagePageSelect(0), new ImageThumbnail(400, 300, "image/png")});
	//				System.out.println("Instance: " + ii);
	//			}
	//		} catch(Exception x) {
	//			x.printStackTrace();
	//		}
	//	}

}
