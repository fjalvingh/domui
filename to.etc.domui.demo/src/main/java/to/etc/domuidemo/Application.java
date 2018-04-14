package to.etc.domuidemo;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.caches.images.ImageCache;
import to.etc.domui.component.layout.BreadCrumb;
import to.etc.domui.component.layout.ErrorMessageDiv;
import to.etc.domui.component.tbl.RowRenderer;
import to.etc.domui.component.tbl.RowRenderer.ColumnWidth;
import to.etc.domui.component.tbl.RowRenderer.IColumnListener;
import to.etc.domui.component.tbl.TableModelTableBase;
import to.etc.domui.derbydata.init.DbUtil;
import to.etc.domui.derbydata.init.TestDB;
import to.etc.domui.dom.errors.IExceptionListener;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.dom.header.FaviconContributor;
import to.etc.domui.dom.header.HeaderContributor;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.Page;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.server.ConfigParameters;
import to.etc.domui.server.DomApplication;
import to.etc.domui.server.IRequestContext;
import to.etc.domui.themes.sass.SassThemeFactory;
import to.etc.domui.trouble.UIException;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.INewPageInstantiated;
import to.etc.domui.util.Msgs;
import to.etc.domuidemo.components.PageHeader;
import to.etc.domuidemo.pages.HomePage;
import to.etc.domuidemo.sourceviewer.SourcePage;
import to.etc.formbuilder.pages.FormDesigner;
import to.etc.webapp.query.QContextManager;

import javax.servlet.UnavailableException;
import java.io.File;
import java.util.List;

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

		setDefaultThemeFactory(SassThemeFactory.INSTANCE);
		//
		//
		//String newtheme = DeveloperOptions.getString("domuidemo.simpletheme");
		//if(null != newtheme) {
		//	//-- Set the SIMPLE theme provider with the specified theme set.
		//	setDefaultThemeFactory(SimpleThemeFactory.INSTANCE);
		//	setDefaultTheme(newtheme);
		//} else {
		//	setDefaultThemeFactory(FragmentedThemeFactory.getInstance());
		//	String stylename = DeveloperOptions.getString("domuidemo.theme", "domui/domui/orange");        // Default to DomUI's native fragmented theme
		//	setDefaultTheme(stylename);
		//}

		//-- Append the default style sheet.
		addHeaderContributor(HeaderContributor.loadStylesheet("css/demostyle.scss"), 1000);	// Add default stylesheet for the app
		addHeaderContributor(new FaviconContributor("img/favicon.ico"), 10);

		//-- If we have a Google Analytics code- add the script blurb to every page.
		String uacode = System.getProperty("uacode");
		if(!DomUtil.isBlank(uacode)) {
			addHeaderContributor(HeaderContributor.loadGoogleAnalytics(uacode), 0);
		}

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
				if(null != source)
					source.addGlobalMessage(UIMessage.error(Msgs.BUNDLE, Msgs.UNEXPECTED_EXCEPTION, x.toString()));
				return true;
			}
		});

		/*
		 * Add a new page listener. Every new page automatically gets a Breadcrumb injected @ it's start
		 */
		addNewPageInstantiatedListener(new INewPageInstantiated() {
			@Override
			public void newPageBuilt(@NonNull UrlPage body) throws Exception {
				onNewPage(body);
			}

			@Override
			public void newPageCreated(@NonNull UrlPage body) throws Exception {
			}
		});

		/*
		 * Add a generic listener for column width changed events.
		 */
		setAttribute(RowRenderer.COLUMN_LISTENER, new IColumnListener<Object>() {
			@Override public void columnsChanged(TableModelTableBase<Object> tbl, List<ColumnWidth<Object, ?>> newWidths) throws Exception {
				saveColumnWidths(tbl, newWidths);
			}
		});


	}

	private void saveColumnWidths(TableModelTableBase<Object> tbl, List<ColumnWidth<Object, ?>> newWidths) {
		System.out.println("Columns changed for page " + tbl.getPage().getBody().getClass().getCanonicalName() + ", table " + tbl.getActualID() + ":");
		for(ColumnWidth<Object, ?> nw : newWidths) {
			System.out.println(" - column " + nw.getIndex() + " (" + nw.getColumn() + ") width " + nw.getWidth());
		}
	}

	@Override public void addDefaultErrorComponent(NodeContainer page) {
		NodeContainer panel = new ErrorMessageDiv(page, true);
		for(int i = 0; i < page.getChildCount(); i++) {
			NodeBase child = page.getChild(i);
			if(child instanceof PageHeader) {
				page.add(i + 1, panel);
				return;
			}
		}
		page.add(0, panel);
	}

	void onNewPage(final UrlPage p) throws Exception {
		if(p instanceof SourcePage || p instanceof FormDesigner)
			return;

		if(null != DomUtil.findComponentInTree(p, BreadCrumb.class))
			return;

		//-- Insert a shelve breadcrumb.
		p.add(0, new PageHeader());
	}

	@Override
	public Class<? extends UrlPage> getRootPage() {
		return HomePage.class;
	}

	public void slowInit() throws UnavailableException {
		try {
			initDatabase();
			synchronized(this) {
				m_hibinit = true;
				notifyAll();
			}

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
		File appFile = getAppFile(".").getAbsoluteFile();
		String context = appFile.getParentFile().getName();
		File dbPath = new File("/tmp/" + context + "DB");

		DbUtil.initialize(TestDB.getDataSource(dbPath.toString()));

		//-- Tell the generic layer how to create default DataContext's.
		QContextManager.setImplementation(QContextManager.DEFAULT, DbUtil.getContextSource()); // Prime factory with connection source
	}

	static public void main(String[] args) {
		try {
			TestDB.initialize();
		} catch(Exception x) {
			x.printStackTrace();
		}
	}
}
