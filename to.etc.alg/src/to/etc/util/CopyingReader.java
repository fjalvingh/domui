package to.etc.util;

import java.io.*;

/**
 * This is a reader class which appends all of the data
 * read to an appendable.
 *
 * Created on Aug 23, 2005
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class CopyingReader extends Reader {
	private Reader	m_r;

	private Writer	m_sb;

	public CopyingReader(Reader r, Writer sb) {
		m_r = r;
		m_sb = sb;
	}

	/**
	 *
	 * @see java.io.Reader#close()
	 */
	@Override
	public void close() throws IOException {
		m_r.close();
	}

	/**
	 * Read the crud, and append all that was read to the
	 * string buffer also.
	 */
	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		int sz = m_r.read(cbuf, off, len); // Ask source
		if(sz > 0)
			m_sb.write(cbuf, off, sz);
		return sz;
	}

	public String getRead() {
		return m_sb.toString();
	}
}
