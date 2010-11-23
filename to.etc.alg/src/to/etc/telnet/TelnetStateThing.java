package to.etc.telnet;

/**
 * Known about states for MultiThread-safe objects.
 *
 * <p>Title: Mumble Global Libraries - Non-database tools</p>
 * <p>Description: Small tools for Java programs</p>
 * <p>Copyright: Copyright (c) 2002 Frits Jalvingh; released under the LGPL licence.</p>
 * <p>Website <a href="http://www.mumble.to/">Mumble</a></p>
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 * @version 1.0
 */
public class TelnetStateThing {
	static public final int	tsNONE		= 0;

	static public final int	tsRUN		= 1;

	static public final int	tsSHUT		= 2;

	static public final int	tsDOWN		= 3;

	static public final int	tsINITING	= 4;

	/// State of the server..
	private int				m_state;

	public TelnetStateThing() {
		m_state = tsNONE;
	}

	/**
	 *	Returns T if the server is in a given state.
	 */
	public synchronized boolean inState(int st) {
		return m_state == st;
	}

	/**
	 *	Sets the server's state,
	 */
	public synchronized void setState(int st) {
		m_state = st;
	}

	/**
	 *	Returns the server's state.
	 */
	public synchronized int getState() {
		return m_state;
	}

	public String getStateString() {
		switch(m_state){
			default:
				return "?? Unknown";
			case tsNONE:
				return "none";
			case tsRUN:
				return "running";
			case tsSHUT:
				return "being shut down";
			case tsDOWN:
				return "down";
			case tsINITING:
				return "initializing";
		}
	}

}
