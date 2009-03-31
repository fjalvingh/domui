package to.etc.domui.util.images.cache;

import java.io.*;
import java.util.*;

import to.etc.domui.util.*;
import to.etc.domui.util.images.*;
import to.etc.domui.util.images.converters.*;
import to.etc.util.*;

/**
 * UNSTABLE INTERFACE
 * Singleton cache for images. Used to cache often-accessed images and their resizes.
 * <h2>User interface</h2>
 * <p>Any base image is uniquely identified by it's cacheKey. Multiple (resized, paged) copies of a base image can exist in the
 * cache; all these instances share the <i>same</i> cache key but have different page,size combinations. All permutations of a
 * given image instance share the same cache slot.
 * </p>
 * <h2>Image retrieval process</h2>
 * <ul>
 * 	<li>Using the cacheKey, try to locate the image in the hashmap by finding the ImageRoot, then walking the ImageInstance list
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
	static private ImageCache	m_instance;

	private File				m_cacheDir;

//	/** Max. #bytes we may allocate on the file system for cached data; defaults to 1GB */
//	private long				m_maxFileCacheSize = 1024l * 1024l * 1024l;
//
//	private long				m_currentFileCacheSize;

	/** The max. #bytes that this cache may use in memory; defaults to 32M */
	private long				m_maxMemorySize = 32*1024*1024;

	private long				m_currentMemorySize;

	/** The map of keys to their image root */
	private Map<Object, ImageRoot>	m_cacheMap = new HashMap<Object, ImageRoot>();

	private ImageInstance		m_lruFirst, m_lruLast;

	public ImageCache(long maxsize, File cachedir) {
//		m_maxFileCacheSize = maxsize;
		m_cacheDir = cachedir;
	}
	public ImageCache() {
	}

	static synchronized public ImageCache		getInstance() {
		if(m_instance == null)
			throw new IllegalStateException("The image cache has not been initialized. Call ImageCache.initialize() before using the thing.");
		return m_instance;
	}

	static public synchronized void	initialize(long maxsize, File cacheDir) {
		if(! cacheDir.mkdirs() && ! cacheDir.exists() && ! cacheDir.isDirectory())
			throw new IllegalStateException("Cannot create "+cacheDir);
		m_instance = new ImageCache(maxsize, cacheDir);
	}

	public File getCacheDir() {
		return m_cacheDir;
	}
	
	static void	d(String s) {
		System.out.println(s);
	}

	/**
	 * Get (and if not present - create) the imageRoot for the specified key/factory. If the
	 * root is to be created it will not be initialized with the original *yet*.
	 *
	 * @param irt
	 * @param cacheKey
	 * @return
	 */
	private ImageRoot		getImageRoot(IImageRetriever irt, Object cacheKey) {
		synchronized(this) {
			ImageRoot	r = m_cacheMap.get(cacheKey);
			if(r == null) {
				String	file = irt.keyAsFilenameString(cacheKey);		// Obtain cache base name
				r	= new ImageRoot(this, cacheKey, file);
				m_cacheMap.put(cacheKey, r);
			}
			return r;
		}
	}

	/**
	 * Retrieves the ORIGINAL for a given image. It checks the cache first; if that does not contain the
	 * original then it will be retrieved. This uses a double locking mechanism: it locks the cache (global
	 * lock) to obtain an ImageInstance after which the cache (global) lock is released. This is a very 
	 * fast operation because only small structures are allocated, if needed. It then uses the second lock
	 * on a forced initialize on the data that should be contained in the instance. If the instance has 
	 * already initialized this returns immediately; if not the first accessor (which by definition has all
	 * of the required data needed to create the image) will perform the initialization. If initialization
	 * fails for whatever reason this code will retry the initialization the next time the object is 
	 * accessed.
	 *
	 * @param irt
	 * @param cacheKey
	 * @return
	 */
	public ImageInstance		getOriginal(IImageRetriever irt, Object cacheKey) throws Exception {
		ImageInstance	ii;
		ImageRoot		root;
		synchronized(this) {									// First lock: find/create the appropriate ImageInstance
			root	= getImageRoot(irt, cacheKey);				// Locate the image root,
			ii = root.findOriginal();							// Find unpermutated original
			if(ii == null) {
				//-- We'll get a new ImageInstance containing the original..
				ii = new ImageInstance(root, "");
				root.registerInstance(ii);						// Append the thingy to the root,
			}
		}

		//-- 2nd phase lock: lock the object, then initialize directly from the provider.
		boolean	okay = false;
		try {
			ii.initializeInstance(irt, cacheKey);		// Force initialization of the thingy.
			okay	= true;
//			root.setOriginalDimension(ii.getDimension());
			return ii;
		} finally {
			if(okay) {
				/*
				 * If image initialization is succesful we register the resulting image into the LRU chain and add it's load
				 * to the cache. After this the image is usable.
				 */
				registerAndLink(ii);					// Register as MRU and add size to cache load, in lock,
			} else {
				/*
				 * Image creation has failed. We must discard this instance. It is possible that this same
				 * instance is undergoing initialization /again/ (because the init lock has been released,
				 * and the thingy is accessible from cache). This is OK; we just try to set the "invalid"
				 * flag in the instance and do not link it. If, during this, we see that the instance *has*
				 * been linked (because the n-th init that ran after our init failed worked) we quit immediately
				 * and exit.
				 */
				discardFailed(ii);
			}
		}
	}

	/**
	 * Registers an instance. When called it is implied that initialization WORKED and that the
	 * thingy contains a valid resource.
	 * This must take care of the race conditions caused by the double-lock initialization.
	 * @param ii
	 */
	private void	registerAndLink(ImageInstance ii) {
		//-- Atomically add in new cache load, and if it exceeds the maximum reap the thingies to remove.
		synchronized(this) {
			if(ii.m_cacheState != InstanceCacheState.NONE)	// Already linked (cannot happen) or discarded (can happen if 2nd init works && race)
				return;
			link(ii);										// Link as 1st
			m_currentMemorySize	+= ii.getSize();			// Add cache load, in bytes.
			ii.m_cacheState = InstanceCacheState.LINKED;	// Properly linked and accounted.

			if(m_currentMemorySize <= m_maxMemorySize)		// If we're not overdrawn...
				return;

			//-- Determine the oldest pages && discard 'm
			int		count = 0;
			long size = 0;
			while(m_lruLast != m_lruFirst && m_currentMemorySize > m_maxMemorySize) {
				//-- Discard from all metadata
				ImageInstance	itd = m_lruLast;
				unlink(itd);								// Discard thingy from LRU chain;
				m_currentMemorySize -= itd.getSize();		// Reduce cache load with this-item's size;
				size += itd.getSize();
				count++;
				if(itd.remove()) {							// Ask it's root object to discard this,
					//-- Root has zero entries-> delete
					m_cacheMap.remove(itd.getRoot().getImageKey());	// Drop ImageRoot
				}
			}
			System.out.println("ImageCache: reaped "+count+" image instances totalling "+size+" bytes");
		}
	}

	/**
	 * Discards an instance that failed to initialize, if still possible. If this
	 * instance has a state LINKED this means that of several initializations at least
	 * one succeeded; in that case we'll leave the properly initialized instance intact.
	 * In all other cases we'll remove the instance from the root and cache.
	 *
	 * @param ii
	 */
	private void	discardFailed(ImageInstance ii) {
		synchronized(this) {
			if(ii.m_cacheState == InstanceCacheState.LINKED)// Linked, though- another init has worked & completed it's sequence
				return;

			//-- Discard all referrals to this instance. It is only linked thru the hash access path.
			ImageRoot	root = ii.getRoot();
			root.unregisterInstance(ii);					// Unlink from root -> no longer accessible from outside
			ii.m_cacheState = InstanceCacheState.DISCARD;	// Is DISCARDED now - prevents LRU linking while access path has been dropped.

			if(root.getInstanceCount() == 0) {				// No instances left in this root thingy?
				m_cacheMap.remove(root.getImageKey());		// Discard root referral
			}
		}
	}

	/**
	 * Links the entry at the most recently used position of the LRU chain.
	 * @param e
	 */
	private void	link(ImageInstance e) {
		unlink(e);							// Make sure we're unlinked
		if(m_lruFirst == null) {			// Empty initial list?
			m_lruFirst = e;
			m_lruLast = e;
			e.m_lruNext	= e;
			e.m_lruPrev	= e;
			return;
		}
		e.m_lruPrev = m_lruFirst;			// Previous first is my previous
		e.m_lruNext	= m_lruLast;			// After me I wrap back to the end
		m_lruLast.m_lruPrev	= e;
		m_lruFirst.m_lruNext = e;			// I'm his next
		m_lruFirst	= e;					// I'm the 1st one now;
	}

	private void	unlink(ImageInstance e) {
		if(e.m_lruNext == null)				// Already unlinked?
			return;
		if(e.m_lruNext == e.m_lruPrev) {	// I'm the only one?
			m_lruFirst = null;
			m_lruLast	= null;
			e.m_lruNext = null;
			e.m_lruPrev	= null;
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

	
	public ImageInstance	getImage(IImageRetriever irt, Object cacheKey, IImageConversionSpecifier[] conversions) throws Exception {
		List<IImageConversionSpecifier> l  = new ArrayList<IImageConversionSpecifier>();
		for(IImageConversionSpecifier s: conversions)
			l.add(s);
		return getImage(irt, cacheKey, l);
	}	

	/**
	 * This is the main workhorse for the image cache. This retrieves a possible converted image off some source,
	 * using all kinds of caching to make it fast.
	 * 
	 * @param cacheKey
	 * @param sir
	 * @param conversions
	 * @return
	 * @throws Exception
	 */
	public ImageInstance	getImage(IImageRetriever irt, Object cacheKey, List<IImageConversionSpecifier> conversions) throws Exception {
		//-- Shortcut: if referring to the ORIGINAL...
		if(conversions == null || conversions.size() == 0)
			return getOriginal(irt, cacheKey);

		//-- Create the cache strings,
		StringBuilder	sb	= new StringBuilder(128);
		for(IImageConversionSpecifier ic: conversions)
			sb.append(ic.getConversionKey());					// Get a string rep of the conversion applied
		String	perm	= sb.toString();						// Permutation string;

		//-- 2. Is this permutation in the memory cache?
		ImageInstance	ii;
		synchronized(this) {									// First lock: find/create the appropriate ImageInstance
			ImageRoot	root	= getImageRoot(irt, cacheKey);	// Locate the image root,
			ii = root.findPermutation(perm);					// Find mutation
			if(ii == null) {
				//-- We'll get a new ImageInstance containing the original..
				ii = new ImageInstance(root, perm);
				root.registerInstance(ii);						// Append the thingy to the root,
			}
		}
		//-- 2nd phase lock: lock the object, then initialize in a few steps
		boolean	okay = false;
		try {
			ii.initializeConvertedInstance(irt, cacheKey, conversions);		// Force initialization of the thingy, using conversions.
			okay	= true;
			return ii;
		} finally {
			if(okay) {
				/*
				 * If image initialization is succesful we register the resulting image into the LRU chain and add it's load
				 * to the cache. After this the image is usable.
				 */
				registerAndLink(ii);					// Register as MRU and add size to cache load, in lock,
			} else {
				/*
				 * Image creation has failed. We must discard this instance. It is possible that this same
				 * instance is undergoing initialization /again/ (because the init lock has been released,
				 * and the thingy is accessible from cache). This is OK; we just try to set the "invalid"
				 * flag in the instance and do not link it. If, during this, we see that the instance *has*
				 * been linked (because the n-th init that ran after our init failed worked) we quit immediately
				 * and exit.
				 */
				discardFailed(ii);
			}
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Test code.											*/
	/*--------------------------------------------------------------*/

	public static void main(String[] args) {
		try {
			IImageRetriever	golden_retriever = new IImageRetriever() {
				public IStreamingImageInfo loadImage(Object key) throws Exception {
					final File src = new File(new File("/home/jal"), (String)key);
					final String mime = ServerTools.getExtMimeType(FileTool.getFileExtension((String)key));

					return new IStreamingImageInfo() {
						public String getMimeType() throws Exception {
							return mime;
						}
					
						public InputStream getInputStream() throws Exception {
							return new FileInputStream(src);
						}
					
						public ImageData getImageData() throws Exception {
							return null;
						}
					};
				}

				public String keyAsFilenameString(Object s) {
					return s.toString().replace('/', '$').replace('\\', '$');
				}
			};

			ImageCache		ic = new ImageCache();

			String	key = "img_5589.jpg";
			for(int i = 0; i < 5; i++) {
				ImageInstance	ii	= ic.getOriginal(golden_retriever, key);
				System.out.println("Instance: "+ii);
			}

			//-- Get a cached, converted result.
			System.out.println("Getting a thumbnailed thingy 5x.");
			for(int i = 0; i < 5; i++) {
				ImageInstance	ii	= ic.getImage(golden_retriever, key, new IImageConversionSpecifier[] {
					new ImagePageSelect(0),
					new ImageThumbnail(400, 300, "image/png")
				});
				System.out.println("Instance: "+ii);
			}			
		} catch(Exception x)  {
			x.printStackTrace();
		}
	}

}
