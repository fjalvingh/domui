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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.domui.dom.html.Page;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.server.PageUrlMapping.Target;
import to.etc.domui.state.AppSession;
import to.etc.domui.state.CidPair;
import to.etc.domui.state.ConversationContext;
import to.etc.domui.state.IPageParameters;
import to.etc.domui.state.RequestContextParameters;
import to.etc.domui.state.UIContext;
import to.etc.domui.state.WindowSession;
import to.etc.domui.themes.DefaultThemeVariant;
import to.etc.domui.themes.ITheme;
import to.etc.domui.themes.IThemeVariant;
import to.etc.domui.util.Constants;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.upload.UploadItem;
import to.etc.util.FileTool;
import to.etc.util.WrappedException;
import to.etc.webapp.crawlers.Crawlers;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RequestContextImpl implements IRequestContext, IAttributeContainer {
	static private final Logger LOG = LoggerFactory.getLogger(RequestContextImpl.class);

	@NonNull
	final private DomApplication m_application;

	@NonNull
	final private AppSession m_session;

	@NonNull
	final private IRequestResponse m_requestResponse;

	private WindowSession m_windowSession;

	private StringWriter m_sw;

	private Writer m_outWriter;

	private BrowserVersion m_browserVersion;

	private Map<String, Object> m_attributeMap = Collections.EMPTY_MAP;

	@NonNull
	final private String m_inputPath;

	/**
	 * The extension used in the page name, INCLUDING the dot. If no extension is present this is "".
	 */
	@NonNull
	final private String m_extension;

	/**
	 * The extension used in the page name, EXCLUDING the dot. If no extension is present this is "".
	 */
	@NonNull
	final private String m_extensionWithoutDot;

	/**
	 * The name part of the URL, which is the last segment which contained the extension. Only filled if the last part HAS an extension.
	 */
	@Nullable
	final private String m_pageName;

	/**
	 * The URL part befoer the pageName, if present. This never contains a leading / but, if present, always ends with a /.
	 */
	@NonNull
	final private String m_urlContextString;

	private boolean m_amLockingSession;

	private String m_outputContentType;

	private String m_outputEncoding;

	private Exception m_outputAllocated;

	@NonNull
	private Map<String, String> m_persistedParameterMap = new HashMap<>();

	/**
	 * Cached copy of theme for this user, lazy
	 */
	@Nullable
	private ITheme m_currentTheme;

	/**
	 * The theme name for this user, lazily initialized.
	 */
	@Nullable
	private String m_themeName;

	@NonNull
	private IThemeVariant m_themeVariant = DefaultThemeVariant.INSTANCE;

	static private final int PAGE_HEADER_BUFFER_LENGTH = 4000;

	@NonNull
	private IPageParameters m_parameterWrapper;

	public RequestContextImpl(@NonNull IRequestResponse rr, @NonNull DomApplication app, @NonNull AppSession ses) {
		m_requestResponse = rr;
		m_application = app;
		m_session = ses;
		m_parameterWrapper = new RequestContextParameters(this);

		//-- ViewPoint sends malconstructed URLs containing duplicated slashes.
		XssChecker xssChecker = app.getXssChecker();
		String urlin = xssChecker.stripXSS(rr.getRequestURI());
		while(urlin.startsWith("/"))
			urlin = urlin.substring(1);

		//-- Strip webapp name from url if it is there.
		String webapp = rr.getWebappContext();                        // Get "viewpoint/" like webapp context
		if(!webapp.isEmpty()) {
			if(!urlin.startsWith(webapp)) {
				throw new IllegalStateException("webapp url '" + urlin + "' incorrect: it does not start with '" + webapp + "'");
			}
			urlin = urlin.substring(webapp.length());
			while(urlin.startsWith("/"))
				urlin = urlin.substring(1);
		}
		m_inputPath = urlin;

		/*
		 * Is the input known to the URL mapper?
		 */
		Target target = app.getPageUrlMapping().findTarget(urlin, m_parameterWrapper);
		if(null != target) {
			m_pageName = target.getTargetPage();
			m_parameterWrapper = target.getParameters();
			m_extension = "." + m_application.getUrlExtension();
			m_extensionWithoutDot = m_application.getUrlExtension();
			m_urlContextString = "";
		} else {
			/*
			 * Split the url into separate parts. If the URL has an extension then the last part
			 * is treated as the pageName, and it gets stored into pageName, without the extension.
			 * The part before the pageName is the contextString, which - if present - is the
			 * items/separated/by/slashes/ part which always ends in a / if actually present.
			 */

			//-- Extension and context
			int pos = urlin.lastIndexOf('.');
			int slpos = urlin.lastIndexOf('/');
			if(pos < slpos)                                    // Dot before the slash means it's inside the context, leave that
				pos = -1;

			if(pos == -1) {
				//-- No extension, treat the whole as the urlContextString
				m_pageName = null;
				m_extension = "";
				m_extensionWithoutDot = "";
				if(!urlin.isEmpty() && !urlin.endsWith("/"))
					urlin += "/";
				m_urlContextString = urlin;
			} else {
				//-- No slash but an extension.
				m_extension = urlin.substring(pos);            // Extension INCLUDING dot
				m_extensionWithoutDot = urlin.substring(pos + 1);
				if(slpos == -1) {
					m_urlContextString = "";                // No URL context string
					m_pageName = urlin.substring(0, pos);    // page name without extension
				} else {
					slpos++;                                // past /
					m_urlContextString = urlin.substring(0, slpos);
					m_pageName = urlin.substring(slpos, pos);
				}
			}
		}

		Set<String> nameSet = app.getPersistentParameterSet();
		for(String name : nameSet) {
			String parameter = getRequestResponse().getParameter(name);
			if(null != parameter)
				m_persistedParameterMap.put(name, parameter);
		}
	}

	@NonNull
	@Override
	public IPageParameters getPageParameters() {
		return m_parameterWrapper;
	}

	@NonNull
	public Map<String, String> getPersistedParameterMap() {
		return m_persistedParameterMap;
	}

	public void updatePersistentParameters(Map<String, String> persistedParameterMap) {
		m_persistedParameterMap.putAll(persistedParameterMap);
	}

	@Override
	public void setPersistedParameter(String name, String value) {
		Set<String> nameSet = m_application.getPersistentParameterSet();
		if(!nameSet.contains(name))
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
	final public @NonNull DomApplication getApplication() {
		return m_application;
	}

	/**
	 * Get the session for this context.
	 *
	 * @see to.etc.domui.server.IRequestContext#getSession()
	 */
	@Override
	final public @NonNull AppSession getSession() {
		m_session.internalLockSession();                        // Someone uses session -> lock it for use by CURRENT-THREAD.
		if(!m_amLockingSession)
			m_session.internalCheckExpiredWindowSessions();
		m_amLockingSession = true;
		return m_session;
	}

	@Override
	@NonNull
	public IRequestResponse getRequestResponse() {
		return m_requestResponse;
	}

	/**
	 * QUESTIONABLE.
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
	final public @NonNull WindowSession getWindowSession() {
		if(m_windowSession != null)
			return m_windowSession;

		//-- Conversation manager needed.. Can we find one?
		String cid = getRequestResponse().getParameter(Constants.PARAM_CONVERSATION_ID);
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

	@Override
	@NonNull
	public String getExtension() {
		return m_extensionWithoutDot;
	}

	@NonNull
	public String getExtensionWithDot() {
		return m_extension;
	}

	/**
	 * The complete input URL without the JSDK context part and the query part. Specifically:
	 * <ul>
	 * 	<li>The JSDK webapp context is always removed from the start, i.e. this is always the webapp-relative path</li>
	 * 	<li>The context NEVER starts with a slash</li>
	 * </ul>
	 */
	@Override
	@NonNull
	public final String getInputPath() {
		return m_inputPath;
	}

	/**
	 * Returns the URL context string, defined as the part inside the input URL that is before the name in the URL.
	 * Specifically:
	 * <ul>
	 * 	<li>This uses the {@link #getInputPath()} URL as the basis, so the path never starts with the webapp context</li>
	 * 	<li>If the URL's last part has a suffix then the last part is assumed to be the pageName, and everything
	 * 		before this pageName is the urlContextString</li>
	 * 	<li>If an urlContextString is present then it always ends in a /</li>
	 * 	<li>If the URL is just a pageName then this returns the empty string</li>
	 * </ul>
	 * <p>
	 * The following always holds: {@link #getUrlContextString()} + {@link #getPageName()} + m_extension = {@link #getInputPath()}.
	 */
	@NonNull
	public String getUrlContextString() {
		return m_urlContextString;
	}

	/**
	 * Returns the last part of the URL, provided that part has an extension. If not there is no page name.
	 */
	@Override
	@Nullable
	public String getPageName() {
		return m_pageName;
	}

	/**
	 * @see to.etc.domui.server.IRequestContext#getUserAgent()
	 */
	@Override
	public String getUserAgent() {
		return m_requestResponse.getUserAgent();
	}

	public BrowserVersion getBrowserVersion() {
		if(m_browserVersion == null) {
			m_browserVersion = BrowserVersion.parseUserAgent(getUserAgent());
		}
		return m_browserVersion;
	}

	/**
	 * This should be replaced by getThemeName below as that uniquely identifies the theme.
	 */
	@NonNull
	@Override
	final public ITheme getCurrentTheme() {
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

	@NonNull
	public String getThemeName() {
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

	@Override
	@NonNull
	public IThemeVariant getThemeVariant() {
		return m_themeVariant;
	}

	@Override
	public void setThemeVariant(@NonNull IThemeVariant themeVariant) {
		m_themeVariant = themeVariant;
	}

	public void flush() throws Exception {
		if(m_sw != null) {
			if(getApplication().logOutput()) {
				String res = m_sw.getBuffer().toString();
				File tgt = new File("/tmp/last-domui-output.xml");
				try {
					FileTool.writeFileFromString(tgt, res, "utf-8");
				} catch(Exception x) {
				}

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

	@Override
	public @NonNull String getRelativePath(@NonNull String rel) {
		if(DomUtil.isAbsoluteURL(rel))
			return rel;
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
	@NonNull
	public Writer getOutputWriter(@NonNull String contentType, @Nullable String encoding) throws IOException {
		StringWriter sw = m_sw;
		if(null != sw) {
			if(sw.getBuffer().length() > PAGE_HEADER_BUFFER_LENGTH) {
				LOG.warn("domui warning: outputwriter reallocated after writing " + sw.getBuffer().length() + " characters of data already");
			}
		}

		m_outputContentType = contentType;
		m_outputEncoding = encoding;
		m_sw = new StringWriter(8192);
		m_outWriter = m_sw;
		return m_outWriter;
	}

	public void renderResponseHeaders(@Nullable UrlPage currentPage) throws Exception {
		m_application.applyPageHeaderTransformations(getPageName(), currentPage).forEach((header, value) -> getRequestResponse().addHeader(header, value));
	}

	/**
	 * Send a redirect response to the client.
	 */
	public void redirect(@NonNull String newUrl) throws Exception {
		getRequestResponse().redirect(newUrl);
	}

	/**
	 * Send an error back to the client.
	 */
	public void sendError(int httpErrorCode, @NonNull String message) throws Exception {
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
	 * Returns the names of all file parameters.
	 */
	public String[] getFileParameterNames() throws Exception {
		return m_requestResponse.getFileParameters();
	}

	/**
	 * This retrieves the file(s) stored for the specified name.
	 * <b>Warning: when files are returned the caller assumes
	 * responsibility for them to be deleted after use (i.e. ownership
	 * is passed to the caller)</b>
	 */
	public UploadItem[] getFileParameter(@NonNull String name) throws Exception {
		return m_requestResponse.getFileParameter(name);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IAttributeContainer implementation.					*/
	/*--------------------------------------------------------------*/
	@Override
	public Object getAttribute(@NonNull String name) {
		return m_attributeMap.get(name);
	}

	@Override
	public void setAttribute(@NonNull String name, @Nullable Object value) {
		if(m_attributeMap == Collections.EMPTY_MAP)
			m_attributeMap = new HashMap<String, Object>();
		if(value == null)
			m_attributeMap.remove(name);
		else
			m_attributeMap.put(name, value);
	}

	@Override
	public boolean isCrawler() {
		IRequestResponse rr = getRequestResponse();
		if(rr instanceof HttpServerRequestResponse) {
			return Crawlers.INSTANCE.isCrawler(((HttpServerRequestResponse) rr).getRequest());
		}
		return false;
	}
}
