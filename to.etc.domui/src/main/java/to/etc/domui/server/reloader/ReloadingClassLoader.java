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
import to.etc.domui.util.resources.ClasspathInventory;
import to.etc.domui.util.resources.IModifyableResource;
import to.etc.domui.util.resources.ResourceTimestamp;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * The classloader used by the reloader. Classes matching the include
 * pattern are loaded using this classloader, and all of the files
 * thus accessed are registered with the Reloader. When the Reloader
 * determines that registered sources have changed it will discard the
 * current instance of this classloader, thereby invalidating all classes,
 * and discard all sessions.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 22, 2008
 */
public class ReloadingClassLoader extends URLClassLoader {
	static private final Logger LOG = Reloader.LOG;

	static private int m_nextid = 1;

	private Reloader m_reloader;

	final private int m_id;

	private ClassLoader m_rootLoader;

	final private ClasspathInventory m_classInventory;

	/**
	 * The list of files used in constructing these classes.
	 */
	private final List<ResourceTimestamp> m_dependList = new ArrayList<ResourceTimestamp>();

	static private final synchronized int nextID() {
		return m_nextid++;
	}

	public ReloadingClassLoader(ClassLoader parent, Reloader r) {
		super(r.getUrls(), parent);
		m_reloader = r;
		m_id = nextID();
		m_rootLoader = getClass().getClassLoader();
		m_classInventory = ClasspathInventory.create(getClass().getClassLoader());
		LOG.debug("ReloadingClassLoader: new instance " + this + " created");
	}

	@Override
	public String toString() {
		return "reloader[" + m_id + "]";
	}

	/**
	 * Adds a watch for every properties file loaded by this reloading loader.
	 * @see java.lang.ClassLoader#getResource(java.lang.String)
	 */
	@Override
	public @Nullable
	URL getResource(@Nullable String name) {
		URL resource = super.getResource(name);
		if(resource != null) {
			addResourceWatch(resource);
		}
		return resource;
	}

	private void addWatchFor(Class< ? > clz) {
		IModifyableResource rt = m_classInventory.findClassSource(clz);
		if(rt == null) {
			LOG.info("Cannot find source file for class=" + clz + "; changes to this class are not tracked");
			return;
		}
		if(LOG.isDebugEnabled())
			LOG.debug("Watching " + clz); //rt.getRef());
		synchronized(m_reloader) {
			m_dependList.add(new ResourceTimestamp(rt, rt.getLastModified()));
		}
	}

	List<ResourceTimestamp> getDependencyList() {
		synchronized(m_reloader) {
			return new ArrayList<>(m_dependList);
		}
	}

	/**
	 * Main workhorse for loading.
	 *
	 * @see java.lang.ClassLoader#loadClass(java.lang.String)
	 */
	@Override
	synchronized public Class< ? > loadClass(String name, boolean resolve) throws ClassNotFoundException {
		if(Reloader.DEBUG) {
			System.out.println("reloadingClassLoader: input=" + name);
		}

		if((name.startsWith("java.") || name.startsWith("javax.") || (name.startsWith("to.etc.domui.") /* && !name.startsWith("to.etc.domui.component.") */))) {
			return m_rootLoader.loadClass(name);				// Delegate to the rootLoader.
		}
		//Class< ? > loadClass = m_rootLoader.loadClass(name);	// jal 20171027 Loading from root means the class is never loaded from a NEW jar!!
		Class< ? > loadClass = super.loadClass(name, resolve);	// Ask this classLoader to load the class.
		if(!m_reloader.watchClass(name) && (loadClass.getSuperclass() == null || !loadClass.getSuperclass().getName().equals(ResourceBundle.class.getName()))) {
			if(LOG.isDebugEnabled())
				LOG.debug("Class " + name + " not matching watch pattern delegated to root loader");

			//Meta data changes and found bundles for this class will be watched and reloaded.
			if(m_reloader.watchOnlyClass(name)) {
				addWatchFor(loadClass);
				try {
					scanForForResourceWatches(loadClass);
				} catch(Exception e) {
					e.printStackTrace();
					LOG.warn("Class " + name + " cannot watch resources");
				}
			}
			return loadClass;									// Delegate to the rootLoader.
		}

		//-- We need to watch this class..
		Class< ? > clz = findLoadedClass(name);
		if(clz == null) {
			//-- Must we handle this class?
			if(LOG.isDebugEnabled())
				LOG.debug("loading class-to-watch=" + name);

			//-- Try to find the path for the class resource
			try {
				clz = findClass(name);
				addWatchFor(clz); // Only called if loading worked
			} catch(ClassNotFoundException x) {
				//-- *this* loader cannot find it.
				if(getParent() == null)
					throw x;
				clz = getParent().loadClass(name);				// Try to load by parent,
			}
			if(clz == null)
				throw new ClassNotFoundException(name);
		}

		if(resolve)
			resolveClass(clz);
		return clz;
	}

	/**
	 * This adds watches for resources in the same directory as the onlywatch class. Those will not be loaded by this classloader.
	 */
	@Nonnull
	private final Set<String> m_scannedPackages = new HashSet<String>();

	private void scanForForResourceWatches(@Nonnull Class< ? > loadClass) throws Exception {
		synchronized(m_scannedPackages) {
			if(m_scannedPackages.contains(loadClass.getPackage().getName())) {
				return;
			}
		}
		URL resource = getResource(loadClass.getPackage().getName().replace('.', '/'));
		if(resource != null) {
			final File file = new File(resource.getFile());
			File[] listFiles = file.listFiles();
			if(listFiles != null) {
				for(int i = 0; i < listFiles.length; i++) {
					addResourceWatch(listFiles[i]);
				}
			}
		}
		synchronized(m_scannedPackages) {
			m_scannedPackages.add(loadClass.getPackage().getName());
		}
	}

	public void addResourceWatch(@Nonnull URL resource) {
		if(resource != null && resource.getFile() != null) {
			addResourceWatch(new File(resource.getFile()));
		}

	}

	public void addResourceWatch(@Nonnull final File file) {
		if(file.getName().endsWith(".properties")) {
			synchronized(m_reloader) {
				m_dependList.add(new ResourceTimestamp(new IModifyableResource() {
					@Override
					public long getLastModified() {
						return file.lastModified();
					}
				}, file.lastModified()));
			}
		}
	}
}
