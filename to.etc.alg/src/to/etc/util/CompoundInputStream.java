package to.etc.util;

import java.io.*;

public class CompoundInputStream extends InputStream {
	final private InputStream[]	m_sourceList;

	private InputStream			m_is;

	private int					m_currentIndex;

	private boolean				m_eof;

	public CompoundInputStream(InputStream... isar) {
		m_sourceList = isar;
		if(isar.length == 0) {
			m_eof = true;
		} else {
			m_is = isar[0];
		}
	}

	@Override
	public int read() throws IOException {
		for(;;) {
			if(atEof())
				return -1;

			int val = m_is.read();
			if(val != -1)
				return val;

			m_currentIndex++;
			if(m_currentIndex >= m_sourceList.length) {
				m_eof = true;
				return -1;
			}
			m_is = m_sourceList[m_currentIndex];
		}
	}

	private boolean atEof() {
		return m_eof;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int szleft = len;
		int read = 0;
		while(szleft > 0) {
			if(atEof()) {
				return read > 0 ? read : -1;
			}
			int szread = m_is.read(b, off, szleft);				// Read as much as possible
			if(szread > 0) {
				off += szread;
				szleft -= szread;
				read += szread;
			} else {
				//-- Next input
				m_currentIndex++;
				if(m_currentIndex >= m_sourceList.length) {
					m_eof = true;
				} else {
					m_is = m_sourceList[m_currentIndex];
				}
			}
		}
		return read;
	}

	@Override
	public long skip(long n) throws IOException {
		throw new IOException("Unsupported");
	}

	@Override
	public void close() throws IOException {
		for(InputStream is : m_sourceList) {
			try {
				is.close();
			} catch(Exception x) {}
		}
	}

	@Override
	public synchronized void mark(int readlimit) {
		throw new IllegalStateException("Unsupported");
	}

	@Override
	public synchronized void reset() throws IOException {
		throw new IOException("Unsupported");
	}

	@Override
	public boolean markSupported() {
		return false;
	}
}
