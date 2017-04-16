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
 * This is an OutputStream wrapper which counts the #of bytes that was written to it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 31, 2009
 */
public class SizeCountingOutputStream extends OutputStream {
	private final OutputStream	m_os;

	private long				m_size;

	public SizeCountingOutputStream(final OutputStream os) {
		m_os = os;
	}

	public long getSize() {
		return m_size;
	}

	@Override
	public void write(final int b) throws IOException {
		m_os.write(b);
		m_size++;
	}

	@Override
	public void close() throws IOException {
		m_os.close();
	}

	@Override
	public void flush() throws IOException {
		m_os.flush();
	}

	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException {
		m_os.write(b, off, len);
		m_size += len;
	}

	@Override
	public void write(final byte[] b) throws IOException {
		m_os.write(b);
		m_size += b.length;
	}
}
