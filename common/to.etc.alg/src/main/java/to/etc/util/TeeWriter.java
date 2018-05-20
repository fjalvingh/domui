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
 * A Writer which duplicates all data written to it to two writers.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 25, 2010
 */
public class TeeWriter extends Writer {
	private Writer	m_a;

	private Writer	m_b;

	public TeeWriter(Writer a, Writer b) {
		m_a = a;
		m_b = b;
	}

	@Override
	public void write(String str, int off, int len) throws java.io.IOException {
		m_a.write(str, off, len);
		m_b.write(str, off, len);
	}

	@Override
	public void write(String str) throws java.io.IOException {
		m_a.write(str);
		m_b.write(str);
	}

	@Override
	public void close() throws java.io.IOException {
		m_a.flush();
		m_b.flush();
	}

	@Override
	public void write(char[] parm1) throws java.io.IOException {
		m_a.write(parm1);
		m_b.write(parm1);
	}

	@Override
	public void write(char[] parm1, int parm2, int parm3) throws java.io.IOException {
		m_a.write(parm1, parm2, parm3);
		m_b.write(parm1, parm2, parm3);
	}

	@Override
	public void flush() throws java.io.IOException {
		m_a.flush();
		m_b.flush();
	}

	@Override
	public void write(int c) throws java.io.IOException {
		m_a.write(c);
		m_b.write(c);
	}
}
