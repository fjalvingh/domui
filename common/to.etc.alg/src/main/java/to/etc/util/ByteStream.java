package to.etc.util;

import org.eclipse.jdt.annotation.NonNull;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 11-01-23.
 */
public class ByteStream extends InputStream {
	final private InputStream m_in;

	private final byte[] m_buffer = new byte[4];

	public ByteStream(InputStream in) {
		m_in = in;
	}

	public ByteStream(byte[] data) {
		m_in = new ByteArrayInputStream(data);
	}

	public ByteStream(byte[] data, int offset, int len) {
		m_in = new ByteArrayInputStream(data, offset, len);
	}

	/**
	 * Reads a 4-byte bigendian int off the connection.
	 */
	public int readInt() throws IOException {
		int len = m_in.read(m_buffer, 0, 4);
		if(len != 4)
			throw new EOFException();
		return ((m_buffer[0] & 0xff) << 24) | ((m_buffer[1] & 0xff) << 16) | ((m_buffer[2] & 0xff) << 8) | (m_buffer[3] & 0xff);
	}

	/**
	 * Reads a 2-byte bigendian int off the connection.
	 */
	public int readShort() throws IOException {
		int len = m_in.read(m_buffer, 0, 2);
		if(len != 4)
			throw new EOFException();
		return ((m_buffer[0] & 0xff) << 8) | (m_buffer[1] & 0xff);
	}

	public long readLong() throws IOException {
		int v1 = readInt();
		int v2 = readInt();
		return (long) v1 << 32 | ((long) v2 & 0xffffffff);
	}

	public byte[] readBytes(int count) throws IOException {
		byte[] data = new byte[count];
		int read = m_in.read(data);
		if(read != count)
			throw new EOFException("EOF reading " + count + " bytes (got " + read + ")");
		return data;
	}

	public byte[] readIntBytes() throws IOException {
		int len = readInt();
		return readIntBytes(len);
	}

	public byte[] readShortBytes() throws IOException {
		int len = readShort();
		return readIntBytes(len);
	}

	public byte[] readByteBytes() throws IOException {
		int len = read();
		if(len == -1)
			throw new EOFException();
		return readIntBytes(len);
	}


	private byte[] readIntBytes(int len) throws IOException {
		if(len < 0 || len > 1024 * 1024 * 100)
			throw new IOException("String length incorrect (" + len + ")");
		return readBytes(len);
	}

	public String readIntString(Charset encoding) throws IOException {
		int len = readInt();
		return readString(encoding, len);
	}

	public String readShortString(Charset encoding) throws IOException {
		int len = readShort();
		return readString(encoding, len);
	}

	public String readByteString(Charset encoding) throws IOException {
		int len = read();
		if(len == -1)
			throw new EOFException();
		return readString(encoding, len);
	}

	@NonNull
	private String readString(Charset encoding, int len) throws IOException {
		if(len == 0)
			return "";

		if(len < 0 || len > 1024 * 1024 * 100)
			throw new IOException("String length incorrect (" + len + ")");
		byte[] data = readBytes(len);
		return new String(data, encoding);
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	Delegates													*/
	/*----------------------------------------------------------------------*/

	@Override
	public int read() throws IOException {
		return m_in.read();
	}

	@Override
	public int read(byte[] b) throws IOException {
		return m_in.read(b);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return m_in.read(b, off, len);
	}

	@Override
	public byte[] readAllBytes() throws IOException {
		return m_in.readAllBytes();
	}

	@Override
	public byte[] readNBytes(int len) throws IOException {
		return m_in.readNBytes(len);
	}

	@Override
	public int readNBytes(byte[] b, int off, int len) throws IOException {
		return m_in.readNBytes(b, off, len);
	}

	@Override
	public long skip(long n) throws IOException {
		return m_in.skip(n);
	}

	@Override
	public int available() throws IOException {
		return m_in.available();
	}

	@Override
	public void close() throws IOException {
		m_in.close();
	}

	@Override
	public void mark(int readlimit) {
		m_in.mark(readlimit);
	}

	@Override
	public void reset() throws IOException {
		m_in.reset();
	}

	@Override
	public boolean markSupported() {
		return m_in.markSupported();
	}

	@Override
	public long transferTo(OutputStream out) throws IOException {
		return m_in.transferTo(out);
	}
}
