package to.etc.domui.util.images.cache;

import java.io.*;
import java.util.*;

import javax.annotation.*;
import javax.annotation.concurrent.*;

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
	ImageInstance loadOriginal() throws Exception {
		long cts = getCurrentVersionLong();
		ImageInstance ii = new ImageInstance(getRoot(), "", cts);
		InputStream is = null;
		try {
			//-- Copy the data into the files AND a buffer set,
			long memload = 0l;
			long fileload = 0l;
			is = getImageSource().getInputStream();
			copyAndCache(ii, is);
			is.close();
			is = null;
			memload += ii.getSize();

			//-- Data read. Now expose as a file, then load and identify
			File f = makeFileBased(ii);
			fileload += ii.getSize();
			OriginalImageData odata = ImageManipulator.identify(f);
			ii.initImageData(odata);
			memload += 32 + 32 * odata.getPageCount();

			//-- Housekeeping: sizes for the new ImageInstance.
			ii.setFileCacheSize(fileload);
			ii.setMemoryCacheSize(memload);
			addUsedImage(ii);
			addFileLoad(fileload);
			addMemoryLoad(memload);

			//-- At this point the root instance is fully initialized. Add it to the image set.
			getRoot().registerInstance(ii);
			return ii;

			//-- FIXME Save the loaded identify to the database as a dbcached value. There is no need to store it in a file: it is never reused.
			//			FileTool.saveSerialized(dets, odata); // Cache file details NO: THIS IS USELESS- it will never be reused. We need to get the data from the database instead.

		} finally {
			FileTool.closeAll(is);
		}
	}

	/**
	 * Return the original version of an image. It's usecount remains 0; the cache will increment it after return.
	 * you're done!
	 * @return
	 */
	public ImageInstance getOriginal() throws Exception {
		removeOutdatedVersions();




	}


	/**
	 * Force the instance to a file. If the thing is already file based use
	 * that file. If a file is created for it this will be added to the "new file"
	 * list, to allow it to be accounted for in the cache when the task ends.
	 * @param ii
	 * @return
	 */
	private File makeFileBased(ImageInstance ii) throws IOException {
		File f = ii.getCacheFile();
		if(f == null) {
			f = cache().createTemp();
			FileTool.save(f, ii.getBuffers());
			ii.setCachedFile(f);
		}
		return f;
	}

	private void copyAndCache(ImageInstance ii, @WillNotClose InputStream is) throws Exception {
		List<byte[]> buflist = new ArrayList<byte[]>();
		int fence = cache().getMemoryFence();
		int total = 0;
		byte[] buf = null;
		OutputStream os = null;
		File dataf = null;
		try {
			for(;;) {
				//-- Are we past the max amount to cache in memory?
				if(total >= fence && os == null) {
					dataf = cache().createTemp();
					os	= new FileOutputStream(dataf);
					for(byte[] b: buflist)
						os.write(b);
					buflist = null;
				} else {
					buf = new byte[32768];
					int szrd = readFully(is, buf);
					if(szrd <= 0)
						break;
					total += szrd;

					//-- If this was the last block resize buffer and be done;
					if(szrd < buf.length) {
						if(os != null) {
							os.write(buf, 0, szrd);		// Just flush last part,
						} else {
							byte[] w = new byte[szrd];	// Resize byte[] buffer
							System.arraycopy(buf, 0, w, 0, szrd);
							buflist.add(buf);
						}
						break;
					}
					if(os == null)
						buflist.add(buf);
				}
			}
			if(os != null) {
				os.close();
				os	= null;
				ii.initFile(dataf, total);
			} else {
				ii.initBuffers(buflist.toArray(new byte[buflist.size()][]), total);
			}
		} finally {
			try {
				if(os != null)
					os.close();
			} catch(Exception x) { // Ignore double fault
			}
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:		*/
	/*--------------------------------------------------------------*/
	/**
	 * This fully reads a buffer, issuing another read when an incomplete read
	 * returns. It only exits on buffer full or eof.
	 * @param is
	 * @param buf
	 * @return
	 * @throws IOException
	 */
	static private int readFully(InputStream is, byte[] buf) throws IOException {
		int off = 0;
		for(;;) {
			int left = buf.length - off;
			if(left <= 0)
				return off;
			int rd = is.read(buf, off, left);
			if(rd <= 0)
				return off;
			off += rd;
		}
	}

	/**
	 * Indicate that a new file has been created in this task. This file gets added to the
	 * cache's load.
	 * @param f
	 */
	private void addCachedFile(File f) {
		if(m_addedFiles == null)
			m_addedFiles = new ArrayList<File>();
		m_addedFiles.add(f);
	}

	/**
	 *
	 * @param ii
	 * @throws Exception
	 */
	private void refreshInstance(ImageInstance ii) throws Exception {


		long dts = getKey().getRetriever().getCheckInterval();


	}


}
