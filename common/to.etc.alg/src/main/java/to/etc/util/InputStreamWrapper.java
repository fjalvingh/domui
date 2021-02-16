package to.etc.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 16-02-21.
 */
public class InputStreamWrapper extends InputStream {
	@Override
	public int read() throws IOException {
		return m_wrapped.read();
	}

	@Override
	public byte[] readAllBytes() throws IOException {
		return m_wrapped.readAllBytes();
	}

	@Override
	public byte[] readNBytes(int len) throws IOException {
		return m_wrapped.readNBytes(len);
	}

	@Override
	public int readNBytes(byte[] b, int off, int len) throws IOException {
		return m_wrapped.readNBytes(b, off, len);
	}

	@Override
	public long skip(long n) throws IOException {
		return m_wrapped.skip(n);
	}

	@Override
	public int available() throws IOException {
		return m_wrapped.available();
	}

	@Override
	public void close() throws IOException {
		m_wrapped.close();
	}

	@Override
	public void mark(int readlimit) {
		m_wrapped.mark(readlimit);
	}

	@Override
	public void reset() throws IOException {
		m_wrapped.reset();
	}

	@Override
	public boolean markSupported() {
		return m_wrapped.markSupported();
	}

	@Override
	public long transferTo(OutputStream out) throws IOException {
		return m_wrapped.transferTo(out);
	}

	private final InputStream m_wrapped;

	public InputStreamWrapper(InputStream wrapped) {
		m_wrapped = wrapped;
	}

	@Override
	public int read(byte[] b) throws IOException {
		return m_wrapped.read(b);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return m_wrapped.read(b, off, len);
	}
}
