package to.etc.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-01-21.
 */
public class SizeCountingInputStream extends InputStream {
	private final InputStream m_is;

	private long m_byteCount;

	public SizeCountingInputStream(InputStream is) {
		m_is = is;
	}

	@Override
	public int read(byte[] b) throws IOException {
		int szrd = m_is.read(b);
		if(szrd == -1)
			return szrd;
		m_byteCount += szrd;
		return szrd;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int szrd = m_is.read(b, off, len);
		if(szrd == -1)
			return szrd;
		m_byteCount += szrd;
		return szrd;
	}

	@Override
	public byte[] readAllBytes() throws IOException {
		byte[] data = m_is.readAllBytes();
		if(null == data)
			return null;
		m_byteCount += data.length;
		return data;
	}

	@Override
	public byte[] readNBytes(int len) throws IOException {
		byte[] data = m_is.readNBytes(len);
		if(null == data)
			return null;
		m_byteCount += data.length;
		return data;
	}

	@Override
	public int readNBytes(byte[] b, int off, int len) throws IOException {
		int szrd = m_is.readNBytes(b, off, len);
		if(szrd == -1)
			return szrd;
		m_byteCount += szrd;
		return szrd;
	}

	@Override
	public long skip(long n) throws IOException {
		return m_is.skip(n);
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
	public synchronized void mark(int readlimit) {
		m_is.mark(readlimit);
	}

	@Override
	public synchronized void reset() throws IOException {
		m_is.reset();
	}

	@Override
	public boolean markSupported() {
		return m_is.markSupported();
	}

	@Override
	public long transferTo(OutputStream out) throws IOException {
		return m_is.transferTo(out);
	}

	@Override
	public int read() throws IOException {
		int val = m_is.read();
		if(val == -1)
			return -1;
		m_byteCount++;
		return val;
	}

	public long size() {
		return m_byteCount;
	}
}
