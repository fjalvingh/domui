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
package to.etc.telnet;

import java.io.*;

/**
 * A writer which outputs data to a Telnet session.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class TelnetWriter extends Writer {
	private TelnetSession	m_tc;

	protected TelnetWriter(TelnetSession tc) {
		m_tc = tc;
	}

	@Override
	public void write(char[] parm1, int off, int len) throws java.io.IOException {
		try {
			m_tc.write(new String(parm1, off, len));
		} catch(IOException x) {
			throw x;
		} catch(Exception x) {}

	}

	@Override
	public void flush() throws java.io.IOException {
	}

	@Override
	public void close() throws java.io.IOException {
	}

	public TelnetSession getSession() {
		return m_tc;
	}

}
