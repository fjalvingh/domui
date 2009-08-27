package to.etc.util;

import java.io.*;

/**
 * This is an OutputStream wrapper which counts the #of bytes that was written to it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 31, 2009
 */
public class SizeCountingOutputStream extends OutputStream {
	private final OutputStream	m_os;

	private long				m_size;

	public SizeCountingOutputStream(final OutputStream os) {
		m_os = os;
	}

	public long getSize() {
		return m_size;
	}

	@Override
	public void write(final int b) throws IOException {
		m_os.write(b);
		m_size++;
	}

	@Override
	public void close() throws IOException {
		m_os.close();
	}

	@Override
	public void flush() throws IOException {
		m_os.flush();
	}

	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException {
		m_os.write(b, off, len);
		m_size += len;
	}

	@Override
	public void write(final byte[] b) throws IOException {
		m_os.write(b);
		m_size += b.length;
	}
}
