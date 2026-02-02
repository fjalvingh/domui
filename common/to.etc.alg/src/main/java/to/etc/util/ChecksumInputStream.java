package to.etc.util;

import to.etc.function.ConsumerEx;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 * Wraps an input stream, and calculates a hash and the size
 * of all bytes read.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 07-09-2023.
 */
final public class ChecksumInputStream extends InputStream {
	private final InputStream m_is;

	private final ConsumerEx<ChecksumInputStream> m_onBytesRead;

	private final MessageDigest m_digest;

	private long m_sizeRead;

	private byte[] m_byte = new byte[1];

	public ChecksumInputStream(InputStream is, ConsumerEx<ChecksumInputStream> onBytesRead) throws Exception {
		m_is = is;
		m_onBytesRead = onBytesRead;
		m_digest = MessageDigest.getInstance("MD5");
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int szrd = m_is.read(b, off, len);
		if(szrd < 0)
			return szrd;
		synchronized(this) {
			m_sizeRead += szrd;
			m_digest.update(b, off, szrd);
		}
		try {
			m_onBytesRead.accept(this);
		} catch(Exception x) {
			throw WrappedException.wrap(x);
		}
		return szrd;
	}

	@Override
	public int read() throws IOException {
		int b = m_is.read();
		if(b == -1)
			return b;
		m_byte[0] = (byte) b;
		synchronized(this) {
			m_sizeRead += 1;
			m_digest.update(m_byte, 0, 1);
		}
		return b;
	}

	@Override
	public void close() throws IOException {
		m_is.close();
	}

	/**
	 *
	 */
	public synchronized byte[] getHash() {
		return m_digest.digest();
	}

	public synchronized long getSize() {
		return m_sizeRead;
	}
}
