/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.server;

import java.io.*;
import java.util.*;

import javax.annotation.*;

import to.etc.domui.state.*;
import to.etc.domui.util.*;
import to.etc.domui.util.upload.*;
import to.etc.util.*;

public class RequestContextImpl implements IRequestContext, IAttributeContainer {
	@Nonnull
	final private DomApplication m_application;

	@Nonnull
	final private AppSession m_session;

	@Nonnull
	final private IRequestResponse m_requestResponse;

	private WindowSession m_windowSession;

	private StringWriter m_sw;

	private Writer m_outWriter;

	private BrowserVersion m_browserVersion;

	private Map<String, Object> m_attributeMap = Collections.EMPTY_MAP;

	@Nonnull
	final private String m_urlin;

	@Nonnull
	final private String m_extension;
	
	static private final int HEADER_LENGTH = 2927;

	public RequestContextImpl(@Nonnull IRequestResponse rr, @Nonnull DomApplication app, @Nonnull AppSession ses) {
		m_requestResponse = rr;
		m_application = app;
		m_session = ses;

		//-- ViewPoint sends malconstructed URLs containing duplicated slashes.
		String urlin = rr.getRequestURI();
		while(urlin.startsWith("/"))
			urlin = urlin.substring(1);

		int pos = urlin.lastIndexOf('.');
		m_extension = pos < 0 ? "" : urlin.substring(pos + 1).toLowerCase();

		//-- Strip webapp name from url.
		String webapp = rr.getWebappContext();						// Get "viewpoint/" like webapp context
		if(webapp.length() > 0) {
			if(!urlin.startsWith(webapp)) {
				throw new IllegalStateException("webapp url '" + urlin + "' incorrect: it does not start with '" + webapp + "'");
			}
			urlin = urlin.substring(webapp.length());
			while(urlin.startsWith("/"))
				urlin = urlin.substring(1);
		}
		m_urlin = urlin;
	}

	/**
	 * @see to.etc.domui.server.IRequestContext#getApplication()
	 */
	@Override
	final public @Nonnull DomApplication getApplication() {
		return m_application;
	}

	private boolean m_amLockingSession;

	private String m_outputContentType;

	private String m_outputEncoding;

	private Exception m_outputAllocated;

	/**
	 * Get the session for this context.
	 * @see to.etc.domui.server.IRequestContext#getSession()
	 */
	@Override
	final public @Nonnull AppSession getSession() {
		m_session.internalLockSession(); 						// Someone uses session -> lock it for use by CURRENT-THREAD.
		if(!m_amLockingSession)
			m_session.internalCheckExpiredWindowSessions();
		m_amLockingSession = true;
		return m_session;
	}

