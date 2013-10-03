package to.etc.domui.autotest;

import java.io.*;
import java.util.*;

import javax.annotation.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;

/**
 * Experimental.
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 2, 2013
 */
public class DomuiPageTester {
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

	private void run(@Nonnull Class< ? extends UrlPage> pageClass, @Nonnull PageParameters parameters) {
		checkInit();

		//-- We need an appsession for this page.
		AppSession session = createTestSession();



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


}
