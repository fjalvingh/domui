package to.etc.util;

import java.io.*;

/**
 * This is an outputstream which accepts data and sends it to the bit
 * bucket (no data is stored). The amount of data sent to the stream is
 * counted though. Although this class specified throws thingies it never
 * throws an exception (throwing data away is never an error ;-)
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class NullOutputStream extends OutputStream {
	/** The #of bytes currently written. */
	public long	m_sz_written;


	public NullOutputStream() {
	}

	@Override
	public void write(byte[] parm1) throws java.io.IOException {
		m_sz_written += parm1.length;
	}

	@Override
	public void write(int b) throws java.io.IOException {
		m_sz_written++;
	}

	@Override
	public void write(byte[] parm1, int off, int len) throws java.io.IOException {
		m_sz_written += len;
	}

	/**
	 *	Returns the #bytes currently written.
	 */
	public long getSzWritten() {
		return m_sz_written;
	}

	/**
	 *	Resets the size written.
	 */
	public void reset() {
		m_sz_written = 0;
	}
}
