package to.etc.domui.util.images.cache;

import java.io.*;
import java.util.*;

import to.etc.domui.util.images.*;
import to.etc.domui.util.images.converters.*;
import to.etc.util.*;

/**
 * A cached instance of an image or permutated image. Parts of this act as a REF to an image.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 2, 2008
 */
public class ImageInstance {
	static private final int	FRAGSZ = 32768;

	/** The root descriptor of the image's base */
	private final ImageRoot			m_imageRoot;

	/**
	 * The LRU pointers for the cache's LRU list. These are locked and maintained by the ImageCache itself; access to these is "verboten" from self.
	 */
	ImageInstance				m_lruPrev, m_lruNext;

	InstanceCacheState			m_cacheState;

	boolean						m_initialized;

//	boolean						m_discard;

	/** An unique string describing the permutation of the original that this contains. When "" this is the ORIGINAL image. */
	private final String				m_permutation;

	/** The cached data of this image as a byte stream */
	private byte[][]			m_buffers;

	/** The number of bytes of data in the above buffers. Also the cache load. */
	private int					m_size;

	/** The mime type of the data stored in this data block (original mime or mime of permutation) */
	private String				m_mimeType;

	private ImageData			m_imageData;

	ImageInstance(final ImageRoot root, final String perm) {
		m_imageRoot = root;
		m_permutation = perm;
	}
	ImageRoot getRoot() {
		return m_imageRoot;
	}

	public String getPermutation() {
		return m_permutation;
	}

	/**
	 * Loads all data in the inputstream and puts it in the buffers of this thingy.
	 * @Locker ImageRoot.
	 * @param is
	 * @return
	 * @throws Exception
	 */
	void	loadAsBuffers(final InputStream is) throws Exception {
		int		szread;
		byte[]	curbuf = new byte[FRAGSZ];

		List<byte[]>	res = new ArrayList<byte[]>();
		for(;;) {
			szread = is.read(curbuf);							// Read as much as fits.
			if(szread <= 0)
				break;
			m_size += szread;

			//-- One buffer done. If not read fully we're at the end, so truncate and be done;
			if(szread < curbuf.length) {
				byte[] nw = new byte[szread];
				System.arraycopy(curbuf, 0, nw, 0, szread);		// Copy to new work buffert
				res.add(nw);
				break;
			} else {
				res.add(curbuf);
				curbuf = new byte[FRAGSZ];
			}
		}

		//-- Data read fully.
		m_buffers	= res.toArray(new byte[res.size()][]);
	}

	/**
	 * Part of the double-lock mechanism, this checks if the
	 * @param irt
	 * @param ii
	 * @param cacheKey
	 * @throws Exception
	 */
	synchronized void	initializeInstance(final IImageRetriever irt, final Object cacheKey) throws Exception {
		if(m_initialized) {
			ImageCache.d("Re-using CACHED original instance with key="+cacheKey);
			return;
		}

		//-- No original: try to retrieve && store it;
		ImageCache.d("Initializing ORIGINAL instance with key="+cacheKey);
		IStreamingImageInfo	sii	= irt.loadImage(cacheKey);			// Try to load the original
		if(sii == null)
			throw new IllegalStateException("The factory "+irt+" did not return an ImageInfo for key="+cacheKey);

		//-- Get the original's data as a byte[][] and prepare to create the original ImageInstance
		InputStream	is	= null;
		try {
			m_mimeType = sii.getMimeType();							// Initialize the mime type.
			if(m_mimeType == null)
				throw new IllegalStateException("Unknown MIME type returned from factory "+irt+" using key "+cacheKey);
			is	= sii.getInputStream();								// Get stream instance,
			loadAsBuffers(is);										// And load the original into the cache
			m_imageData	= sii.getImageData();						// Try to get image data,
			m_initialized = true;
		} finally {
			try { if(is != null) is.close(); } catch(Exception x) {}
		}
	}

	/**
	 * Return the size in bytes of the cached data.
	 * @return
	 */
	public int getSize() {
		return m_size;
	}

	/**
	 * This retrieves the ImageData for this image. If the data is not yet known this instance gets locked and
	 * a new Identify action is done.
	 * @return
	 */
	public synchronized ImageData getImageData() throws Exception {
		if(m_imageData == null) {
			//-- Create a file containing the thingy.
			File	tmp = File.createTempFile("imgi", ".tmp");
			try {
				FileTool.save(tmp, m_buffers);
				m_imageData = ImageConverterRegistry.identify(m_mimeType, tmp);
			} finally {
				try { tmp.delete(); } catch(Exception x) {}
			}
		}
		return m_imageData;
	}

	boolean	remove() {
		m_cacheState = InstanceCacheState.DISCARD;
		return getRoot().unregisterInstance(this);
	}

	/**
	 * Main workhorse for converting images.
	 *
	 * @param irt
	 * @param cacheKey
	 * @param conversions
	 * @throws Exception
	 */
	synchronized void	initializeConvertedInstance(final IImageRetriever irt, final Object cacheKey, final List<IImageConversionSpecifier> conversions) throws Exception {
		if(m_initialized)
			return;

		//-- Create a cache key & try to load off secondary cache quickly.
		StringBuilder	sb = new StringBuilder(128);
		sb.append(m_imageRoot.getFilenameBase());					// Base of the image as cached file
		sb.append("-");
		sb.append(m_permutation);
		sb.append(".cf");
		try {
			if(loadCachedFile(sb.toString())) {						// Try to load it using the file.
				m_initialized = true;								// Mark as completed
				return;
			}
		} catch(Exception x) {
			//-- Load from file cache failed-> yecc. We must recreate the object.
		}

		//-- 2. We need to (re)create the object from it's source. So retrieve the original. DOUBLE LOCK ON different ImageInstance's, and RECURSIVE LOCK [ImageInstance->ImageCache]!!!
		ImageInstance	original = getRoot().getCache().getOriginal(irt, cacheKey);

		//-- 2. Create the object using the permutator factories.
		File	tmp = null;
		ImageConverterHelper	ich = new ImageConverterHelper();
		try {
			//-- FIXME Write the original as a file (should be a cached file later on)
			tmp	 = File.createTempFile("imgorg", ".tmp");
			FileTool.save(tmp, original.getBuffers());					// Save original as a tempfile,
			ImageSpec sis = new ImageSpec(tmp, original.getImageData());
			ich.executeConversionChain(sis, conversions);				// Execute the conversion chain

			sis	= ich.getTarget();										// Result after completion.
			m_buffers	= FileTool.loadByteBuffers(sis.getSource());	// Read result into buffer chain,
			m_size		= (int)sis.getSource().length();
			m_imageData	= sis.getData();
			m_initialized = true;
		} finally {
			try { if(tmp != null) tmp.delete(); } catch(Exception x) {}
			ich.destroy();
		}
	}

	/**
	 * Tries to load the whole shebang from the file system.
	 * @param f
	 * @throws Exception
	 */
	private boolean loadCachedFile(final String key) throws Exception {
		File	cacheFile	= new File(m_imageRoot.getCache().getCacheDir(), key+".cf");
		if(! cacheFile.exists())
			return false;

		//-- bla bla bla

		throw new IllegalStateException("Not implemented yet");		// FIXME Implement.
//
//		cacheFile.setLastModified(System.currentTimeMillis());	// Touch the file to indicate it's been used
//		return true;
	}

	public byte[][] getBuffers() {
		return m_buffers;
	}


}
