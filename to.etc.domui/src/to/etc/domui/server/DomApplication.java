package to.etc.domui.server;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.logging.*;

import to.etc.domui.ajax.*;
import to.etc.domui.component.form.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.component.lookup.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.dom.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.header.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.login.*;
import to.etc.domui.server.parts.*;
import to.etc.domui.state.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.domui.util.resources.*;
import to.etc.util.*;
import to.etc.webapp.nls.*;

/**
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 22, 2008
 */
public abstract class DomApplication {
	static public final Logger LOG = Logger.getLogger(DomApplication.class.getName());

	private final ApplicationRequestHandler m_requestHandler = new ApplicationRequestHandler(this);

	private final PartRequestHandler m_partHandler = new PartRequestHandler(this);

	private final ResourceRequestHandler m_resourceHandler = new ResourceRequestHandler(this, m_partHandler);

	private final AjaxRequestHandler m_ajaxHandler = new AjaxRequestHandler(this);

	private Set<IAppSessionListener> m_appSessionListeners = new HashSet<IAppSessionListener>();

	private File m_webFilePath;

	private String m_urlExtension;

	private List<ControlFactory> m_controlFactoryList = new ArrayList<ControlFactory>();

	private String m_defaultTheme = "blue";

	private boolean m_developmentMode;

	private final LookupControlRegistry m_lookupControlRegistry = new LookupControlRegistry();

	static private final ThreadLocal<DomApplication> m_current = new ThreadLocal<DomApplication>();

	static private int m_nextPageTag = (int) (System.nanoTime() & 0x7fffffff);

	private final boolean m_logOutput = DeveloperOptions.getBool("domui.log", false);

	private List<IRequestInterceptor> m_interceptorList = new ArrayList<IRequestInterceptor>();

	/**
	 * Contains the header contributors in the order that they were added.
	 */
	private List<HeaderContributor> m_orderedContributorList = Collections.EMPTY_LIST;

	private List<INewPageInstantiated> m_newPageInstListeners = Collections.EMPTY_LIST;

	private IControlLabelFactory m_controlLabelFactory = new DefaultControlLabelFactory();

	/** Timeout for a window session, in minutes. */
	private int m_windowSessionTimeout = 15;

	/** The default expiry time for resources, in seconds. */
	private int m_defaultExpiryTime = 7 * 24 * 60 * 60;

	private ILoginAuthenticator m_loginAuthenticator;

	private ILoginDialogFactory m_loginDialogFactory;

	private List<ILoginListener> m_loginListenerList = Collections.EMPTY_LIST;

	/**
	 * Must return the "root" class of the application; the class rendered when the application's
	 * root URL is entered without a class name.
	 * @return
	 */
	abstract public Class< ? extends UrlPage> getRootPage();

	/*--------------------------------------------------------------*/
	/*	CODING:	Initialization and session management.				*/
	/*--------------------------------------------------------------*/
	/**
	 * The only constructor.
	 */
	public DomApplication() {
		m_controlFactoryList.add(ControlFactory.STRING_CF);
		m_controlFactoryList.add(ControlFactory.TEXTAREA_CF);
		m_controlFactoryList.add(ControlFactory.BOOLEAN_AND_ENUM_CF);
		m_controlFactoryList.add(ControlFactory.DATE_CF);
		m_controlFactoryList.add(ControlFactory.RELATION_COMBOBOX_CF);
		m_controlFactoryList.add(ControlFactory.RELATION_LOOKUP_CF);
		m_controlFactoryList.add(new ControlFactoryMoney());
	}

	static public void internalSetCurrent(final DomApplication da) {
		m_current.set(da);
	}

