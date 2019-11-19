package to.etc.domui.server;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 24-10-19.
 */
public class PageUrlPatternException extends RuntimeException {
	private final String m_segment;

	public PageUrlPatternException(String segment, String message) {
		super(message);
		m_segment = segment;
	}

	public String getSegment() {
		return m_segment;
	}
}
