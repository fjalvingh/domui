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
 * This is a wrapper for email-data streams. It should encapsulate
 * the primary data stream sent as the content of the DATA command
 * to the mail server. It ensures that data sent to the mail server
 * does not start with a single '.' by adding another dot to all
 * lines starting with it. In addition it handles line length
 * encapsulation by splitting up lines that are longer than 76 characters
 * in soft-quoted lines.
 * Email is hell.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 24, 2010
 */
public class QuotedPrintableOutputStream extends OutputStream {
	private OutputStream	m_os;

	private int				m_llen;

	private int				m_ineol;

	public QuotedPrintableOutputStream(OutputStream os) {
		m_os = os;
	}

	@Override
	public void write(int b) throws IOException {
		if(b == 0x0d) {
			if(m_ineol != 0)
				throw new IllegalStateException("Unexpected 0x0d-0x0d (2x) in input. All lines should have crlf endings");

			m_os.write(b);
			m_ineol = 1;
		} else if(b == 0xa) {
			if(m_ineol == 0) {
				// Lone eol - abort
				throw new IllegalStateException("Unexpected single 0x0a in input. All lines should have crlf endings");
			}
			m_os.write(0x0a);
			m_llen = 0;
			m_ineol = 0;
		} else {
			if(m_llen >= 76) {
				//-- Generate soft line ending.


			}


		}
	}



}
