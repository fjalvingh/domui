package to.etc.alg.process;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * This is used to async read stdout and stderr streams from a process into another output stream.
 */
public class StreamCopyThread extends Thread {
	/**
	 * The stream to read,
	 */
	private InputStream m_is;

	private OutputStream m_os;

	private final byte[] m_buf;

	public StreamCopyThread(final OutputStream os, String name, InputStream is) {
		m_os = os;
		m_is = is;
		m_buf = new byte[1024];
		setName("StreamReader" + name);
	}

	/**
	 * Read data from the stream until it closes line by line; add each line to
	 * the output channel.
	 */
	@Override
	public void run() {
		try {
			int szrd;
			while(0 < (szrd = m_is.read(m_buf))) {
				m_os.write(m_buf, 0, szrd);
			}
			m_os.flush();
		} catch(Throwable x) {
			x.printStackTrace();
		} finally {
			try {
				if(m_is != null)
					m_is.close();
			} catch(Exception x) {
			}
		}
	}
}
