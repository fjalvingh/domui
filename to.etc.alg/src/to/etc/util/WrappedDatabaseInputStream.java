package to.etc.util;

import java.io.*;
import java.sql.*;

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
