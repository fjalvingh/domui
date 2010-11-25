package to.etc.telnet;

import java.io.*;

/**
 * A writer which outputs data to a Telnet session.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class TelnetWriter extends Writer {
	private TelnetSession	m_tc;

	protected TelnetWriter(TelnetSession tc) {
		m_tc = tc;
	}

	@Override
	public void write(char[] parm1, int off, int len) throws java.io.IOException {
		try {
			m_tc.write(new String(parm1, off, len));
		} catch(IOException x) {
			throw x;
		} catch(Exception x) {}

	}

	@Override
	public void flush() throws java.io.IOException {
	}

	@Override
	public void close() throws java.io.IOException {
	}

	public TelnetSession getSession() {
		return m_tc;
	}

}
