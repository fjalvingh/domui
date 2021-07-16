package to.etc.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class ProgressOutputStream extends OutputStream {

	private OutputStream m_out;
	private long m_expectedSize = -1;
	private long m_notifyChunk = 1024 * 1024;
	private long m_lastReported = 0;
	private double m_lastReportedPercentage = 0.0;
	private long m_totalRead = 0;
	private java.util.List<Consumer<Long>> m_onSizeListeners = new CopyOnWriteArrayList<>();
	private java.util.List<Consumer<Integer>> m_onPercentListeners = new CopyOnWriteArrayList<>();
	private int m_percent = 0;

	public ProgressOutputStream(OutputStream outputStream, int expectedSize) {
		m_out = outputStream;
		m_expectedSize = expectedSize;
	}

	public ProgressOutputStream(OutputStream outputStream, int expectedSize, int notifyChunk) {
		m_out = outputStream;
		m_expectedSize = expectedSize;
		m_notifyChunk = notifyChunk;
	}

	public ProgressOutputStream(OutputStream outputStream) {
		m_out = outputStream;
	}

	@Override
	public void write(int b) throws IOException {
		m_out.write(b);
		notifyProgress(1, false);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		m_out.write(b, off, len);
		notifyProgress(len, false);
	}

	@Override
	public void write(byte[] b) throws IOException {
		m_out.write(b);
		notifyProgress(b.length, false);
	}

	@Override
	public void flush() throws IOException {
		m_out.flush();
	}

	@Override
	public void close() throws IOException {
		m_out.close();
		notifyProgress(0, true);
	}

	public ProgressOutputStream addOnSizeListener(Consumer<Long> listener){
		m_onSizeListeners.add(listener);
		return this;
	}

	public ProgressOutputStream addOnPercentListener(Consumer<Integer> listener){
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
