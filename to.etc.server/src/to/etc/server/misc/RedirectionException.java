package to.etc.server.misc;

/**
 *	This is a special exception base class that is used when a page decides that
 *  a special redirection is required. For instance when a helper class needs
 *  an authenticated user ID it can throw this exception when the ID is not known,
 *  redirecting the browser to the login pages.
 *  Actual implementations derive a class from this class which will deliver the
 *  actual URL to go to, and which specifies whether a redirect or a regenerate
 *  is needed.
 */
public class RedirectionException extends ActionException {
	/** T if this is a redirection; F if this is a regenerate. */
	private boolean	m_redir;

	/** The (relative?) URL to go to / to generate. */
	private String	m_url;

	public RedirectionException(String msg, String where, boolean redirect) {
		super(msg);
		m_url = where;
		m_redir = redirect;
	}

	public boolean isRedirection() {
		return m_redir;
	}

	public String getURL() {
		return m_url;
	}
}
