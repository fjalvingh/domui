package to.etc.domui.trouble;

/**
 * Causes a 404 error to be sent back to the browser.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 4, 2008
 */
public class ThingyNotFoundException extends RuntimeException {
	private String	m_details;

	public ThingyNotFoundException(String message, String details) {
		super(message);
		m_details = details;
	}
	public ThingyNotFoundException(String message) {
		super(message);
	}
	public String getDetails() {
		return m_details;
	}
}
