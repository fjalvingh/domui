package to.etc.domui.server.reloader;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;

import to.etc.domui.server.*;
import to.etc.util.*;

/**
 * This class handles loading classes in such a way that when their source .class
 * files change we discard existing info and cause the classes to be reloaded. Used
 * in development.
 * <h2>Application class chores</h2>
 * <p>The DomApplication class is responsible for instantiating all user-changeable classes
 * by converting strings to Class instances. This DomApplication class itself is not reloadable
 * (for now), but it's classloader must pass all loads to the classloader here so we can
 * determine if a class is to be reloadable.</p>
 * <p>We have a total of three ClassLoaders in this process:
 * <ul>
 *	<li>The ReloadingClassLoader is a discardable ClassLoader. All classes loaded thru this
 *		class are ALWAYS watched for changes. The system can ask this ClassLoader to check
 *		if any of the classes it loaded have changed. When the system decides to reload
 *		the classes it discards the <i>current</i> instance of the ReloadingClassLoader,
 *		thereby invalidating all of the classes loaded by it. These classes will become
 *		garbage as soon as all references to it are gone. The ReloadingClassLoader (for now)
 *		never contains the Application class, but the Application class will force loads of
 *		classes it instantiates thru the ReloadingClassLoader.
 *  </li>
 *	<li>The CheckingClassLoader is the ClassLoader used to load the Application class, and
 *		only that class. It causes all classes that are loaded via the Application class
 *		to pass through it, and it decides how to load those classes: either through the
 *		normal classloader (WebappLoader, usually) or thru the ReloadingClassLoader.
 *	</li>
 * </ul>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 22, 2008
 */
final public class Reloader {
	static final Logger LOG = Logger.getLogger(Reloader.class.getName());

	static private final IResourceRef NOT_FOUND = new IResourceRef() {
		public long lastModified() {
			throw new IllegalStateException("Whazzup!?");
		}
	};

	static private class LoadSpec {
		private Pattern m_pat;

		private boolean m_accept;

		public LoadSpec(Pattern pat, boolean accept) {
			m_accept = accept;
			m_pat = pat;
		}

		public boolean matches(String in) {
			return m_pat.matcher(in).matches();
		}

		public boolean isAccept() {
			return m_accept;
		}
	}

	/** The spec for classes to load thru the reloader. */
	private List<LoadSpec> m_loadSpecList = new ArrayList<LoadSpec>();

	/** The current classloader, */
	private ReloadingClassLoader m_currentLoader;

	/** The classloader used for other classes */
	private CheckingClassLoader m_checkLoader;

	/** The set of URLs that are accessed by all my classloaders. */
	private Set<URL> m_urlSet = new HashSet<URL>();

	private Map<String, IResourceRef> m_lookupMap = new HashMap<String, IResourceRef>();

	private boolean m_changed;

	/**
	 * Create a reloader which handles the specified classes.
	 * @param paths
	 */
	public Reloader(String paths) {
		m_loadSpecList.add(new LoadSpec(Pattern.compile("to.etc.domui.*"), false)); // Never accept internal classes!!

		StringTokenizer st = new StringTokenizer(paths, " \t;,");
		while(st.hasMoreTokens()) {
			String path = st.nextToken().trim();
			if(path.length() > 0) {
				boolean on = true;
				if(path.startsWith("-")) {
					on = false;
					path = path.substring(1).trim();
				} else if(path.startsWith("+")) {
					on = false;
					path = path.substring(1).trim();
				}
				Pattern p = Pattern.compile(path);
				m_loadSpecList.add(new LoadSpec(p, on));
			}
		}
		if(m_loadSpecList.size() == 0)
			throw new IllegalStateException("No load specifiers added.");
		//		findUrlsFor(m_currentLoader);
		findUrlsFor(getClass().getClassLoader());

		//-- ORDERED: must be below findUrlFor's
		m_currentLoader = new ReloadingClassLoader(getClass().getClassLoader(), this);
	}

	URL[] getUrls() {
		return m_urlSet.toArray(new URL[m_urlSet.size()]);
	}

	//	ClassLoader	getCheckingLoader() {
	//		return m_checkLoader;
	//	}
	public ClassLoader getReloadingLoader() {
		return m_currentLoader;
	}

