package to.etc.util;

import to.etc.function.ConsumerEx;

import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;

/**
 * Wraps an output stream, and calculates a hash and the size
 * of all bytes written.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-07-2023.
 */
final public class ChecksumOutputStream extends OutputStream {
	private final OutputStream m_os;

	private final ConsumerEx<ChecksumOutputStream> m_onBytesWritten;

	private final MessageDigest m_digest;

	private long m_sizeWritten;

	private byte[] m_byte = new byte[1];

	public ChecksumOutputStream(OutputStream os, ConsumerEx<ChecksumOutputStream> onBytesWritten) throws Exception {
		m_os = os;
		m_onBytesWritten = onBytesWritten;
		m_digest = MessageDigest.getInstance("MD5");
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		m_os.write(b, off, len);
		synchronized(this) {
			m_sizeWritten += len;
			m_digest.update(b, off, len);
		}
		try {
			m_onBytesWritten.accept(this);
		} catch(Exception x) {
			throw WrappedException.wrap(x);
		}
	}

	@Override
	public void write(int b) throws IOException {
		m_byte[0] = (byte) b;
		write(m_byte, 0, 1);
	}

	@Override
	public void close() throws IOException {
		m_os.close();
	}

	/**
	 *
	 */
	public synchronized byte[] getHash() {
		return m_digest.digest();
	}

	public synchronized long getSize() {
		return m_sizeWritten;
	}
}
