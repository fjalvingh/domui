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
 * An outputstream which duplicates all contents written to it to two
 * other output streams.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 25, 2010
 */
public class TeeStream extends OutputStream {
	private OutputStream	m_a, m_b;

	public TeeStream(OutputStream a, OutputStream b) {
		m_a = a;
		m_b = b;
	}

	@Override
	public void write(byte[] parm1) throws java.io.IOException {
		IOException sx = null;
		try {
			m_a.write(parm1);
		} catch(IOException x) {
			sx = x;
		}

		try {
			m_b.write(parm1);
		} catch(IOException x) {
			sx = x;
		}
		if(sx != null)
			throw sx;
	}

	@Override
	public void flush() throws java.io.IOException {
		IOException sx = null;
		try {
			m_a.flush();
		} catch(IOException x) {
			sx = x;
		}

		try {
			m_b.flush();
		} catch(IOException x) {
			sx = x;
		}
		if(sx != null)
			throw sx;
	}

	@Override
	public void write(int b) throws java.io.IOException {
		IOException sx = null;
		try {
			m_a.write(b);
		} catch(IOException x) {
			sx = x;
		}

		try {
			m_b.write(b);
		} catch(IOException x) {
			sx = x;
		}
		if(sx != null)
			throw sx;
	}

	@Override
	public void write(byte[] parm1, int parm2, int parm3) throws java.io.IOException {
		IOException sx = null;
		try {
			m_a.write(parm1, parm2, parm3);
		} catch(IOException x) {
			sx = x;
		}

		try {
			m_b.write(parm1, parm2, parm3);
		} catch(IOException x) {
			sx = x;
		}
		if(sx != null)
			throw sx;
	}

	@Override
	public void close() throws java.io.IOException {
		IOException sx = null;
		try {
			m_a.close();
		} catch(IOException x) {
			sx = x;
		}

		try {
			m_b.close();
		} catch(IOException x) {
			sx = x;
		}
		if(sx != null)
			throw sx;
	}


}