	/**
	 * Used to load the Application class so it uses the proper ClassLoader.
	 * @param classname
	 * @return
	 */
	public Class<DomApplication> loadApplication(String classname) throws Exception {
		if(m_checkLoader == null)
			m_checkLoader = new CheckingClassLoader(getClass().getClassLoader(), this, classname);
		return (Class<DomApplication>) m_checkLoader.loadClass(classname);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Code which locates the source for a class.			*/
	/*--------------------------------------------------------------*/

	private void addURL(URL u) {
		//		LOG.info("adding URL="+u);
		//		System.out.println(".    url="+u);
		m_urlSet.add(u);
	}

	/**
	 * Checks to see what kind of classloader this is, and add all paths to my list.
	 * @param loader
	 */
	private void findUrlsFor(ClassLoader loader) {
		//		System.out.println(".. loader="+loader);
		if(loader == null)
			return;
		if(loader instanceof URLClassLoader) {
			URLClassLoader ucl = (URLClassLoader) loader;
			for(URL u : ucl.getURLs()) {
				addURL(u);
			}
		}
		findUrlsFor(loader.getParent());
	}

	/**
	 * Tries to find the class in the set of URL's passed. This currently does not
	 * cache the set; it scans for resources every time the class is passed in.
	 *
	 * @param clz
	 * @return
	 */
	synchronized ResourceTimestamp findClassSource(Class< ? > clz) {
		IResourceRef rr = m_lookupMap.get(clz.getName()); // Already looked up earlier?
		if(rr != null) {
			if(rr == NOT_FOUND)
				return null;
			return new ResourceTimestamp(rr, rr.lastModified()); // Return timestamp
		}

		String path = clz.getName().replace('.', '/') + ".class"; // Make path-like structure
		ResourceTimestamp ts = null;
		for(URL u : m_urlSet) {
			ts = checkForFile(u, path);
			if(ts != null)
				break;
		}
		if(ts == null) {
			//-- Check all jars.
			for(URL u : m_urlSet) {
				ts = checkForJar(u, path);
				if(ts != null)
					break;
			}

			//-- Still not found -> make BADREF && store, then return nuttin'
			if(ts == null) {
				m_lookupMap.put(clz.getName(), NOT_FOUND);
				return null;
			}
		}
		m_lookupMap.put(clz.getName(), ts.getRef()); // Save found/not found ref
		return ts;
	}

	private ResourceTimestamp checkForFile(URL u, String rel) {
		if(!"file".equals(u.getProtocol()))
			return null;
		if(u.getPath().endsWith(".jar"))
			return null;
		File f = new File(u.getFile());
		if(!f.exists() || !f.isDirectory()) // Must be a dir here,
			return null;

		//-- Can we locate the class here, then?
		File nw = new File(f, rel);
		if(!nw.exists() || !nw.isFile())
			return null;

		LOG.fine("Found class " + rel + " in " + u);
		return new ResourceTimestamp(new FileRef(nw), nw.lastModified());
	}

	/**
	 * Check if the thingy is a jar, and if the path is contained therein,
	 * @param u
	 * @param rel
	 * @return
	 */
	private ResourceTimestamp checkForJar(URL u, String rel) {
		if(!"file".equals(u.getProtocol()))
			return null;
		if(!u.getPath().endsWith(".jar"))
			return null;
		File f = new File(u.getFile());
		if(!f.exists() || !f.isFile()) // Must be a file,
			return null;
		long ts = JarRef.getTimestamp(f, rel);
		if(ts == -1)
			return null;
		return new ResourceTimestamp(new JarRef(f, rel), ts);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Check files.										*/
	/*--------------------------------------------------------------*/
	/**
	 * Returns T if this class is to be watched, false otherwise.
	 *
	 * @param name
	 * @return
	 */
	boolean watchClass(String name) {
		for(LoadSpec ls : m_loadSpecList) {
			if(ls.matches(name))
				return ls.isAccept();
		}
		return false;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Code to check for changes in classes loaded by me	*/
	/*--------------------------------------------------------------*/
	/**
	 * Checks for changes on this classloader. This blocks if another thread is checking and if so
	 * it does not sweep again when that thread completes the sweep.
	 */
	public boolean isChanged() {
		List<ResourceTimestamp> sweeplist; // Whatever will be sweeped
		synchronized(this) {
			if(m_changed)
				return true;
			sweeplist = m_currentLoader.getDependencyList();
		}

		//-- We are responsible for sweeping - we own the sweep baton
		if(!sweep(sweeplist)) // Has any resource changed?
			return false;
		synchronized(this) {
			m_changed = true;
			return true;
		}
	}

	public void clear() {
		synchronized(this) {
			m_changed = false;
			m_currentLoader = new ReloadingClassLoader(getClass().getClassLoader(), this);
		}
	}

	/**
	 * Pass all refs, and return TRUE if any file there has changed.
	 * @param list
	 * @return
	 */
	private boolean sweep(List<ResourceTimestamp> list) {
		int fc = 0;
		long ts = System.nanoTime();
		try {
			for(ResourceTimestamp fr : list) {
				if(fr.changed()) {
					LOG.info("Class Source " + fr + " has changed.");
					return true;
				} else
					LOG.fine("Unchanged source for " + fr);
				fc++;
			}
			return false;
		} finally {
			ts = System.nanoTime() - ts;
			if(LOG.isLoggable(Level.FINE))
				LOG.fine("Scanned " + fc + " .class files in " + StringTool.strNanoTime(ts));
		}
	}


}
