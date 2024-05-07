package to.etc.alg.process;

import org.eclipse.jdt.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 * This is used to async read strout and stderr streams from a process...
 */
public class StreamReaderThread extends Thread {
	/**
	 * The stream to read,
	 */
	private Reader m_reader;

	/**
	 * The output writer thing.
	 */
	private final Writer m_w;

	@NonNull
	private final char[] m_buf;

	/**
	 * When T this flushes written output.
	 */
	private boolean m_flush;

	private IFollow m_follow;

	public StreamReaderThread(final Appendable sb, String name, InputStream is) {
		this(sb, name, is, System.getProperty("file.encoding"));
	}

	public StreamReaderThread(final Appendable sb, String name, InputStream is, String encoding) {
		this(new Writer() {
			@Override
			public void write(char[] cbuf, int off, int len) throws IOException {
				while(len-- > 0)
					sb.append(cbuf[off++]);
			}

			@Override
			public void flush() throws IOException {
			}

			@Override
			public void close() throws IOException {
			}
		}, name, is, encoding, null, false);
	}

	public StreamReaderThread(Writer sb, String name, InputStream is) {
		this(sb, name, is, System.getProperty("file.encoding"), null, false);
	}

	public StreamReaderThread(Writer w, String name, InputStream is, String encoding, IFollow follow, boolean flush) {
		m_w = w;
		m_buf = new char[8192];
		m_follow = follow;
		m_flush = flush;
		setName("StreamReader" + name);
		if(null == encoding)
			encoding = System.getProperty("file.encoding");
		try {
			m_reader = new InputStreamReader(is, encoding);
		} catch(UnsupportedEncodingException x) {
			throw new IllegalStateException("Unsupported encoding " + encoding);
		}
	}

	/**
	 * Read data from the stream until it closes line by line; add each line to
	 * the output channel.
	 */
	@Override
	public void run() {
		try {
			int szrd;
			IFollow follow = m_follow;
			while(0 <= (szrd = m_reader.read(m_buf))) {
				m_w.write(m_buf, 0, szrd);
				if(m_flush) {
					if(szrd > 512 || needsFlush(m_buf, szrd))
						m_w.flush();
				}
				if(null != follow) {
					try {
						m_follow.newData(false, m_buf, szrd);
					} catch(Exception x) {
						x.printStackTrace();
					}
				}
			}
			m_w.flush();
		} catch(Throwable x) {
			x.printStackTrace();
		} finally {
			try {
				if(m_reader != null)
					m_reader.close();
			} catch(Exception x) {
			}
		}
	}

	static private boolean needsFlush(@NonNull char[] buf, int szrd) {
		while(--szrd >= 0) {
			if(buf[szrd] == '\n')
				return true;
		}
		return false;
	}
}