	/**
	 * Returns the single DomApplication instance in use for the webapp.
	 * @return
	 */
	static public DomApplication get() {
		DomApplication da = m_current.get();
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
		if(getUrlExtension().equals(ctx.getExtension()) || (getRootPage() != null && ctx.getInputPath().length() == 0)) {
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
		AppSession aps = new AppSession();
		aps.internalInitialize(this);
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
		LOG.fine("Destroying application " + this);
		try {
			destroy();
		} catch(Throwable x) {
			AppFilter.LOG.log(Level.INFO, "Exception when destroying Application", x);
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


	final public void internalInitialize(final ConfigParameters pp) throws Exception {
		//		m_myClassLoader = appClassLoader;
		m_webFilePath = pp.getWebFileRoot();

		//-- Get the page extension to use.
		m_urlExtension = "http";
		String ext = pp.getString("extension");
		if(ext != null && ext.trim().length() > 0) {
			ext = ext.trim();
			if(ext.startsWith("."))
				ext = ext.substring(1);
			if(ext.indexOf('.') != -1)
				throw new IllegalArgumentException("The 'extension' parameter contains too many dots...");
			m_urlExtension = ext;
		}
		initialize(pp);
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


	protected FullHtmlRenderer findRendererFor(final String useragent, final IBrowserOutput o) {
		HtmlRenderer base = new HtmlRenderer(o);
		return new FullHtmlRenderer(base, o);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Webapp configuration								*/
	/*--------------------------------------------------------------*/
	/**
	 * Returns the name of the current theme, like "blue". This returns the
	 * name only, not the URL to the theme or something.
	 */
	public synchronized String getDefaultTheme() {
		return m_defaultTheme;
	}

	/**
	 * Sets a new default theme. The theme name is the name of a directory, like "blue", below the
	 * "themes" map in the webapp or the root resources.
	 * @param defaultTheme
	 */
	public synchronized void setDefaultTheme(final String defaultTheme) {
		m_defaultTheme = defaultTheme;
	}

	/**
	 * Returns T when running in development mode; this is defined as a mode where web.xml contains
	 * reloadable classes.
	 * @return
	 */
	public boolean inDevelopmentMode() {
		return m_developmentMode;
	}

	/**
	 * DO NOT USE Force the webapp in development mode.
	 * @param developmentMode
	 */
	public void setDevelopmentMode(final boolean developmentMode) {
		m_developmentMode = developmentMode;
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

	/*--------------------------------------------------------------*/
	/*	CODING:	Global header contributors.							*/
	/*--------------------------------------------------------------*/
	/**
	 * Call from within the onHeaderContributor call on a node to register any header
	 * contributors needed by a node.
	 * @param hc
	 */
	final public synchronized void addHeaderContributor(final HeaderContributor hc) {
		m_orderedContributorList = new ArrayList<HeaderContributor>(m_orderedContributorList); // Dup the original list,
		m_orderedContributorList.add(hc); // And add the new'un
	}

	public synchronized List<HeaderContributor> getHeaderContributorList() {
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


	/*--------------------------------------------------------------*/
	/*	CODING:	Control factories.									*/
	/*--------------------------------------------------------------*/
	/**
	 * Add a new control factory to the registry.
	 * @param cf		The new factory
	 */
	public synchronized void registerControlFactory(final ControlFactory cf) {
		m_controlFactoryList = new ArrayList<ControlFactory>(m_controlFactoryList); // Dup original
		m_controlFactoryList.add(cf);
	}

	/**
	 * Get the immutable list of current control factories.
	 * @return
	 */
	private synchronized List<ControlFactory> getControlFactoryList() {
		return m_controlFactoryList;
	}

	/**
	 * Find the best control factory to use to create a control for the given property and mode.
	 * @param pmm		The property to find a control for
	 * @param editable	When false this is a displayonly control request.
	 * @return			null if no factory is found.
	 */
	public ControlFactory findControlFactory(final PropertyMetaModel pmm, final boolean editable) {
		ControlFactory best = null;
		int score = 0;
		for(ControlFactory cf : getControlFactoryList()) {
			int v = cf.accepts(pmm, editable);
			if(v > score) {
				score = v;
				best = cf;
			}
		}
		return best;
	}

	/**
	 * Find the best control factory to use to create a control for the given property and mode, throws
	 * an Exception if the factory cannot be found.
	 *
	 * @param pmm
	 * @param editable
	 * @return	The factory to use
	 */
	public ControlFactory getControlFactory(final PropertyMetaModel pmm, final boolean editable) {
		ControlFactory cf = findControlFactory(pmm, editable);
		if(cf == null)
			throw new IllegalStateException("Cannot get a control factory for " + pmm);
		return cf;
	}


	/**
	 * Add another LookupControlFactory to the registry.
	 * @param f
	 */
	public void register(final LookupControlFactory f) {
		m_lookupControlRegistry.register(f);
	}

	public LookupControlFactory findLookupControlFactory(final PropertyMetaModel pmm) {
		return m_lookupControlRegistry.findFactory(pmm);
	}

	public LookupControlFactory getLookupControlFactory(final PropertyMetaModel pmm) {
		return m_lookupControlRegistry.getControlFactory(pmm);
	}

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

	/** Cache for 'hasApplicationResource' containing all resources we have checked existence for */
	private final Map<String, Boolean> m_knownResourceSet = new HashMap<String, Boolean>();

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
		Boolean k = Boolean.valueOf(internalHasResource(name));
		synchronized(this) {
			m_knownResourceSet.put(name, k);
		}
		return k.booleanValue();
	}

	/**
	 * Determines the existence of a resource. This <b>must</b> mirror the logic of {@link #getApplicationResourceByName(String)}.
	 *
	 * @param name
	 * @return
	 */
	private boolean internalHasResource(String name) {
		if(name == null || name.length() == 0)
			return false;
		if(name.startsWith(Constants.RESOURCE_PREFIX))
			return DomUtil.classResourceExists(getClass(), name.substring(Constants.RESOURCE_PREFIX.length() - 1));
		if(name.startsWith("$")) {
			name = name.substring(1);

			//-- 1. Is a file-based resource available?
			File f = getAppFile(name);
			if(f.exists() && f.isFile())
				return true;

			//-- 2. Must be /resources/ class resource. In the url, replace all '.' but the last one with /
			int pos = name.lastIndexOf('.');
			if(pos != -1) {
				name = name.substring(0, pos).replace('.', '/') + name.substring(pos);
			}
			return DomUtil.classResourceExists(getClass(), "/resources/" + name);
		}

		//-- Normal unprefixed rurl. Use webapp-direct path.
		File src = new File(m_webFilePath, name);
		if(src.exists() && src.isFile())
			return true;
		return false;
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
		if(dialect != null) {
			sb.append('_');
			sb.append(dialect);
		}
		if(lang != null) {
			sb.append('_');
			sb.append(lang);
		}
		if(country != null) {
			sb.append('_');
			sb.append(country);
		}
		if(variant != null) {
			sb.append('_');
			sb.append(variant);
		}
		if(suffix != null)
			sb.append(suffix);
		String res = sb.toString();
		if(hasApplicationResource(res))
			return res;
		return null;
	}

	/**
	 * Tries to resolve an application-based resource by decoding it's name. We allow
	 * the following constructs:
	 * <ul>
	 *	<li>$RES/xxxx: denotes a class-based resource. The xxxx is the full package/classname of the resource</li>
	 *	<li>$THEME/xxxx: denotes a current-theme based resource.</li>
	 * </ul>
	 *
	 * @param name
	 * @return
	 */
	public IResourceRef getApplicationResourceByName(String name) {
		if(name.startsWith(Constants.RESOURCE_PREFIX))
			return new ClassResourceRef(getClass(), name.substring(Constants.RESOURCE_PREFIX.length() - 1));
		if(name.startsWith("$")) {
			name = name.substring(1);

			//-- 1. Is a file-based resource available?
			File f = getAppFile(name);
			if(f.exists())
				return new WebappResourceRef(f);

			//-- In the url, replace all '.' but the last one with /
			int pos = name.lastIndexOf('.');
			if(pos != -1) {
				name = name.substring(0, pos).replace('.', '/') + name.substring(pos);
			}
			return new ClassResourceRef(getClass(), "/resources/" + name);
		}

		//		if(name.startsWith(Constants.THEME_PREFIX)) {
		//			//-- Is an override available? If so use that one;
		//			String	rel	= "themes/"+name.substring(Constants.THEME_PREFIX.length());	// Reform '$THEME/blue/style.css' to 'themes/blue/style.css'
		//			File	src	= new File(m_webFilePath, rel);
		//			if(src.exists())
		//				return new WebappResourceRef(src);
		//
		//			//-- Theme data. Try to get it from a resource; use the webapp path if it does not exist.
		//			return new ClassResourceRef(getClass(), "/"+rel);
		//		}
		//
		//-- Normal url. Use webapp-direct path.
		File src = new File(m_webFilePath, name);
		if(src.exists())
			return new WebappResourceRef(src);
		throw new ThingyNotFoundException(name);
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
			if(ee.getExceptionClass().isAssignableFrom(xclass)) {
				//-- Class [ee] is a SUPERCLASS of [xclass]; you can do [ee] = [xclass]. We need to add this handler BEFORE this superclass! If they are the SAME class throw up,
				if(ee.getExceptionClass() == xclass)
					throw new IllegalStateException("An exception handler for Exception=" + xclass + " has already been registered. There can be only one handler for every exception type.");
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

	public synchronized IControlLabelFactory getControlLabelFactory() {
		return m_controlLabelFactory;
	}

	public synchronized void setControlLabelFactory(final IControlLabelFactory controlLabelFactory) {
		m_controlLabelFactory = controlLabelFactory;
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
						} catch(Exception x) {}
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
		return br == null ? null : br.findMessage(NlsContext.getLocale(), "right." + right);
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
	/*	CODING:	Silly helpers.										*/
	/*--------------------------------------------------------------*/

}
