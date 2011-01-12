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
import java.lang.reflect.*;
import java.util.*;

import javax.annotation.*;
import javax.servlet.http.*;

import org.slf4j.*;

import to.etc.domui.ajax.*;
import to.etc.domui.component.form.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.component.layout.title.*;
import to.etc.domui.component.lookup.*;
import to.etc.domui.dom.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.header.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.injector.*;
import to.etc.domui.login.*;
import to.etc.domui.parts.*;
import to.etc.domui.server.parts.*;
import to.etc.domui.state.*;
import to.etc.domui.themes.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.domui.util.resources.*;
import to.etc.template.*;
import to.etc.util.*;
import to.etc.webapp.nls.*;
import to.etc.webapp.query.*;

/**
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 22, 2008
 */
public abstract class DomApplication {
	static public final Logger LOG = LoggerFactory.getLogger(DomApplication.class);

	private final ApplicationRequestHandler m_requestHandler = new ApplicationRequestHandler(this);

	private final PartRequestHandler m_partHandler = new PartRequestHandler(this);

	private final ResourceRequestHandler m_resourceHandler = new ResourceRequestHandler(this, m_partHandler);

	private final AjaxRequestHandler m_ajaxHandler = new AjaxRequestHandler(this);

	private Set<IAppSessionListener> m_appSessionListeners = new HashSet<IAppSessionListener>();

	private File m_webFilePath;

	private String m_urlExtension;

	private ControlBuilder m_controlBuilder = new ControlBuilder(this);

	//	private String m_currentTheme = "domui";
	//
	//	private String m_currentIconSet = "domui";
	//
	//	private String m_currentColorSet = "domui";

	private boolean m_developmentMode;

	//	static private final ThreadLocal<DomApplication> m_current = new ThreadLocal<DomApplication>();

	static private DomApplication m_application;

	static private int m_nextPageTag = (int) (System.nanoTime() & 0x7fffffff);

	private final boolean m_logOutput = DeveloperOptions.getBool("domui.log", false);

	private List<IRequestInterceptor> m_interceptorList = new ArrayList<IRequestInterceptor>();

	/**
	 * Contains the header contributors in the order that they were added.
	 */
	private List<HeaderContributorEntry> m_orderedContributorList = Collections.EMPTY_LIST;

	private List<INewPageInstantiated> m_newPageInstListeners = Collections.EMPTY_LIST;

	/** Timeout for a window session, in minutes. */
	private int m_windowSessionTimeout = 15;

	/** The default expiry time for resources, in seconds. */
	private int m_defaultExpiryTime = 7 * 24 * 60 * 60;

	private ILoginAuthenticator m_loginAuthenticator;

	private ILoginDialogFactory m_loginDialogFactory;

	private List<ILoginListener> m_loginListenerList = Collections.EMPTY_LIST;

	private IPageInjector m_injector = new DefaultPageInjector();

	/**
	 * Must return the "root" class of the application; the class rendered when the application's
	 * root URL is entered without a class name.
	 * @return
	 */
	abstract public Class< ? extends UrlPage> getRootPage();

	/**
	 * Render factories for different browser versions.
	 */
	private List<IHtmlRenderFactory> m_renderFactoryList = new ArrayList<IHtmlRenderFactory>();

	final private String m_scriptVersion;

	/*--------------------------------------------------------------*/
	/*	CODING:	Initialization and session management.				*/
	/*--------------------------------------------------------------*/
	/**
	 * The only constructor.
	 */
	public DomApplication() {
		m_scriptVersion = DeveloperOptions.getString("domui.scriptversion", "jquery-1.4.1");
		registerControlFactories();
		registerPartFactories();
		initHeaderContributors();
		addRenderFactory(new MsCrapwareRenderFactory()); // Add html renderers for IE <= 8
		addExceptionListener(QNotFoundException.class, new IExceptionListener() {
			@Override
			public boolean handleException(final IRequestContext ctx, final Page page, final NodeBase source, final Throwable x) throws Exception {
				if(!(x instanceof QNotFoundException))
					throw new IllegalStateException("??");

				// data has removed in meanwhile: redirect to error page.
				String rurl = DomUtil.createPageURL(ExpiredDataPage.class, new PageParameters("errorMessage", x.getLocalizedMessage()));
				//-- Add info about the failed thingy.
				/*StringBuilder sb = new StringBuilder(1024);
				sb.append(rurl);
				sb.append("?errorMessage=");
				StringTool.encodeURLEncoded(sb, x.getLocalizedMessage());*/
				ApplicationRequestHandler.generateHttpRedirect((RequestContextImpl) ctx, rurl, "Data not found");
				return true;
			}
		});
		m_themer = new SimpleThemer("unsplit");
	}

	protected void registerControlFactories() {
		registerControlFactory(ControlFactory.STRING_CF);
		registerControlFactory(ControlFactory.TEXTAREA_CF);
		registerControlFactory(ControlFactory.BOOLEAN_AND_ENUM_CF);
		registerControlFactory(ControlFactory.DATE_CF);
		registerControlFactory(ControlFactory.RELATION_COMBOBOX_CF);
		registerControlFactory(ControlFactory.RELATION_LOOKUP_CF);
		registerControlFactory(new ControlFactoryMoney());
	}

	protected void registerPartFactories() {
		registerUrlPart(new ThemePartFactory()); // convert *.theme.* as a JSTemplate.
		registerUrlPart(new SvgPartFactory()); // Converts .svg.png to png.
	}

	static private synchronized void setCurrentApplication(DomApplication da) {
		m_application = da;
	}

	/**
	 * Returns the single DomApplication instance in use for the webapp.
	 * @return
	 */
	static synchronized public DomApplication get() {
		DomApplication da = m_application;
		if(da == null)
			throw new IllegalStateException("The 'current application' is unset!?");
		return da;
	}

	public synchronized void addSessionListener(final IAppSessionListener l) {
		m_appSessionListeners = new HashSet<IAppSessionListener>(m_appSessionListeners);
		m_appSessionListeners.add(l);
	}

