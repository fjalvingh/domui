package to.etc.domui.server;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.util.upload.UploadHttpRequestWrapper;
import to.etc.domui.util.upload.UploadItem;
import to.etc.domui.util.upload.UploadParser;
import to.etc.net.NetTools;
import to.etc.webapp.core.ServerTools;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpServerRequestResponse implements IRequestResponse {
	@NonNull
	final private HttpServletRequest m_request;

	@NonNull
	final private HttpServletResponse m_response;

	@NonNull
	final private String m_webappContext;

	private Map<String, String[]> m_parameterMap;

	private Map<String, List<String>> m_headerMap;

	private IServerSession m_serverSession;

	private HttpServerRequestResponse(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull String webappContext, Map<String, String[]> parameterMap,
		Map<String, List<String>> headerMap) {
		m_request = request;
		m_response = response;
		m_webappContext = webappContext;
		m_parameterMap = parameterMap;
		m_headerMap = headerMap;
	}

	@Override
	@NonNull
	public String getRequestURI() {
		return XssChecker.stripXSS(m_request.getRequestURI());
	}

	@Override
	@NonNull
	public String getQueryString() {
		return XssChecker.stripXSS(m_request.getQueryString());
	}

	@NonNull
	public HttpServletRequest getRequest() {
		return m_request;
	}

	@NonNull
	public HttpServletResponse getResponse() {
		return m_response;
	}

	@Override
	@NonNull
	public String getWebappContext() {
		return m_webappContext;
	}

	@NonNull
	static public HttpServerRequestResponse create(@NonNull DomApplication application, @NonNull HttpServletRequest request, @NonNull HttpServletResponse response) {
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

		//-- Check all parameters for xss issues
		Map<String, String[]> paramMap = new HashMap<>();
		realrequest.getParameterMap().forEach((name, values) -> {
			name = XssChecker.stripXSS(name);
			if(values != null) {
				for(int i = 0; i < values.length; i++) {
					values[i] = XssChecker.stripXSS(values[i]);
				}
			}
			paramMap.put(name.toLowerCase(), values);
		});

		Map<String, List<String>> headerMap = new HashMap<>();
		for(Enumeration<String> e = realrequest.getHeaderNames(); e.hasMoreElements();) {
			String hn = XssChecker.stripXSS(e.nextElement()).toLowerCase();
			Enumeration<String> headers = realrequest.getHeaders(hn);
			if(null != headers) {
				while(headers.hasMoreElements()) {
					String value = XssChecker.stripXSS(headers.nextElement());
					List<String> list = headerMap.computeIfAbsent(hn, a -> new ArrayList<>());
					list.add(value);
				}
			}
		}

		return new HttpServerRequestResponse(realrequest, response, webapp, paramMap, headerMap);
	}

	@Override
	public void releaseUploads() {
		if(!(m_request instanceof UploadHttpRequestWrapper))
			return;

		UploadHttpRequestWrapper w = (UploadHttpRequestWrapper) m_request;
		w.releaseFiles();
	}

	@Override
	@NonNull
	public String getUserAgent() {
		return m_request.getHeader("user-agent");
	}

	@Override
	@NonNull
	public String getApplicationURL() {
		String appUrl = DomApplication.get().getApplicationURL();
		if(null != appUrl) {
			return appUrl;
		}

		return XssChecker.stripXSS(NetTools.getApplicationURL(getRequest()));
	}

	@Override
	@NonNull
	public String getHostURL() {
		String appUrl = DomApplication.get().getApplicationURL();
		if(null != appUrl) {
			int ix = appUrl.indexOf('/', 8);			// After http and https, first /
			if(ix == -1)
				return appUrl;
			return appUrl.substring(0, ix + 1);
		}
		return XssChecker.stripXSS(NetTools.getHostURL(getRequest()));
	}

	@Override
	@NonNull
	public String getHostName() {
		String hostName = DomApplication.get().getHostName();
		if(null != hostName)
			return hostName;
		return NetTools.getHostName(m_request);
	}

	@Override
	@Nullable
	public String getParameter(@NonNull String name) {
		String[] strings = m_parameterMap.get(name.toLowerCase());
		if(strings != null && strings.length == 1) {
			return strings[0];
		}
		return null;
	}

	@Override
	@Nullable
	public String[] getParameters(@NonNull String name) {
		return m_parameterMap.get(name.toLowerCase());
	}

	@Override
	@NonNull
	public String[] getParameterNames() {
		return (String[]) m_parameterMap.keySet().toArray(new String[m_parameterMap.size()]);
	}

	public List<String> getHeaderNames() {
		return new ArrayList<>(m_headerMap.keySet());
	}

	@Nullable
	public List<String> getHeaders(String name) {
		return m_headerMap.get(name.toLowerCase());
	}

	@Nullable
	public String getHeader(String name) {
		List<String> strings = m_headerMap.get(name.toLowerCase());
		if(strings != null && strings.size() == 1) {
			return strings.get(0);
		}
		return null;
	}

	/**
	 * Returns the names of all file parameters.
	 */
	@Override
	@NonNull
	public String[] getFileParameters() throws Exception {
		if(!(m_request instanceof UploadHttpRequestWrapper))
			return new String[0];
		UploadHttpRequestWrapper urw = (UploadHttpRequestWrapper) m_request;
		return urw.getFileItemMap().keySet().toArray(new String[urw.getFileItemMap().size()]);
	}

	@Override
	@NonNull
	public UploadItem[] getFileParameter(@NonNull String name) throws Exception {
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
	@NonNull
	public Writer getOutputWriter(@NonNull String contentType, @Nullable String encoding) throws IOException {
		getResponse().setContentType(contentType);
		if(null != encoding)
			getResponse().setCharacterEncoding(encoding);
		return getResponse().getWriter();
	}

	@Override
	@NonNull
	public OutputStream getOutputStream(@NonNull String contentType, @Nullable String encoding, int contentLength) throws Exception {
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
	public void redirect(@NonNull String newUrl) throws Exception {
		getResponse().sendRedirect(newUrl);
	}

	@Override
	public void sendError(int httpErrorCode, @NonNull String message) throws Exception {
		getResponse().sendError(httpErrorCode, message);
	}

	@Override
	public void addHeader(@NonNull String name, @NonNull String value) {
		getResponse().addHeader(name, value);
	}


	@Override
	public void addCookie(@NonNull Cookie cookie) {
		cookie.setDomain(getHostName());
		getResponse().addCookie(cookie);
	}

	@Override
	@NonNull
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
	public static HttpServerRequestResponse get(@NonNull IRequestContext ctx) {
		IRequestResponse rr = ctx.getRequestResponse();
		if(rr instanceof HttpServerRequestResponse)
			return (HttpServerRequestResponse) rr;
		return null;
	}
}
