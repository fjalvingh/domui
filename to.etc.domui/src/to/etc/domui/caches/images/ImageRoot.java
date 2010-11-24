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

import java.util.*;

import javax.annotation.*;
import javax.annotation.concurrent.*;

/**
 * Contains the data for the ROOT (original) image. It also holds the list of permutations
 * currently available in the cache. This object and it's list-of-images is locked thru locking
 * the image cache instance.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 2, 2008
 */
final class ImageRoot {
	@Nonnull
	private ImageCache m_cache;

	/**
	 * This contains the #of attached image fragments that the cache knows of; if this reaches
	 * zero the cache may remove this ImageRoot instance. This gets lazily added to/removed from
	 * in the "administration" (2nd locking) of cache changes, and during LRU removals.
	 */
	@GuardedBy("getCache()")
	int m_cacheUseCount;

	/** The unique key for this image, which includes it's retriever. */
	@Nonnull
	private ImageKey m_imageKey;

	//	private ImageInfo m_originalData;

	private long m_tsLastCheck;

	private long m_versionLong;

	//	private String				m_mimeType;
	//	private Dimension			m_originalDimension;

	@GuardedBy("this")
	private List<CachedImageData> m_dataList = new ArrayList<CachedImageData>();

	@GuardedBy("this")
	private List<CachedImageInfo> m_infoList = new ArrayList<CachedImageInfo>();

	ImageRoot(@Nonnull ImageCache ic, @Nonnull ImageKey key) {
		if(ic == null || key == null)
			throw new IllegalArgumentException("Args cannot be null");
		m_cache = ic;
		m_imageKey = key;
	}

	@Nonnull
	public ImageKey getKey() {
		return m_imageKey;
	}

	@GuardedBy("this")
	synchronized long getTSLastCheck() {
		return m_tsLastCheck;
	}

	@GuardedBy("this")
	synchronized void setTSLastCheck(long ts) {
		m_tsLastCheck = ts;
	}

	/**
	 * If the original image reference is present locate and return it.
	 * @return
	 */
	@Nullable
	CachedImageData findOriginalData() {
		synchronized(this) {
			for(CachedImageData ii : m_dataList) {
				if(ii.getPermutation().length() == 0)
					return ii;
			}
			return null;
		}
	}

	/**
	 * If the original image reference is present locate and return it.
	 * @return
	 */
	@Nullable
	CachedImageInfo findOriginalInfo() {
		synchronized(this) {
			for(CachedImageInfo ii : m_infoList) {
				if(ii.getPermutation().length() == 0)
					return ii;
			}
			return null;
		}
	}



	/**
	 * Try to find the specified permutation of the original document in this root document's cache entry.
	 * @param perm
	 * @return
	 */
	@Nullable
	CachedImageData findPermutationData(String perm) {
		synchronized(this) {
			for(CachedImageData ii : m_dataList) {
				if(perm.equals(ii.getPermutation()))
					return ii;
			}
			return null;
		}
	}

	CachedImageInfo findPermutationInfo(String perm) {
		synchronized(this) {
			for(CachedImageInfo ii : m_infoList) {
				if(perm.equals(ii.getPermutation()))
					return ii;
			}
			return null;
		}
	}

	/**
	 * Get the cache that this is in.
	 * @return
	 */
	@Nonnull
	final ImageCache getCache() {
		return m_cache;
	}

	/**
	 * Adds the image to the list of instances. This does not register it in the
	 * LRU cache nor does it register it's cache load.
	 *
	 * @param ii
	 */
	void registerInstance(CachedImageData id) {
		synchronized(this) {
			m_dataList.add(id);
		}
	}

	void registerInstance(CachedImageInfo id) {
		synchronized(this) {
			m_infoList.add(id);
		}
	}

	boolean unregisterInstance(CachedImageData ii) {
		synchronized(this) {
			m_dataList.remove(ii);
			return m_dataList.size() == 0;
		}
	}

	boolean unregisterInstance(CachedImageInfo ii) {
		synchronized(this) {
			m_infoList.remove(ii);
			return m_infoList.size() == 0;
		}
	}

	/**
	 * LOCKS THIS: Called when a new source version has been found, this discards all instances
	 * currently in the list and returns the original list.
	 * @return
	 */
	@GuardedBy("this")
	synchronized void checkVersionLong(CacheChange cc, long currentversion) {
		if(m_versionLong == currentversion)
			return;

		//-- All versions are outdated- discard the lot of 'm.
		m_versionLong = currentversion;
		List<CachedImageData> old = m_dataList;
		m_dataList = new ArrayList<CachedImageData>();

		//-- Now decrement all of their use counts- they are removed from cache. Usecount is protected by THIS too.
		for(CachedImageData ii : old) {
			try {
				cc.addDeletedFragment(ii); // Account for deleting this instance
			} catch(Exception x) {
				System.err.println("Exception while release()ing " + ii + ": " + x);
				x.printStackTrace();
			}
		}
	}

	/**
	 * Called when the cache has (already) deleted this instance. We need to remove it from this root, and we need to
	 * release it's resources.
	 * @param cif
	 */
	public void lruInstanceDeleted(CachedImageFragment cif) {
		synchronized(this) {
			boolean deleted;
			if(cif instanceof CachedImageData)
				deleted = m_dataList.remove(cif);
			else
				deleted = m_infoList.remove(cif);
			/*
			 * Because we have lock traversal it is possible that the cache deleted these instances but that
			 * an ImageTask also deleted these records while it held the lock on THIS. So if the instances\
			 * do not exist here anymore - don't bother.
			 */
			if(!deleted)
				return;

			//-- Thingy was deleted...
			if(cif.getFileRef() != null)
				cif.getFileRef().close();
		}
	}
}
