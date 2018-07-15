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
 * This is a utility class that implements an input stream which retrieves it's
 * data from an array of byte arrays. Each byte array contains part of the data,
 * and the sum of all data in the arrays is the total data in the set.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class ByteBufferInputStream extends InputStream {
	/** The table of byte arrays that form this-stream's data */
	private byte[][]	m_data;

	/** The #of bytes in this stream; usually the sum of al byte array lengths */
	private int			m_sz;

	/** The current byte offset */
	private int			m_off;

	/** The current buffer being read, */
	private int			m_bufix;

	/** The current buffer's offset */
	private int			m_bufoff;

	public ByteBufferInputStream(byte[][] data) {
		m_data = data;
		m_sz = calcDataSize();
		//		System.out.println("Byte stream of "+m_sz);
	}

	/**
	 * Resets the read position to the one specified by scanning all data buffers
	 * @param pos
	 */
	private void setReadPos(int pos) {
		m_bufix = 0;
		m_off = 0;
		while(m_bufix < m_data.length) {
			int epos = m_off + m_data[m_bufix].length; // End position in this buffer
			if(epos > pos) {
				//-- Found- in this buffer!
				m_bufoff = pos - m_off; // Get index in this buffer
				m_off = pos; // Set current location,
				return;
			}

			//-- Need next buffer,
			m_off = epos;
			m_bufix++;
		}

		//-- EOF reached-
		m_off = m_sz;
	}

	private final int calcDataSize() {
		int sz = 0;
		for(int i = m_data.length; --i >= 0;) {
			byte[] d = m_data[i];
			if(d != null)
				sz += d.length;
		}
		return sz;
	}


	@Override
	public long skip(long n) throws java.io.IOException {
		m_off += (int) n;
		setReadPos(m_off);
		return m_off;
	}

	@Override
	public int read(byte[] parm1) throws java.io.IOException {
		return read(parm1, 0, parm1.length);
	}

	@Override
	public synchronized void reset() throws java.io.IOException {
		setReadPos(0);
	}

	@Override
	public boolean markSupported() {
		return false;
	}

	@Override
	public int available() throws java.io.IOException {
		int sz = m_sz - m_off;
		if(sz < 0)
			return 0;
		return sz;
	}

	@Override
	public int read() throws java.io.IOException {
		if(m_off >= m_sz)
			return -1;

		//-- Data in the current buffer?
		byte[] buf = m_data[m_bufix];
		if(m_bufoff < buf.length) {
			m_off++;
			return buf[m_bufoff++] & 0xff;
		}

		//-- Move to next buffer,
		m_bufix++;
		buf = m_data[m_bufix];
		m_bufoff = 1;
		m_off++;
		return buf[0] & 0xff;
	}


	@Override
	public int read(byte[] odata, int ooff, int osz) throws java.io.IOException {
		if(m_off >= m_sz)
			return -1;

		int szread = 0;
		while(osz > 0) {
			//-- Make a run for it... How much CAN we copy, max?
			int rsz = m_sz - m_off; // This much IS available

			if(rsz <= 0)
				break;
			byte[] buf = m_data[m_bufix]; // Get current buffer,
			if(m_bufoff >= buf.length) {
				m_bufix++;
				buf = m_data[m_bufix];
				m_bufoff = 0;
			}
			int bsz = buf.length - m_bufoff; // Can get this much from the current buffer
			if(rsz > bsz)
				rsz = bsz; // Reduce to size in buffer,
			if(rsz > osz)
				rsz = osz; // Reduce to max. size left to read

			//-- Now copy the data,
			System.arraycopy(buf, m_bufoff, odata, ooff, rsz);
			m_bufoff += rsz;
			osz -= rsz;
			ooff += rsz;
			m_off += rsz;
			szread += rsz;
		}
		return szread;
	}
}
