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
package to.etc.domui.server.reloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.domui.server.DomApplication;
import to.etc.domui.util.resources.ResourceTimestamp;
import to.etc.util.ClassUtil;
import to.etc.util.StringTool;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

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
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "DMI_COLLECTION_OF_URLS", justification = "URLs here are not network based")
final public class Reloader {
	static public final boolean DEBUG = false;

	static final Logger LOG = LoggerFactory.getLogger(Reloader.class);

	//	/** A reloader exists only once in a webapp. */
	//	static private Reloader m_instance;

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

	/** The spec for classes to watch only thru the reloader, found bundles in de same package en Metadata will be reloaded only	. */
	private List<LoadSpec> m_watchSpecList = new ArrayList<LoadSpec>();

	/** The current classloader, */
	private ReloadingClassLoader m_currentLoader;

	/** The classloader used for other classes */
	private CheckingClassLoader m_checkLoader;

	private URL[] m_urls;

	private boolean m_changed;

	/**
	 * Create a reloader which handles the specified classes.
	 */
	public Reloader(String paths, String pathsWatchOnly) {
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


		if(pathsWatchOnly != null) {
			st = new StringTokenizer(pathsWatchOnly, " \t;,");
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
					m_watchSpecList.add(new LoadSpec(p, on));
				}
			}

		}


		m_urls = ClassUtil.findUrlsFor(getClass().getClassLoader());

		//-- ORDERED: must be below findUrlFor's
		m_currentLoader = new ReloadingClassLoader(getClass().getClassLoader(), this);
		//		m_instance = this;
	}

	public URL[] getUrls() {
		return m_urls;
	}

	public synchronized ClassLoader getReloadingLoader() {
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

	//	/**
	//	 * Can be called by code to locate a class's .class file in debug mode. This returns null if the resource
	//	 * is not found OR if we're not running with a reloader (i.e. not in debug mode).
	//	 * @param clz
	//	 * @return
	//	 * @throws URISyntaxException
	//	 */
	//	static public IModifyableResource findClasspathSource(Class< ? > clz) {
	//		Reloader r = internalGetReloader();
	//		if(r == null)
	//			return null;
	//		return r.findClassSource(clz);
	//	}

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

	/**
	 * Returns T if this class is to be watched, false otherwise.
	 *
	 * @param name
	 * @return
	 */
	boolean watchOnlyClass(String name) {
		for(LoadSpec ls : m_watchSpecList) {
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
