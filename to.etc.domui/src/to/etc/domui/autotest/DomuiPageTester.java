package to.etc.domui.autotest;

import java.io.*;
import java.util.*;

import javax.annotation.*;
import javax.servlet.http.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;
import to.etc.domui.trouble.*;
import to.etc.util.*;

/**
 * Experimental.
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 2, 2013
 */
public class DomuiPageTester implements IDomUITestInfo {
	private static class PageRef {
		@Nonnull
		private final Class< ? extends UrlPage> m_pageClass;

		@Nonnull
		private final PageParameters	m_parameters;

		public PageRef(@Nonnull Class< ? extends UrlPage> pageClass, @Nonnull PageParameters parameters) {
			m_pageClass = pageClass;
			m_parameters = parameters;
		}

		@Nonnull
		public Class< ? extends UrlPage> getPageClass() {
			return m_pageClass;
		}

		@Nonnull
		public PageParameters getParameters() {
			return m_parameters;
		}
	}

	@Nonnull
	final private List<PageRef> m_pageList = new ArrayList<PageRef>();

	@Nonnull
	private String m_userAgent = "Mozilla/5.0 (Windows; U; MSIE 9.0; Windows NT 9.0; en-US)";

	@Nullable
	static private DomApplication m_appInstance;

	@Nullable
	private TestServerSession m_ssession;


	public void addPage(@Nonnull Class< ? extends UrlPage> page, @Nonnull PageParameters pp) {
		m_pageList.add(new PageRef(page, pp));
	}

	public void addPage(@Nonnull Class< ? extends UrlPage> page, Object... data) {
		PageParameters pp = new PageParameters(data);
		m_pageList.add(new PageRef(page, pp));
	}

	public void run() throws Exception {
		for(PageRef pr : m_pageList)
			run(pr.getPageClass(), pr.getParameters());
	}

	private void run(@Nonnull Class< ? extends UrlPage> pageClass, @Nonnull PageParameters parameters) throws Exception {
		checkInit();

		//-- We need an appsession for this page.
		AppSession appSession = createTestSession();
		m_ssession = new TestServerSession();

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
					System.out.println(StringTool.strTrunc(doc, 512));
					newrr = handleDocument(doc);
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
	private TestRequestResponse handleDocument(@Nonnull String doc) {
		//-- Contains javascript redirect (initial-response)?
		String str = "location.replace(";
		int pos = doc.indexOf(str);
		if(pos > 0) {
			return handleDocumentRedirect(doc, pos + str.length());
		}


		return null;
	}

	@Nullable
	private TestRequestResponse handleDocumentRedirect(@Nonnull String doc, int pos) {
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

	@Nullable
	private TestRequestResponse handleRedirect(@Nonnull TestRequestResponse rr) {
		String redirectURL = rr.getRedirectURL();
		if(null == redirectURL)
			throw new IllegalStateException("Null redirect URL in test response");
		return handleRedirectTo(redirectURL);
	}

	@Nullable
	private TestRequestResponse handleRedirectTo(@Nonnull String redirectURL) {
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

		PageParameters pp = decodeParameters(query);
		TestRequestResponse rr = new TestRequestResponse(m_ssession, this, redirectURL, pp);
		return rr;
	}

	/**
	 * Decode a http query string into a PageParameters instance.
	 * @param query
	 * @return
	 */
	@Nonnull
	private PageParameters decodeParameters(@Nonnull String query) {
		String[] indiar = query.split("&");
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		for(String frag : indiar) {
			int pos = frag.indexOf('=');
			if(pos >= 0) {
				String name = frag.substring(0, pos).toLowerCase();
				String value = frag.substring(pos + 1);
				name = StringTool.decodeURLEncoded(name);
				value = StringTool.decodeURLEncoded(value);

				List<String>	l = map.get(name);
				if(null == l) {
					l = new ArrayList<String>();
					map.put(name, l);
				}
				l.add(value);
			}
		}

		PageParameters pp = new PageParameters();
		for(Map.Entry<String, List<String>> me : map.entrySet()) {
			if(me.getValue().size() == 1) {
				pp.addParameter(me.getKey(), me.getValue().get(0));
			} else {
				pp.addParameter(me.getKey(), me.getValue().toArray(new String[0]));
			}
		}
		return pp;
	}

	/**
	 * Create a request/response object for the specified page.
	 * @param clz
	 * @param pp
	 * @return
	 */
	@Nonnull
	private TestRequestResponse createRequestResponse(@Nonnull Class< ? extends UrlPage> clz, @Nonnull PageParameters pp) {
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

		String requestURI = sb.toString();

		TestRequestResponse rr = new TestRequestResponse(m_ssession, this, requestURI, pp);
		return rr;
	}



	private void interact(@Nonnull AppSession session, @Nonnull TestRequestResponse rr) throws Exception {
		RequestContextImpl ctx = new RequestContextImpl(rr, getApplication(), session);

		List<IRequestInterceptor> il = ctx.getApplication().getInterceptorList();
		Exception xx = null;
		IFilterRequestHandler rh = null;
		try {
			UIContext.internalSet(ctx);
			AbstractContextMaker.callInterceptorsBegin(il, ctx);
			rh = ctx.getApplication().findRequestHandler(ctx);
			if(rh == null) {
				//-- Non-DomUI request.
				throw new IllegalStateException("Test DomUI request not a DomUI URL: " + ctx.getRequestResponse().getRequestURI());
			}
			rh.handleRequest(ctx);
			ctx.flush();
			rr.flush();

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

	@Nonnull
	private AppSession createTestSession() {
		AppSession ass = new AppSession(app());


		return ass;
	}


	static synchronized public void initApplication(@Nonnull Class< ? extends DomApplication> applicationClass, @Nonnull File webappFiles) throws Exception {
		AppFilter.initializeLogging(null);

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

	@Nonnull
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
	@Nonnull
	public DomApplication getApplication() {
		return app();
	}

	@Override
	@Nonnull
	public String getApplicationHost() {
		return "http://www.test.nl/";
	}

	@Nonnull
	@Override
	public String getWebappContext() {
		return "test";
	}

	@Nonnull
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
	@Nonnull
	public String getUserAgent() {
		return m_userAgent;
	}

	public void setUserAgent(@Nonnull String userAgent) {
		m_userAgent = userAgent;
	}

	@Override
	@Nullable
	public String getRemoteUser() {
		return "VPC";
	}


}
