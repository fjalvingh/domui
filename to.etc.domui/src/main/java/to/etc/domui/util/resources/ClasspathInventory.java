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

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;

import javax.annotation.*;

import org.slf4j.*;

import to.etc.util.*;

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
public class ClasspathInventory {
	static final Logger LOG = LoggerFactory.getLogger(ClasspathInventory.class);

	/** If needed: a singleton maintaining the inventory data. */
	static private ClasspathInventory m_instance;

	/** The set of directories and .jar files. */
	private final Set<File> m_fileSet;

	private Map<String, IModifyableResource> m_lookupMap = new HashMap<String, IModifyableResource>();

	/**
	 * This maps resourcePath names to the jar they are contained in.
	 */
	private Map<String, ClasspathJarRef> m_jarMap = new HashMap<String, ClasspathJarRef>();

	/** Maps jar name to it's header. Used to check if the jar changed. */
	private Map<String, ClasspathJarRef> m_jarModificationMap = new HashMap<String, ClasspathJarRef>();

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
		if(m_instance == null)
			m_instance = create(ClasspathInventory.class.getClassLoader());
		return m_instance;
	}

	/**
	 * Checks to see what kind of classloader this is, and add all paths to my list.
	 * @param loader
	 */
	static private void findUrlsFor(Set<File> result, ClassLoader loader) {
		//		System.out.println(".. loader="+loader);
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
	 * @return
	 * @throws URISyntaxException
	 */
	public synchronized IModifyableResource findResourceSource(String resourcePath) {
		long t = System.nanoTime();
		if(resourcePath.startsWith("/")) // Resources should start with /, but do not use that in the scan.
			resourcePath = resourcePath.substring(1);

		IModifyableResource rr = m_lookupMap.get(resourcePath); // Already looked up earlier?
		if(rr != null) {
			if(rr == NOT_FOUND)
				return null;
			return rr;
		}

		//-- Scan all paths for this thingy.
		IModifyableResource ts = scanActually(resourcePath);

		//-- Still not found -> make BADREF && store, then return nuttin'
		m_lookupMap.put(resourcePath, ts == null ? NOT_FOUND : ts);
		if(LOG.isDebugEnabled()) {
			t = System.nanoTime() - t;
			LOG.debug("inventory: " + (ts == null ? "un" : "") + "succesful findResourceSource " + resourcePath + " took " + StringTool.strNanoTime(t));
		}
		return ts;
	}

	/**
	 * Tries to find the .class file for the specified class.
	 *
	 * @param clz
	 * @return
	 * @throws URISyntaxException
	 */
	public synchronized IModifyableResource findClassSource(Class< ? > clz) {
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
	 * Do a scan for a source, uncached.
	 * @param path
	 * @return
	 * @throws URISyntaxException
	 */
	private synchronized IModifyableResource scanActually(String path) {
		//-- Initially scan all directories. This is fast; we just see if the file exists in the specified directories.
		for(File u : m_fileSet) {
			IModifyableResource ts = checkForFile(u, path);
			if(ts != null)
				return ts;
		}

		//-- Not a file. Does the jar map contain an entry?
		if(m_jarMap.size() != 0) {
			ClasspathJarRef jref = m_jarMap.get(path);
			if(jref != null)
				return jref;
			LOG.info("The classpath resource '" + path + "' cannot be found in the jars... Scanning them for changes.");
		}

		/*
		 * Not in the jars, or the jar cache was empty. Do a full rescan then try again
		 */
		scanJars();
		return m_jarMap.get(path);
	}

	/**
	 * Try to find the specified class file name as a file relative to the
	 * specified File base (provided it is a directory), ignoring .jar files.
	 * If found this returns the current timestamp and reference that can be
	 * checked later on for changes on this resource's source.
	 *
	 * @param u
	 * @param rel
	 * @return
	 * @throws URISyntaxException
	 */
	private IModifyableResource checkForFile(File f, String rel) {
		if(f.getName().toLowerCase().endsWith(".jar")) // Skip all .jar files for now
			return null;
		if(!f.exists() || !f.isDirectory()) // Must be a dir here,
			return null;

		//-- Can we locate the class based @ here, then?
		File nw = new File(f, rel);
		if(!nw.exists() || !nw.isFile())
			return null;
		if(LOG.isDebugEnabled())
			LOG.debug("Found classpathentry " + rel + " in " + f);
		return new ClasspathFileRef(nw);
	}

	/**
	 * Scan all JAR files and create a map of their content linked to the jar file itself.
	 * @throws URISyntaxException
	 */
	private synchronized void scanJars() {
		long ts = System.nanoTime();
		//		m_jarMap.clear();	// jal 20110115 Do not clear; jar entries are removed at rescan time.
		int jcount = 0, lcount = 0;
		for(File f : m_fileSet) {
			if(!f.getName().endsWith(".jar"))
				continue;
			if(!f.exists() || !f.isFile()) // Must be a file,
				continue;

			//-- This is a jar... Get all of it's files.
			if(loadJarInventory(f))
				lcount++;
			jcount++;
		}
		ts = System.nanoTime() - ts;
		LOG.info("(Re)loading " + lcount + " changed .jar files of " + jcount + " total containing " + m_jarMap.size() + " entries took " + StringTool.strNanoTime(ts));
	}

	/**
	 * If the specified url is a JAR load it's fileset and store in the jarmap.
	 * @param u
	 */
	private synchronized boolean loadJarInventory(File f) {
		String name = f.getAbsolutePath();
		ClasspathJarRef orig = m_jarModificationMap.get(name);
		if(orig != null) {
			//-- It was already loaded. Has the file changed?
			if(!orig.isModified())
				return false; // Just return- nothing to do here.

			//-- We need to reload. Discard everything for this from jar map
			for(String entry : orig.getNameList()) {
				if(orig == m_jarMap.get(entry))
					m_jarMap.remove(entry);
			}
			m_jarModificationMap.remove(name);
		}

		ClasspathJarRef jarref = new ClasspathJarRef(f);
		m_jarModificationMap.put(name, jarref);
		InputStream is = null;
		ZipInputStream zis = null;
		try {
			is = new FileInputStream(f);
			zis = new ZipInputStream(is);
			ZipEntry ze;
			while(null != (ze = zis.getNextEntry())) { // Walk entry
				m_jarMap.put(ze.getName(), jarref); // Update the reference.
				jarref.getNameList().add(ze.getName());
			}
		} catch(Exception xz) {
			// Ignore all exceptions: when a classpath jar is corrupt let someone else bother...
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
		return true;
	}

	/**
	 * This scans the entire known classpath and constructs all stuff that is available in the
	 * specified package directory. These are all files there: .class and others.
	 * @param pkgdirname
	 * @return
	 */
	@Nonnull
	public List<String> getPackageInventory(@Nonnull String pkgdirname) {
		pkgdirname = pkgdirname.replace('.', '/');

		//-- Walk all dirs in the set;
		List<String>	res = new ArrayList<String>();
		for(File f: m_fileSet) {
			if(f.getName().toLowerCase().endsWith(".jar")) // Skip all .jar files for now
				continue;
			if(!f.exists() || !f.isDirectory()) // Must be a dir here,
				continue;

			//-- Can we locate the class based @ here, then?
			File nw = new File(f, pkgdirname);
			if(!nw.isDirectory())
				continue;
			File[] far = nw.listFiles();
			for(File fi : far) {
				if(fi.isFile())
					res.add(fi.getName());
			}
		}
		if(m_jarMap.size() == 0)
			scanJars();

		//-- Walk all jar entries
		int pdl = pkgdirname.length();
		synchronized(this) {
			for(String ks : m_jarMap.keySet()) {
				if(ks.startsWith(pkgdirname)) {
					if(ks.length() > pdl + 1 && ks.charAt(pdl) == '/') {
						String rn = ks.substring(pdl + 1);
						if(rn.indexOf('/') == -1)
							res.add(rn);
					}
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
