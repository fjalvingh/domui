package to.etc.util;

import java.io.*;


/**
 *	The complement of HtmlStringOutputStream, this reads a string encoded by that
 *  class as a kind of base64 encoded data stream and allows one to retrieve
 *  values from the stream.
 */
public class HtmlStringInputStream extends InputStream {
	/// The string being considered
	private String	m_s;

	/// The current index within the string
	private int		m_ix;

	/// The current bit buffer to retrieve chars from
	private int		m_bb;

	/// The current #of bits in the bit buffer.
	private int		m_n_bits;

	/// T if eof was reached.
	private boolean	m_eof;

	public HtmlStringInputStream(String s) {
		if(s == null || s.length() == 0)
			m_eof = true;
		m_s = s;
	}

	@Override
	public int read() throws java.io.IOException {
		if(m_eof)
			return -1;

		//-- Now get 8 bits in the buffer,
		while(m_n_bits < 8) {
			//-- Get a character,
			if(m_ix >= m_s.length()) // End of string?
			{
				m_eof = true;
				return -1; // And be done!
			}
			char c = m_s.charAt(m_ix++); // Get the character,

			//-- Decode according to the encode table
			int cv = HtmlStringOutputStream.BASECODES.indexOf(c);
			if(cv == -1) // Bad character?
			{
				m_eof = true; // Fake EOF,
				throw new IOException("Bad encoding in HTML data stream");
			}

			//-- Add this char to the buffer.
			cv <<= m_n_bits; // Shift to correct pos
			m_bb |= cv; // And include in the buffer
			m_n_bits += 6; // And 6 bits more!
		}

		//-- Get one byte from the buffer,
		int v = m_bb & 0xff; // Get a byte.
		m_n_bits -= 8; // Less there,
		m_bb >>= 8;
		//		System.out.println("rd: value="+Integer.toHexString(v));
		return v;
	}

	@Override
	public void close() throws java.io.IOException {
		m_eof = true;
		super.close();
	}
}
