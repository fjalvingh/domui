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
package to.etc.domui.server;

import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.slf4j.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.server.reloader.*;
import to.etc.domui.state.*;

public class ReloadingContextMaker extends AbstractContextMaker {
	static private final Logger LOG = LoggerFactory.getLogger(ReloadingContextMaker.class);

	private String m_applicationClassName;

	private ConfigParameters m_config;

	private Reloader m_reloader;

	private DomApplication m_application;

	private int m_nestCount;

	private Set<IReloadedClassesListener> m_listenerSet = new HashSet<IReloadedClassesListener>();


	public ReloadingContextMaker(String applicationClassName, ConfigParameters pp, String patterns) throws Exception {
		super(pp);
		m_applicationClassName = applicationClassName;
		m_config = pp;
		m_reloader = new Reloader(patterns);
		System.out.println("DomUI: We are running in DEVELOPMENT mode. This will be VERY slow when used in a production environment.");

		checkReload(); // Initial: force load and init of Application object.
	}

	public Reloader getReloader() {
		return m_reloader;
	}

	@Override
	public boolean handleRequest(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws Exception {
		synchronized(this) {
			if(m_nestCount == 0)
				checkReload();
			m_nestCount++;
		}

		//-- If a reload has taken place all my session data is gone and a new appl object is instantiated.
		try {
			//-- Get sessionLink (locked on session)
			HttpSession sess = request.getSession(true);
			HttpSessionLink link;
			synchronized(sess) {
				link = (HttpSessionLink) sess.getAttribute(AppSession.class.getName());
				if(link == null) {
					link = new HttpSessionLink(this);
					sess.setAttribute(AppSession.class.getName(), link);
					addListener(link);
				}
			}

			//-- Ok: does the sessionlink have a session?
			//			DomApplication.internalSetCurrent(m_application);
			AppSession ass = link.getAppSession(m_application);
			RequestContextImpl ctx = new RequestContextImpl(m_application, ass, request, response);
			return execute(ctx, chain);
		} finally {
			synchronized(this) {
				m_nestCount--;
			}
			//			DomApplication.internalSetCurrent(null);
		}
	}

	/**
	 * Check if classes have changed. If so we discard all current info by releasing all sessions
	 * and terminating the application, then we reinit the application. When called we are sure no
	 * other threads execute in the code (and this remains true while this runs).
	 */
	private void checkReload() throws Exception {
		LOG.debug("Checking for reload");
		if(m_application == null) {
			//-- Just load && be done
			m_application = createApplication();
			return;
		}
		if(!m_reloader.isChanged())
			return;

		LOG.info("Reloading system");
		//-- Call all listeners
		for(IReloadedClassesListener l : getListeners()) {
			try {
				l.classesReloaded();
			} catch(Exception x) {
				AppFilter.LOG.error("Error calling listener: " + x, x);
			}
		}
		m_reloader.clear();

		//-- Check to see if the application has changed
		Class< ? > clz;
		try {
			clz = m_reloader.loadApplication(m_applicationClassName);
		} catch(ClassNotFoundException x) {
			throw new IllegalStateException("The main application class '" + m_applicationClassName + "' cannot be found: " + x, x);
		}
		if(m_application != null) {
			MetaManager.clear();
			Class< ? > oclz = m_application.getClass();
			System.out.println("OLD app = " + oclz + ", loaded by " + oclz.getClassLoader());
			System.out.println("NEW app = " + clz + ", loaded by " + clz.getClassLoader());
			if(clz.isAssignableFrom(oclz))
				return;
			DomApplication old = m_application;
			m_application = null;
			old.internalDestroy();
		}

		m_application = createApplication();
	}

	private DomApplication createApplication() throws Exception {
		Class< ? > clz;
		try {
			clz = m_reloader.loadApplication(m_applicationClassName);
		} catch(ClassNotFoundException x) {
			throw new IllegalStateException("The main application class '" + m_applicationClassName + "' cannot be found: " + x, x);
		}

		/*
		 * We have to create/replace the application class.
		 */
		Object o;
		DomApplication a;
		try {
			o = clz.newInstance();
			a = (DomApplication) o;
		} catch(Exception x) {
			System.out.println("DomApplication classloader: " + DomApplication.class.getClassLoader());
			System.out.println("Instance classloader: " + clz.getClassLoader());
			Class< ? > pc = clz.getSuperclass();
			System.out.println("Instance superclass=" + pc);
			System.out.println("Instance superclass classloader: " + pc.getClassLoader());

			throw new IllegalStateException("The main application class '" + m_applicationClassName + "' cannot be INSTANTIATED: " + x, x);
		}
		a.internalInitialize(m_config, true);
		return a;
	}

	public synchronized void addListener(IReloadedClassesListener l) {
		m_listenerSet = new HashSet<IReloadedClassesListener>(m_listenerSet);
		m_listenerSet.add(l);
	}

	public synchronized void removeListener(IReloadedClassesListener l) {
		m_listenerSet = new HashSet<IReloadedClassesListener>(m_listenerSet);
		m_listenerSet.remove(l);
	}

	private Set<IReloadedClassesListener> getListeners() {
		return m_listenerSet;
	}
}
