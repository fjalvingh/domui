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
package to.etc.domui.caches.filecache;

import java.io.*;
import java.util.*;

/**
 * Contains a set of references that you are working on from the
 * file cache. Entries in here are protected from being garbage-collected
 * on the file system.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 1, 2009
 */
public class FileCacheSet {
	private FileCache m_cache;

	private List<FileCacheEntry> m_contents = new ArrayList<FileCacheEntry>();

	FileCacheSet(FileCache cache) {
		m_cache = cache;
	}

	/**
	 * Get a cache entry. If one already exists this just returns the file, the use count is left
	 * at 1.
	 * @param path
	 * @return
	 */
	public File getFile(String path) {
		for(FileCacheEntry ce : m_contents) {
			if(ce.getKey().equals(path))
				return ce.getFile(); // Re-use existing entry without incrementing usecount
		}

		//-- Allocate a new REF for here
		FileCacheEntry r = m_cache.getCacheEntry(path);
		m_contents.add(r);
		return r.getFile();
	}

	/**
	 * Discard all elements in the set (allows them to be reaped).
	 */
	public void close() {
		for(FileCacheEntry fe : m_contents) {
			fe.dec();
		}
		m_contents.clear();
	}
}