	public synchronized void removeSessionListener(final IAppSessionListener l) {
		m_appSessionListeners = new HashSet<IAppSessionListener>(m_appSessionListeners);
		m_appSessionListeners.remove(l);
	}

	private synchronized Set<IAppSessionListener> getAppSessionListeners() {
		return m_appSessionListeners;
	}

	/**
	 * Returns the defined extension for DomUI pages. This returns the extension without
	 * the dot, i.e. "ui" for [classname].ui pages.
	 * @return
	 */
	public String getUrlExtension() {
		return m_urlExtension;
	}

	public IFilterRequestHandler findRequestHandler(final IRequestContext ctx) {
		//		System.out.println("Input: "+ctx.getInputPath());
		if(getUrlExtension().equals(ctx.getExtension()) || ctx.getExtension().equals("obit") || (getRootPage() != null && ctx.getInputPath().length() == 0)) {
			return m_requestHandler;
		} else if(m_partHandler.acceptURL(ctx.getInputPath())) {
			return m_partHandler;
		} else if(ctx.getInputPath().startsWith("$")) {
			return m_resourceHandler;
		} else if(ctx.getExtension().equals("xaja"))
			return m_ajaxHandler;
		return null;
	}

	/**
	 * Can be overridden to create your own instance of a session.
	 * @return
	 */
	protected AppSession createSession() {
		AppSession aps = new AppSession(this);
		return aps;
	}

	/**
	 * Called when the session is bound to the HTTPSession. This calls all session listeners.
	 * @param sess
	 */
	void registerSession(final AppSession aps) {
		for(IAppSessionListener l : getAppSessionListeners()) {
			try {
				l.sessionCreated(this, aps);
			} catch(Exception x) {
				x.printStackTrace();
			}
		}
	}

	void unregisterSession(final AppSession aps) {

	}

	final void internalDestroy() {
		LOG.info("Destroying application " + this);
		try {
			destroy();
		} catch(Throwable x) {
			AppFilter.LOG.error("Exception when destroying Application", x);
		}
	}

	/**
	 * Override to destroy resources when the application terminates.
	 */
	protected void destroy() {}

	/**
	 * Override to initialize the application, called as soon as the webabb starts by the
	 * filter's initialization code.
	 *
	 * @param pp
	 * @throws Exception
	 */
	protected void initialize(final ConfigParameters pp) throws Exception {}


	final synchronized public void internalInitialize(final ConfigParameters pp, boolean development) throws Exception {
		setCurrentApplication(this);

		//		m_myClassLoader = appClassLoader;
		m_webFilePath = pp.getWebFileRoot();

		//-- Get the page extension to use.
		String ext = pp.getString("extension");
		if(ext == null || ext.trim().length() == 0)
			m_urlExtension = "ui";
		else {
			ext = ext.trim();
			if(ext.startsWith("."))
				ext = ext.substring(1);
			if(ext.indexOf('.') != -1)
				throw new IllegalArgumentException("The 'extension' parameter contains too many dots...");
			m_urlExtension = ext;
		}
		initialize(pp);
		m_developmentMode = development;
	}

	static public synchronized final int internalNextPageTag() {
		int id = ++m_nextPageTag;
		if(id <= 0) {
			id = m_nextPageTag = 1;
		}
		return id;
	}

	final Class< ? > loadApplicationClass(final String name) throws ClassNotFoundException {
		/*
		 * jal 20081030 Code below is very wrong. When the application is not reloaded due to a
		 * change the classloader passed at init time does not change. But a new classloader will
		 * have been allocated!!
		 */
		//		return m_myClassLoader.loadClass(name);

		return getClass().getClassLoader().loadClass(name);
	}

	public Class< ? extends UrlPage> loadPageClass(final String name) {
		//-- This should be a classname now
		Class< ? > clz = null;
		try {
			clz = loadApplicationClass(name);
		} catch(ClassNotFoundException x) {
			throw new ThingyNotFoundException("404 class " + name + " not found");
		} catch(Exception x) {
			throw new IllegalStateException("Error in class " + name, x);
		}

		//-- Check type && validity,
		if(!NodeContainer.class.isAssignableFrom(clz))
			throw new IllegalStateException("Class " + clz + " is not a valid page class (does not extend " + UrlPage.class.getName() + ")");

		return (Class< ? extends UrlPage>) clz;
	}

