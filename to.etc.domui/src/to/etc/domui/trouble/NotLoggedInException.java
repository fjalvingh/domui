package to.etc.domui.trouble;

/**
 * Thrown when access control is specified on a page but the user is not logged in.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 15, 2009
 */
public class NotLoggedInException extends RuntimeException {
	private final String m_url;

	public NotLoggedInException(final String url) {
		super("You need to be logged in");
		m_url = url;
	}

	public String getURL() {
		return m_url;
	}
}
