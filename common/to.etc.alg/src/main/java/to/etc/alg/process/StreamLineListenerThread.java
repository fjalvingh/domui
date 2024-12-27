package to.etc.alg.process;

import org.eclipse.jdt.annotation.NonNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 10-01-2024.
 */
final public class StreamLineListenerThread extends Thread {
	private final Consumer<String> m_lineListener;

	/**
	 * The stream to read,
	 */
	private final Reader m_reader;

	@NonNull
	private final char[] m_buf;

	private StringBuilder m_lineBuffer = new StringBuilder();

	/**
	 * When T this flushes written output.
	 */
	private boolean m_flush;

	public StreamLineListenerThread(Consumer<String> listener, String name, InputStream is, String encoding) {
		m_lineListener = listener;
		m_buf = new char[8192];
		m_flush = true;
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
			while(0 <= (szrd = m_reader.read(m_buf))) {
				//-- Copy all that is read to line buffer and flush when \n is found
				for(int i = 0; i < szrd; i++) {
					char c = m_buf[i];
					m_lineBuffer.append(c);
					if(c == '\n') {
						m_lineListener.accept(m_lineBuffer.toString());
						m_lineBuffer.setLength(0);
					}
				}
			}

			//-- EOF reached; write last part
			if(m_lineBuffer.length() > 0) {
				m_lineListener.accept(m_lineBuffer.toString());
				m_lineBuffer.setLength(0);
			}
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
}
