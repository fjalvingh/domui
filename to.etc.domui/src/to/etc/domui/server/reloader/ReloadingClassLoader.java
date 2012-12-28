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

import java.net.*;
import java.util.*;

import org.slf4j.*;

import to.etc.domui.util.resources.*;

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
		LOG.debug("ReloadingClassLoader: new instance " + this + " created");
	}

	@Override
	public String toString() {
		return "reloader[" + m_id + "]";
	}

	private void addWatchFor(Class< ? > clz) {
		IModifyableResource rt = ClasspathInventory.getInstance().findClassSource(clz);
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
			return new ArrayList<ResourceTimestamp>(m_dependList);
		}
	}

	/**
	 * Main workhorse for loading.
	 *
	 * @see java.lang.ClassLoader#loadClass(java.lang.String)
	 */
	@Override
	synchronized public Class< ? > loadClass(String name, boolean resolve) throws ClassNotFoundException {
		//		System.out.println("reloadingLoader: input=" + name);
		if(name.startsWith("java.") || name.startsWith("javax.") || (name.startsWith("to.etc.domui.") /* && !name.startsWith("to.etc.domui.component.") */)) {
			return m_rootLoader.loadClass(name); // Delegate to the rootLoader.
		}
		if(!m_reloader.watchClass(name)) {
			if(LOG.isDebugEnabled())
				LOG.debug("Class " + name + " not matching watch pattern delegated to root loader");
			Class< ? > loadClass = m_rootLoader.loadClass(name);
			if(m_reloader.watchOnlyClass(name)) {
				addWatchFor(loadClass);
			}
			return loadClass; // Delegate to the rootLoader.
		}
		//		System.out.println("reloadingClassLoader: watching " + name);

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
				clz = getParent().loadClass(name); // Try to load by parent,
			}
			if(clz == null)
				throw new ClassNotFoundException(name);
		} // else
		//System.out.println("reloadingLoader: got existing class "+clz);

		if(resolve)
			resolveClass(clz);
		//		System.out.println("rcl: loaded "+clz+" using "+clz.getClassLoader());
		return clz;
	}
}
