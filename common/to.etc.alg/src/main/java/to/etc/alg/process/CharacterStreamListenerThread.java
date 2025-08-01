package to.etc.alg.process;

import org.eclipse.jdt.annotation.NonNullByDefault;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * This listens for data to be appended to an InputStream,
 * and as soon as data appears it will read it and call a listener
 * with that data. This can be used to tail a file.
 *
 * Because this tails a stream it never exits by itself! Only
 * when close() gets called will it try to exit by reading all
 * that remains and then exiting the reader thread.
 */
@NonNullByDefault
final public class CharacterStreamListenerThread implements AutoCloseable{
	private final InputStream m_inputStream;

	private final InputStreamReader m_reader;

	private final IReadListener m_listener;

	private final Charset m_encoding;

	private final Thread m_readerThread;

	private volatile boolean m_terminate;

	public interface IReadListener {
		void data(char[] buffer, int len) throws Exception;
	}

	/**
	 * Create a tailer that calls a listener every time data gets added
	 * to the input stream.
	 */
	public CharacterStreamListenerThread(InputStream inputStream, Charset encoding, IReadListener listener) {
		m_inputStream = inputStream;
		m_reader = new InputStreamReader(m_inputStream, encoding);
		m_listener = listener;
		m_encoding = encoding;

		m_readerThread = new Thread(this::readerLoop);
		m_readerThread.setName("CharacterStreamListenerThread");
		m_readerThread.setDaemon(true);
		m_readerThread.start();
	}

	/**
	 * Append all data added to the input stream to the StringBuilder.
	 */
	public CharacterStreamListenerThread(InputStream inputStream, Charset encoding, StringBuilder appendable) {
		this(inputStream, encoding, new IReadListener() {
			@Override
			public void data(char[] buffer, int len) throws Exception {
				appendable.append(buffer, 0, len);
			}
		});
	}

	private void readerLoop() {
		char[] buffer = new char[1024];
		try {
			for(; ; ) {
				//-- Is there data to read?
				int read = m_reader.read(buffer);
				if(read > 0) {
					m_listener.data(buffer, read);
				} else if(m_terminate) {
					break;
				} else {
					Thread.sleep(500);					// No other real thing to do, sadly enough
				}
			}
		} catch(Exception x) {
			x.printStackTrace();
		}
	}

	@Override
	public void close() throws Exception {
		if(m_terminate)								// Already closed
			return;

		m_terminate = true;
		try {
			m_readerThread.join(20_000);		// Wait 20 seconds to make the reader stop
			if(m_readerThread.isAlive()) {
				m_readerThread.interrupt();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		m_reader.close();
	}
}
