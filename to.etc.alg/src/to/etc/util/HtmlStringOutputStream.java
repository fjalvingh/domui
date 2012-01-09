package to.etc.util;

import java.io.*;

/**
 *	This is an outputstream that accepts data and encodes it in kind of a base64
 *  format into a string. This string can be sent to an HTML page and can be
 *  read later on, allowing a DataOutputStream to save data into a HTML parameter
 *  or input field.
 */
public class HtmlStringOutputStream extends OutputStream {
	static protected final String	BASECODES	= "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz$_";

	/// The stringbuffer we collect data in,
	private StringBuffer			m_sb;

	/// T if this stream was closed.
	private boolean					m_isclosed;

	/// The byte collector buffer.
	private int						m_bb;

	/// The #of bits currently in the byte buffer.
	private int						m_n_bits;

	/**
	 *	Create a stream accepting data.
	 */
	public HtmlStringOutputStream() {
		m_sb = new StringBuffer();
	}

	/**
	 *	Create a stream accepting data in the buffer specified.
	 */
	public HtmlStringOutputStream(StringBuffer sb) {
		m_sb = sb;
	}

	/**
	 *	Create a stream accepting data, the expected output size is passed.
	 *  @param alloc	The expected #of chars needed in the buffer.
	 */
	public HtmlStringOutputStream(int alloc) {
		m_sb = new StringBuffer(alloc);
	}

	private void flushBb() {
		m_isclosed = true;
		if(m_n_bits == 0)
			return;

		//-- Flush the last bits to the stream.
		int mask = (1 << m_n_bits) - 1; // Get a mask for the bits
		int bc = (m_bb & mask); // Get bits of the value,
		m_sb.append(BASECODES.charAt(bc)); // Add the char,
	}


	/**
	 *	Write the byte in b to the stream.
	 */
	@Override
	public void write(int b) throws java.io.IOException {
		//		System.out.println("WT: Byte = "+Integer.toHexString(b));

		b &= 0xff; // Make sure it's a byte,
		b <<= m_n_bits; // Add to the bytebuffer,
		m_bb |= b; // Include the value
		m_n_bits += 8;
		while(m_n_bits >= 6) // Do we have data?
		{
			int bc = (m_bb & 0x3f); // Get 6 bits of the value,
			m_sb.append(BASECODES.charAt(bc)); // Add the char,
			m_n_bits -= 6;
			m_bb >>= 6;
		}
	}


	@Override
	public void close() throws java.io.IOException {
		if(m_isclosed)
			return;
		flushBb();
		super.close();
	}

	@Override
	public String toString() {
		if(!m_isclosed)
			throw new IllegalStateException("HtmlStringOutputStream not closed");
		return m_sb.toString();
	}
}
