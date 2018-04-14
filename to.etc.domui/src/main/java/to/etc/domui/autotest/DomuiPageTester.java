package to.etc.domui.autotest;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.html.Input;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.Page;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.server.AbstractContextMaker;
import to.etc.domui.server.AppFilter;
import to.etc.domui.server.ConfigParameters;
import to.etc.domui.server.DomApplication;
import to.etc.domui.server.HttpServerRequestResponse;
import to.etc.domui.server.IRequestContext;
import to.etc.domui.server.IRequestInterceptor;
import to.etc.domui.server.RequestContextImpl;
import to.etc.domui.state.AppSession;
import to.etc.domui.state.ConversationContext;
import to.etc.domui.state.PageParameters;
import to.etc.domui.state.UIContext;
import to.etc.domui.trouble.ThingyNotFoundException;
import to.etc.domui.util.Constants;
import to.etc.util.StringTool;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Experimental.
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 2, 2013
 */
public class DomuiPageTester implements IDomUITestInfo {
	private static class PageRef {
		@NonNull
		private final Class< ? extends UrlPage> m_pageClass;

		@NonNull
		private final PageParameters	m_parameters;

		public PageRef(@NonNull Class< ? extends UrlPage> pageClass, @NonNull PageParameters parameters) {
			m_pageClass = pageClass;
			m_parameters = parameters;
		}

		@NonNull
		public Class< ? extends UrlPage> getPageClass() {
			return m_pageClass;
		}

		@NonNull
		public PageParameters getParameters() {
			return m_parameters;
		}
	}

	@NonNull
	final private List<PageRef> m_pageList = new ArrayList<PageRef>();

	@NonNull
	private String m_userAgent = "Mozilla/5.0 (Windows; U; MSIE 9.0; Windows NT 9.0; en-US)";

	@Nullable
	static private DomApplication m_appInstance;

	@Nullable
	private TestServerSession m_ssession;

	private Page m_lastPage;

	private IRequestContext m_lastContext;

	/** Set of all nodes that were "clicked" on the "current" screen. */
	private Set<String>		m_clickedNodeSet = new HashSet<String>();


	static synchronized public void initApplication(@NonNull Class< ? extends DomApplication> applicationClass, @NonNull File webappFiles) throws Exception {
		AppFilter.initLogConfig(null, null);

		DomApplication da = m_appInstance;
		if(da != null) {
			if(applicationClass.isAssignableFrom(da.getClass()))
				return;
			throw new IllegalStateException("DomApplication already defined as " + da);
		}
		da = applicationClass.newInstance();

		//-- Start initialization
		Map<String, String> map = new HashMap<String, String>();

		ConfigParameters cp = new TestConfigParameters(webappFiles, map);
		da.internalInitialize(cp, false);
		m_appInstance = da;

		//-- Application instance is now active.

	}

	@NonNull
	static public DomApplication app() {
		DomApplication da = m_appInstance;
		if(null == da)
			throw new IllegalStateException("Call #initApplication() before you can run tests.");
		return da;
	}

	static private void checkInit() {
		app();
	}

	@Override
	@NonNull
	public DomApplication getApplication() {
		return app();
	}

	@Override
	@NonNull
	public String getApplicationHost() {
		return "http://www.test.nl/";
	}

	@NonNull
	@Override
	public String getWebappContext() {
		return "test";
	}

	@NonNull
	public String getApplicationURL() {
		StringBuilder sb = new StringBuilder();
		sb.append(getApplicationHost());
		String wc = getWebappContext();
		if(wc.length() > 0) {
			sb.append(wc);
			sb.append('/');
		}
		return sb.toString();
	}

	@Override
	@NonNull
	public String getUserAgent() {
		return m_userAgent;
	}

	public void setUserAgent(@NonNull String userAgent) {
		m_userAgent = userAgent;
	}

	@Override
	@Nullable
	public String getRemoteUser() {
		return "VPC";
	}


	public void addPage(@NonNull Class< ? extends UrlPage> page, @NonNull PageParameters pp) {
		m_pageList.add(new PageRef(page, pp));
	}

	public void addPage(@NonNull Class< ? extends UrlPage> page, Object... data) {
		PageParameters pp = new PageParameters(data);
		m_pageList.add(new PageRef(page, pp));
	}

