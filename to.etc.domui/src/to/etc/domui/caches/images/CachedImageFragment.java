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

import javax.annotation.concurrent.*;

import to.etc.domui.caches.filecache.*;

/**
 * The base for a cached image thingerydoo. This is maintained by
 * the ImageCache.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 9, 2009
 */
class CachedImageFragment {
	/** The root descriptor of the image's base */
	private final ImageRoot m_imageRoot;

	/** An unique string describing the permutation of the original that this contains. When "" (empty string) this is the ORIGINAL image. */
	private final String m_permutation;

	//	/** The versionLong of the source for this image at the time it was created. */
	//	@GuardedBy("getRoot()")
	//	private long m_sourceVersionLong;

	/**
	 * The LRU pointers for the cache's LRU list. These are locked and maintained by the ImageCache itself; access to these is "verboten" from self.
	 */
	@GuardedBy("cache()")
	CachedImageFragment m_lruPrev, m_lruNext;

	@GuardedBy("cache()")
	InstanceCacheState m_cacheState = InstanceCacheState.NONE;

	/** The current actual memory size taken by this entry. */
	@GuardedBy("getRoot()")
	private long m_memoryCacheSize;

	/** The cacheref for the file while this thingy is in use. */
	private FileCacheRef m_fileRef;

	CachedImageFragment(final ImageRoot root, final String perm, long sourceVersionLong, int memorysize, FileCacheRef ref) {
		m_imageRoot = root;
		m_permutation = perm;
		//		m_sourceVersionLong = sourceVersionLong;
		m_memoryCacheSize = memorysize;
		m_fileRef = ref;
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

	final public ImageCache cache() {
		return getRoot().getCache();
	}

	public long getMemoryCacheSize() {
		return m_memoryCacheSize;
	}

	final public File getFile() {
		return m_fileRef.getFile();
	}

	final public FileCacheRef getFileRef() {
		return m_fileRef;
	}
}