	@Override
	@Nonnull
	public IRequestResponse getRequestResponse() {
		return m_requestResponse;
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
	 * @see to.etc.domui.server.IRequestContext#getWindowSession()
	 */
	@Override
	final public @Nonnull WindowSession getWindowSession() {
		if(m_windowSession != null)
			return m_windowSession;

		//-- Conversation manager needed.. Can we find one?
		String cid = getParameter(Constants.PARAM_CONVERSATION_ID);
		if(cid != null) {
			CidPair cida = CidPair.decode(cid);
			m_windowSession = getSession().findWindowSession(cida.getWindowId());
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
	private void internalDetachConversations() {
		if(m_windowSession != null)
			getWindowSession().internalDetachConversations();
	}

	/**
	 * If this was an upload we discard all files that have not yet been claimed.
	 */
	private void internalReleaseUploads() {
		m_requestResponse.releaseUploads();
	}

	/**
	 * Called for all requests that have used a RequestContextImpl. This releases all lazily-aquired resources.
	 */
	public void internalOnRequestFinished() {
		internalDetachConversations();
		internalReleaseUploads();
		//		m_session.getWindowSession().dump();
		internalUnlockSession(); // Unlock any session access.
	}


	/**
	 * @see to.etc.domui.server.IRequestContext#getExtension()
	 */
	@Override
	@Nonnull
	public String getExtension() {
		return m_extension;
	}

	/**
	 * @see to.etc.domui.server.IRequestContext#getInputPath()
	 */
	@Override
	@Nonnull
	public final String getInputPath() {
		return m_urlin;
	}

	/**
	 * @see to.etc.domui.server.IRequestContext#getUserAgent()
	 */
	@Override
	public String getUserAgent() {
		return m_requestResponse.getUserAgent();
	}

	@Override
	public BrowserVersion getBrowserVersion() {
		if(m_browserVersion == null) {
			m_browserVersion = BrowserVersion.parseUserAgent(getUserAgent());
		}
		return m_browserVersion;
	}

	public void flush() throws Exception {
		if(m_sw != null) {
			if(getApplication().logOutput()) {
				String res = m_sw.getBuffer().toString();
				File tgt = new File("/tmp/last-domui-output.xml");
				try {
					FileTool.writeFileFromString(tgt, res, "utf-8");
				} catch(Exception x) {}

				System.out.println("---- rendered output:");
				System.out.println(res);
				System.out.println("---- end");
			}
			String outputContentType = m_outputContentType;
			if(null == outputContentType)
				throw new IllegalStateException("The content type for buffered output is not set.");
			Writer ow = getRequestResponse().getOutputWriter(outputContentType, m_outputEncoding);
			ow.append(m_sw.getBuffer());
			m_sw = null;
		}
	}


	public void discard() throws IOException {
	//		if(m_sw != null) {
	//			String res = m_sw.getBuffer().toString();
	//			System.out.println("---- rendered output:");
	//			System.out.println(res);
	//			System.out.println("---- end");
	//			getResponse().getWriter().append(res);
	//		}
	}

	/**
	 * @see to.etc.domui.server.IRequestContext#getRelativePath(java.lang.String)
	 */
	@Override
	public @Nonnull String getRelativePath(@Nonnull String rel) {
		StringBuilder sb = new StringBuilder(rel.length() + 128);
		sb.append(m_requestResponse.getApplicationURL());
		sb.append(rel);
		return sb.toString();
	}

	@Override
	public String getThemedPath(String in) {
		String p = getApplication().getThemedResourceRURL(in);
		if(p == null)
			throw new NullPointerException("?");
		return getRelativePath(p);
	}

	/**
	 * This returns a fully buffered output writer. Calling it twice is explicitly
	 * allowed, but clears the data written before as it's assumed that another route
	 * to output will be chosen.
	 *
	 * @see to.etc.domui.server.IRequestContext#getOutputWriter()
	 */
	@Override
	@Nonnull
	public Writer getOutputWriter(@Nonnull String contentType, @Nullable String encoding) throws IOException {
		StringWriter sw = m_sw;
		if(null != sw) {
			if(sw.getBuffer().length() > HEADER_LENGTH) {
				System.out.println("domui warning: outputwriter reallocated after writing " + sw.getBuffer().length() + " characters of data already");
			}
		}

		m_outputContentType = contentType;
		m_outputEncoding = encoding;
		m_sw = new StringWriter(8192);
		m_outWriter = m_sw;
		return m_outWriter;
	}

	/**
	 * Send a redirect response to the client.
	 * @param newUrl
	 */
	public void redirect(@Nonnull String newUrl) throws Exception {
		getRequestResponse().redirect(newUrl);
	}

	/**
	 * Send an error back to the client.
	 * @param httpErrorCode
	 * @param message
	 */
	public void sendError(int httpErrorCode, @Nonnull String message) throws Exception {
		getRequestResponse().sendError(httpErrorCode, message);
	}

	@Override
	@Nullable
	public IServerSession getServerSession(boolean create) {
		return getRequestResponse().getServerSession(create);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	ParameterInfo implementation						*/
	/*--------------------------------------------------------------*/

	/**
	 * @see to.etc.domui.server.IRequestContext#getParameter(java.lang.String)
	 */
	@Override
	@Nullable
	public String getParameter(@Nonnull String name) {
		return m_requestResponse.getParameter(name);
	}

	/**
	 * @see to.etc.domui.server.IRequestContext#getParameters(java.lang.String)
	 */
	@Override
	@Nonnull
	public String[] getParameters(@Nonnull String name) {
		return m_requestResponse.getParameters(name);
	}

	/**
	 * @see to.etc.domui.server.IRequestContext#getParameterNames()
	 */
	@Override
	@Nonnull
	public String[] getParameterNames() {
		return m_requestResponse.getParameterNames();
	}

	/**
	 * Returns the names of all file parameters.
	 * @return
	 */
	public String[] getFileParameters() {
		return m_requestResponse.getFileParameters();
	}

	public UploadItem[] getFileParameter(@Nonnull String name) {
		return m_requestResponse.getFileParameter(name);
	}

//	/**
//	 * DO NOT USE - functionality only present for declarative security.
//	 * @see to.etc.domui.server.IRequestContext#getRemoteUser()
//	 */
//	@Deprecated
//	@Override
//	public String getRemoteUser() {
//		return getRequest().getRemoteUser();
//	}
//
	/*--------------------------------------------------------------*/
	/*	CODING:	IAttributeContainer implementation.					*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @see to.etc.domui.server.IAttributeContainer#getAttribute(java.lang.String)
	 */
	@Override
	public Object getAttribute(@Nonnull String name) {
		return m_attributeMap.get(name);
	}

	@Override
	public void setAttribute(@Nonnull String name, @Nullable Object value) {
		if(m_attributeMap == Collections.EMPTY_MAP)
			m_attributeMap = new HashMap<String, Object>();
		if(value == null)
			m_attributeMap.remove(name);
		else
			m_attributeMap.put(name, value);
	}

}
