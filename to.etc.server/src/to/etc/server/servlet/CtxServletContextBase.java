package to.etc.server.servlet;

import javax.servlet.http.*;

public class CtxServletContextBase {
	private CtxServlet			m_servlet;

	private HttpServletRequest	m_request;

	private HttpServletResponse	m_response;

	private String				m_method;


	public CtxServletContextBase(CtxServlet servlet, HttpServletRequest request, HttpServletResponse response, String method) {
		m_servlet = servlet;
		m_request = request;
		m_response = response;
		m_method = method;
	}

	public void exception(Throwable t, String s) {
		m_servlet.exception(t, s);
	}

	public void log(String s) {
		m_servlet.log(s);
	}


	protected void setResponse(HttpServletResponse r, String method) {
		m_response = r;
		m_method = method;
	}

	public CtxServlet getServlet() {
		return m_servlet;
	}

	public HttpServletRequest getRequest() {
		return m_request;
	}

	public HttpServletResponse getResponse() {
		return m_response;
	}

	public String getHttpMethod() {
		return m_method;
	}

	protected void destroy() throws Exception {

	}

	protected void initialize() throws Exception {

	}

	protected Exception doException(Exception x) throws Exception {
		return x;
	}

	protected void doGet() throws Exception {
	}

	protected void doPost() throws Exception {
	}
}
