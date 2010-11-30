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

import javax.annotation.*;
import javax.annotation.concurrent.*;

class FileCacheEntry {
	private FileCache m_cache;

	private String m_key;

	private File m_file;

	@GuardedBy("m_cache")
	int m_useCount;

	public FileCacheEntry(FileCache fc, File file, String key) {
		m_cache = fc;
		m_file = file;
		m_key = key;
		m_useCount = 1;
	}

	@Nonnull
	public String getKey() {
		return m_key;
	}

	@Nonnull
	public File getFile() {
		return m_file;
	}

	void inc() {
		m_cache.incUse(this);
	}

	void dec() {
		m_cache.decUse(this);
	}
}
