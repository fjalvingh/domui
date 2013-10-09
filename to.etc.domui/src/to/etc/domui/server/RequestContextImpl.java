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

	private String m_urlin;

	private String m_extension;

	private String m_webapp;

	//	private boolean					m_logging = true;
	private StringWriter m_sw;

	private Writer m_outWriter;

	private BrowserVersion m_browserVersion;

	private Map<String, Object> m_attributeMap = Collections.EMPTY_MAP;

	RequestContextImpl(@Nonnull IRequestResponse rr, @Nonnull DomApplication app, @Nonnull AppSession ses) {
		m_requestResponse = rr;
		m_application = app;
		m_session = ses;
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
	void onRequestFinished() {
		internalDetachConversations();
		internalReleaseUploads();
		//		m_session.getWindowSession().dump();
		internalUnlockSession(); // Unlock any session access.
	}


	/**
	 * @see to.etc.domui.server.IRequestContext#getExtension()
	 */
	@Override
	public @Nonnull String getExtension() {
		return m_extension;
	}

	//	public HttpServletRequest getRequest() {
	//		return m_request;
	//	}
	//
	//	public HttpServletResponse getResponse() {
	//		return m_response;
	//	}
	//
	/**
	 * @see to.etc.domui.server.IRequestContext#getInputPath()
	 */
	@Override
	public final @Nonnull String getInputPath() {
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

	protected void discard() throws IOException {
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

	/**
	 * This returns a fully buffered output writer. Flush will write data
	 * to the "real" output stream.
	 * @see to.etc.domui.server.IRequestContext#getOutputWriter()
	 */
	@Override
	@Nonnull
	public Writer getOutputWriter(@Nonnull String contentType, @Nullable String encoding) throws IOException {
		if(m_outWriter != null)
			throw new IllegalStateException("Output writer already created");

		m_outputContentType = contentType;
		m_outputEncoding = encoding;

		if(m_outWriter == null) {
			m_sw = new StringWriter(8192);
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

	protected void flush() throws Exception {
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

	/**
	 * Send a redirect response to the client.
	 * @param newUrl
	 */
	public void redirect(@Nonnull String newUrl) throws Exception {
		getRequestResponse().redirect(newUrl);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	ParameterInfo implementation						*/
	/*--------------------------------------------------------------*/

	/**
	 * @see to.etc.domui.server.IRequestContext#getParameter(java.lang.String)
	 */
	@Override
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
