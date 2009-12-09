package to.etc.domui.caches.images;

import java.io.*;
import java.util.*;

import javax.annotation.*;
import javax.annotation.concurrent.*;

import to.etc.domui.caches.filecache.*;
import to.etc.domui.util.images.*;
import to.etc.domui.util.images.machines.*;
import to.etc.util.*;

/**
 * Represents some action being handled within the image cache: the retrieval of some
 * image. This separates the cache-based actions from the image-based actions.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 1, 2009
 */
public class ImageTask extends CacheChange {
	private ImageRoot m_root;

	/** The unique key for the <i>source</i> of the image before any transformations and such */
	private ImageKey m_key;

	private IImageReference m_imageSource;

	/** When T the call to determine the <i>current</i> source image version has been done. */
	@GuardedBy("getRoot()")
	private boolean m_currentVersionGotten;

	/** The current versionLong value for the image. */
	@GuardedBy("getRoot()")
	private long m_currentVersionLong;

	/** The outdated-versions check has been done once for this imagetask and doesn't have to happen again. */
	@GuardedBy("getRoot()")
	private boolean m_outdatedChecked;

	private List<File> m_addedFiles;

	ImageTask(ImageKey key, ImageRoot root) {
		m_key = key;
		m_root = root;
	}

	public ImageKey getKey() {
		if(m_key == null)
			throw new IllegalStateException("The image task has already been closed");
		return m_key;
	}

	public ImageRoot getRoot() {
		return m_root;
	}

	ImageCache cache() {
		return getRoot().getCache();
	}



	/*--------------------------------------------------------------*/
	/*	CODING:	Working with the raw image source.				 	*/
	/*--------------------------------------------------------------*/
	/**
	 * Release all resources.
	 */
	void close() {
		if(m_key == null)
			return;

		try {
			if(m_imageSource != null)
				m_imageSource.close();
		} catch(Exception x) {}
		m_imageSource = null;
	}

