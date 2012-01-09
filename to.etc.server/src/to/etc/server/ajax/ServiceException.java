package to.etc.server.ajax;

import java.util.*;

import javax.servlet.http.*;

import to.etc.server.servlet.*;

/**
 * Any kind of service requested thru a generic container.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 14, 2006
 */
public class ServiceException extends Exception {
	/** The incoming request URL, if applicable. */
	private String					m_url;

	/** The request type */
	private String					m_method;

	/** The query string if this was a get */
	private String					m_queryString;

	/** The incoming parameters, */
	private Map<String, String[]>	m_parameters;

	private Map<String, String[]>	m_headers;

	/** The servlet that is supposed to handle this */
	private String					m_servletPath;

	/** The remote address for this thingy. */
	private String					m_remoteAddress;

	private String					m_remoteUser;

	private HttpServlet				m_servlet;

	public ServiceException(RequestContext ctx, String message) {
		super(message);
		init(ctx);
	}

	public ServiceException(RequestContext ctx, String message, Throwable cause) {
		super(message, cause);
		init(ctx);
	}

	public ServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServiceException(String message) {
		super(message);
	}

	public boolean hasContext() {
		return m_method != null;
	}

	/**
	 * Retrieve info on the call from the context passed.
	 * @param ctx
	 */
	private void init(RequestContext ctx) {
		init(ctx.getRequest());
	}

	/**
	 * Retrieve info on the call from the context passed.
	 * @param ctx
	 */
	private void init(HttpServletRequest req) {
		m_url = req.getRequestURI();
		m_queryString = req.getQueryString();
		m_method = req.getMethod();
		m_servletPath = req.getServletPath();
		m_remoteAddress = req.getRemoteHost();
		m_remoteUser = req.getRemoteUser();
		m_parameters = new HashMap(req.getParameterMap());
		m_headers = new HashMap<String, String[]>();
		ArrayList<String> al = new ArrayList<String>();
		for(Enumeration e = req.getHeaderNames(); e.hasMoreElements();) {
			String name = (String) e.nextElement();
			for(Enumeration ve = req.getHeaders(name); ve.hasMoreElements();)
				al.add((String) ve.nextElement());
			m_headers.put(name, al.toArray(new String[al.size()]));
			al.clear();
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Getters and setters.								*/
	/*--------------------------------------------------------------*/
	public Map<String, String[]> getHeaders() {
		return m_headers;
	}

	public void setHeaders(Map<String, String[]> headers) {
		m_headers = headers;
	}

	public String getMethod() {
		return m_method;
	}

	public void setMethod(String method) {
		m_method = method;
	}

	public Map<String, String[]> getParameters() {
		return m_parameters;
	}

	public void setParameters(Map<String, String[]> parameters) {
		m_parameters = parameters;
	}

	public String getQueryString() {
		return m_queryString;
	}

	public void setQueryString(String queryString) {
		m_queryString = queryString;
	}

	public String getRemoteAddress() {
		return m_remoteAddress;
	}

	public void setRemoteAddress(String remoteAddress) {
		m_remoteAddress = remoteAddress;
	}

	public String getRemoteUser() {
		return m_remoteUser;
	}

	public void setRemoteUser(String remoteUser) {
		m_remoteUser = remoteUser;
	}

	public String getServletPath() {
		return m_servletPath;
	}

	public void setServletPath(String servlet) {
		m_servletPath = servlet;
	}

	public String getUrl() {
		return m_url;
	}

	public void setUrl(String url) {
		m_url = url;
	}

	public void setServlet(HttpServlet servlet) {
		m_servlet = servlet;
	}

	public HttpServlet getServlet() {
		return m_servlet;
	}

	public void setContext(RequestContext ctx) {
		if(m_url != null)
			return;
		init(ctx);
	}
}
