package to.etc.domui.server;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import to.etc.domui.component.form.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.component.lookup.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.dom.FullHtmlRenderer;
import to.etc.domui.dom.HtmlRenderer;
import to.etc.domui.dom.BrowserOutput;
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

/**
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 22, 2008
 */
public abstract class DomApplication {
	static public final Logger	LOG	= Logger.getLogger(DomApplication.class.getName());

	private final ApplicationRequestHandler	m_requestHandler	= new ApplicationRequestHandler(this);

	private final PartRequestHandler			m_partHandler 		= new PartRequestHandler(this);

	private final ResourceRequestHandler		m_resourceHandler	= new ResourceRequestHandler(this, m_partHandler);

	private Set<AppSessionListener>		m_appSessionListeners = new HashSet<AppSessionListener>();

	private File						m_webFilePath;

	private String						m_urlExtension;

	private final List<ControlFactory>		m_controlFactoryList = new ArrayList<ControlFactory>();

	private String						m_defaultTheme = "blue";

	private boolean						m_developmentMode;

	private final LookupControlRegistry		m_lookupControlRegistry = new LookupControlRegistry();

	static private final ThreadLocal<DomApplication>		m_current = new ThreadLocal<DomApplication>();

	static private int					m_nextPageTag = (int)(System.nanoTime() & 0x7fffffff);

	private final boolean						m_logOutput = DeveloperOptions.getBool("domui.log", false);

	private List<IRequestInterceptor>	m_interceptorList = new ArrayList<IRequestInterceptor>();

	/**
	 * Contains the header contributors in the order that they were added.
	 */
	private List<HeaderContributor>		m_orderedContributorList = Collections.EMPTY_LIST;

	private List<INewPageInstantiated>	m_newPageInstListeners = Collections.EMPTY_LIST;

	private IControlLabelFactory		m_controlLabelFactory = new DefaultControlLabelFactory();

	/** Timeout for a window session, in minutes. */
	private int							m_windowSessionTimeout = 15;

	/** The default expiry time for resources, in seconds. */
	private int							m_defaultExpiryTime = 7*24*60*60;

	private ILoginAuthenticator				m_loginAuthenticator;

	private ILoginDialogFactory			m_loginDialogFactory;

	public DomApplication() {
		m_controlFactoryList.add(ControlFactory.STRING_CF);
		m_controlFactoryList.add(ControlFactory.BOOLEAN_AND_ENUM_CF);
		m_controlFactoryList.add(ControlFactory.DATE_CF);
		m_controlFactoryList.add(ControlFactory.RELATION_COMBOBOX_CF);
		m_controlFactoryList.add(ControlFactory.RELATION_LOOKUP_CF);
	}

	static public void		internalSetCurrent(final DomApplication da) {
		m_current.set(da);
	}
	static public DomApplication	get() {
		DomApplication da = m_current.get();
		if(da == null)
			throw new IllegalStateException("The 'current application' is unset!?");
		return da;
	}

	public synchronized void	addSessionListener(final AppSessionListener l) {
		m_appSessionListeners = new HashSet<AppSessionListener>(m_appSessionListeners);
		m_appSessionListeners.add(l);
	}
	public synchronized void	removeSessionListener(final AppSessionListener l) {
		m_appSessionListeners = new HashSet<AppSessionListener>(m_appSessionListeners);
		m_appSessionListeners.remove(l);
	}
	private synchronized Set<AppSessionListener>	getAppSessionListeners() {
		return m_appSessionListeners;
	}
	public String getUrlExtension() {
		return m_urlExtension;
	}

	abstract public Class<? extends UrlPage>	getRootPage();

	public FilterRequestHandler	findRequestHandler(final RequestContext ctx) {
//		System.out.println("Input: "+ctx.getInputPath());
		if(getUrlExtension().equals(ctx.getExtension()) || (getRootPage() != null && ctx.getInputPath().length() == 0)) {
			return m_requestHandler;
		} else if(m_partHandler.acceptURL(ctx.getInputPath())) {
			return m_partHandler;
		} else if(ctx.getInputPath().startsWith("$")) {
			return m_resourceHandler;
		}

		return null;
	}

