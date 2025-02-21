package to.etc.util;

import org.eclipse.jdt.annotation.Nullable;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A wrapped outputstream which calls listeners every 1MB of data output, to track
 * size.
 */
public class SizeCountingOutputStream extends OutputStream {
	/** Report progress every 100MB */
	static private final long REPORTINTERVALBYTES = 100 * 1024L * 1024L;

	final private OutputStream m_out;

	private long m_notifyChunk = REPORTINTERVALBYTES;

	private long m_totalWritten;

	@Nullable
	private IBytesWrittenListener m_listener;

	public interface IBytesWrittenListener {
		void bytesWritten(long amount);
	}

	public SizeCountingOutputStream(OutputStream outputStream) {
		m_out = outputStream;
	}

	public SizeCountingOutputStream(OutputStream outputStream, IBytesWrittenListener listener) {
		m_out = outputStream;
		m_listener = listener;
	}


	@Override
	public void write(int b) throws IOException {
		m_out.write(b);
		notifyProgress(1);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		m_out.write(b, off, len);
		notifyProgress(len);
	}

	@Override
	public void flush() throws IOException {
		m_out.flush();
	}

	@Override
	public void close() throws IOException {
		m_out.close();
		IBytesWrittenListener listener = m_listener;
		if(null != listener)
			listener.bytesWritten(m_totalWritten);
	}

	public void setListener(@Nullable IBytesWrittenListener listener) {
		m_listener = listener;
	}

	private void notifyProgress(int bytes) {
		m_totalWritten += bytes;
		IBytesWrittenListener listener = m_listener;
		if(null != listener) {
			m_notifyChunk -= bytes;
			if(m_notifyChunk <= 0) {
				//-- Time to report
				m_notifyChunk = REPORTINTERVALBYTES;
				listener.bytesWritten(m_totalWritten);
			}
		}
	}

	public long getSize() {
		return m_totalWritten;
	}
}
