/*
 * DomUI Java User Interface - shared code
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
package to.etc.util;

import java.io.*;

/**
 * This is a reader class which appends all of the data
 * read to an appendable.
 *
 * Created on Aug 23, 2005
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class CopyingReader extends Reader {
	private Reader	m_r;

	private Writer	m_sb;

	public CopyingReader(Reader r, Writer sb) {
		m_r = r;
		m_sb = sb;
	}

	/**
	 *
	 * @see java.io.Reader#close()
	 */
	@Override
	public void close() throws IOException {
		m_r.close();
	}

	/**
	 * Read the crud, and append all that was read to the
	 * string buffer also.
	 */
	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		int sz = m_r.read(cbuf, off, len); // Ask source
		if(sz > 0)
			m_sb.write(cbuf, off, sz);
		return sz;
	}

	public String getRead() {
		return m_sb.toString();
	}
}
