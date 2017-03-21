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
package to.etc.smtp;

import java.io.*;

/**
 * Escapes lines starting with a '.' to use 2 dots. Replaces
 * all single LF with CRLF.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 24, 2010
 */
public class EmailOutputStream extends OutputStream {
	private OutputStream	m_os;

	private int				m_prev;

	private int				m_col;

	public EmailOutputStream(OutputStream os) {
		m_os = os;
	}

	@Override
	public void write(int b) throws IOException {
		if(b == 0x0d) {
			if(m_prev == 0x0d)
				throw new IllegalStateException("CR CR sequence in stream is invalid");
			m_os.write(b);
		} else if(b == 0x0a) {
			if(m_prev != 0x0d)
				m_os.write(0x0d);
			m_os.write(b);
			m_col = 0;
		} else if(b == '.' && m_col == 0) {
			m_os.write('.');
			m_os.write('.');
			m_col = 1;
		} else {
			m_os.write(b);
			m_col++;
		}
		m_prev = b;
	}
}
