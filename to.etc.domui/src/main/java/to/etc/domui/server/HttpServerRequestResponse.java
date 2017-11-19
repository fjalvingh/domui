package to.etc.domui.server;

import to.etc.domui.util.upload.*;
import to.etc.net.*;
import to.etc.webapp.core.*;

import javax.annotation.*;
import javax.servlet.http.*;
import java.io.*;

public class HttpServerRequestResponse implements IRequestResponse {
	@Nonnull
	final private HttpServletRequest m_request;

	@Nonnull
	final private HttpServletResponse m_response;

	@Nonnull
	final private String m_webappContext;

	private IServerSession m_serverSession;

	private HttpServerRequestResponse(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull String webappContext) {
		m_request = request;
		m_response = response;
		m_webappContext = webappContext;
	}

	@Override
	@Nonnull
	public String getRequestURI() {
		return m_request.getRequestURI();
	}

	@Override
	@Nonnull
	public String getQueryString() {
		return m_request.getQueryString();
	}

	@Nonnull
	public HttpServletRequest getRequest() {
		return m_request;
	}

	@Nonnull
	public HttpServletResponse getResponse() {
		return m_response;
	}

	@Override
	@Nonnull
	public String getWebappContext() {
		return m_webappContext;
	}

	@Nonnull
	static public HttpServerRequestResponse create(@Nonnull DomApplication application, @Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response) {
		String webapp = request.getContextPath();
		if(webapp == null)
			webapp = "";
		else {
			if(webapp.startsWith("/"))
				webapp = webapp.substring(1);
			if(!webapp.endsWith("/"))
				webapp = webapp + "/";
		}

		//-- Wrap request with multipart code if needed
		String requesturi = request.getRequestURI();
		int pos = requesturi.lastIndexOf('.');
		String extension = pos < 0 ? "" : requesturi.substring(pos + 1).toLowerCase();

		HttpServletRequest realrequest = request;
		if(application.getUrlExtension().equals(extension) || requesturi.contains(".part")) // QD Fix for upload
			realrequest = UploadParser.wrapIfNeeded(request); 								// Make multipart wrapper if multipart/form-data

//		for(Enumeration<String> en = m_request.getHeaderNames(); en.hasMoreElements();) {
//			String name = en.nextElement();
//			System.out.println("Header: "+name);
//			for(Enumeration<String> en2 = m_request.getHeaders(name); en2.hasMoreElements();) {
//				String val = en2.nextElement();
//				System.out.println("     ="+val);
//			}
//		}

		return new HttpServerRequestResponse(realrequest, response, webapp);
	}

	@Override
	public void releaseUploads() {
		if(!(m_request instanceof UploadHttpRequestWrapper))
			return;

		UploadHttpRequestWrapper w = (UploadHttpRequestWrapper) m_request;
		w.releaseFiles();
	}

	@Override
	@Nonnull
	public String getUserAgent() {
		return m_request.getHeader("user-agent");
	}

	@Override
	@Nonnull
	public String getApplicationURL() {
		String appUrl = DomApplication.get().getApplicationURL();
		if(null != appUrl) {
			return appUrl;
		}

		return NetTools.getApplicationURL(getRequest());
	}

	@Override
	@Nonnull
	public String getHostURL() {
		String appUrl = DomApplication.get().getApplicationURL();
		if(null != appUrl) {
			int ix = appUrl.indexOf('/', 8);			// After http and https, first /
			if(ix == -1)
				return appUrl;
			return appUrl.substring(0, ix + 1);
		}
		return NetTools.getHostURL(getRequest());
	}

	@Override
	@Nonnull
	public String getHostName() {
		String hostName = DomApplication.get().getHostName();
		if(null != hostName)
			return hostName;
		return NetTools.getHostName(m_request);
	}

	/**
	 * @see to.etc.domui.server.IRequestContext#getParameter(java.lang.String)
	 */
	@Override
	@Nullable
	public String getParameter(@Nonnull String name) {
		return getRequest().getParameter(name);
	}

