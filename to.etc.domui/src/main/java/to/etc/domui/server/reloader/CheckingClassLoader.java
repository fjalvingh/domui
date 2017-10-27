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

import java.net.URLClassLoader;

/**
 * This ClassLoader is used to load classes that are not to be reloaded but which must
 * cause the classes <b>they</b> load to pass thru the checking mechanism. Classes loaded
 * by this class have their classloader set to this class, and every time such a class
 * needs another class that other class passes thru the "must I reload this class" check
 * too.
 * Classes loaded by this loader are not discarded; the classloader exists only to
 * force other classloads thru a check.
 * <p>This classloader loads classes as follows:
 * <ul>
 * 	<li>If a class is already known we're done</li>
 * 	<li>If a class is part of the system (java.*, javax.*) we load thru a system loader always</li>
 *	<li>All other classes are checked against the inclusion/exclusion patterns.</li>
 *	<li>Excluded classes are loaded thru this classloader, so classes they load can be checked.</li>
 *	<li>Included classes are loaded thru the DISCARDABLE classloader.</li>
 * </ul>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 17, 2008
 */
public class CheckingClassLoader extends URLClassLoader {
	static private final Logger LOG = Reloader.LOG;

	private Reloader m_reloader;

	private String m_applicationClass;

	public CheckingClassLoader(ClassLoader parent, Reloader r, String appclass) {
		super(r.getUrls(), parent);
		m_reloader = r;
		m_applicationClass = appclass;
	}

	/**
	 * Main workhorse for loading. This ONLY loads the SPECIFIED class thru this-loader; all OTHER
	 * classes go through the RELOADING classloader.
	 *
	 * @see java.lang.ClassLoader#loadClass(java.lang.String)
	 */
	@Override
	public synchronized Class< ? > loadClass(String name, boolean resolve) throws ClassNotFoundException {
		if(Reloader.DEBUG)
			System.out.println("checkingLoader: input="+name);
		if(!name.startsWith(m_applicationClass)) // Not the Application class?
			return m_reloader.getReloadingLoader().loadClass(name); // Then delegate to the reloading classloader

		//-- Load this class here, so other classes loaded *by* it will be checked too
		Class< ? > clz = findLoadedClass(name);
		if(clz == null) {
			//-- Must we handle this class?
			LOG.debug("Load class " + name + " using checking loader");

			//-- Try to find the path for the class resource
			try {
				clz = findClass(name);
			} catch(ClassNotFoundException x) {
				//-- *this* loader cannot find it.
				if(getParent() == null)
					throw x;
				clz = getParent().loadClass(name); // Try to load by parent,
			}
			if(clz == null)
				throw new ClassNotFoundException(name);
		}

		if(resolve)
			resolveClass(clz);
		return clz;
	}
}
