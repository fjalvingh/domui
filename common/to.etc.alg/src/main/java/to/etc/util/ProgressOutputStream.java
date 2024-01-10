package to.etc.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * A wrapped outputstream which calls listeners every 1MB of data output, to track
 * size.
 */
public class ProgressOutputStream extends OutputStream {
	static private final long REPORTINTERVALBYTES = 1024L * 1024L;

	final private OutputStream m_out;

	private long m_notifyChunk = REPORTINTERVALBYTES;

	private long m_totalWritten;

	private List<Consumer<Long>> m_onSizeListeners = new CopyOnWriteArrayList<>();

	public ProgressOutputStream(OutputStream outputStream) {
		m_out = outputStream;
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
		m_onSizeListeners.forEach(a -> a.accept(m_totalWritten));
	}

	public ProgressOutputStream addOnSizeListener(Consumer<Long> listener) {
		m_onSizeListeners.add(listener);
		return this;
	}

	private void notifyProgress(int bytes) {
		m_totalWritten += bytes;
		m_notifyChunk -= bytes;
		if(m_notifyChunk <= 0) {
			//-- Time to report
			m_notifyChunk = REPORTINTERVALBYTES;
			m_onSizeListeners.forEach(a -> a.accept(m_totalWritten));
		}
	}

	public long getTotalWritten() {
		return m_totalWritten;
	}
}
