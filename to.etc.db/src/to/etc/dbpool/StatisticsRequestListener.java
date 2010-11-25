/*
 * DomUI Java User Interface - shared code
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
package to.etc.dbpool;

import javax.annotation.*;
import javax.servlet.*;
import javax.servlet.http.*;

import to.etc.dbpool.info.*;

/**
 * This listener can be used to collect statistics for the database pool
 * when to.etc.dbpool's pool manager is used. This collects the URL's
 * used as input and collates database usage statistics per page.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 22, 2007
 */
public class StatisticsRequestListener implements ServletRequestListener {
	static private final int MAX_SESSION_REQUESTS = 50;

	static private class RecursionCounter {
		public int m_count;
	}

	private final ThreadLocal<RecursionCounter> m_ctr = new ThreadLocal<RecursionCounter>();

	static private GlobalPerformanceStore m_globalStore;

	public void requestDestroyed(ServletRequestEvent ev) {
		RecursionCounter rc = m_ctr.get();
		if(rc == null)
			return;
		rc.m_count--;
		if(rc.m_count != 0)
			return;
		m_ctr.set(null);
		ServletRequest sr = ev.getServletRequest();
		if(!(sr instanceof HttpServletRequest))
			return;
		HttpServletRequest r = (HttpServletRequest) sr;

		InfoCollectorExpenseBased td = (InfoCollectorExpenseBased) PoolManager.getInstance().stopCollecting(getClass().getName());
		if(null == td)
			return;
		long duration = System.nanoTime() - td.getStartTS(); // Store duration now, before any other action.

		PerformanceCollector pc = td.findCollector(PerformanceCollector.class);
		if(null != pc) {
			/*
			 * Is session-based collection on? Get the httpsession; this will abort if the session
			 * has been destroyed (logout) at a higher level. In that case we just return.
			 */
			HttpSession hs;
			try {
				hs = r.getSession();
			} catch(Exception x) {
				return;
			}

			SessionStatistics ss = null;
			if(hs != null) {
				synchronized(hs) {
					ss = (SessionStatistics) hs.getAttribute(getClass().getName());
				}
			}

			//-- Merge with central statistics.
			GlobalPerformanceStore global;
			synchronized(getClass()) {
				global = m_globalStore;
			}
			if(global != null || ss != null) {
				//-- Pass on the criteria..
				InfoCollectorBase icb = new InfoCollectorBase(td, duration); // Copy all request statistics.

				//-- Merge into globals if applicable
				if(null != global) {
					synchronized(global.getStore()) {
						global.getStore().merge(pc);
						global.addRequestInfo(icb);
					}
				}

				//-- Merge into session
				if(null != ss) {
					synchronized(ss) {
						ss.addRequestInfo(pc, icb);
					}
				}
			}
		}
		td.reportSimple();
	}

	/**
	 *
	 * @see javax.servlet.ServletRequestListener#requestInitialized(javax.servlet.ServletRequestEvent)
	 */
	public void requestInitialized(ServletRequestEvent ev) {
		ServletRequest sr = ev.getServletRequest();
		if(!(sr instanceof HttpServletRequest))
			return;
		HttpServletRequest r = (HttpServletRequest) sr;

		/*
		 * Recursion handling.
		 */
		RecursionCounter rc = m_ctr.get();
		if(rc != null) {
			rc.m_count++;
			return;
		}

		rc = new RecursionCounter();
		rc.m_count = 1;
		m_ctr.set(rc);

		/*
		 * Action handling: check for special URL parameters influencing the working of statistics gathering.
		 */
		String val = r.getParameter("__statistics");
		if(null != val) {
			val = val.toLowerCase();
			if("on".equals(val) || val.startsWith("t"))
				PoolManager.getInstance().setCollectStatistics(true);
			else if("off".equals(val) || val.startsWith("f")) {
				PoolManager.getInstance().setCollectStatistics(false);
				destroySessionStats(r);
				synchronized(getClass()) {
					m_globalStore = null;
				}
			}
		}
		if(!PoolManager.getInstance().isCollectStatistics())
			return;

		//-- Make sure a global store exists.
		synchronized(getClass()) {
			if(m_globalStore == null) {
				m_globalStore = new GlobalPerformanceStore();
			}
		}

		val = r.getParameter("__session");
		if(null != val) {
			//-- Requesting to collect stats for this session?
			if("on".equals(val) || val.startsWith("t"))
				createSessionStats(r);
			else if("off".equals(val) || val.startsWith("f"))
				destroySessionStats(r);
		}
		InfoCollectorExpenseBased ic = new InfoCollectorExpenseBased(r.getRequestURI(), r.getQueryString(), true);
		PerformanceCollector pc = new PerformanceCollector();
		ic.addPerformanceCollector(pc);
		PoolManager.getInstance().startCollecting(getClass().getName(), ic);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Session-based detailed statistics collection.		*/
	/*--------------------------------------------------------------*/
	/**
	 * Destroy any known session stats structure.
	 * @param r
	 */
	public static void destroySessionStats(HttpServletRequest r) {
		HttpSession hs = r.getSession(false); // Does session exist?
		if(null == hs)
			return;
		synchronized(hs) {
			hs.removeAttribute(StatisticsRequestListener.class.getName());
		}
	}

	/**
	 * Create a HttpSession, and add a Session Statistics block there. This will start
	 * session statistics collection. If the block already exists nothing happens.
	 * @param r
	 */
	public static void createSessionStats(HttpServletRequest r) {
		HttpSession hs = r.getSession(true); // Get/create session.
		SessionStatistics ss;
		synchronized(hs) {
			ss = (SessionStatistics) hs.getAttribute(StatisticsRequestListener.class.getName());
			if(null != ss)
				return;
			ss = new SessionStatistics(MAX_SESSION_REQUESTS);
			hs.setAttribute(StatisticsRequestListener.class.getName(), ss); // Store in session.
		}
	}

	@Nullable
	static public SessionStatistics getSessionStatistics(HttpServletRequest r) {
		HttpSession hs = r.getSession(false); // Does session exist?
		if(null == hs)
			return null;
		synchronized(hs) {
			return (SessionStatistics) hs.getAttribute(StatisticsRequestListener.class.getName());
		}
	}


	/**
	 * Returns the current global performance store maintained by this listener. Returns null if not collecting statistics.
	 * @return
	 */
	public synchronized static GlobalPerformanceStore getGlobalStore() {
		return m_globalStore;
	}
}
