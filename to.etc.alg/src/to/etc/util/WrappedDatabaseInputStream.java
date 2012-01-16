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
import java.sql.*;

/**
 * An input stream which encapsulates the database resources it is derived
 * from, to allow returning BLOB streams safely. When the stream is closed
 * it also closes all database resources.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 25, 2010
 */
public class WrappedDatabaseInputStream extends InputStream {
	private final InputStream	m_is;

	private final ResultSet		m_rs;

	private final Statement		m_st;

	private final Connection	m_dbc;

	@Override
	public int available() throws IOException {
		return m_is.available();
	}

	@Override
	public void close() throws IOException {
		try {
			if(m_is != null)
				m_is.close();
		} catch(Exception x) {}
		try {
			if(m_rs != null)
				m_rs.close();
		} catch(Exception x) {}
		try {
			if(m_st != null)
				m_st.close();
		} catch(Exception x) {}
		try {
			if(m_dbc != null)
				m_dbc.close();
		} catch(Exception x) {}
	}

	/**
	 * Despite the Eclipse warning: do NOT add synchronized - it is wrong.
	 *
	 * @see java.io.InputStream#mark(int)
	 */
	@Override
	public void mark(final int readlimit) {
		m_is.mark(readlimit);
	}

	@Override
	public boolean markSupported() {
		return m_is.markSupported();
	}

	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException {
		return m_is.read(b, off, len);
	}

	@Override
	public int read(final byte[] b) throws IOException {
		return m_is.read(b);
	}

	/**
	 * Despite the Eclipse warning: do NOT add synchronized - it is wrong.
	 *
	 * @see java.io.InputStream#reset()
	 */
	@Override
	public void reset() throws IOException {
		m_is.reset();
	}

	@Override
	public long skip(final long n) throws IOException {
		return m_is.skip(n);
	}

	public WrappedDatabaseInputStream(final Connection dbc, final Statement st, final ResultSet rs, final InputStream is) {
		m_dbc = dbc;
		m_st = st;
		m_rs = rs;
		m_is = is;
	}


	@Override
	public int read() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

}
