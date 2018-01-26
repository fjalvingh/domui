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
package to.etc.domui.util.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.domui.server.reloader.Reloader;
import to.etc.util.StringTool;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Inventory of all files (.class and other resources) reachable in the classpath. It lazily
 * loads all entries in all directory classpath entries and all .jar entries and caches those
 * as soon as they are needed.
 *
 * <p>Used in development mode to get timestamps for .class and other files, and the contents of
 * resources.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 5, 2011
 */
@DefaultNonNull
public class ClasspathInventory {
	static final Logger LOG = LoggerFactory.getLogger(ClasspathInventory.class);

	/** If needed: a singleton maintaining the inventory data. */
	@Nullable
	static private ClasspathInventory m_instance;

	/** The set of directories and .jar files. */
	private final Set<File> m_fileSet;

	@Nullable
	private List<IFileContainer> m_classPathContainers;

	/** Maps resource and class names to the container they were found in first */
	private final Map<String, IFileContainer> m_fileContainerMap = new HashMap<>();

	static private final IModifyableResource NOT_FOUND = new IModifyableResource() {
		@Override
		public long getLastModified() {
			throw new IllegalStateException("Whazzup!?");
		}
	};

	/**
	 * Create an inventory on the specified set of paths, either directories or .jar files.
	 * @param files
	 */
	ClasspathInventory(Set<File> files) {
		m_fileSet = files;
	}

	/**
	 * Checks to see what kind of classloader this is, and add all paths to my list.
	 * @param loader
	 */
	static private void findUrlsFor(Set<File> result, ClassLoader loader) {
		if(loader == null)
			return;
		if(loader instanceof URLClassLoader) {
			URLClassLoader ucl = (URLClassLoader) loader;
			for(URL u : ucl.getURLs()) {
				addUrl(result, u);
			}
		}
		findUrlsFor(result, loader.getParent());
	}

	/**
	 * If this URL is recognised add it to the fileset.
	 * @param result
	 * @param u
	 */
	static private void addUrl(Set<File> result, URL u) {
		if("file".equalsIgnoreCase(u.getProtocol())) {
			try {
				File f = new File(u.toURI());
				result.add(f);
			} catch(URISyntaxException x) {}
		}
	}

	/**
	 * Locate the source for some file that is part of the classpath (either a class resource or a .class file itself),
	 * and return a timestamp for that thing if found. If the resource is not found this returns null.
	 * @param resourcePath			Absolute resource pathname, preferably without leading /
	 */
	@Nullable
	public synchronized IModifyableResource findResourceSource(String resourcePath) {
		long t = System.nanoTime();
		IModifyableResource ref = null;
		try {
			if(resourcePath.startsWith("/"))							// Resources should start with /, but do not use that in the scan.
				resourcePath = resourcePath.substring(1);
			IFileContainer container = m_fileContainerMap.get(resourcePath);
			if(null != container) {
				//-- Lookup the thingy where it was last found
				ref = container.findFile(resourcePath);
				if(null != ref)
					return ref;
			}

			//-- Not found or no container - rescan; the thing might have moved.
			for(IFileContainer current : getClassPathContainers()) {
				ref = current.findFile(resourcePath);
				if(ref != null) {
					//-- Gotcha.
					m_fileContainerMap.put(resourcePath, current);		// Found here
					return ref;
				}
			}

			return null;
		} finally {
			if(LOG.isDebugEnabled()) {
				t = System.nanoTime() - t;
				LOG.debug("inventory: " + (ref == null ? "un" : "") + "successful findResourceSource " + resourcePath + " took " + StringTool.strNanoTime(t));
				if(Reloader.DEBUG)
					System.out.println("inventory: " + (ref == null ? "un" : "") + "successful findResourceSource " + resourcePath + " took " + StringTool.strNanoTime(t));
			}
		}
	}

	/**
	 * Tries to find the .class file for the specified class.
	 */
	@Nullable
	public synchronized IModifyableResource findClassSource(Class< ? > clz) {
		return findResourceSource(clz.getName().replace('.', '/') + ".class");
	}

	/**
	 * Get all containers for the class path, in order.
	 */
	private synchronized List<IFileContainer> getClassPathContainers() {
		List<IFileContainer> list = m_classPathContainers;
		if(null == list) {
			list = m_classPathContainers = new ArrayList<>();
			for(File file : m_fileSet) {
				IFileContainer container = createContainer(file);
				if(null != container)
					list.add(container);
			}
		}
		return list;
	}

	@Nullable
	private IFileContainer createContainer(File file) {
		if(file.getName().endsWith(".jar")) {
			return JarFileContainer.create(file);
		}
		if(file.isDirectory()) {
			return DirectoryFileContainer.create(file);
		}
		return null;
	}

	/**
	 * Create an inventory on the specified URLs. Only file: urls are actually used, the
	 * rest is ignored.
	 *
	 * @param urls
	 * @return
	 */
	static public ClasspathInventory create(URL[] urls) {
		Set<File> fileSet = new HashSet<File>();
		for(URL u : urls) {
			addUrl(fileSet, u);
		}
		return new ClasspathInventory(fileSet);
	}

	/**
	 * Create an inventory for the specified classloader.
	 * @param cl
	 * @return
	 */
	static public ClasspathInventory create(ClassLoader cl) {
		Set<File> fileSet = new HashSet<File>();
		findUrlsFor(fileSet, cl);
		return new ClasspathInventory(fileSet);
	}

	/**
	 * Create and/or return an instance that uses it's own classloader to initialize all classpath entries.
	 * @return
	 */
	static synchronized public ClasspathInventory getInstance() {
		ClasspathInventory instance = m_instance;
		if(instance == null)
			m_instance = instance = create(ClasspathInventory.class.getClassLoader());
		return instance;
	}

	/**
	 * This scans the entire known classpath and constructs all stuff that is available in the
	 * specified package directory. These are all files there: .class and others.
	 *
	 * @param pkgdirname
	 * @return
	 */
	@Nonnull
	public List<String> getPackageInventory(@Nonnull String pkgdirname) {
		pkgdirname = pkgdirname.replace('.', '/') + "/";

		List<String>	res = new ArrayList<String>();
		for(IFileContainer con : getClassPathContainers()) {
			List<String> inventory = con.getInventory();
			for(String s : inventory) {
				if(s.startsWith(pkgdirname)) {
					res.add(s.substring(pkgdirname.length()));
				}
			}
		}
		return res;
	}

	public static void main(String[] args) {
		try {
			List<String> res = getInstance().getPackageInventory("org.apache.batik.css.dom");
			for(String s : res)
				System.out.println("res=" + s);

		} catch(Exception x) {
			x.printStackTrace();
		}
	}

}
