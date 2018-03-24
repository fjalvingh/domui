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

import to.etc.domui.dom.html.Page;
import to.etc.domui.state.AppSession;
import to.etc.domui.state.CidPair;
import to.etc.domui.state.ConversationContext;
import to.etc.domui.state.UIContext;
import to.etc.domui.state.WindowSession;
import to.etc.domui.themes.DefaultThemeVariant;
import to.etc.domui.themes.ITheme;
import to.etc.domui.themes.IThemeVariant;
import to.etc.domui.util.Constants;
import to.etc.domui.util.upload.UploadItem;
import to.etc.util.FileTool;
import to.etc.util.WrappedException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

	private boolean m_amLockingSession;

	private String m_outputContentType;

	private String m_outputEncoding;

	private Exception m_outputAllocated;

	@Nonnull
	private Map<String, String> m_persistedParameterMap = new HashMap<>();

	/** Cached copy of theme for this user, lazy */
	@Nullable
	private ITheme m_currentTheme;

	/** The theme name for this user, lazily initialized. */
	@Nullable
	private String m_themeName;

	@Nonnull
	private IThemeVariant m_themeVariant = DefaultThemeVariant.INSTANCE;
	
	static private final int PAGE_HEADER_BUFFER_LENGTH = 4000;

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

		Set<String> nameSet = app.getPersistentParameterSet();
		for(String name : nameSet) {
			String parameter = getParameter(name);
			if(null != parameter)
				m_persistedParameterMap.put(name, parameter);
		}
	}

	@Nonnull public Map<String, String> getPersistedParameterMap() {
		return m_persistedParameterMap;
	}


	public void updatePersistentParameters(Map<String, String> persistedParameterMap) {
		m_persistedParameterMap.putAll(persistedParameterMap);
	}

	@Override
	public void setPersistedParameter(String name, String value) {
		Set<String> nameSet = m_application.getPersistentParameterSet();
		if(! nameSet.contains(name))
			throw new IllegalStateException("The parameter name '" + name + "' is not registered as a persistent parameter. Add it in DomApplication.initialize() using addPersistentParameter");
		m_persistedParameterMap.put(name, value);
		Page page = UIContext.internalGetPage();
		if(null != page) {
			ConversationContext conversation = page.getConversation();
			conversation.savePersistedParameter(name, value);
		}
	}

	/**
	 * @see to.etc.domui.server.IRequestContext#getApplication()
	 */
	@Override
	final public @Nonnull DomApplication getApplication() {
		return m_application;
	}

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

	/**
	 * This should be replaced by getThemeName below as that uniquely identifies the theme.
	 * @return
	 */
	@Nonnull @Override final public ITheme getCurrentTheme() {
		ITheme currentTheme = m_currentTheme;
		if(null == currentTheme) {
			try {
				currentTheme = m_currentTheme = m_application.getTheme(getThemeName(), null);
			} catch(Exception x) {
				throw WrappedException.wrap(x);
			}
		}
		return currentTheme;
	}

	static private final String THEMENAME = "ctx$themename";

	@Nonnull @Override public String getThemeName() {
		String themeName = m_themeName;
		if(null == themeName) {
			 themeName = (String) getSession().getAttribute(THEMENAME);
			 if(null == themeName) {
				 themeName = m_application.calculateUserTheme(this);
			 }
			 m_themeName = themeName;
		}
		return themeName;
	}

	@Override
	public void setThemeName(String userThemeName) {
		m_themeName = userThemeName;
		getSession().setAttribute(THEMENAME, userThemeName);
	}

	@Override @Nonnull public IThemeVariant getThemeVariant() {
		return m_themeVariant;
	}

	@Override public void setThemeVariant(@Nonnull IThemeVariant themeVariant) {
		m_themeVariant = themeVariant;
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

	/**
	 * This returns a fully buffered output writer. Calling it twice is explicitly
	 * allowed, but clears the data written before as it's assumed that another route
	 * to output will be chosen.
	 */
	@Override
	@Nonnull
	public Writer getOutputWriter(@Nonnull String contentType, @Nullable String encoding) throws IOException {
		StringWriter sw = m_sw;
		if(null != sw) {
			if(sw.getBuffer().length() > PAGE_HEADER_BUFFER_LENGTH) {
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
	public String[] getFileParameters() throws Exception {
		return m_requestResponse.getFileParameters();
	}

	public UploadItem[] getFileParameter(@Nonnull String name) throws Exception {
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