	/**
	 * Can be overridden to create your own instance of a session.
	 * @return
	 */
	protected AppSession	createSession() {
		AppSession aps = new AppSession();
		aps.internalInitialize(this);
		return aps;
	}

	/**
	 * Called when the session is bound to the HTTPSession. This calls all session listeners.
	 * @param sess
	 */
	void		registerSession(final AppSession aps) {
		for(AppSessionListener l : getAppSessionListeners()) {
			try {
				l.sessionCreated(this, aps);
			} catch(Exception x) {
				x.printStackTrace();
			}
		}
	}

	void	unregisterSession(final AppSession aps) {

	}


	final void internalDestroy() {
		LOG.fine("Destroying application "+this);
		try {
			destroy();
		} catch(Throwable x) {
			AppFilter.LOG.log(Level.INFO, "Exception when destroying Application", x);
		}
	}

	protected void	destroy() {}

	protected void	initialize(final ConfigParameters pp) throws Exception {}


	final public void		internalInitialize(final ConfigParameters pp) throws Exception {
//		m_myClassLoader = appClassLoader;
		m_webFilePath = pp.getWebFileRoot();

		//-- Get the page extension to use.
		m_urlExtension = "http";
		String ext = pp.getString("extension");
		if(ext != null && ext.trim().length() > 0) {
			ext	= ext.trim();
			if(ext.startsWith("."))
				ext = ext.substring(1);
			if(ext.indexOf('.') != -1)
				throw new IllegalArgumentException("The 'extension' parameter contains too many dots...");
			m_urlExtension = ext;
		}
		initialize(pp);
	}

	static public synchronized final int	internalNextPageTag() {
		int id = ++m_nextPageTag;
		if(id <= 0) {
			id = m_nextPageTag = 1;
		}
		return id;
	}

	final Class<?>		loadApplicationClass(final String name) throws ClassNotFoundException {
		/*
		 * jal 20081030 Code below is very wrong. When the application is not reloaded due to a
		 * change the classloader passed at init time does not change. But a new classloader will
		 * have been allocated!!
		 */
//		return m_myClassLoader.loadClass(name);

		return getClass().getClassLoader().loadClass(name);
	}

	public Class<? extends UrlPage>	loadPageClass(final String name) {
		//-- This should be a classname now
		Class<?>	clz = null;
		try {
			clz	= loadApplicationClass(name);
		} catch(ClassNotFoundException x) {
			throw new ThingyNotFoundException("404 class "+name+" not found");
		} catch(Exception x) {
			throw new IllegalStateException("Error in class "+name, x);
		}

		//-- Check type && validity,
		if(! NodeContainer.class.isAssignableFrom(clz))
			throw new IllegalStateException("Class "+clz+" is not a valid page class (does not extend "+UrlPage.class.getName()+")");

		return (Class<? extends UrlPage>)clz;
	}


	protected FullHtmlRenderer	findRendererFor(final String useragent, final BrowserOutput o) {
		HtmlRenderer	base = new HtmlRenderer(o);
		return new FullHtmlRenderer(base, o);
	}

//	public synchronized List<ErrorMessageListener>	getDefaultErrorListeners() {
//		if(m_defaultErrorListeners == null) {
//			m_defaultErrorListeners = new ArrayList<ErrorMessageListener>();
//			m_defaultErrorListeners.add(new DefaultErrorMessageListener());
//		}
//		return m_defaultErrorListeners;
//	}
//
//	public synchronized void		setDefaultErrorListeners(List<ErrorMessageListener> nw) {
//		m_defaultErrorListeners = new ArrayList<ErrorMessageListener>(nw);		// Dup the list,
//	}

	/**
	 * Return a file from the webapp's web context.
	 *
	 * @param path
	 * @return
	 */
	public File	getAppFile(final String path) {
		return new File(m_webFilePath, path);
	}

	public synchronized String getDefaultTheme() {
		return m_defaultTheme;
	}
	public synchronized void setDefaultTheme(final String defaultTheme) {
		m_defaultTheme = defaultTheme;
	}
	public boolean inDevelopmentMode() {
		return m_developmentMode;
	}
	public void setDevelopmentMode(final boolean developmentMode) {
		m_developmentMode = developmentMode;
	}

