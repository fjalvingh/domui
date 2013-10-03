package to.etc.domui.server;

import java.io.*;

import javax.annotation.*;
import javax.servlet.http.*;

import to.etc.domui.util.upload.*;
import to.etc.net.*;
import to.etc.webapp.core.*;

public class HttpServerRequestResponse implements IRequestResponse {
	@Nonnull
	final private HttpServletRequest m_request;

	@Nonnull
	final private HttpServletResponse m_response;

	@Nonnull
	final private String m_relativeURL;

	@Nonnull
	final private String m_webappContext;

	@Nonnull
	final private String m_extension;

	private HttpServerRequestResponse(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull String relativeURL, @Nonnull String webappContext,
		@Nonnull String extension) {
		m_request = request;
		m_response = response;
		m_relativeURL = relativeURL;
		m_webappContext = webappContext;
		m_extension = extension;
	}

	@Override
	@Nonnull
	public String getRequestURI() {
		return m_request.getRequestURI();
	}

	@Nonnull
	public HttpServletRequest getRequest() {
		return m_request;
	}

	@Nonnull
	public HttpServletResponse getResponse() {
		return m_response;
	}

	@Nonnull
	public String getRelativeURL() {
		return m_relativeURL;
	}

	@Nonnull
	public String getWebappContext() {
		return m_webappContext;
	}

	@Nonnull
	public String getExtension() {
		return m_extension;
	}

	@Nonnull
	static public HttpServerRequestResponse create(@Nonnull DomApplication application, @Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response) {
		//-- If this is a multipart (file transfer) request we need to parse the request,
		String urlin = request.getRequestURI();
		int pos = urlin.lastIndexOf('.');
		String extension = "";
		if(pos != -1)
			extension = urlin.substring(pos + 1).toLowerCase();

		//FIXME dubbele slashes in viewpoint, wellicht anders oplossen
		while(urlin.startsWith("/"))
			urlin = urlin.substring(1);
		HttpServletRequest realrequest = request;
		if(application.getUrlExtension().equals(extension) || urlin.contains(".part")) 		// QD Fix for upload
			realrequest = UploadParser.wrapIfNeeded(request); 								// Make multipart wrapper if multipart/form-data

		String webapp = request.getContextPath();
		if(webapp == null)
			webapp = "";
		else {
			if(webapp.startsWith("/"))
				webapp = webapp.substring(1);
			if(!webapp.endsWith("/"))
				webapp = webapp + "/";
			if(!urlin.startsWith(webapp)) {
				throw new IllegalStateException("webapp url incorrect: lousy SUN spec");
			}
			urlin = urlin.substring(webapp.length());
		}

//		for(Enumeration<String> en = m_request.getHeaderNames(); en.hasMoreElements();) {
//			String name = en.nextElement();
//			System.out.println("Header: "+name);
//			for(Enumeration<String> en2 = m_request.getHeaders(name); en2.hasMoreElements();) {
//				String val = en2.nextElement();
//				System.out.println("     ="+val);
//			}
//		}

		return new HttpServerRequestResponse(realrequest, response, urlin, webapp, extension);
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
		return NetTools.getApplicationURL(getRequest());
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
	public String[] getFileParameters() {
		if(!(m_request instanceof UploadHttpRequestWrapper))
			return new String[0];
		UploadHttpRequestWrapper urw = (UploadHttpRequestWrapper) m_request;
		return urw.getFileItemMap().keySet().toArray(new String[urw.getFileItemMap().size()]);
	}

	@Override
	@Nonnull
	public UploadItem[] getFileParameter(@Nonnull String name) {
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

	@Override
	public void addHeader(@Nonnull String name, @Nonnull String value) {
		getResponse().addHeader(name, value);
	}
}
