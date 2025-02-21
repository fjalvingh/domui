package to.etc.util;

import org.eclipse.jdt.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-01-21.
 */
public class SizeCountingInputStream extends InputStream {
	/** Report progress every 50MB */
	static private final long REPORTINTERVALBYTES = 50 * 1024L * 1024L;

	private final InputStream m_is;

	private long m_totalRead;

	private long m_notifyChunk = REPORTINTERVALBYTES;

	final private long m_maxSize;

	@Nullable
	private IBytesReadListener m_listener;

	public interface IBytesReadListener {
		void bytesRead(long amount);
	}

	public SizeCountingInputStream(InputStream is) {
		this(is, Long.MAX_VALUE, null);
	}

	public SizeCountingInputStream(InputStream is, long maxSize) {
		this(is, maxSize, null);
	}

	public SizeCountingInputStream(InputStream is, long maxSize, @Nullable IBytesReadListener listener) {
		m_is = is;
		m_maxSize = maxSize;
		m_listener = listener;
	}

	public void setListener(@Nullable IBytesReadListener listener) {
		m_listener = listener;
	}

	@Override
	public int read(byte[] b) throws IOException {
		int szrd = m_is.read(b);
		if(szrd == -1)
			return szrd;
		notifyProgress(szrd);
		return szrd;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int szrd = m_is.read(b, off, len);
		if(szrd == -1)
			return szrd;
		notifyProgress(szrd);
		return szrd;
	}

	@Override
	public byte[] readAllBytes() throws IOException {
		byte[] data = m_is.readAllBytes();
		if(null == data)
			return null;
		notifyProgress(data.length);
		return data;
	}

	@Override
	public byte[] readNBytes(int len) throws IOException {
		byte[] data = m_is.readNBytes(len);
		if(null == data)
			return null;
		notifyProgress(data.length);
		return data;
	}

	@Override
	public int readNBytes(byte[] b, int off, int len) throws IOException {
		int szrd = m_is.readNBytes(b, off, len);
		if(szrd == -1)
			return szrd;
		notifyProgress(szrd);
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
		IBytesReadListener listener = m_listener;
		if(null != listener)
			listener.bytesRead(m_totalRead);
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
		notifyProgress(1);
		return val;
	}

	private void notifyProgress(int bytes) throws IOException {
		m_totalRead += bytes;
		if(m_totalRead >= m_maxSize)
			throw new IOException("Stream size exceeded maximum size");

		IBytesReadListener listener = m_listener;
		if(null != listener) {
			m_notifyChunk -= bytes;
			if(m_notifyChunk <= 0) {
				//-- Time to report
				m_notifyChunk = REPORTINTERVALBYTES;
				listener.bytesRead(m_totalRead);
			}
		}
	}

	public long size() {
		return m_totalRead;
	}
}