	/**
	 * Get a reference to the original image as gotten from wherever it comes from.
	 * @return
	 * @throws Exception
	 */
	IImageReference getImageSource() throws Exception {
		if(m_imageSource == null)
			m_imageSource = getKey().getRetriever().loadImage(getKey().getInstanceKey());
		return m_imageSource;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Outdated version handling.							*/
	/*--------------------------------------------------------------*/
	/**
	 * This gets called from every <i>master</i> call. The first time it gets called
	 * this checks if versions have expired from this set. It does this only once; and
	 * obeys the checkinterval.
	 *
	 * @throws Exception
	 */
	private void removeOutdatedVersions() throws Exception {
		synchronized(getRoot()) {
			if(m_outdatedChecked)
				return;

			//-- Is it already time to check?
			long dts = getKey().getRetriever().getCheckInterval();
			if(dts > 0) {
				//-- Time to check again?
				long ct = System.currentTimeMillis();
				if(ct < getRoot().getTSLastCheck() + dts) { // Past expiry time?
					//-- No- do not check
					m_outdatedChecked = true; // Do not check again in this task.
					return;
				}
				getRoot().setTSLastCheck(ct); // We'll go and check now- save the current ts
			}

			//-- Check actual data timestamp
			long versionlong = getCurrentVersionLong();
			getRoot().checkVersionLong(this, versionlong); // Clear all ImageInstances if source has changed;
			m_outdatedChecked = true;
		}
	}

	/**
	 * If the version has not yet been retrieved for this task it gets retrieved; all
	 * calls following will use that version. The call must be done <i>before</i> any
	 * data gets read; this prevents the race condition where the record is changed
	 * between [read data] and [read version].
	 * @return
	 * @throws Exception
	 */
	protected long getCurrentVersionLong() throws Exception {
		if(! m_currentVersionGotten) {
			m_currentVersionLong = getImageSource().getVersionLong();
			m_currentVersionGotten = true;
		}
		return m_currentVersionLong;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Handling the Original.								*/
	/*--------------------------------------------------------------*/
	/**
	 * Create a <b>new</b> image instance from the <i>original</i> image by loading it
	 * from it's source. This issues an IDENTIFY if needed. The data is read and the used
	 * resources are accounted in this instance.
	 *
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	CachedImageData getOriginalData() throws Exception {
		removeOutdatedVersions(); // Remove all old thingies from the cache.

		//-- Is the required version cached? In that case it's current so reuse
		CachedImageData cid = getRoot().findOriginalData();
		if(cid != null) {
			addUsedImage(cid); // Mark recently used
			return cid;
		}

		//-- We need to create the thingy. Get a file ref to store it in,
		long cts = getCurrentVersionLong();
		String	cachename	= getKey().getRetriever().getRetrieverKey()+"/"+getKey().getInstanceKey()+"-"+Long.toHexString(cts)+".data";
		FileCacheRef ref = cache().getFileRef(cachename);
		InputStream is = null;
		OutputStream os = null;
		boolean ok = false;
		try {
			//-- 1. Does the file data exist on the file system still?
			int	len = 0;
			if(!ref.getFile().exists() || ref.getFile().length() == 0) {
				//-- The file does not exist or is 0 bytes long. Reload from it's source.
				is = getImageSource().getInputStream();
				os = new FileOutputStream(ref.getFile());
				FileTool.copyFile(os, is);
				os.close();
				os = null;
				is.close();
				is = null;
			}
			len = (int) ref.getFile().length();

			//-- 2. The file exists. Is it suitable for memory caching?
			int memload = 64;
			byte[][] bufs = null;
			if(len < cache().getMemoryFence()) {
				bufs = FileTool.loadByteBuffers(ref.getFile()); // Load as a bufferset
				memload += len;
			}

			//-- All of this worked!! Nothing normal can go wrong after this so link and register all data
			CachedImageData ii = new CachedImageData(getRoot(), "", cts, ref, len, bufs, memload);
			addUsedImage(ii); // Link/relink in LRU
			getRoot().registerInstance(ii);
			ok = true;
			return ii;
		} finally {
			if(!ok) {
				ref.getFile().delete();
				ref.close();
			}

			FileTool.closeAll(is, os);
		}
	}

	/**
	 *
	 * @return
	 * @throws Exception
	 */
	CachedImageInfo getOriginalInfo() throws Exception {
		removeOutdatedVersions(); // Remove all old thingies from the cache.

		//-- Is the required version cached? In that case it's current so reuse
		CachedImageInfo cii = getRoot().findOriginalInfo();
		if(cii != null) {
			addUsedImage(cii); // Mark recently used
			return cii;
		}

		//-- Not cached in memory.. Try to get the stuff from a cachefile..
		long cts = getCurrentVersionLong();
		String cachename = getKey().getRetriever().getRetrieverKey() + "/" + getKey().getInstanceKey() + "-" + Long.toHexString(cts) + ".meta";
		FileCacheRef ref = cache().getFileRef(cachename);
		InputStream is = null;
		OutputStream os = null;
		boolean ok = false;
		try {
			//-- 1. Does the file data exist on the file system still?
			OriginalImageData oid = null;
			try {
				oid = (OriginalImageData) FileTool.loadSerialized(ref.getFile());
			} catch(Exception x) {}

			//-- 2. If the data is null we need to (re)create it.
			if(oid == null) {
				//-- Obtain the ORIGINAL data (exception on whatever error)
				CachedImageData	cid = getOriginalData();
				oid = ImageManipulator.identify(cid.getFile());

				//-- IDENTIFY complete, now serialize to cachefile
				FileTool.saveSerialized(ref.getFile(), oid);
			}

			//-- 3. Done - nothing normal can go wrong after this
			int memload = 64 + oid.getPageCount() * 32;

			//-- All of this worked!! Nothing normal can go wrong after this so link and register all data
			CachedImageInfo ii = new CachedImageInfo(getRoot(), "", cts, ref, oid, memload);
			addUsedImage(ii); // Link/relink in LRU
			getRoot().registerInstance(ii);
			ok = true;
			return ii;
		} finally {
			if(!ok) {
				ref.getFile().delete();
				ref.close();
			}
			FileTool.closeAll(is, os);
		}
	}


}
