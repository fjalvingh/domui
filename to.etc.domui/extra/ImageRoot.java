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
package to.etc.domui.util.images.cache;

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
	private ImageCache m_lock;

	/** The unique key for this image, which includes it's retriever. */
	@Nonnull
	private ImageKey m_imageKey;

	//	private OriginalImageData m_originalData;

	private long m_tsLastCheck;

	private long m_versionLong;

	//	private String				m_mimeType;
	//	private Dimension			m_originalDimension;

	@GuardedBy("this")
	private List<ImageInstance> m_instanceList = new ArrayList<ImageInstance>();

	ImageRoot(@Nonnull ImageCache ic, @Nonnull ImageKey key) {
		if(ic == null || key == null)
			throw new IllegalArgumentException("Args cannot be null");
		m_lock = ic;
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
	ImageInstance findOriginal() {
		synchronized(this) {
			for(ImageInstance ii : m_instanceList) {
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
	ImageInstance findPermutation(String perm) {
		synchronized(this) {
			for(ImageInstance ii : m_instanceList) {
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
		return m_lock;
	}

	/**
	 * Adds the image to the list of instances. This does not register it in the
	 * LRU cache nor does it register it's cache load.
	 *
	 * @param ii
	 */
	void registerInstance(ImageInstance id) {
		synchronized(m_lock) {
			m_instanceList.add(id);
		}
	}

	boolean unregisterInstance(ImageInstance ii) {
		synchronized(m_lock) {
			m_instanceList.remove(ii);
			return m_instanceList.size() == 0;
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
		List<ImageInstance> old = m_instanceList;
		m_instanceList = new ArrayList<ImageInstance>();

		//-- Now decrement all of their use counts- they are removed from cache. Usecount is protected by THIS too.
		for(ImageInstance ii : old) {
			try {
				cc.addDeletedImage(ii); // Account for deleting this instance
				ii.release();
			} catch(Exception x) {
				System.err.println("Exception while release()ing " + ii + ": " + x);
				x.printStackTrace();
			}
		}
	}

	int getInstanceCount() {
		return m_instanceList.size();
	}
}
