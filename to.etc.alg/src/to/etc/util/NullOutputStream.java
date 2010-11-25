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
 * This is an outputstream which accepts data and sends it to the bit
 * bucket (no data is stored). The amount of data sent to the stream is
 * counted though. Although this class specified throws thingies it never
 * throws an exception (throwing data away is never an error ;-)
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class NullOutputStream extends OutputStream {
	/** The #of bytes currently written. */
	public long	m_sz_written;


	public NullOutputStream() {
	}

	@Override
	public void write(byte[] parm1) throws java.io.IOException {
		m_sz_written += parm1.length;
	}

	@Override
	public void write(int b) throws java.io.IOException {
		m_sz_written++;
	}

	@Override
	public void write(byte[] parm1, int off, int len) throws java.io.IOException {
		m_sz_written += len;
	}

	/**
	 *	Returns the #bytes currently written.
	 */
	public long getSzWritten() {
		return m_sz_written;
	}

	/**
	 *	Resets the size written.
	 */
	public void reset() {
		m_sz_written = 0;
	}
}
