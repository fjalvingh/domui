package to.etc.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Wraps simple logic that can call close only once and ignores later close calls, as any normal OutputStream should behave.
 */
public class WrappedOutputStream extends OutputStream {

	private OutputStream m_out;
	private boolean m_closed = false;

	public WrappedOutputStream(OutputStream outputStream) {
		m_out = outputStream;
	}

	@Override
	public void write(int b) throws IOException {
		m_out.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		m_out.write(b, off, len);
	}

	@Override
	public void write(byte[] b) throws IOException {
		m_out.write(b);
	}

	@Override
	public void flush() throws IOException {
		m_out.flush();
	}

	@Override
	public void close() throws IOException {
		if(! m_closed) {
			m_out.close();
			m_closed = true;
		}
	}
}
