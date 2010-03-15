package to.etc.domui.server.reloader;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.util.zip.*;

import org.slf4j.*;

import to.etc.domui.server.*;
import to.etc.domui.util.resources.*;
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
	static final Logger LOG = LoggerFactory.getLogger(Reloader.class);

	/** A reloader exists only once in a webapp. */
	static private Reloader m_instance;

	static private final IModifyableResource NOT_FOUND = new IModifyableResource() {
		public long getLastModified() {
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

	private Map<String, IModifyableResource> m_lookupMap = new HashMap<String, IModifyableResource>();

	private boolean m_changed;

	/**
	 * Create a reloader which handles the specified classes.
	 * @param paths
	 */
	public Reloader(String paths) {
		//		m_loadSpecList.add(new LoadSpec(Pattern.compile("to.etc.domui.*"), false)); // Never accept internal classes!! jal 20090817 Removed, handled in ReloadingClassloader instead.

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
		m_instance = this;
	}

	URL[] getUrls() {
		return m_urlSet.toArray(new URL[m_urlSet.size()]);
	}

	static private Reloader internalGetReloader() {
		return m_instance;
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
	/*	CODING:	Code which locates the source for a class/resource.	*/
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
	 * Locate the source for some file that is part of the classpath (either a class resource or a .class file itself),
	 * and return a timestamp for that thing if found. If the resource is not found this returns null.
	 * @param resourceName
	 * @return
	 */
	synchronized IModifyableResource findResourceSource(String resourceName) {
		long t = System.nanoTime();
		if(resourceName.startsWith("/")) // Resources should start with /, but do not use that in the scan.
			resourceName = resourceName.substring(1);

		IModifyableResource rr = m_lookupMap.get(resourceName); // Already looked up earlier?
		if(rr != null) {
			if(rr == NOT_FOUND)
				return null;
			return rr;
		}

		//-- Scan all paths for this thingy.
		IModifyableResource ts = scanActually(resourceName);

		//-- Still not found -> make BADREF && store, then return nuttin'
		m_lookupMap.put(resourceName, ts == null ? NOT_FOUND : ts);
		if(LOG.isDebugEnabled()) {
			t = System.nanoTime() - t;
			LOG.debug("reloader: " + (ts == null ? "un" : "") + "succesful findResourceSource " + resourceName + " took " + StringTool.strNanoTime(t));
		}
		return ts;
	}

	/**
	 * Tries to find the class in the set of URL's passed.
	 *
	 * @param clz
	 * @return
	 */
	synchronized IModifyableResource findClassSource(Class< ? > clz) {
		//-- 1. Do a quick lookup of the classname itself
		IModifyableResource rr = m_lookupMap.get(clz.getName()); // Already looked up earlier?
		if(rr != null) {
			if(rr == NOT_FOUND)
				return null;
			return rr; // Return timestamp
		}

		//-- 2. Lookup the .class file as a resource.
		String path = clz.getName().replace('.', '/') + ".class"; // Make path-like structure
		IModifyableResource ts = findResourceSource(path); // Lookup .class file
		m_lookupMap.put(clz.getName(), ts == null ? NOT_FOUND : ts);
		return ts;
	}

	/**
	 * Can be called by code to locate a class's .class file in debug mode. This returns null if the resource
	 * is not found OR if we're not running with a reloader (i.e. not in debug mode).
	 * @param clz
	 * @return
	 */
	static public IModifyableResource findClasspathSource(Class< ? > clz) {
		Reloader r = internalGetReloader();
		if(r == null)
			return null;
		return r.findClassSource(clz);
	}

	/**
	 * Can be called by code to locate class resources in debug mode. This returns null if the resource
	 * is not found OR if we're not running with a reloader (i.e. not in debug mode).
	 * @param clz
	 * @return
	 */
	static public IModifyableResource findClasspathSource(String resourceName) {
		Reloader r = internalGetReloader();
		if(r == null)
			throw new IllegalStateException("Do not call reloader code when running in production mode!");
		return r.findResourceSource(resourceName);
	}

	/**
	 * Do a scan for a source, uncached.
	 * @param path
	 * @return
	 */
	private synchronized IModifyableResource scanActually(String path) {
		IModifyableResource ts = null;

		/*
		 * Initially scan all directories. This is fast; we just see if the file exists in the specified directories.
		 */
		for(URL u : m_urlSet) {
			ts = checkForFile(u, path);
			if(ts != null)
				return ts;
		}

		/*
		 * Not a file. Does the jar map contain an entry?
		 */
		if(m_jarMap.size() != 0) {
			ClasspathJarRef jref = m_jarMap.get(path);
			if(jref != null)
				return jref;
			LOG.info("? Odd: the classpath resource '" + path + "' cannot be found in the jars... I will do a full rescan.");
		}

		/*
		 * Not in the jars, or the jar cache was empty. Do a full rescan then try again
		 */
		scanJars();
		return m_jarMap.get(path);
	}

	/**
	 * Try to find the specified class file name as a file relative to the
	 * specified URL base (provided it is a directory). If found this returns
	 * the current timestamp and reference that can be checked later on for
	 * changes on this class' source.
	 *
	 * @param u
	 * @param rel
	 * @return
	 */
	private IModifyableResource checkForFile(URL u, String rel) {
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

		LOG.debug("Found class " + rel + " in " + u);
		return new ClasspathFileRef(nw);
	}

	/**
	 * This maps classpath resource names to the jar they are contained in...
	 */
	private Map<String, ClasspathJarRef> m_jarMap = new HashMap<String, ClasspathJarRef>();

	/**
	 * Scan all JAR files and create a map of their content linked to the jar file itself.
	 */
	private synchronized void scanJars() {
		long ts = System.nanoTime();
		m_jarMap.clear();
		for(URL u : m_urlSet) {
			if(!"file".equals(u.getProtocol()))
				continue;
			if(!u.getPath().endsWith(".jar"))
				continue;
			File f = new File(u.getFile());
			if(!f.exists() || !f.isFile()) // Must be a file,
				continue;

			//-- This is a jar... Get all of it's files.
			loadJarFiles(f);
		}
		ts = System.nanoTime() - ts;
		LOG.info("Loading full JAR inventory of " + m_jarMap.size() + " entries took " + StringTool.strNanoTime(ts));
	}

	/**
	 * If the specified url is a JAR load it's fileset and store in the jarmap.
	 * @param u
	 */
	private void loadJarFiles(File f) {
		ClasspathJarRef jarref = new ClasspathJarRef(f);
		InputStream is = null;
		ZipInputStream zis = null;
		try {
			is = new FileInputStream(f);
			zis = new ZipInputStream(is);
			ZipEntry ze;
			while(null != (ze = zis.getNextEntry())) { // Walk entry
				m_jarMap.put(ze.getName(), jarref); // Update the reference.
			}
		} catch(Exception xz) {
		} finally {
			try {
				if(is != null)
					is.close();
			} catch(Exception x) {}
			try {
				if(zis != null)
					zis.close();
			} catch(Exception x) {}
		}
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
	private boolean	m_sweeping;



	/**
	 * Checks for changes on this classloader. This blocks if another thread is checking and if so
	 * it does not sweep again when that thread completes the sweep.
	 */
	public boolean isChanged() {
		List<ResourceTimestamp> sweeplist; // Whatever will be sweeped
		synchronized(this) {
			if(m_changed)
				return true;
			if(m_sweeping) {
				//-- Some other thread is sweeping; block until the baton is released
				while(m_sweeping) {
					try {
						wait(5000);
					} catch(InterruptedException x) {
					}
				}
				return m_changed;
			}

			//-- We were not sweeping but this thread will do so now; take control of the sweep baton
			m_sweeping= true;
			sweeplist = m_currentLoader.getDependencyList();
		}

		//-- We are responsible for sweeping - we own the sweep baton
		boolean changed = false;
		try {
			changed = sweep(sweeplist); // Has any resource changed?
			return changed;
		} finally {
			synchronized(this) {
				if(changed)
					m_changed = true;
				m_sweeping = false;			// Release sweep baton
				notifyAll();
			}
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
				if(fr.isModified()) {
					if(LOG.isDebugEnabled())
						LOG.debug("Class Source " + fr + " has changed.");
					return true;
				} else {
					if(LOG.isDebugEnabled())
						LOG.debug("Unchanged source for " + fr);
				}
				fc++;
			}
			return false;
		} finally {
			ts = System.nanoTime() - ts;
			if(LOG.isDebugEnabled())
				LOG.debug("Scanned " + fc + " .class files in " + StringTool.strNanoTime(ts));
		}
	}
}
