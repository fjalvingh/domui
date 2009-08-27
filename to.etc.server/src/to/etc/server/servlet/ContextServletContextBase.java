package to.etc.server.servlet;

import javax.servlet.http.*;

/**
 * Abstract base class for a Context. Implements all but the execute() method.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 11, 2006
 */
abstract public class ContextServletContextBase implements ContextServletContext {
	private ContextServletBase	m_servlet;

	private HttpServletRequest	m_request;

	private HttpServletResponse	m_response;

	private boolean				m_post;

	private WebsiteInfoImpl		m_siteInfo;

	abstract public void execute() throws Exception;

	protected ContextServletContextBase(ContextServletBase servlet, HttpServletRequest request, HttpServletResponse response, boolean post) {
		m_servlet = servlet;
		m_request = request;
		m_response = response;
		m_post = post;
	}

	public void discard() {
	}

	public ContextServletBase getServlet() {
		return m_servlet;
	}

	public HttpServletRequest getRequest() {
		return m_request;
	}

	public HttpServletResponse getResponse() {
		return m_response;
	}

	public boolean isPost() {
		return m_post;
	}

	public void initialize() {
	}

	public WebsiteInfo getSiteInfo() {
		if(m_siteInfo == null)
			m_siteInfo = new WebsiteInfoImpl(getRequest());
		return m_siteInfo;
	}
}
