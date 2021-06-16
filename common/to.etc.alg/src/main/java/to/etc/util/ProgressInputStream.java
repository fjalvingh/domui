package to.etc.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class ProgressInputStream extends InputStream {

	private InputStream m_in;
	private long m_expectedSize = -1;
	private long m_notifyChunk = 1024 * 1024;
	private long m_lastReported = 0;
	private double m_lastReportedPercentage = 0.0;
	private long m_totalRead = 0;
	private java.util.List<Consumer<Long>> m_onSizeListeners = new CopyOnWriteArrayList<>();
	private java.util.List<Consumer<Integer>> m_onPercentListeners = new CopyOnWriteArrayList<>();
	private int m_percent = 0;

	public ProgressInputStream(InputStream inputStream, int expectedSize) {
		m_in = inputStream;
		m_expectedSize = expectedSize;
	}

	public ProgressInputStream(InputStream inputStream, int expectedSize, int notifyChunk) {
		m_in = inputStream;
		m_expectedSize = expectedSize;
		m_notifyChunk = notifyChunk;
	}

	public ProgressInputStream(InputStream inputStream) {
		m_in = inputStream;
	}

	@Override
	public int read(byte[] b) throws IOException {
		int readCount = m_in.read(b);
		notifyProgress(readCount, false);
		return readCount;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int readCount = m_in.read(b, off, len);
		notifyProgress(readCount, false);
		return readCount;
	}

	@Override
	public long skip(long n) throws IOException {
		long skip = m_in.skip(n);
		notifyProgress(skip, false);
		return skip;
	}

	@Override
	public int read() throws IOException {
		int read = m_in.read();
		if(read != -1){
			notifyProgress(1, false);
		}
		return read;
	}

	@Override
	public void close() throws IOException {
		m_in.close();
		notifyProgress(0, true);
	}

	public ProgressInputStream addOnSizeListener(Consumer<Long> listener){
		m_onSizeListeners.add(listener);
		return this;
	}

	public ProgressInputStream addOnPercentListener(Consumer<Integer> listener){
		m_onPercentListeners.add(listener);
		return this;
	}

	private void notifyProgress(long readCount, boolean completed){
		if(readCount != -1) {
			m_totalRead += readCount;
			if(m_notifyChunk > -1) {
				if(completed || m_notifyChunk <= m_totalRead - m_lastReported) {
					m_onSizeListeners.forEach(it -> it.accept(m_totalRead));
					m_lastReported = m_totalRead;
				}
			}
			if(! m_onPercentListeners.isEmpty()) {
				if(m_expectedSize < m_totalRead) {
					if(completed) {
						m_expectedSize = m_totalRead;
					}else {
						m_expectedSize = m_totalRead * 2;
					}
				}
				if(m_expectedSize <= 0) {
					m_expectedSize = 1;
				}
				m_percent = (int) Math.round(((1.0 * m_totalRead) / m_expectedSize) * 100);
				if(completed || m_percent != m_lastReportedPercentage) {
					m_onPercentListeners.forEach(it -> it.accept(m_percent));
					m_lastReportedPercentage = m_percent;
				}
			}
		}
	}
}
