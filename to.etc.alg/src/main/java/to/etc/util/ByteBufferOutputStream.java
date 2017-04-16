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
import java.util.*;

/**
 * This is a streamer class which accepts data and writes this data into an
 * array of fixed-size buffers. At the end of the write the array of buffers can
 * be gotten from the instance.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class ByteBufferOutputStream extends OutputStream {
	/** The buffer size requested */
	private int			m_bufsz;

	/** The current size of the data, */
	private int			m_sz;

	/** The byte offset within the buffer. */
	private int			m_boff	= Integer.MAX_VALUE;

	/** The current buffer being filled. */
	private byte[]		m_buf;

	/** The array of buffers. */
	private List<byte[]>	m_al	= new ArrayList<byte[]>(16);

	private byte[][]	m_bar;

	public ByteBufferOutputStream() {
		m_bufsz = 8192;
	}

	public ByteBufferOutputStream(int bufsz) {
		m_bufsz = bufsz;
		if(m_bufsz <= 0)
			throw new IllegalArgumentException("bufsz <= 0: " + bufsz);
	}

	@Override
	public void write(byte[] odata, int ooff, int osz) throws java.io.IOException {
		while(osz > 0) {
			//			System.out.println(Thread.currentThread().getName()+": ByteBufferedOutputStream: still need to write "+osz);

			//-- 1. Is there space in the current buffer?
			if(m_boff >= m_bufsz) {
				if(m_buf != null) // There IS a buffer?
					m_al.add(m_buf); // Add it to the list,
				m_buf = new byte[m_bufsz]; // Allocate a new buffer
				m_boff = 0; // And start at it's beginning
			}

			//-- 2. Get size to write in this buffer
			int wsz = m_bufsz - m_boff; // #bytes left in this buffer
			if(wsz > osz)
				wsz = osz; // Truncate if too big
			System.arraycopy(odata, ooff, m_buf, m_boff, wsz);
			ooff += wsz;
			osz -= wsz;
			m_boff += wsz;
			m_sz += wsz;
		}
	}

	@Override
	public void write(byte[] parm1) throws java.io.IOException {
		write(parm1, 0, parm1.length);
	}

	@Override
	public void write(int b) throws java.io.IOException {
		if(m_boff >= m_bufsz) {
			if(m_buf != null) // There IS a buffer?
				m_al.add(m_buf); // Add it to the list,
			m_buf = new byte[m_bufsz]; // Allocate a new buffer
			m_boff = 0; // And start at it's beginning
		}
		m_buf[m_boff++] = (byte) b;
		m_sz++;
	}

	@Override
	public void close() throws java.io.IOException {
		if(m_bar != null)
			return;

		//-- Create the buffer table: first truncate the last buffer, if needed,
		if(m_buf != null) {
			if(m_boff >= m_bufsz) // Last buffer is EXACTLY full?
				m_al.add(m_buf); // Then just add it,
			else {
				byte[] nbuf = new byte[m_boff];
				System.arraycopy(m_buf, 0, nbuf, 0, m_boff);
				m_al.add(nbuf);
			}
			m_buf = null;
		}

		m_bar = m_al.toArray(new byte[m_al.size()][]);
		m_al = null;
	}

	/**
	 * Returns the buffer table for the write.
	 * @return
	 */
	public byte[][] getBuffers() {
		return m_bar;
	}

	public int getSize() {
		return m_sz;
	}
}
