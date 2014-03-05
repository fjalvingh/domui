package to.etc.util;

import java.io.*;

import javax.annotation.*;

/**
 * A character stream wrapped around (part of) a character array.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 3, 2013
 */
public class CharReader extends Reader {
	private final char[]	m_data;

	private int				m_soff;

	private int				m_eoff;

	public CharReader(@Nonnull char[] data) {
		m_data = data;
		m_soff = 0;
		m_eoff = data.length;
	}

	public CharReader(@Nonnull char[] data, int soff, int eoff) {
		m_data = data;
		m_soff = soff;
		m_eoff = eoff;
	}

	@Override
	public int read() throws IOException {
		if(m_soff >= m_eoff)
			return -1;
		return m_data[m_soff++] & 0xffff;
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		int rlen = m_eoff - m_soff;						// This much we have left,
		if(rlen <= 0)
			return -1;
		if(rlen > len)
			rlen = len;
		System.arraycopy(m_data, m_soff, cbuf, off, rlen);
		m_soff += rlen;
		return rlen;
	}

	@Override
	public void close() throws IOException {
	}
}
