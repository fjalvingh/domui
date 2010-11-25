package to.etc.util;

import java.io.*;

/**
 * Returns a stream which is constructed from the specified string.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 10, 2007
 */
public class StringInputStream extends InputStream {
	private ByteArrayInputStream	m_is;

	public StringInputStream(String s, String enc) throws UnsupportedEncodingException {
		byte[] data = s.getBytes(enc);
		m_is = new ByteArrayInputStream(data);
	}

	@Override
	public int available() {
		return m_is.available();
	}

	@Override
	public void close() throws IOException {
		m_is.close();
	}

	/**
	 * Despite the Eclipse warning: do NOT add synchronized - it is wrong.
	 * @see java.io.InputStream#mark(int)
	 */
	@Override
	public void mark(int readAheadLimit) {
		m_is.mark(readAheadLimit);
	}

	@Override
	public boolean markSupported() {
		return m_is.markSupported();
	}

	@Override
	public int read() {
		return m_is.read();
	}

	@Override
	public int read(byte[] b, int off, int len) {
		return m_is.read(b, off, len);
	}

	@Override
	public int read(byte[] arg0) throws IOException {
		return m_is.read(arg0);
	}

	/**
	 * Despite the Eclipse warning: do NOT add synchronized - it is wrong.
	 * @see java.io.InputStream#reset()
	 */
	@Override
	public void reset() {
		m_is.reset();
	}

	@Override
	public long skip(long n) {
		return m_is.skip(n);
	}
}
