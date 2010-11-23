package to.etc.telnet;

import to.etc.log.*;

/**
 * A PrintWriter which writes to a telnet session, and which knows about the
 * session.
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * @version 1.0
 */
public class TelnetPrintWriter extends ExtendedPrintWriter {
	private TelnetWriter	m_tw;

	public TelnetPrintWriter(TelnetWriter tw) {
		super(tw);
		m_tw = tw;
	}

	public TelnetSession getSession() {
		return m_tw.getSession();
	}
}
