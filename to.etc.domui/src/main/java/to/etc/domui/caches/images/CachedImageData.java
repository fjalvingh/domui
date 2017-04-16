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

import javax.annotation.concurrent.*;

import to.etc.domui.caches.filecache.*;

/**
 * Some permutation of an image that was recently used. Once created, an CachedImageData is immutable
 * and will be discarded fully when changes to it are necessary, for instance because a newer
 * version of the source is available. The immutable character means that when ImageInstances have
 * to change existing "readers" of an instance may continue to use that instance; when they are
 * done using it the garbage collector will release it's resources automatically.
 *
 * <p>An CachedImageData always contains a cachefile containing the datastream for the resource, and
 * optionally a bufferset containing a memory-cached copy. The amount of memory used by an ImageEntry
 * determines it's <i>cache load</i>, and is used to add to the cache's actual size. This is the only
 * metric used to determine that the cache is full causing LRU removal of entries.</p>
 *
 * <p>The "immutableness" of an ImageSource also extends to it's backing file in the filecache. This
 * means that different "versions" of the ImageSource need different cachefiles, and in no way may
 * an existing cachefile be "overwritten" with a different version - because clients can be reading
 * it. This means that all cachefiles for a given CachedImageData use the source's version number in
 * their filename.</p>
 *
 * <p>Because filenames are versioned we can reuse existing files on the file system when encountered.
 * This can be after a server restart where the memory cache is empty; when a resource is accessed and
 * it's cachefiles are present and valid there is no need to get them again from the db; this means that
 * only a "version number ping" is done to the db. Another way is when a resource is evicted from the
 * memory cache because the cache was full and it was the least-recently-used (LRU) object: in that
 * case the memory copy is removed but the files remain. When the object is used again it will pick
 * up it's datafiles when possible, making for speedy recovery of a cache miss.</p>
 *
 * <h3>Exceptions to the immutableness</h3>
 * <p>Although all user-required fields are immutable, this object contains some fields like the LRU
 * pointers and the cache state which are changed and maintained by the ImageCache instance itself,
 * and that change during the object's lifecycle.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 2, 2008
 */
@Immutable
final class CachedImageData extends CachedImageFragment {
	/** If this is cached in memory the data of this image. */
	private byte[][] m_buffers;

	/** The number of bytes of data in the above buffers. Also the cache load. */
	private int m_size;

	//	/** The mime type of the data stored in this data block (original mime or mime of permutation) */
	//	private String m_mimeType;

	CachedImageData(final ImageRoot root, final String perm, long sourceVersionLong, FileCacheRef ref, int datasize, byte[][] membuffers, int cacheload) {
		super(root, perm, sourceVersionLong, cacheload, ref);
		m_buffers = membuffers;
		m_size = datasize;
	}

	/**
	 * Return the size in bytes of the cached data.
	 * @return
	 */
	public int getSize() {
		return m_size;
	}

	//	/**
	//	 * This retrieves the ImageData for this image. If the data is not yet known this instance gets locked and
	//	 * a new Identify action is done.
	//	 * @return
	//	 */
	//	public synchronized ImageInfo getImageData() throws Exception {
	//		if(m_imageData == null) {
	//			//-- Create a file containing the thingy.
	//			File tmp = File.createTempFile("imgi", ".tmp");
	//			try {
	//				FileTool.save(tmp, m_buffers);
	//				m_imageData = ImageConverterRegistry.identify(m_mimeType, tmp);
	//			} finally {
	//				try {
	//					tmp.delete();
	//				} catch(Exception x) {}
	//			}
	//		}
	//		return m_imageData;
	//	}
	//

	boolean remove() {
		m_cacheState = InstanceCacheState.DISCARD;
		return getRoot().unregisterInstance(this);
	}


	public byte[][] getBuffers() {
		return m_buffers;
	}

}