	public String getScriptVersion() {
		return m_scriptVersion;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	HTML per-browser rendering code.					*/
	/*--------------------------------------------------------------*/
	/**
	 * Creates the appropriate full renderer for the specified browser version.
	 * @param bv
	 * @param o
	 * @return
	 */
	public HtmlFullRenderer findRendererFor(BrowserVersion bv, final IBrowserOutput o) {
		for(IHtmlRenderFactory f : getRenderFactoryList()) {
			HtmlFullRenderer tr = f.createFullRenderer(bv, o);
			if(tr != null)
				return tr;
		}

		return new StandardHtmlFullRenderer(new StandardHtmlTagRenderer(bv, o), o);
		//		HtmlTagRenderer base = new HtmlTagRenderer(bv, o);
		//		return new HtmlFullRenderer(base, o);
	}

	public HtmlTagRenderer findTagRendererFor(BrowserVersion bv, final IBrowserOutput o) {
		for(IHtmlRenderFactory f : getRenderFactoryList()) {
			HtmlTagRenderer tr = f.createTagRenderer(bv, o);
			if(tr != null)
				return tr;
		}
		return new StandardHtmlTagRenderer(bv, o);
	}

	private synchronized List<IHtmlRenderFactory> getRenderFactoryList() {
		return m_renderFactoryList;
	}

	public synchronized void addRenderFactory(IHtmlRenderFactory f) {
		if(m_renderFactoryList.contains(f))
			throw new IllegalStateException("Don't be silly, this one is already added");
		m_renderFactoryList = new ArrayList<IHtmlRenderFactory>(m_renderFactoryList);
		m_renderFactoryList.add(0, f);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Webapp configuration								*/
	/*--------------------------------------------------------------*/
	//	/**
	//	 * Returns the name of the current theme, like "blue". This returns the
	//	 * name only, not the URL to the theme or something.
	//	 */
	//	@Nonnull
	//	public synchronized String getCurrentTheme() {
	//		return m_currentTheme;
	//	}
	//
	//	/**
	//	 * Sets a new default theme. The theme name is the name of a directory, like "blue", below the
	//	 * "themes" map in the webapp or the root resources.
	//	 * @param defaultTheme
	//	 */
	//	public synchronized void setDefaultTheme(@Nonnull final String defaultTheme) {
	//		if(null == defaultTheme)
	//			throw new IllegalArgumentException();
	//		m_currentTheme = defaultTheme;
	//	}
	//
	//	/**
	//	 * Get the name of the current icon set. This must resolve to a directory "icons/[name]" in
	//	 * either the class resources or the webapp.
	//	 * @return
	//	 */
	//	@Nonnull
	//	public synchronized String getCurrentIconSet() {
	//		return m_currentIconSet;
	//	}
	//
	//	/**
	//	 * Set the name of the current icon set. This must resolve to a directory "icons/[name]" in
	//	 * either the class resources or the webapp.
	//	 *
	//	 * @param currentIconSet
	//	 */
	//	public synchronized void setCurrentIconSet(@Nonnull String currentIconSet) {
	//		if(null == currentIconSet)
	//			throw new IllegalArgumentException();
	//		m_currentIconSet = currentIconSet;
	//	}
	//
	//	@Nonnull
	//	public synchronized String getCurrentColorSet() {
	//		return m_currentColorSet;
	//	}
	//
	//	public synchronized void setCurrentColorSet(@Nonnull String currentColorSet) {
	//		if(null == currentColorSet)
	//			throw new IllegalArgumentException();
	//		m_currentColorSet = currentColorSet;
	//	}

	/**
	 * Returns T when running in development mode; this is defined as a mode where web.xml contains
	 * reloadable classes.
	 * @return
	 */
	public boolean inDevelopmentMode() {
		return m_developmentMode;
	}

	/**
	 * The #of minutes that a WindowSession remains valid; defaults to 15 minutes.
	 *
	 * @return
	 */
	public int getWindowSessionTimeout() {
		return m_windowSessionTimeout;
	}

	/**
	 * Sets the windowSession timeout, in minutes.
	 * @param windowSessionTimeout
	 */
	public void setWindowSessionTimeout(final int windowSessionTimeout) {
		m_windowSessionTimeout = windowSessionTimeout;
	}

	/**
	 * Returns the default browser cache resource expiry time in seconds. When
	 * running in production mode all "static" resources are sent to the browser
	 * with an "Expiry" header. This causes the browser to cache the resources
	 * until the expiry time has been reached. This is important for performance.
	 * @return
	 */
	public synchronized int getDefaultExpiryTime() {
		return m_defaultExpiryTime;
	}

	/**
	 * Set the static resource browser cache expiry time, in seconds.
	 * @param defaultExpiryTime
	 */
	public synchronized void setDefaultExpiryTime(final int defaultExpiryTime) {
		m_defaultExpiryTime = defaultExpiryTime;
	}

	/**
	 * This returns the locale to use for the request passed. It defaults to the locale
	 * in the request itself, as returned by {@link HttpServletRequest#getLocale()}. You
	 * can override this method to define the locale by yourself.
	 * @param request
	 * @return
	 */
	@Nonnull
	public Locale getRequestLocale(HttpServletRequest request) {
		return request.getLocale();
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Global header contributors.							*/
	/*--------------------------------------------------------------*/

	protected void initHeaderContributors() {
		addHeaderContributor(HeaderContributor.loadJavascript("$js/jquery.js"), -1000);
		addHeaderContributor(HeaderContributor.loadJavascript("$js/ui.core.js"), -990);
		addHeaderContributor(HeaderContributor.loadJavascript("$js/ui.draggable.js"), -980);
		addHeaderContributor(HeaderContributor.loadJavascript("$js/jquery.blockUI.js"), -970);
		addHeaderContributor(HeaderContributor.loadJavascript("$js/domui.js"), -900);
		addHeaderContributor(HeaderContributor.loadJavascript("$js/weekagenda.js"), -790);
		addHeaderContributor(HeaderContributor.loadJavascript("$js/jquery.wysiwyg.js"), -780);
		addHeaderContributor(HeaderContributor.loadStylesheet("$js/jquery.wysiwyg.css"), -780);

		/*
		 * FIXME: Delayed construction of components causes problems with components
		 * that are delayed and that contribute. Example: tab pabel containing a
		 * DateInput. The TabPanel gets built when header contributions have already
		 * been handled... For now we add all JS files here 8-(
		 */
		addHeaderContributor(HeaderContributor.loadJavascript("$js/calendar.js"), -780);
		addHeaderContributor(HeaderContributor.loadJavascript("$js/calendar-setup.js"), -770);
		//-- Localized calendar resources are added per-page.

		/*
		 * FIXME Same as above, this is for loading the FCKEditor.
		 */
		addHeaderContributor(HeaderContributor.loadJavascript("$fckeditor/fckeditor.js"), -760);
	}


	/**
	 * Call from within the onHeaderContributor call on a node to register any header
	 * contributors needed by a node. The order value determines the order for contributors
	 * which is mostly important for Javascript ones; higher order items are written later than
	 * lower order items. All DomUI required Javascript code has orders < 0; user code should
	 * start at 0 and go up.
	 *
	 * @param hc
	 * @param order
	 */
	final public synchronized void addHeaderContributor(final HeaderContributor hc, int order) {
		for(HeaderContributorEntry hce : m_orderedContributorList) {
			if(hce.getContributor().equals(hc))
				throw new IllegalArgumentException("The header contributor " + hc + " has already been added.");
		}

		m_orderedContributorList = new ArrayList<HeaderContributorEntry>(m_orderedContributorList); // Dup the original list,
		m_orderedContributorList.add(new HeaderContributorEntry(hc, order)); // And add the new'un
	}

	public synchronized List<HeaderContributorEntry> getHeaderContributorList() {
		return m_orderedContributorList;
	}

	/**
	 * When a page has no error handling components (no component has registered an error listener) then
	 * errors will not be visible. If such a page encounters an error it will call this method; the default
	 * implementation will add an ErrorPanel as the first component in the Body; this panel will then
	 * accept and render the errors.
	 *
	 * @param page
	 */
	public void addDefaultErrorComponent(final NodeContainer page) {
		ErrorPanel panel = new ErrorPanel();
		page.add(0, panel);
	}

	/**
	 * FIXME This code requires an absolute title which is not needed for the
	 * DomUI framework. It's also only needed for the "BasicPage" and has no
	 * meaning for any other part of the framework. It should move to some
	 * BasicPage factory.
	 *
	 * This returns default page title component.
	 * {@link AppPageTitleBar} is default one used by framework.
	 * To set some custom page title component override this method in your application specific class.
	 *
	 * @param title
	 * @return
	 */
	@Deprecated
	public BasePageTitleBar getDefaultPageTitleBar(String title) {
		return new AppPageTitleBar(title);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Control factories.									*/
	/*--------------------------------------------------------------*/

	/**
	 * Return the component that knows everything you ever wanted to know about controls - but were afraid to ask...
	 */
	final public ControlBuilder getControlBuilder() {
		return m_controlBuilder;
	}

	/**
	 * Add a new control factory to the registry.
	 * @param cf		The new factory
	 */
	final public void registerControlFactory(final ControlFactory cf) {
		getControlBuilder().registerControlFactory(cf);
	}

	/**
	 * Register a new LookupControl factory.
	 * @param f
	 */
	public void register(final ILookupControlFactory f) {
		getControlBuilder().register(f);
	}

	//	/**
	//	 * Get the immutable list of current control factories.
	//	 * @return
	//	 */
	//	private synchronized List<ControlFactory> getControlFactoryList() {
	//		return m_controlFactoryList;
	//	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Generic data factories.								*/
	/*--------------------------------------------------------------*/
	/**
	 * FIXME Needs a proper, injected implementation instead of a quicky.
	 */
	public <T> T createInstance(final Class<T> clz, final Object... args) {
		try {
			return clz.newInstance();
		} catch(IllegalAccessException x) {
			throw new WrappedException(x);
		} catch(InstantiationException x) {
			throw new WrappedException(x);
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	WebApp resource management.							*/
	/*--------------------------------------------------------------*/
	/**
	 * Return a file from the webapp's root directory. Example: passing WEB-INF/web.xml
	 * would return the file for the web.xml.
	 *
	 * @param path
	 * @return
	 */
	public File getAppFile(final String path) {
		return new File(m_webFilePath, path);
	}

	/**
	 * Returns the root of the webapp's installation directory on the local file system.
	 * @return
	 */
	public final File getWebAppFileRoot() {
		return m_webFilePath;
	}

	public String getApplicationURL() {
		return AppFilter.getApplicationURL();
	}

	/** Cache for application resources containing all resources we have checked existence for */
	private final Map<String, IResourceRef> m_resourceSet = new HashMap<String, IResourceRef>();

	private final Map<String, Boolean> m_knownResourceSet = new HashMap<String, Boolean>();

	/**
	 * Create a resource ref to a class based resource. If we are running in DEBUG mode this will
	 * generate something which knows the source of the resource, so it can handle changes to that
	 * source while developing.
	 *
	 * @param name
	 * @return
	 */
	private IResourceRef createClasspathReference(String name) {
		if(inDevelopmentMode()) {
			//-- If running in debug mode get this classpath resource's original source file
			IModifyableResource ts = ClasspathInventory.getInstance().findResourceSource(name);
			return new ReloadingClassResourceRef(ts, name);
		}
		return new ProductionClassResourceRef(name);
	}

	/**
	 * Tries to resolve an application-based resource by decoding it's name, and throw an exception if not found. We allow
	 * the following constructs:
	 * <ul>
	 *	<li>$RES/xxxx: denotes a class-based resource. The xxxx is the full package/classname of the resource</li>
	 *	<li>$THEME/xxxx: denotes a current-theme based resource.</li>
	 * </ul>
	 *
	 * @param name
	 * @return
	 */
	@Nonnull
	public IResourceRef getApplicationResourceByName(String name) {
		IResourceRef ref = internalFindResource(name);

		/*
		 * The code below was needed because the original code caused a 404 exception on web resources which were
		 * checked every time. All other resource types like class resources were not checked for existence.
		 */
		if(ref instanceof WebappResourceRef) {
			if(ref.getLastModified() == -1)
				throw new ThingyNotFoundException(name);
		}
		return ref;
	}

	private IResourceRef tryVersionedResource(String name) {
		name = "/resources/" + name;
		if(!DomUtil.classResourceExists(getClass(), name))
			return null;
		return createClasspathReference(name);
	}

	/**
	 * Quickly determines if a given resource exists. Enter with the full resource path, like $js/xxx, THEME/xxx and the like; it
	 * mirrors the logic of {@link #getApplicationResourceByName(String)}.
	 * @param name
	 * @return
	 */
	public boolean hasApplicationResource(final String name) {
		synchronized(this) {
			Boolean k = m_knownResourceSet.get(name);
			if(k != null)
				return k.booleanValue();
		}

		//-- Determine existence out-of-lock (single init is unimportant)
		IResourceRef ref = internalFindCachedResource(name);
		Boolean k = Boolean.valueOf(ref.getLastModified() != -1);
		//		System.out.println("hasAppResource: locate " + ref + ", exists=" + k);
		synchronized(this) {
			m_knownResourceSet.put(name, k);
		}
		return k.booleanValue();
	}

	/**
	 * Cached version to locate an application resource.
	 * @param name
	 * @return
	 */
	private IResourceRef internalFindCachedResource(@Nonnull String name) {
		IResourceRef ref;
		synchronized(this) {
			ref = m_resourceSet.get(name);
			if(ref != null)
				return ref;
		}

		//-- Determine existence out-of-lock (single init is unimportant)
		ref = internalFindResource(name);
		synchronized(this) {
			m_resourceSet.put(name, ref);
		}
		return ref;
	}

	/**
	 * UNCACHED version to locate a resource. This handles all special DomUI url's
	 * starting with '$' but also all webapp-relative requests. It also handles the
	 * "scriptVersion" logic and the expanded/compressed logic for $js/ resources.
	 *
	 * @param name
	 * @return
	 */
	@Nonnull
	private IResourceRef internalFindResource(@Nonnull String name) {
		if(name.startsWith(Constants.RESOURCE_PREFIX)) {
			return createClasspathReference(name.substring(Constants.RESOURCE_PREFIX.length() - 1)); // Strip off $RES, rest is absolute resource path starting with /
		}

		if(name.startsWith("$")) {
			name = name.substring(1);
			//-- 1. Is a file-based resource available?
			File f = getAppFile(name);
			if(f.exists())
				return new WebappResourceRef(f);
			// 20091019 jal removed: $ resources are literal entries; they are never classnames - that is done using $RES/ only.
			//			//-- In the url, replace all '.' but the last one with /
			//			int pos = name.lastIndexOf('.');
			//			if(pos != -1) {
			//				name = name.substring(0, pos).replace('.', '/') + name.substring(pos);
			//			}

			/*
			 * For class-based resources we are able to select different versions of a resource if it's name
			 * starts with $js/. These will be scanned in resources/js/[scriptversion]/[name] and resources/js/[name].
			 */
			if(!name.startsWith("js/"))
				return createClasspathReference("/resources/" + name);

			//-- 1. Create a 'min version of the name
			name = name.substring(2); // Strip js, leave leading /.
			int pos = name.lastIndexOf('.');
			String min = pos < 0 ? null : name.substring(0, pos) + "-min" + name.substring(pos);

			StringBuilder sb = new StringBuilder(64);
			IResourceRef r;
			if(!inDevelopmentMode() && min != null) {
				//-- Try all min versions in production, first
				sb.append("js/").append(getScriptVersion()).append(min);
				r = tryVersionedResource(sb.toString());
				if(r != null)
					return r;
				sb.setLength(0);
				sb.append("js").append(min);
				r = tryVersionedResource(sb.toString());
				if(r != null)
					return r;
			}

			//-- Try normal versions only in development.
			sb.setLength(0);
			sb.append("js/").append(getScriptVersion()).append(name);
			r = tryVersionedResource(sb.toString());
			if(r != null)
				return r;

			r = createClasspathReference("/resources/js" + name);
			//			System.out.println("RR: Default ref to " + name + " is " + r);
			return r;
		}

		//-- Normal url. Use webapp-direct path.
		File src = new File(m_webFilePath, name);
		return new WebappResourceRef(src);
	}

	/**
	 * This returns the name of an <i>existing</i> resource for the given name/suffix and locale. It uses the
	 * default DomUI/webapp.core resource resolution pattern.
	 *
	 * @see BundleRef#loadBundleList(Locale)
	 *
	 * @param basename		The base name: the part before the locale info
	 * @param suffix		The suffix: the part after the locale info. This usually includes a ., like .js
	 * @param loc			The locale to get the resource for.
	 * @return
	 */
	public String findLocalizedResourceName(final String basename, final String suffix, final Locale loc) {
		StringBuilder sb = new StringBuilder(128);
		String s;
		s = tryKey(sb, basename, suffix, loc.getLanguage(), loc.getCountry(), loc.getVariant(), NlsContext.getDialect());
		if(s != null)
			return s;
		s = tryKey(sb, basename, suffix, loc.getLanguage(), loc.getCountry(), loc.getVariant(), null);
		if(s != null)
			return s;
		s = tryKey(sb, basename, suffix, loc.getLanguage(), loc.getCountry(), null, NlsContext.getDialect());
		if(s != null)
			return s;
		s = tryKey(sb, basename, suffix, loc.getLanguage(), loc.getCountry(), null, null);
		if(s != null)
			return s;
		s = tryKey(sb, basename, suffix, loc.getLanguage(), null, null, NlsContext.getDialect());
		if(s != null)
			return s;
		s = tryKey(sb, basename, suffix, loc.getLanguage(), null, null, null);
		if(s != null)
			return s;
		s = tryKey(sb, basename, suffix, null, null, null, NlsContext.getDialect());
		if(s != null)
			return s;
		s = tryKey(sb, basename, suffix, null, null, null, null);
		if(s != null)
			return s;
		return null;
	}

	private String tryKey(final StringBuilder sb, final String basename, final String suffix, final String lang, final String country, final String variant, final String dialect) {
		sb.setLength(0);
		sb.append(basename);
		if(dialect != null && dialect.length() > 0) {
			sb.append('_');
			sb.append(dialect);
		}
		if(lang != null && lang.length() > 0) {
			sb.append('_');
			sb.append(lang);
		}
		if(country != null && country.length() > 0) {
			sb.append('_');
			sb.append(country);
		}
		if(variant != null && variant.length() > 0) {
			sb.append('_');
			sb.append(variant);
		}
		if(suffix != null && suffix.length() > 0)
			sb.append(suffix);
		String res = sb.toString();
		if(hasApplicationResource(res))
			return res;
		return null;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Code table cache.									*/
	/*--------------------------------------------------------------*/

	private final Map<String, ListRef< ? >> m_listCacheMap = new HashMap<String, ListRef< ? >>();

	static private final class ListRef<T> {
		private List<T> m_list;

		private final ICachedListMaker<T> m_maker;

		public ListRef(final ICachedListMaker<T> maker) {
			m_maker = maker;
		}

		public synchronized List<T> initialize() throws Exception {
			if(m_list == null)
				m_list = m_maker.createList(DomApplication.get());
			return m_list;
		}
	}

	/**
	 *
	 * @param <T>
	 * @param key
	 * @param maker
	 * @return
	 */
	public <T> List<T> getCachedList(final IListMaker<T> maker) throws Exception {
		if(!(maker instanceof ICachedListMaker< ? >)) {
			//-- Just make on the fly.
			return maker.createList(this);
		}

		ICachedListMaker<T> cm = (ICachedListMaker<T>) maker;
		ListRef<T> ref;
		String key = cm.getCacheKey();
		synchronized(m_listCacheMap) {
			ref = (ListRef<T>) m_listCacheMap.get(key);
			if(ref == null) {
				ref = new ListRef<T>(cm);
				m_listCacheMap.put(key, ref);
			}
		}
		return new ArrayList<T>(ref.initialize());
	}

	/**
	 * Discard all cached stuff in the list cache.
	 */
	public void clearListCaches() {
		synchronized(m_listCacheMap) {
			m_listCacheMap.clear();
		}
		// FIXME URGENT Clear all other server's caches too by sending a VP event.
	}

	public void clearListCache(final ICachedListMaker< ? > maker) {
		synchronized(m_listCacheMap) {
			m_listCacheMap.remove(maker.getCacheKey());
		}
		// FIXME URGENT Clear all other server's caches too by sending a VP event.
	}

	public boolean logOutput() {
		return m_logOutput;
	}

	public synchronized void addInterceptor(final IRequestInterceptor r) {
		List<IRequestInterceptor> l = new ArrayList<IRequestInterceptor>(m_interceptorList);
		l.add(r);
		m_interceptorList = l;
	}

	public synchronized List<IRequestInterceptor> getInterceptorList() {
		return m_interceptorList;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Exception translator handling.						*/
	/*--------------------------------------------------------------*/
	/**
	 * An entry in the exception table.
	 */
	static public class ExceptionEntry {
		private final Class< ? extends Exception> m_exceptionClass;

		private final IExceptionListener m_listener;

		public ExceptionEntry(final Class< ? extends Exception> exceptionClass, final IExceptionListener listener) {
			m_exceptionClass = exceptionClass;
			m_listener = listener;
		}

		public Class< ? extends Exception> getExceptionClass() {
			return m_exceptionClass;
		}

		public IExceptionListener getListener() {
			return m_listener;
		}
	}

	/** The ORDERED list of [exception.class, handler] pairs. Exception SUPERCLASSES are ordered AFTER their subclasses. */
	private List<ExceptionEntry> m_exceptionListeners = new ArrayList<ExceptionEntry>();

	/**
	 * Return the current, immutable, threadsafe copy of the list-of-listeners.
	 * @return
	 */
	private synchronized List<ExceptionEntry> getExceptionListeners() {
		return m_exceptionListeners;
	}

	/**
	 * Adds an exception handler for a given exception type. The handler is inserted ordered,
	 * exceptions that are a superclass of other exceptions in the list are sorted AFTER their
	 * subclass (this prevents the handler for the superclass from being called all the time).
	 * Any given exception type may occur in this list only once or an exception occurs.
	 *
	 * @param l
	 */
	public synchronized void addExceptionListener(final Class< ? extends Exception> xclass, final IExceptionListener l) {
		m_exceptionListeners = new ArrayList<ExceptionEntry>(m_exceptionListeners);

		//-- Do a sortish insert.
		for(int i = 0; i < m_exceptionListeners.size(); i++) {
			ExceptionEntry ee = m_exceptionListeners.get(i);
			if(ee.getExceptionClass() == xclass) {
				//-- Same class-> replace the handler with the new one.
				m_exceptionListeners.set(i, new ExceptionEntry(xclass, l));
				return;
			} else if(ee.getExceptionClass().isAssignableFrom(xclass)) {
				//-- Class [ee] is a SUPERCLASS of [xclass]; you can do [ee] = [xclass]. We need to add this handler BEFORE this superclass!
				m_exceptionListeners.add(i, new ExceptionEntry(xclass, l));
				return;
			}
		}
		m_exceptionListeners.add(new ExceptionEntry(xclass, l));
	}

	/**
	 * This locates the handler for the specfied exception type, if it has been registered. It
	 * currently uses a loop to locate the appropriate handler.
	 * @param x
	 * @return	null if the handler was not registered.
	 */
	public IExceptionListener findExceptionListenerFor(final Exception x) {
		Class< ? extends Exception> xclass = x.getClass();
		for(ExceptionEntry ee : getExceptionListeners()) {
			if(ee.getExceptionClass().isAssignableFrom(xclass))
				return ee.getListener();
		}
		return null;
	}

	public synchronized void addNewPageInstantiatedListener(final INewPageInstantiated l) {
		m_newPageInstListeners = new ArrayList<INewPageInstantiated>(m_newPageInstListeners);
		m_newPageInstListeners.add(l);
	}

	public synchronized void removeNewPageInstantiatedListener(final INewPageInstantiated l) {
		m_newPageInstListeners = new ArrayList<INewPageInstantiated>(m_newPageInstListeners);
		m_newPageInstListeners.remove(l);
	}

	public synchronized List<INewPageInstantiated> getNewPageInstantiatedListeners() {
		return m_newPageInstListeners;
	}

	public synchronized ILoginAuthenticator getLoginAuthenticator() {
		return m_loginAuthenticator;
	}

	public synchronized void setLoginAuthenticator(final ILoginAuthenticator loginAuthenticator) {
		m_loginAuthenticator = loginAuthenticator;
	}

	public synchronized ILoginDialogFactory getLoginDialogFactory() {
		return m_loginDialogFactory;
	}

	public synchronized void setLoginDialogFactory(final ILoginDialogFactory loginDialogFactory) {
		m_loginDialogFactory = loginDialogFactory;
	}

	public synchronized void addLoginListener(final ILoginListener l) {
		if(m_loginListenerList.contains(l))
			return;
		m_loginListenerList = new ArrayList<ILoginListener>(m_loginListenerList);
		m_loginListenerList.add(l);
	}

	public synchronized List<ILoginListener> getLoginListenerList() {
		return m_loginListenerList;
	}

	/**
	 * Responsible for redirecting to the appropriate login page. This default implementation checks
	 * to see if there is an authenticator registered and uses it's result to redirect. If no
	 * authenticator is registered this returns null, asking the caller to do normal exception
	 * handling.
	 *
	 * @param ci
	 * @param page
	 * @param nlix
	 */
	public String handleNotLoggedInException(RequestContextImpl ci, Page page, NotLoggedInException x) {
		ILoginDialogFactory ldf = ci.getApplication().getLoginDialogFactory();
		if(ldf == null)
			return null; // Nothing can be done- I don't know how to log in.

		//-- Redirect to the LOGIN page, passing the current page to return back to.
		String target = ldf.getLoginRURL(x.getURL()); // Create a RURL to move to.
		if(target == null)
			throw new IllegalStateException("The Login Dialog Handler=" + ldf + " returned an invalid URL for the login dialog.");

		//-- Make this an absolute URL by appending the webapp path
		return ci.getRelativePath(target);
	}

	/**
	 * Get the page injector.
	 * @return
	 */
	public synchronized IPageInjector getInjector() {
		return m_injector;
	}

	public synchronized void setInjector(IPageInjector injector) {
		m_injector = injector;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Rights registry.									*/
	/*--------------------------------------------------------------*/
	private final Map<String, BundleRef> m_rightsBundleMap = new HashMap<String, BundleRef>();

	/**
	 * Registers a set of possible rights and their names/translation bundle.
	 * @param bundle
	 * @param rights
	 */
	public void registerRight(final BundleRef bundle, final String... rights) {
		synchronized(m_rightsBundleMap) {
			for(String r : rights) {
				if(!m_rightsBundleMap.containsKey(r))
					m_rightsBundleMap.put(r, bundle);
			}
		}
	}

	/**
	 * Takes a class (or interface) and scans all static public final String fields therein. For
	 * each field it's literal string value is used as a rights name and associated with the bundle.
	 * If a right already exists it is skipped, meaning the first ever definition of a right wins.
	 *
	 * @param bundle
	 * @param constantsclass
	 */
	public void registerRights(final BundleRef bundle, final Class< ? > constantsclass) {
		//-- Find all class fields.
		Field[] far = constantsclass.getDeclaredFields();
		synchronized(m_rightsBundleMap) {
			for(Field f : far) {
				int mod = f.getModifiers();
				if(Modifier.isFinal(mod) && Modifier.isPublic(mod) && Modifier.isStatic(mod)) {
					if(f.getType() == String.class) {
						try {
							String s = (String) f.get(null);
							if(s != null) {
								if(!m_rightsBundleMap.containsKey(s)) {
									m_rightsBundleMap.put(s, bundle);
									//									System.out.println("app: registering right="+s);
								}
							}
						} catch(Exception x) {
							// Ignore all exceptions due to accessing the field using Introspection
						}
					}
				}
			}
		}
	}

	/**
	 * Return a list of all currently registered right names.
	 * @return
	 */
	public List<String> getRegisteredRights() {
		synchronized(m_rightsBundleMap) {
			return new ArrayList<String>(m_rightsBundleMap.keySet());
		}
	}

	/**
	 * Translates a right name to a description from the registered bundle, if registered.
	 * @param right
	 * @return
	 */
	public String findRightsDescription(final String right) {
		BundleRef br;
		synchronized(m_rightsBundleMap) {
			br = m_rightsBundleMap.get(right);
		}
		return br == null ? null : br.findMessage(NlsContext.getLocale(), right);
	}

	/**
	 * Translates a right name to a description from the registered bundle, if registered. Returns the right name if no bundle or description is
	 * found.
	 * @param right
	 * @return
	 */
	public String getRightsDescription(final String right) {
		String v = findRightsDescription(right);
		return v == null ? right : v;
	}

	public AjaxRequestHandler getAjaxHandler() {
		return m_ajaxHandler;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Programmable theme code.							*/
	/*--------------------------------------------------------------*/
	/** The thing that themes the application. Set only once @ init time. */
	private IThemer m_themer = new SimpleThemer("domui");

	private ITheme m_themeStore;

	private ResourceDependencies m_themeDependencies;

	public synchronized IThemer getThemer() {
		return m_themer;
	}

	/**
	 * Set the factory for handling the theme.
	 * @param themer
	 */
	public synchronized void setThemer(IThemer themer) {
		m_themer = themer;
		m_themeStore = null;
		m_themeDependencies = null;
	}


	/**
	 * FIXME Mechanism is slow
	 * @param factory
	 */
	public void registerUrlPart(IUrlPart factory) {
		m_partHandler.registerUrlPart(factory);
	}

	public String getThemeReplacedString(@Nonnull ResourceDependencyList rdl, String rurl) throws Exception {
		return getThemeReplacedString(rdl, rurl, null);
	}

	/**
	 * EXPENSIVE CALL - ONLY USE TO CREATE CACHED RESOURCES
	 *
	 * This loads a theme resource as an utf-8 encoded template, then does expansion using the
	 * current theme's variable map. This map is either a "style.properties" file
	 * inside the theme's folder, or can be configured dynamically using a IThemeMapFactory.
	 *
	 * The result is returned as a string.
	 *
	 * @param rdl
	 * @param key
	 * @return
	 */
	public String getThemeReplacedString(@Nonnull ResourceDependencyList rdl, @Nonnull String rurl, @Nullable BrowserVersion bv) throws Exception {
		long ts = System.nanoTime();
		IResourceRef ires = getApplicationResourceByName(rurl); // Get the template source file
//		if(ires == null)
//			throw new ThingyNotFoundException("The theme-replaced file " + rurl + " cannot be found");
		rdl.add(ires); // We're dependent on it...

		//-- Get the variable map to use.
		Map<String, Object> themeMap = getThemeMap(rdl);
		themeMap = new HashMap<String, Object>(themeMap); // Create a modifyable duplicate
		if(bv != null) {
			themeMap.put("browser", bv);
		}
		themeMap.put("util", new ThemeCssUtils());

		augmentThemeMap(themeMap); // Provide a hook to let user code add stuff to the theme map

		//-- 2. Get a reader.
		InputStream is = ires.getInputStream();
		if(is == null) {
			System.out.println(">>>> RESOURCE ERROR: " + rurl + ", ref=" + ires);
			throw new ThingyNotFoundException("Unexpected: cannot get input stream for IResourceRef rurl=" + rurl + ", ref=" + ires);
		}
		try {
			Reader r = new InputStreamReader(is, "utf-8");
			StringBuilder sb = new StringBuilder(65536);

			JSTemplateCompiler tc = new JSTemplateCompiler();
			tc.executeMap(sb, r, rurl, themeMap);

			ts = System.nanoTime() - ts;
			System.out.println("theme-replace: " + rurl + " took " + StringTool.strNanoTime(ts));
			return sb.toString();
		} finally {
			try {
				is.close();
			} catch(Exception x) {}
		}
	}

	/**
	 * Get the theme that is used for this application. The dependencies for the theme will
	 * be added to the dependency list. This allows users of the theme to update themselves
	 * when (parts of) the theme change.
	 *
	 * @param rdl
	 * @return
	 * @throws Exception
	 */
	public ITheme getTheme(@Nullable ResourceDependencyList rdl) throws Exception {
		synchronized(this) {
			//-- Do we have a theme store present?
			if(m_themeStore != null) {
				//-- Yes-> has it expired?
				if(m_themeDependencies == null) // We're not checking (not in debug mode)
					return m_themeStore;

				if(!m_themeDependencies.isModified()) { // Not expired?
					if(rdl != null)
						rdl.add(m_themeDependencies);
					return m_themeStore;
				}

				//-- We have an expired one...
			}

			//-- We need to (re)load a theme store.
			m_themeStore = getThemer().loadTheme(this);
			if(inDevelopmentMode()) {
				ThemeModifyableResource tmr = new ThemeModifyableResource(m_themeStore.getDependencies(), 3000); // Check for changes every 3 secs
				m_themeDependencies = new ResourceDependencies(new IIsModified[]{tmr});
				if(rdl != null)
					rdl.add(m_themeDependencies);
			}
		}

		return m_themeStore;
	}

	/**
	 * This method can be overridden to add extra stuff to the theme map, after
	 * it has been loaded from properties or whatnot.
	 * @param themeMap
	 */
	protected void augmentThemeMap(Map<String, Object> themeMap) {}

	/**
	 * Return the current theme map (a readonly map), cached from the last
	 * time. It will refresh automatically when the resource dependencies
	 * for the theme are updated.
	 *
	 * @param rdl
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getThemeMap(ResourceDependencyList rdlin) throws Exception {
		ITheme ts = getTheme(rdlin);
		Map<String, Object> tmap = ts.getThemeProperties();
		return tmap;
	}

	@Nullable
	public String getThemedResourceRURL(String path) {
		if(null == path)
			return null;
		if(path.startsWith("THEME/"))
			path = path.substring(6); // Strip THEME/
		else if(path.startsWith("ICON/"))
			path = path.substring(5); // Strip ICON
		else
			return path; // Not theme-relative, so return as-is.

		//-- We need to translate this according to the icon rules.
		try {
			String res = getTheme(null).getIconURL(path);
			return res == null ? path : res;
		} catch(Exception x) {
			throw WrappedException.wrap(x);
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	DomUI state listener handling.						*/
	/*--------------------------------------------------------------*/

	private List<IDomUIStateListener> m_uiStateListeners = Collections.EMPTY_LIST;

	/**
	 * Register a listener for internal DomUI events.
	 * @param sl
	 */
	public synchronized void addUIStateListener(IDomUIStateListener sl) {
		m_uiStateListeners = new ArrayList<IDomUIStateListener>(m_uiStateListeners); // Dup list;
		m_uiStateListeners.add(sl);
	}

	/**
	 * Remove a registered UI state listener.
	 * @param sl
	 */
	public synchronized void removeUIStateListener(IDomUIStateListener sl) {
		m_uiStateListeners = new ArrayList<IDomUIStateListener>(m_uiStateListeners); // Dup list;
		m_uiStateListeners.remove(sl);
	}

	private synchronized List<IDomUIStateListener> getUIStateListeners() {
		return m_uiStateListeners;
	}

	public final void internalCallWindowSessionCreated(WindowSession ws) {
		for(IDomUIStateListener sl : getUIStateListeners()) {
			try {
				sl.windowSessionCreated(ws);
			} catch(Exception x) {
				x.printStackTrace();
			}
		}
	}

	public final void internalCallWindowSessionDestroyed(WindowSession ws) {
		for(IDomUIStateListener sl : getUIStateListeners()) {
			try {
				sl.windowSessionDestroyed(ws);
			} catch(Exception x) {
				x.printStackTrace();
			}
		}
	}

	public final void internalCallConversationCreated(ConversationContext ws) {
		for(IDomUIStateListener sl : getUIStateListeners()) {
			try {
				sl.conversationCreated(ws);
			} catch(Exception x) {
				x.printStackTrace();
			}
		}
	}

	public final void internalCallConversationDestroyed(ConversationContext ws) {
		for(IDomUIStateListener sl : getUIStateListeners()) {
			try {
				sl.conversationDestroyed(ws);
			} catch(Exception x) {
				x.printStackTrace();
			}
		}
	}

	public final void internalCallPageFullRender(RequestContextImpl ctx, Page ws) {
		for(IDomUIStateListener sl : getUIStateListeners()) {
			try {
				sl.onBeforeFullRender(ctx, ws);
			} catch(Exception x) {
				x.printStackTrace();
			}
		}
	}

	public final void internalCallPageAction(RequestContextImpl ctx, Page ws) {
		for(IDomUIStateListener sl : getUIStateListeners()) {
			try {
				sl.onBeforePageAction(ctx, ws);
			} catch(Exception x) {
				x.printStackTrace();
			}
		}
	}

	public final void internalCallPageComplete(IRequestContext ctx, Page ws) {
		for(IDomUIStateListener sl : getUIStateListeners()) {
			try {
				sl.onAfterPage(ctx, ws);
			} catch(Exception x) {
				x.printStackTrace();
			}
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Silly helpers.										*/
	/*--------------------------------------------------------------*/

}