	/**
	 * @see to.etc.domui.server.IRequestContext#getParameters(java.lang.String)
	 */
	@Override
	@Nonnull
	public String[] getParameters(@Nonnull String name) {
		return getRequest().getParameterValues(name);
	}

	/**
	 * @see to.etc.domui.server.IRequestContext#getParameterNames()
	 */
	@Override
	@Nonnull
	public String[] getParameterNames() {
		return (String[]) getRequest().getParameterMap().keySet().toArray(new String[getRequest().getParameterMap().size()]);
	}

	/**
	 * Returns the names of all file parameters.
	 * @return
	 */
	@Override
	@Nonnull
	public String[] getFileParameters() throws Exception {
		if(!(m_request instanceof UploadHttpRequestWrapper))
			return new String[0];
		UploadHttpRequestWrapper urw = (UploadHttpRequestWrapper) m_request;
		return urw.getFileItemMap().keySet().toArray(new String[urw.getFileItemMap().size()]);
	}

	@Override
	@Nonnull
	public UploadItem[] getFileParameter(@Nonnull String name) throws Exception {
		if(!(m_request instanceof UploadHttpRequestWrapper))
			return new UploadItem[0];
		UploadHttpRequestWrapper urw = (UploadHttpRequestWrapper) m_request;
		return urw.getFileItems(name);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Output												*/
	/*--------------------------------------------------------------*/

	@Override
	public void setNoCache() {
		ServerTools.generateNoCache(getResponse());
	}

	@Override
	public void setExpiry(int cacheTime) {
		ServerTools.generateExpiryHeader(getResponse(), cacheTime); 						// Allow browser-local caching.
	}

	@Override
	@Nonnull
	public Writer getOutputWriter(@Nonnull String contentType, @Nullable String encoding) throws IOException {
		getResponse().setContentType(contentType);
		if(null != encoding)
			getResponse().setCharacterEncoding(encoding);
		return getResponse().getWriter();
	}

	@Override
	@Nonnull
	public OutputStream getOutputStream(@Nonnull String contentType, @Nullable String encoding, int contentLength) throws Exception {
		getResponse().setContentType(contentType);
		if(null != encoding)
			getResponse().setCharacterEncoding(encoding);
		if(contentLength > 0)
			getResponse().setContentLength(contentLength);
		return getResponse().getOutputStream();
	}

	/**
	 * Send a redirect response to the client.
	 * @param newUrl
	 */
	@Override
	public void redirect(@Nonnull String newUrl) throws Exception {
		getResponse().sendRedirect(newUrl);
	}

	@Override
	public void sendError(int httpErrorCode, @Nonnull String message) throws Exception {
		getResponse().sendError(httpErrorCode, message);
	}

	@Override
	public void addHeader(@Nonnull String name, @Nonnull String value) {
		getResponse().addHeader(name, value);
	}


	@Override
	public void addCookie(@Nonnull Cookie cookie) {
		cookie.setDomain(getHostName());
		getResponse().addCookie(cookie);
	}

	@Override
	@Nonnull
	public Cookie[] getCookies() {
		return getRequest().getCookies();
	}

	@Override
	@Nullable
	public String getRemoteUser() {
		return getRequest().getRemoteUser();
	}

	@Override
	@Nullable
	public IServerSession getServerSession(boolean create) {
		IServerSession ss = m_serverSession;
		if(ss == null) {
			HttpSession ses = getRequest().getSession(create);
			if(null != ses) {
				ss = new HttpServerSession(ses);
				m_serverSession = ss;
			}
		}
		return ss;
	}

	@Nullable
	public static HttpServerRequestResponse get(@Nonnull IRequestContext ctx) {
		IRequestResponse rr = ctx.getRequestResponse();
		if(rr instanceof HttpServerRequestResponse)
			return (HttpServerRequestResponse) rr;
		return null;
	}
}
