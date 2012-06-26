package to.etc.domuidemo;

import java.io.*;

import javax.servlet.*;

import to.etc.dbpool.*;
import to.etc.domui.caches.images.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.header.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.themes.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.domuidemo.components.*;
import to.etc.domuidemo.db.*;
import to.etc.domuidemo.pages.*;
import to.etc.domuidemo.sourceviewer.*;
import to.etc.util.*;
import to.etc.webapp.query.*;

public class Application extends DomApplication {
	private boolean m_hibinit;

	Exception m_hibabort;

	/**
	 * Called at application start time: this initializes the application by initializing everything required by it.
	 *
	 * @see to.etc.domui.server.DomApplication#initialize(to.etc.domui.server.ConfigParameters)
	 */
	@Override
	protected void initialize(final ConfigParameters pp) throws Exception {
		ImageCache.initialize(32 * 1024 * 1024, 5l * 1024 * 1024 * 1024, new File("/tmp/imagecache"));

		if(DeveloperOptions.getBool("domui.newtheme", false)) {
			setThemeFactory(new FragmentedThemeFactory());
		} else {
			//-- Set the SIMPLE theme provider with the specified theme set.
			String stylename = DeveloperOptions.getString("domuidemo.theme");
			if(null != stylename)
				setThemeFactory(new SimpleThemeFactory(stylename, "orange", "blue"));
		}

		//-- Append the default style sheet.
		addHeaderContributor(HeaderContributor.loadStylesheet("css/style.css"), 1000); // Add default stylesheet for the app

		//-- If we have a Google Analytics code- add the script blurb to every page.
		String uacode = System.getProperty("uacode");
		if(!DomUtil.isBlank(uacode)) {
			addHeaderContributor(HeaderContributor.loadGoogleAnalytics(uacode), 0);
		}

		//-- Parallel initialization can run into tomcat synchronisation/classloader issues, so disable it by default.
		slowInit();

		/*
		 * Registreer een paar default exception handlers.
		 */
		addExceptionListener(Exception.class, new IExceptionListener() {
			@Override
			public boolean handleException(final IRequestContext ctx, final Page pg, final NodeBase source, final Throwable x) throws Exception {
				if(x instanceof RuntimeException || x instanceof UIException)
					return false;

				x.printStackTrace();
				source.addGlobalMessage(UIMessage.error(Msgs.BUNDLE, Msgs.UNEXPECTED_EXCEPTION, x.toString()));
				return true;
			}
		});

		//		addExceptionListener(ConstraintViolationException.class, new IExceptionListener() {
		//			public boolean handleException(final IRequestContext ctx, final Page pg, final NodeBase source, final Throwable x) throws Exception {
		//				ConstraintViolationException e = (ConstraintViolationException) x;
		//				x.printStackTrace();
		//				String msg = DaoConstraintMessage.getConstraintMessage(DbUtil.getContext(pg), e.getConstraintName()).getUserMessage().replaceAll("<br />", "\\\n");
		//				source.addGlobalMessage(UIMessage.error(Msgs.BUNDLE, Msgs.UNEXPECTED_EXCEPTION, msg));
		//				return true;
		//			}
		//		});


		/*
		 * Add a new page listener. Every new page automatically gets a Breadcrumb injected @ it's start
		 */
		addNewPageInstantiatedListener(new INewPageInstantiated() {
			@Override
			public void newPageInstantiated(final UrlPage body) throws Exception {
				onNewPage(body);
			}
		});
	}

	void onNewPage(final UrlPage p) throws Exception {
		if(p instanceof SourcePage)
			return;

		if(null != DomUtil.findComponentInTree(p, ShelveBreadCrumb.class))
			return;
		if(null != DomUtil.findComponentInTree(p, BreadCrumb.class))
			return;

		//-- Insert a shelve breadcrumb.
		p.add(0, new SourceBreadCrumb());
	}


	//	public VpContextCache getContextCache() {
	//		return m_contextCache;
	//	}

	@Override
	public Class< ? extends UrlPage> getRootPage() {
		return HomePage.class;
	}

	public void slowInit() throws UnavailableException {
		try {
			initDatabase();
			synchronized(this) {
				m_hibinit = true;
				notifyAll();
			}

//			//-- Initialize context cache && register interceptor
//			m_contextCache.initialize();
//			VP.internalInitialize(m_contextCache);
//			addInterceptor(new IRequestInterceptor() {
//				public void before(final IRequestContext rc) throws Exception {
//					RequestContextImpl rci = (RequestContextImpl) rc;
//					VpUserContext uc = getContextCache().associate(rci.getRequest());
//					if(uc == null)
//						throw new IllegalStateException("No logged-in user!!!");
//					VP.internalInitialize(uc);
//				}
//
//				public void after(final IRequestContext rc, final Exception x) throws Exception {
//					VP.internalInitialize((VpUserContext) null);
//				}
//			});
		} catch(Exception x) {
			x.printStackTrace();
			throw new UnavailableException("Cannot init database: " + x);
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Initializing and populating the demo database		*/
	/*--------------------------------------------------------------*/
	/**
	 * Initialize the database. This code uses the embedded Derby database but the same logic
	 * as shown here can be used to open any database. In addition, this code will see if the
	 * database is empty and if so will re-create the demo/example tables and populate them.
	 *
	 * @throws Exception
	 */
	private void initDatabase() throws Exception {
		String poolid = DeveloperOptions.getString("domuidemo.poolid"); // Is a poolid defined in .developer.proeprties? Then use that,
		ConnectionPool p;
		if(poolid != null) {
			//-- Local configuration. Init using local.
			System.out.println("** WARNING: Using local database configuration, pool=" + poolid);
			p = PoolManager.getInstance().initializePool(poolid);
		} else {
			//-- Must have a proper database file in web-inf
			File pf = getAppFile("WEB-INF/pool.xml");
			if(!pf.exists())
				throw new UnavailableException("Missing file WEB-INF/pool.xml containing the database to use");
			p = PoolManager.getInstance().initializePool(pf, "demo");
		}
		DBInitialize.fillDatabase(p.getUnpooledDataSource());
		DbUtil.initialize(p.getPooledDataSource());

		//-- Tell the generic layer how to create default DataContext's.
		QContextManager.initialize(DbUtil.getContextSource()); // Prime factory with connection source
	}

	synchronized void waitForInit() throws Exception {
		int tries = 10;
		for(;;) {
			if(m_hibinit)
				return;
			if(m_hibabort != null)
				throw new IllegalStateException("Hibernate initialization failed: " + m_hibabort);
			if(--tries <= 0)
				throw new IllegalStateException("Parallel Hibernate init took too long.");
			wait(10000);
		}
	}

	static public void main(String[] args) {
		try {
			File pf = new File("WebContent/WEB-INF/pool.xml");
			if(!pf.exists())
				throw new UnavailableException("Missing file WEB-INF/pool.xml containing the database to use");
			ConnectionPool p = PoolManager.getInstance().initializePool(pf, "demo");
			System.out.println("Got a db");
			DBInitialize.fillDatabase(p.getUnpooledDataSource());
		} catch(Exception x) {
			x.printStackTrace();
		}

	}
}
