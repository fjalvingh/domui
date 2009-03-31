package to.etc.domui.server;

import java.io.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import to.etc.domui.state.*;
import to.etc.domui.util.*;
import to.etc.domui.util.upload.*;
import to.etc.net.NetTools;
import to.etc.util.*;

public class RequestContextImpl implements RequestContext {
	private HttpServletRequest		m_request;
	private HttpServletResponse		m_response;
	private DomApplication			m_application;
	private AppSession				m_session;
	private WindowSession			m_windowSession;
	private String					m_urlin;
	private String					m_extension;
	private String					m_webapp;
//	private boolean					m_logging = true;
	private StringWriter			m_sw;
	private Writer					m_outWriter;

	RequestContextImpl(DomApplication app, AppSession ses, HttpServletRequest request, HttpServletResponse response) {
		m_response = response;
		m_application = app;
		m_session	= ses;

		//-- If this is a multipart (file transfer) request we need to parse the request,
		m_request	= UploadParser.wrapIfNeeded(request);			// Make multipart wrapper if multipart/form-data

		m_urlin = getRequest().getRequestURI();
		int	pos	= m_urlin.lastIndexOf('.');
		m_extension = "";
		if(pos != -1)
			m_extension = m_urlin.substring(pos+1).toLowerCase();
		if(m_urlin.startsWith("/"))
			m_urlin = m_urlin.substring(1);
		m_webapp = request.getContextPath();
		if(m_webapp == null)
			m_webapp = "";
		else {
			if(m_webapp.startsWith("/"))
				m_webapp = m_webapp.substring(1);
			if(! m_webapp.endsWith("/"))
				m_webapp = m_webapp + "/";
			if(! m_urlin.startsWith(m_webapp)) {
				throw new IllegalStateException("webapp url incorrect: lousy SUN spec");
			}
			m_urlin = m_urlin.substring(m_webapp.length());
		}

//		for(Enumeration<String> en = m_request.getHeaderNames(); en.hasMoreElements();) {
//			String name = en.nextElement();
//			System.out.println("Header: "+name);
//			for(Enumeration<String> en2 = m_request.getHeaders(name); en2.hasMoreElements();) {
//				String val = en2.nextElement();
//				System.out.println("     ="+val);
//			}
//		}
	}
	/**
	 * @see to.etc.domui.server.RequestContext#getApplication()
	 */
	final public DomApplication getApplication() {
		return m_application;
	}

	private boolean			m_amLockingSession;

	/**
	 * Get the session for this context.
	 * @see to.etc.domui.server.RequestContext#getSession()
	 */
	final public AppSession getSession() {
		m_session.internalLockSession();						// Someone uses session -> lock it for use by CURRENT-THREAD.
		if(! m_amLockingSession)
			m_session.internalCheckExpiredWindowSessions();
		m_amLockingSession = true;
		return m_session;
	}

	/**
	 * QUESTIONABLE.
	 * @param cm
	 */
	final public void internalSetWindowSession(WindowSession cm) {
		m_windowSession = cm;
	}

	/**
	 * Gets this request's conversation. If not already known it tries to locate the
	 * conversation parameter and locate the manager from there. If that fails it
	 * throws an exception.
	 *
	 * @see to.etc.domui.server.RequestContext#getWindowSession()
	 */
	final public WindowSession	getWindowSession() {
		if(m_windowSession != null)
			return m_windowSession;

		//-- Conversation manager needed.. Can we find one?
		String	cid	= getParameter(Constants.PARAM_CONVERSATION_ID);
		if(cid != null) {
			String[] cida = DomUtil.decodeCID(cid);
			m_windowSession = getSession().findWindowSession(cida[0]);
			if(m_windowSession != null)
				return m_windowSession;
			
		}
		throw new IllegalStateException("WindowSession is not known!!");
	}

	void internalUnlockSession() {
		if(m_amLockingSession) {
			m_session.internalUnlockSession();
			m_amLockingSession = false;
		}
	}
	/**
	 * If this context has caused the conversations to become attached detach 'm.
	 */
	private void	internalDetachConversations() {
		if(m_windowSession != null)
			getWindowSession().internalDetachConversations();
	}

	/**
	 * If this was an upload we discard all files that have not yet been claimed.
	 */
	private void	internalReleaseUploads() {
		if(! (m_request instanceof UploadHttpRequestWrapper))
			return;

		UploadHttpRequestWrapper w = (UploadHttpRequestWrapper) m_request;
		w.releaseFiles();
	}

	/**
	 * Called for all requests that have used a RequestContextImpl. This releases all lazily-aquired resources.
	 */
	void onRequestFinished() {
		internalDetachConversations();
		internalReleaseUploads();
//		m_session.getWindowSession().dump();
		internalUnlockSession();							// Unlock any session access.
	}
	

