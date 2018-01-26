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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.domui.component.meta.init.MetaInitializer;
import to.etc.domui.server.reloader.IReloadedClassesListener;
import to.etc.domui.server.reloader.Reloader;
import to.etc.domui.state.AppSession;
import to.etc.domui.state.HttpSessionLink;
import to.etc.webapp.nls.BundleRef;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReloadingContextMaker extends AbstractContextMaker {
	static private final Logger LOG = LoggerFactory.getLogger(ReloadingContextMaker.class);

	private String m_applicationClassName;

	private ConfigParameters m_config;

	private Reloader m_reloader;

	private DomApplication m_application;

	private int m_nestCount;

	private Set<IReloadedClassesListener> m_listenerSet = new HashSet<IReloadedClassesListener>();

	static private ReloadingContextMaker m_instance;

	@Nonnull
	static public List<IReloadListener> m_reloadListener = new ArrayList<>();

	static private long m_lastReloadTime;


	public ReloadingContextMaker(@Nonnull String applicationClassName, @Nonnull ConfigParameters pp, @Nullable String patterns, @Nullable String patternsWatchOnly) throws Exception {
		super(pp);
		m_instance = this;
		m_applicationClassName = applicationClassName;
		m_config = pp;
		m_reloader = new Reloader(patterns, patternsWatchOnly);
		System.out.println("DomUI: We are running in DEVELOPMENT mode. This will be VERY slow when used in a production environment.");

		checkReload(); 										// Initial: force load and init of Application object.
	}

	static public synchronized void addReloadListener(IReloadListener l) {
		m_reloadListener = new ArrayList<>(m_reloadListener);
		m_reloadListener.add(l);
	}

	static private synchronized List<IReloadListener> listeners() {
		return m_reloadListener;
	}

	static public Class< ? > loadClass(String name) throws Exception {
		if(m_instance != null) {
			return m_instance.getReloader().getReloadingLoader().loadClass(name);
		}
		return ReloadingContextMaker.class.getClassLoader().loadClass(name);
	}

	public Reloader getReloader() {
		return m_reloader;
	}

	static private synchronized void reloaded() {
		m_lastReloadTime = System.currentTimeMillis();
	}

	static public synchronized long getLastReload() {
		return m_lastReloadTime;
	}

	@Override
	public void handleRequest(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain chain) throws Exception {
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
					link = new HttpSessionLink(sess, this);
					sess.setAttribute(AppSession.class.getName(), link);
					addListener(link);
				}
			}

			//-- Ok: does the sessionlink have a session?
			DomApplication application = m_application;
			if(null == application)
				throw new IllegalStateException("Application not loaded/known");
			AppSession ass = link.getAppSession(application);
			HttpServerRequestResponse requestResponse = HttpServerRequestResponse.create(application, request, response);
			RequestContextImpl ctx = new RequestContextImpl(requestResponse, application, ass);
			execute(requestResponse, ctx, chain);
		} finally {
			synchronized(this) {
				m_nestCount--;
			}
		}
	}

	/**
	 * Check if classes have changed. If so we discard all current info by releasing all sessions
	 * and terminating the application, then we reinit the application. When called we are sure no
	 * other threads execute in the code (and this remains true while this runs).
	 */
	private void checkReload() throws Exception {
		if(Reloader.DEBUG)
			System.out.println("reloader: checking");
		LOG.debug("Checking for reload");
		if(m_application == null) {
			//-- Just load && be done
			m_application = createApplication();
			reloaded();
			return;
		}
		if(!m_reloader.isChanged()) {
			if(Reloader.DEBUG)
				System.out.println("reloader: no changes");
			return;
		}

		if(Reloader.DEBUG)
			System.out.println("reloader: reload needed");
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
		reloaded();
		Class< ? > clz;
		try {
			clz = m_reloader.loadApplication(m_applicationClassName);
		} catch(ClassNotFoundException x) {
			throw new IllegalStateException("The main application class '" + m_applicationClassName + "' cannot be found: " + x, x);
		}
		if(m_application != null) {
			MetaInitializer.internalClear();
			BundleRef.internalClear();
			for(IReloadListener ll : listeners()) {
				try {
					ll.reloaded(m_reloader.getReloadingLoader());
				} catch(Exception x) {
					x.printStackTrace();
				}
			}

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
