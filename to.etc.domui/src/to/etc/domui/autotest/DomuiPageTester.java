package to.etc.domui.autotest;

import java.io.*;
import java.util.*;

import javax.annotation.*;
import javax.servlet.http.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;
import to.etc.domui.trouble.*;

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
		AppSession session = createTestSession();

		TestRequestResponse rr = createRequestResponse(pageClass, parameters);
		RequestContextImpl rci = new RequestContextImpl(rr, getApplication(), session);
		interact(rci);


	}

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

		TestRequestResponse rr = new TestRequestResponse(this, requestURI, pp);
		return rr;
	}



	private void interact(@Nonnull RequestContextImpl ctx) throws Exception {
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


	//	@Nonnull
	//	private Page instantiate(@Nonnull Class< ? extends UrlPage> pageClass, @Nonnull PageParameters parameters) {
	//
	//
	//		// TODO Auto-generated method stub
	//		return null;
	//	}

	@Nonnull
	private AppSession createTestSession() {
		AppSession ass = new AppSession(app());


		return ass;
	}


	@Nullable
	static private DomApplication m_appInstance;

	static synchronized public void initApplication(@Nonnull Class< ? extends DomApplication> applicationClass, @Nonnull File webappFiles) throws Exception {
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

	@Override
	@Nonnull
	public String getUserAgent() {
		return m_userAgent;
	}

	public void setUserAgent(@Nonnull String userAgent) {
		m_userAgent = userAgent;
	}


}
