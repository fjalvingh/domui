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
import javax.annotation.concurrent.*;

import to.etc.domui.caches.filecache.*;
import to.etc.domui.util.images.*;
import to.etc.domui.util.images.converters.*;
import to.etc.domui.util.images.machines.*;
import to.etc.util.*;

/**
 * Represents some action being handled within the image cache: the retrieval of some
 * image. This separates the cache-based actions from the image-based actions.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 1, 2009
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE", justification = "FindBugs definition is wrong for mkdirs, and delete() may fail in code here")
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

	//	private List<File> m_addedFiles;

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
			addUsedFragment(cid); // Mark recently used
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
			addUsedFragment(ii); // Link/relink in LRU
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
	 * Get the info for an original image.
	 * @return
	 * @throws Exception
	 */
	CachedImageInfo getOriginalInfo() throws Exception {
		removeOutdatedVersions(); // Remove all old thingies from the cache.

		//-- Is the required version cached? In that case it's current so reuse
		CachedImageInfo cii = getRoot().findOriginalInfo();
		if(cii != null) {
			addUsedFragment(cii); // Mark recently used
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
			ImageInfo oid = null;
			try {
				oid = (ImageInfo) FileTool.loadSerialized(ref.getFile());
			} catch(Exception x) {}

			//-- 2. If the data is null we need to (re)create it.
			if(oid == null) {
				//-- Obtain the ORIGINAL data (exception on whatever error)
				CachedImageData	cid = getOriginalData();
				oid = ImageManipulator.identify(cid.getFile());

				//-- If the mime type is still unknown ask the source for a mime type.
				if(oid.getMime() == null) {
					oid.setMime(getImageSource().getMimeType());
				}


				//-- IDENTIFY complete, now serialize to cachefile
				FileTool.saveSerialized(ref.getFile(), oid);
			}

			//-- 3. Done - nothing normal can go wrong after this
			CachedImageInfo ii = addNewInfo("", cts, ref, oid);
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

	private CachedImageInfo addNewInfo(String perm, long cts, FileCacheRef ref, ImageInfo info) {
		int memload = 64 + info.getPageCount() * 32;
		CachedImageInfo ii = new CachedImageInfo(getRoot(), perm, cts, ref, info, memload);
		addUsedFragment(ii); // Link/relink in LRU
		getRoot().registerInstance(ii);
		return ii;
	}

	static private List<IImageConversionSpecifier>	convertArray(IImageConversionSpecifier[] conversions) {
		List<IImageConversionSpecifier> l = new ArrayList<IImageConversionSpecifier>();
		for(IImageConversionSpecifier s : conversions)
			l.add(s);
		return l;
	}

	static private String getPermutationKey(List<IImageConversionSpecifier> conversions) {
		StringBuilder sb = new StringBuilder(128);
		for(IImageConversionSpecifier ic : conversions)
			sb.append(ic.getConversionKey()); // Get a string rep of the conversion applied
		return sb.toString();
	}


	public CachedImageData getImageData(IImageConversionSpecifier[] conversions) throws Exception {
		return getImageData(convertArray(conversions));
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
	public CachedImageData getImageData(List<IImageConversionSpecifier> conversions) throws Exception {
		//-- Shortcut: if referring to the ORIGINAL...
		if(conversions == null || conversions.size() == 0)
			return getOriginalData();

		removeOutdatedVersions(); // Remove all old thingies from the cache.
		String perm = getPermutationKey(conversions); // Create the key for the specified conversions

		//-- Is the required version cached? In that case it's current so reuse
		CachedImageData cid = getRoot().findPermutationData(perm);
		if(cid != null) {
			addUsedFragment(cid); // Mark recently used
			return cid;
		}

		//-- Not cached in memory.. Try to get the stuff from a cachefile..
		long cts = getCurrentVersionLong();
		String datacachename = getKey().getRetriever().getRetrieverKey() + "/" + getKey().getInstanceKey() + "-" + perm + "-" + Long.toHexString(cts) + ".data";
		String infocachename = getKey().getRetriever().getRetrieverKey() + "/" + getKey().getInstanceKey() + "-" + perm + "-" + Long.toHexString(cts) + ".info";
		FileCacheRef dataref = cache().getFileRef(datacachename);
		FileCacheRef inforef = cache().getFileRef(infocachename);
		InputStream is = null;
		OutputStream os = null;
		boolean ok = false;
		try {
			/*
			 * We need both data and info file present AND readable or we need to regenerate.
			 */
			ImageInfo info = null;
			if(inforef.getFile().exists() && inforef.getFile().length() > 0) {
				//-- Try to read the info thingy.
				try {
					info = (ImageInfo) FileTool.loadSerialized(inforef.getFile());
				} catch(Exception x) {}
			}

			int len = 0;
			if(!dataref.getFile().exists() || dataref.getFile().length() == 0 || info == null) {
				/*
				 * We need to generate the mutation on the image. This does not only create a new
				 * image data file but it also (re) calculates the image's info. Because there is
				 * a possibility that the new image has a different info set than a previously loaded
				 * version we have to replace any existing version.
				 */
				info = generatePermutation(dataref, conversions); // Generate the derived.

				//-- Store the new image's serialized info, then register the new image info
				FileTool.saveSerialized(inforef.getFile(), info);
			}

			//-- Store the info on this image.
			CachedImageInfo oldci = getRoot().findPermutationInfo(perm);// Is an older copy available?
			if(oldci != null) {
				addDeletedFragment(oldci); // Tell the cache to update it's admin
				getRoot().unregisterInstance(oldci);
			}
			addNewInfo(perm, cts, inforef, info);

			//-- Now load the data block.
			len = (int) dataref.getFile().length();

			//-- 2. The file exists. Is it suitable for memory caching?
			int memload = 64;
			byte[][] bufs = null;
			if(len < cache().getMemoryFence()) {
				bufs = FileTool.loadByteBuffers(dataref.getFile()); // Load as a bufferset
				memload += len;
			}

			//-- All of this worked!! Nothing normal can go wrong after this so link and register all data
			CachedImageData ii = new CachedImageData(getRoot(), perm, cts, dataref, len, bufs, memload);
			addUsedFragment(ii); // Link/relink in LRU
			getRoot().registerInstance(ii);
			ok = true;
			return ii;
		} finally {
			if(!ok) {
				dataref.getFile().delete();
				dataref.close();
			}
			FileTool.closeAll(is, os);
		}
	}

	private ImageInfo generatePermutation(FileCacheRef targetref, List<IImageConversionSpecifier> conversions) throws Exception {
		CachedImageData origd = getOriginalData(); // We need the data always
		CachedImageInfo origi = getOriginalInfo(); // And the info's nice too

		//-- 2. Create the object using the permutator factories.
		ImageConverterHelper ich = new ImageConverterHelper();
		try {
			ImageSpec sis = new ImageSpec(origd.getFile(), origi.getImageInfo());
			ich.executeConversionChain(sis, new ArrayList<IImageConversionSpecifier>(conversions)); // Execute the conversion chain

			//-- Now copy the final result into the cachefile;
			FileTool.copyFile(targetref.getFile(), ich.getTarget().getSource()); // Copy result to cachefile
			return ich.getTarget().getInfo();
		} finally {
			ich.destroy();
		}
	}

	public CachedImageInfo getImageInfo(List<IImageConversionSpecifier> conversions) throws Exception {
		//-- Shortcut: if referring to the ORIGINAL...
		if(conversions == null || conversions.size() == 0)
			return getOriginalInfo();

		removeOutdatedVersions(); // Remove all old thingies from the cache.
		String perm = getPermutationKey(conversions); // Create the key for the specified conversions

		//-- Is the required version cached? In that case it's current so reuse
		CachedImageInfo cii = getRoot().findPermutationInfo(perm);
		if(cii != null) {
			addUsedFragment(cii); // Mark recently used
			return cii;
		}

		//-- We need to create it- Just creating the data also creates the info.
		getImageData(conversions);

		cii = getRoot().findPermutationInfo(perm);
		if(cii == null)
			throw new IllegalStateException("? Image transformation did not create an ImageInfo instance in the cache?");
		return cii;
	}

	public Object getFullImage(List<IImageConversionSpecifier> conversions) throws Exception {
		removeOutdatedVersions(); // Remove all old thingies from the cache.
		CachedImageData cid = getImageData(conversions);
		CachedImageInfo cii = getImageInfo(conversions);
		return new FullImage(ImageCache.convert(cid), cii.getImageInfo());
	}
}
