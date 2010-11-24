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

/**
 * A referral (use) of a single cached file. When this gets closed the corresponding
 * FileCacheEntry's use count is decremented by 1 causing it do be discardable when
 * necessary.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 9, 2009
 */
final public class FileCacheRef {
	private FileCacheEntry m_entry;

	FileCacheRef(FileCacheEntry entry) {
		m_entry = entry;
	}

	public File getFile() {
		if(m_entry == null)
			throw new IllegalStateException("The reference to this cached file has been closed");
		return m_entry.getFile();
	}

	public void close() {
		if(m_entry == null) // Allow multiple closes.
			return;
		m_entry.dec();
		m_entry = null;
	}
}
