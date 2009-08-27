package to.etc.server.servlet;

import java.io.*;

public class LogInputStream extends InputStream {
	private ByteArrayOutputStream	m_os	= new ByteArrayOutputStream();

	private InputStream				m_is;

	public LogInputStream(InputStream is) {
		m_is = is;
	}

	@Override
	public int available() throws IOException {
		return m_is.available();
	}

	@Override
	public void close() throws IOException {
		m_is.close();
	}

	@Override
	public void mark(int readlimit) {
		m_is.mark(readlimit);
	}

	@Override
	public boolean markSupported() {
		return m_is.markSupported();
	}

	@Override
	public int read() throws IOException {
		int c = m_is.read();
		m_os.write(c);
		return c;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int lr = m_is.read(b, off, len);
		if(lr > 0)
			m_os.write(b, off, lr);
		return lr;
	}

	@Override
	public int read(byte[] b) throws IOException {
		int lr = m_is.read(b);
		if(lr > 0)
			m_os.write(b, 0, lr);
		return lr;
	}

	@Override
	public void reset() throws IOException {
		m_is.reset();
	}

	@Override
	public long skip(long n) throws IOException {
		return m_is.skip(n);
	}

	public byte[] getData() {
		return m_os.toByteArray();
	}
}
