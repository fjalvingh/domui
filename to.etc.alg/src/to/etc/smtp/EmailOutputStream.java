package to.etc.smtp;

import java.io.*;

/**
 * Escapes lines starting with a '.' to use 2 dots. Replaces
 * all single LF with CRLF.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 24, 2010
 */
public class EmailOutputStream extends OutputStream {
	private OutputStream	m_os;

	private int				m_prev;

	private int				m_col;

	public EmailOutputStream(OutputStream os) {
		m_os = os;
	}

	@Override
	public void write(int b) throws IOException {
		if(b == 0x0d) {
			if(m_prev == 0x0d)
				throw new IllegalStateException("CR CR sequence in stream is invalid");
			m_os.write(b);
		} else if(b == 0x0a) {
			if(m_prev != 0x0d)
				m_os.write(0x0d);
			m_os.write(b);
			m_col = 0;
		} else if(b == '.' && m_col == 0) {
			m_os.write('.');
			m_os.write('.');
			m_col = 1;
		} else {
			m_os.write(b);
			m_col++;
		}
		m_prev = b;
	}
}
