package to.etc.telnet;

/**
 * A PrintWriter which writes to a telnet session, and which knows about the
 * session.
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
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