	public int getWindowSessionTimeout() {
		return m_windowSessionTimeout;
	}
	public void setWindowSessionTimeout(final int windowSessionTimeout) {
		m_windowSessionTimeout = windowSessionTimeout;
	}
	public synchronized int getDefaultExpiryTime() {
		return m_defaultExpiryTime;
	}

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
	final public synchronized void	addHeaderContributor(final HeaderContributor hc) {
		m_orderedContributorList = new ArrayList<HeaderContributor>(m_orderedContributorList);	// Dup the original list,
		m_orderedContributorList.add(hc);					// And add the new'un
	}
	public synchronized List<HeaderContributor> getHeaderContributorList() {
		return m_orderedContributorList;
	}
	/*--------------------------------------------------------------*/
	/*	CODING:	Control factories.									*/
	/*--------------------------------------------------------------*/

	public void		registerControlFactory(final ControlFactory cf) {
		m_controlFactoryList.add(cf);
	}

	public ControlFactory	findControlFactory(final PropertyMetaModel pmm) {
		ControlFactory	best = null;
		int score = 0;
		for(ControlFactory cf : m_controlFactoryList) {
			int v = cf.accepts(pmm);
			if(v > score) {
				score = v;
				best = cf;
			}
		}
		return best;
	}

	public ControlFactory	getControlFactory(final PropertyMetaModel pmm) {
		ControlFactory cf = findControlFactory(pmm);
		if(cf == null)
			throw new IllegalStateException("Cannot get a control factory for "+pmm);
		return cf;
	}



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
	public <T> T	createInstance(final Class<T> clz, final Object... args) {
		try {
			return clz.newInstance();
		} catch(IllegalAccessException x) {
			throw new JamesGoslingIsAnIdiotException(x);
		} catch (InstantiationException x) {
			throw new JamesGoslingIsAnIdiotException(x);
		}
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
	public IResourceRef		getApplicationResourceByName(String name) {
		if(name.startsWith(Constants.RESOURCE_PREFIX))
			return new ClassResourceRef(getClass(), name.substring(Constants.RESOURCE_PREFIX.length()-1));
		if(name.startsWith("$")) {
			name	= name.substring(1);

			//-- 1. Is a file-based resource available?
			File	f 	= getAppFile(name);
			if(f.exists())
				return new WebappResourceRef(f);

			//-- In the url, replace all '.' but the last one with /
			int	pos	= name.lastIndexOf('.');
			if(pos != -1) {
				name = name.substring(0, pos).replace('.', '/')+name.substring(pos);
			}
			return new ClassResourceRef(getClass(), "/resources/"+name);
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
		File	src	= new File(m_webFilePath, name);
		if(src.exists())
			return new WebappResourceRef(src);
		throw new ThingyNotFoundException(name);
	}

	/**
	 * When a page has no error handling components (no component has registered an error listener) then
	 * errors will not be visible. If such a page encounters an error it will call this method; the default
	 * implementation will add an ErrorPanel as the first component in the Body; this panel will then
	 * accept and render the errors.
	 *
	 * @param page
	 */
	public void		addDefaultErrorComponent(final NodeContainer page) {
		ErrorPanel	panel = new ErrorPanel();
		page.add(0, panel);
	}



	/*--------------------------------------------------------------*/
	/*	CODING:	Code table cache.									*/
	/*--------------------------------------------------------------*/

	private final Map<String, ListRef<?>>		m_listCacheMap = new HashMap<String, ListRef<?>>();

	static private final class ListRef<T> {
		private List<T>		m_list;
		private final ICachedListMaker<T>	m_maker;
		public ListRef(final ICachedListMaker<T> maker) {
			m_maker = maker;
		}

		public synchronized List<T>	initialize() throws Exception {
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
	public <T> List<T>	getCachedList(final IListMaker<T> maker) throws Exception {
		if(! (maker instanceof ICachedListMaker)) {
			//-- Just make on the fly.
			return maker.createList(this);
		}

		ICachedListMaker<T>	cm = (ICachedListMaker<T>)maker;
		ListRef<T> ref;
		String key = cm.getCacheKey();
		synchronized(m_listCacheMap) {
			ref = (ListRef<T>) m_listCacheMap.get(key);
			if(ref == null) {
				ref	= new ListRef<T>(cm);
				m_listCacheMap.put(key, ref);
			}
		}
		return new ArrayList<T>(ref.initialize());
	}

	/**
	 * Discard all cached stuff in the list cache.
	 */
	public void		clearListCaches() {
		synchronized(m_listCacheMap) {
			m_listCacheMap.clear();
		}
		// FIXME URGENT Clear all other server's caches too by sending a VP event.
	}

	public void		clearListCache(final ICachedListMaker<?> maker) {
		synchronized(m_listCacheMap) {
			m_listCacheMap.remove(maker.getCacheKey());
		}
		// FIXME URGENT Clear all other server's caches too by sending a VP event.
	}

	public boolean logOutput() {
		return m_logOutput;
	}
	public synchronized void		addInterceptor(final IRequestInterceptor r) {
		List<IRequestInterceptor>	l = new ArrayList<IRequestInterceptor>(m_interceptorList);
		l.add(r);
		m_interceptorList = l;
	}
	public synchronized List<IRequestInterceptor>	getInterceptorList() {
		return m_interceptorList;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Exception translator handling.						*/
	/*--------------------------------------------------------------*/
	/**
	 * An entry in the exception table.
	 */
	static public class ExceptionEntry {
		private final Class<? extends Exception>	m_exceptionClass;
		private final IExceptionListener			m_listener;
		public ExceptionEntry(final Class<? extends Exception> exceptionClass, final IExceptionListener listener) {
			m_exceptionClass = exceptionClass;
			m_listener = listener;
		}
		public Class<? extends Exception> getExceptionClass() {
			return m_exceptionClass;
		}
		public IExceptionListener getListener() {
			return m_listener;
		}
	}

	/** The ORDERED list of [exception.class, handler] pairs. Exception SUPERCLASSES are ordered AFTER their subclasses. */
	private List<ExceptionEntry>	m_exceptionListeners = new ArrayList<ExceptionEntry>();

	/**
	 * Return the current, immutable, threadsafe copy of the list-of-listeners.
	 * @return
	 */
	private synchronized List<ExceptionEntry>	getExceptionListeners() {
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
	public synchronized void	addExceptionListener(final Class<? extends Exception> xclass, final IExceptionListener l) {
		m_exceptionListeners = new ArrayList<ExceptionEntry>(m_exceptionListeners);

		//-- Do a sortish insert.
		for(int i = 0; i < m_exceptionListeners.size(); i++) {
			ExceptionEntry ee = m_exceptionListeners.get(i);
			if(ee.getExceptionClass().isAssignableFrom(xclass)) {
				//-- Class [ee] is a SUPERCLASS of [xclass]; you can do [ee] = [xclass]. We need to add this handler BEFORE this superclass! If they are the SAME class throw up,
				if(ee.getExceptionClass() == xclass)
					throw new IllegalStateException("An exception handler for Exception="+xclass+" has already been registered. There can be only one handler for every exception type.");
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
	public IExceptionListener	findExceptionListenerFor(final Exception x) {
		Class<? extends Exception>	xclass = x.getClass();
		for(ExceptionEntry ee: getExceptionListeners()) {
			if(ee.getExceptionClass().isAssignableFrom(xclass))
				return ee.getListener();
		}
		return null;
	}

	public synchronized void		addNewPageInstantiatedListener(final INewPageInstantiated l) {
		m_newPageInstListeners = new ArrayList<INewPageInstantiated>(m_newPageInstListeners);
		m_newPageInstListeners.add(l);
	}
	public synchronized void		removeNewPageInstantiatedListener(final INewPageInstantiated l) {
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

	public synchronized void setLoginDialogFactory(ILoginDialogFactory loginDialogFactory) {
		m_loginDialogFactory = loginDialogFactory;
	}
}
