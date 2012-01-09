package to.etc.log;

import java.io.*;

/**
 * A writer which outputs data to a Telnet session.
 *
 * <p>Title: Mumble Global Libraries - Non-database tools</p>
 * <p>Description: Small tools for Java programs</p>
 * <p>Copyright: Copyright (c) 2002 Frits Jalvingh; released under the LGPL licence.</p>
 * <p>Website <a href="http://www.mumble.to/">Mumble</a></p>
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 * @version 1.0
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