	/**
	 * @see to.etc.domui.server.RequestContext#getExtension()
	 */
	public String getExtension() {
		return m_extension;
	}
	public HttpServletRequest getRequest() {
		return m_request;
	}
	public HttpServletResponse getResponse() {
		return m_response;
	}

	/**
	 * @see to.etc.domui.server.RequestContext#getInputPath()
	 */
	public final String		getInputPath() {
		return m_urlin;
	}
	/**
	 * @see to.etc.domui.server.RequestContext#getUserAgent()
	 */
	public String	getUserAgent() {
		return m_request.getHeader("user-agent");
	}

	protected void	flush() throws Exception {
		if(m_sw != null) {
			if(getApplication().logOutput()) {
				String res = m_sw.getBuffer().toString();
				File	tgt = new File("/tmp/last-domui-output.xml");
				try {
					FileTool.writeFileFromString(tgt, res, "utf-8");
				} catch(Exception x) {}

				System.out.println("---- rendered output:");
				System.out.println(res);
				System.out.println("---- end");
			}
			getResponse().getWriter().append(m_sw.getBuffer());
			m_sw = null;
		}
		
	}

	protected void	discard() throws IOException {
//		if(m_sw != null) {
//			String res = m_sw.getBuffer().toString();
//			System.out.println("---- rendered output:");
//			System.out.println(res);
//			System.out.println("---- end");
//			getResponse().getWriter().append(res);
//		}
	}

	/**
	 * @see to.etc.domui.server.RequestContext#getRelativePath(java.lang.String)
	 */
	public String	getRelativePath(String rel) {
		StringBuilder	sb = new StringBuilder();
		sb.append(NetTools.getApplicationURL(getRequest()));
		sb.append(rel);
		return sb.toString();
	}

	public String	getRelativeThemePath(String frag) {
		return "$themes/"+getSession().getCurrentTheme()+"/"+frag;
	}

	/**
	 * Translates the input resource specifier by checking for special location indicators like $THEME and
	 * such at the start of the input. If input does not start with any of these it is returned unaltered.
	 * @see to.etc.domui.server.RequestContext#translateResourceName(java.lang.String)
	 */
	public String translateResourceName(String in) {
		if(in == null)
			return in;
		if(in.startsWith("/THEME/"))
			return getRelativeThemePath(in.substring(7));
		if(in.startsWith("THEME/"))
			return getRelativeThemePath(in.substring(6));
		return in;
	}

	/**
	 * @see to.etc.domui.server.RequestContext#getOutputWriter()
	 */
	public Writer	getOutputWriter() throws IOException {
		if(m_outWriter == null) {
			m_sw	= new StringWriter(8192);
			m_outWriter = m_sw;
		}
		return m_outWriter;
		
//		if(m_outWriter == null) {
//			if(m_logging) {
//				m_sw = new StringWriter();
//				m_outWriter = new TeeWriter(getResponse().getWriter(), m_sw);
//			} else
//				m_outWriter = getResponse().getWriter();
//		}
//		return m_outWriter;
	}

	public boolean hasPermission(String permissionName) {
		return m_request.isUserInRole(permissionName);
	}
	
	/*--------------------------------------------------------------*/
	/*	CODING:	ParameterInfo implementation						*/
	/*--------------------------------------------------------------*/
	
	/**
	 * @see to.etc.domui.server.RequestContext#getParameter(java.lang.String)
	 */
	public String getParameter(String name) {
		return getRequest().getParameter(name);
	}
	/**
	 * @see to.etc.domui.server.RequestContext#getParameters(java.lang.String)
	 */
	public String[] getParameters(String name) {
		return getRequest().getParameterValues(name);
	}
	/**
	 * @see to.etc.domui.server.RequestContext#getParameterNames()
	 */
	public String[] getParameterNames() {
		return (String[])getRequest().getParameterMap().keySet().toArray(new String[getRequest().getParameterMap().size()]);
	}

	/**
	 * Returns the names of all file parameters.
	 * @return
	 */
	public String[]	getFileParameters() {
		if(! (m_request instanceof UploadHttpRequestWrapper))
			return new String[0];
		UploadHttpRequestWrapper urw = (UploadHttpRequestWrapper)m_request;
		return urw.getFileItemMap().keySet().toArray(new String[urw.getFileItemMap().size()]);
	}

	public UploadItem[]	getFileParameter(String name) {
		if(! (m_request instanceof UploadHttpRequestWrapper))
			return null;
		UploadHttpRequestWrapper urw = (UploadHttpRequestWrapper)m_request;
		return urw.getFileItems(name);
	}

	public String getRemoteUser() {
		return getRequest().getRemoteUser();
	}
}