	public void run() throws Exception {
		for(PageRef pr : m_pageList)
			run(pr.getPageClass(), pr.getParameters());
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Start of page running code.							*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @param pageClass
	 * @param parameters
	 * @throws Exception
	 */
	private void run(@NonNull Class< ? extends UrlPage> pageClass, @NonNull PageParameters parameters) throws Exception {
		checkInit();

		//-- We need an appsession for this page.
		AppSession appSession = new AppSession(app());
		setSvSession(new TestServerSession());

		m_clickedNodeSet.clear();
		TestRequestResponse rr = createRequestResponse(pageClass, parameters);
		interact(appSession, rr);

		//-- Enter the handle loop.
		for(int counter = 0; counter < 10; counter++) {
			TestRequestResponse newrr = null;
			switch(rr.getResponseType()){
				default:
					throw new IllegalStateException(rr.getResponseType() + ": not handled?");

				case DOCUMENT:
					System.out.println("Responded with a DOCUMENT");
					String doc = rr.getTextDocument();
					if(null == doc)
						break;
					System.out.println(StringTool.strTrunc(doc, 5120));
					newrr = handleDocument(doc, rr);
					break;

				case REDIRECT:
					newrr = handleRedirect(rr);
					break;

				case ERROR:
					System.out.println("http error " + rr.getErrorCode() + ": " + rr.getErrorMessage());
					return;

				case NOTHING:
					throw new IllegalStateException("Page did nothing!?");
			}

			if(newrr == null)
				return;
			rr = newrr;

			interact(appSession, rr);
		}
	}

	@Nullable
	private TestRequestResponse handleDocument(@NonNull String doc, @NonNull TestRequestResponse rr) throws Exception {
		//-- Contains javascript redirect (initial-response)?
		String str = "location.replace(";
		int pos = doc.indexOf(str);
		if(pos > 0) {
			return handleDocumentRedirect(doc, pos + str.length());
		}

		RequestContextImpl rci = (RequestContextImpl) m_lastContext;
		if(null == rci)
			throw new IllegalStateException("? No current context was set");

		Page page = m_lastPage;
		if(null == page)
			throw new IllegalStateException("? No current page was set");
		System.out.println("Page class was " + page.getBody());

		//-- Collect everything we need to know about this incarnation of the page
		PageData pageData = new PageData(page);
		scanPageData(pageData, page.getBody());
		System.out.println("Got " + pageData.getClickTargets().size() + " click targets");

		NodeBase nb = findClickTarget(pageData);
		if(null != nb) {
			return createClickRequest(pageData, nb);
		}

		return null;
	}

	@Nullable
	private NodeBase findClickTarget(@NonNull PageData pinfo) {
		for(NodeBase nb : pinfo.getClickTargets()) {
			if(!m_clickedNodeSet.contains(nb.getActualID())) {
				m_clickedNodeSet.add(nb.getActualID());
				return nb;
			}
		}
		return null;
	}


	/**
	 * Prepare
	 * @param page
	 * @return
	 */
	@NonNull
	private TestRequestResponse createClickRequest(@NonNull PageData pinfo, @NonNull NodeBase nodeToClick) throws Exception {
		PageParameters pp = pinfo.getPage().getPageParameters().getUnlockedCopy();

		for(NodeBase nb : pinfo.getBaseInputs()) {
			if(nb instanceof Input) {
				String id = nb.getActualID();
				String rv = ((Input) nb).getRawValue();
				if(null != rv)
					pp.addParameter(id, rv);
			}
		}

		pp.addParameter(Constants.PARAM_UIACTION, Constants.ACMD_CLICKED);
		pp.addParameter(Constants.PARAM_UICOMPONENT, nodeToClick.getActualID());
		return createRequestResponse(pinfo.getPage(), pp);
	}


	/**
	 * Walk the node and it's children recursively and add all nodes that have a "clicked"
	 * handler to the output list.
	 *
	 * @param clickTargets
	 * @param body
	 */
	private void scanPageData(@NonNull PageData pageData, @NonNull NodeBase body) {
		pageData.checkNode(body);
		if(body instanceof NodeContainer) {
			for(NodeBase nb : ((NodeContainer) body))
				scanPageData(pageData, nb);
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Redirection handling.								*/
	/*--------------------------------------------------------------*/
	/**
	 * Extracts the redirect url from a Javascript redirect response, and then redirects to the new page.
	 * @param doc
	 * @param pos
	 * @return
	 */
	@Nullable
	private TestRequestResponse handleDocumentRedirect(@NonNull String doc, int pos) {
		int end = doc.indexOf(')', pos);
		if(end == -1)
			throw new IllegalStateException("No redirect statement found");
		String sub = doc.substring(pos, end).trim();
		if(sub.length() < 2)
			throw new IllegalStateException("Bad redirect statement found: " + sub);
		char a = sub.charAt(0);
		char b = sub.charAt(sub.length() - 1);
		if(a == b && (a == '\'' || a == '"')) {
			sub = sub.substring(1, sub.length() - 1);
		}
		return handleRedirectTo(sub);
	}

	/**
	 * Redirect as the result of a http-level redirect.
	 * @param rr
	 * @return
	 */
	@Nullable
	private TestRequestResponse handleRedirect(@NonNull TestRequestResponse rr) {
		String redirectURL = rr.getRedirectURL();
		if(null == redirectURL)
			throw new IllegalStateException("Null redirect URL in test response");
		return handleRedirectTo(redirectURL);
	}

	/**
	 * Handle redirect to another screen. If the redirect is outside of the app it is ignored and this
	 * attempt stops.
	 * @param redirectURL
	 * @return
	 */
	@Nullable
	private TestRequestResponse handleRedirectTo(@NonNull String redirectURL) {
		System.out.println("URL redirect to: " + redirectURL);
		String app = getApplicationURL();
		if(redirectURL.contains(":")) {
			//-- Absolute URL.
			String host = getApplicationHost();

			if(!redirectURL.startsWith(host)) {
				//-- Redirected to something ouside our scope -> end.
				System.out.println("Redirected ouside our host -> page test ends");
				return null;
			}
			redirectURL = redirectURL.substring(host.length() - 1);		// Should now be /webapp/rest (starting with /)
			if(!redirectURL.startsWith("/"))
				throw new IllegalStateException();
		} else if(! redirectURL.startsWith("/")) {
			throw new IllegalStateException("Page-relative redirect URLs not supported: " + redirectURL);
		}

		String webapp = getWebappContext();
		if(webapp.length() > 0) {
			webapp = "/" + webapp + "/";
			if(!redirectURL.startsWith(webapp)) {
				System.out.println("Redirected ouside our webapp context -> page test ends");
				return null;
			}
		}

		//-- Remove and split any query string.
		String query = "";
		int qpos = redirectURL.indexOf('?');
		if(qpos >= 0) {
			query = redirectURL.substring(qpos + 1);
			redirectURL = redirectURL.substring(0, qpos);
		}

//		PageParameters pp = PageParameters.decodeParameters(query);
		TestRequestResponse rr = new TestRequestResponse(getSvSession(), this, redirectURL, query);
		return rr;
	}

	/**
	 * Create a request/response object for the specified page class.
	 * @param clz
	 * @param pp
	 * @return
	 */
	@NonNull
	private TestRequestResponse createRequestResponse(@NonNull Class< ? extends UrlPage> clz, @NonNull PageParameters pp) {
		String requestURI = getClassURI(clz);

		TestRequestResponse rr = new TestRequestResponse(getSvSession(), this, requestURI, pp);
		return rr;
	}

	@NonNull
	private String getClassURI(@NonNull Class< ? extends UrlPage> clz) {
		StringBuilder sb = new StringBuilder();
		sb.append('/');
		String webappContext = getWebappContext();
		if(webappContext.length() > 0) {
			sb.append(webappContext);						// Start with the app context as "xxx/" or the empty string.
			sb.append('/');
		}
		sb.append(clz.getName());
		sb.append('.');
		sb.append(getApplication().getUrlExtension());

		return sb.toString();
	}

	@NonNull
	private TestRequestResponse createRequestResponse(@NonNull Page page, @NonNull PageParameters pp) {
		String requestURI = getClassURI(page.getBody().getClass());
		ConversationContext internalGetConversation = page.internalGetConversation();
		if(null == internalGetConversation)
			throw new IllegalStateException("Page conversation is null???");
		pp.addParameter(Constants.PARAM_CONVERSATION_ID, internalGetConversation.getFullId());
		TestRequestResponse rr = new TestRequestResponse(getSvSession(), this, requestURI, pp);
		return rr;
	}


	/**
	 * Do a single interaction with the page-under-test (send the request, get the response). This
	 * code must closely mimic the code inside {@link AbstractContextMaker#execute(HttpServerRequestResponse, RequestContextImpl, javax.servlet.FilterChain)}.
	 *
	 * @param session
	 * @param rr
	 * @throws Exception
	 */
	private void interact(@NonNull AppSession session, @NonNull TestRequestResponse rr) throws Exception {
		System.out.println("T: url: " + rr.getRequestURI() + rr.getQueryString());

		RequestContextImpl ctx = new RequestContextImpl(rr, getApplication(), session);

		List<IRequestInterceptor> il = ctx.getApplication().getInterceptorList();
		Exception xx = null;
		try {
			UIContext.internalSet(ctx);
			AbstractContextMaker.callInterceptorsBegin(il, ctx);
			boolean handled = ctx.getApplication().callRequestHandler(ctx);
			if(! handled) {
				//-- Non-DomUI request.
				throw new IllegalStateException("Test DomUI request not a DomUI URL: " + ctx.getRequestResponse().getRequestURI());
			}
			ctx.flush();
			rr.flush();

			//-- Collect structures to use later on
			m_lastPage = UIContext.internalGetPage();
			m_lastContext = UIContext.internalGetContext();

		} catch(ThingyNotFoundException x) {
			ctx.sendError(HttpServletResponse.SC_NOT_FOUND, x.getMessage());
		} catch(Exception xxx) {
			xx = xxx;
			throw xxx;
		} finally {
			AbstractContextMaker.callInterceptorsAfter(il, ctx, xx);
			ctx.internalOnRequestFinished();
			try {
				ctx.discard();
			} catch(Exception x) {
				x.printStackTrace();
			}
			UIContext.internalClear();
		}
	}

	@NonNull
	private TestServerSession getSvSession() {
		if(null != m_ssession)
			return m_ssession;
		throw new IllegalStateException("Null SvSession");
	}

	private void setSvSession(TestServerSession ssession) {
		m_ssession = ssession;
	}


}
