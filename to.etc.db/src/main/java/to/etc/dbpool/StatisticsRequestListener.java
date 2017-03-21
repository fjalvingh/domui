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

import to.etc.dbpool.info.*;

import javax.annotation.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

/**
 * This listener can be used to collect statistics for the database pool
 * when to.etc.dbpool's pool manager is used. This collects the URL's
 * used as input and collates database usage statistics per page.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 22, 2007
 */
final public class StatisticsRequestListener implements ServletRequestListener {
	static private final int MAX_SESSION_REQUESTS = 50;

	static final private boolean DEBUG = false;

	/**
	 * When set this forces an input request that has no encoding to the encoding specified. This fixes
	 * bugs with IE, who else, which does not send the charset header.
	 *
	 */
	static private String m_forceEncoding;

	/**
	 * Make sure the encoding is set instead of failing silently and fscking up input - because
	 * that takes forever to fix 8-/
	 */
	static private boolean m_encodingSet;

	static private class PerThreadData {
		public PerThreadData() {}

		public int m_count;

		@Nullable
		public StatisticsCollector m_collector;

		/** If we have a http session request collector it's in here. */
		@Nullable
		public SessionStatistics m_sessionStatistics;

		@Nullable
		public String m_id;
	}

	static private volatile boolean m_enableSessionStatisticsForEveryone;

	/**
	 * Per session/thread the data related to performance/statistics collection for that thread.
	 */
	static private final ThreadLocal<PerThreadData> m_perThreadData = new ThreadLocal<PerThreadData>();

	static private GlobalPerformanceStore m_globalStore;

	public interface UnclosedListener {
		void unclosed(HttpServletRequest r, List<ConnectionProxy> list);
	}

	static private UnclosedListener m_unclosedListener;

	static private long m_nextId;

	/**
	 * At request finish, we collect all of the statistics we gathered and add them
	 * to the accounting outputs (the stores). We also check whether all connections
	 * are properly closed.
	 *
	 * @param ev
	 */
	@Override
	public void requestDestroyed(ServletRequestEvent ev) {
		ServletRequest sr = ev.getServletRequest();
		if(!(sr instanceof HttpServletRequest))
			return;
		HttpServletRequest r = (HttpServletRequest) sr;
		PerThreadData threadData = m_perThreadData.get();

		//-- Recursion control.
		if(DEBUG) {
			System.out.println("SRL: " + Thread.currentThread().getName() + " depth=" + (threadData == null ? "null" : threadData.m_count) + " rq=" + r.getRequestURI());
		}
		if(threadData == null)
			return;
		threadData.m_count--;
		if(threadData.m_count != 0)
			return;

		//-- We're at the outer level... Handle the data.
		m_perThreadData.set(null);

		//-- Handle unclosed connections, if needed
		UnclosedListener ucl = getUnclosedListener();
		if(null != ucl) {
			List<ConnectionProxy> uncl = PoolManager.getInstance().getThreadConnections();
			if(uncl.size() > 0)
				ucl.unclosed(r, uncl);
		}

		StatisticsCollector statisticsCollector = (StatisticsCollector) PoolManager.getInstance().stopCollecting(getClass().getName());
		if(null == statisticsCollector)
			return;
		long duration = System.nanoTime() - statisticsCollector.getStartTS(); 		// Store duration now, before any other action.

		PerformanceCollector pc = new PerformanceCollector();	// A new store for the thingy.
		pc.saveCounters(statisticsCollector.getIdent(), statisticsCollector.getCounters());

		/*
		 * Is session-based collection on? Get the httpsession; this will abort if the session
		 * has been destroyed (logout) at a higher level. In that case we just return.
		 */
		SessionStatistics sessionStatistics = threadData.m_sessionStatistics;

		//-- Merge with central statistics.
		GlobalPerformanceStore global;
		synchronized(getClass()) {
			global = m_globalStore;
		}
		if(global != null || sessionStatistics != null) {
			//-- Pass on the criteria..
			StatisticsCollectorBase icb = new StatisticsCollectorBase(statisticsCollector, duration); // Copy all request statistics.

			//-- Merge into globals if applicable
			if(null != global) {
				synchronized(global.getStore()) {
					global.getStore().merge(pc);
					global.addRequestInfo(icb);
				}
			}

			//-- Merge into session
			if(null != sessionStatistics) {
				synchronized(sessionStatistics) {
					sessionStatistics.addRequestInfo(threadData.m_id, pc, icb);
				}
			}
		}
		statisticsCollector.reportSimple();
	}

	/**
	 *
	 * @see javax.servlet.ServletRequestListener#requestInitialized(javax.servlet.ServletRequestEvent)
	 */
	@Override
	public void requestInitialized(ServletRequestEvent ev) {
		ServletRequest sr = ev.getServletRequest();
		if(!(sr instanceof HttpServletRequest))
			return;
		HttpServletRequest r = (HttpServletRequest) sr;
		PerThreadData threadData = m_perThreadData.get();
		if(DEBUG) {
			System.out.println("SRL: " + Thread.currentThread().getName() + " depth=" + (threadData == null ? "null" : threadData.m_count) + " rq=" + r.getRequestURI());
		}
		if(threadData != null) {								// Handle recursion
			threadData.m_count++;
			return;
		}
		PoolManager.getInstance().clearThreadConnections();
		threadData = new PerThreadData();
		threadData.m_count = 1;
		m_perThreadData.set(threadData);

		updateEncoding(r);
		checkEnableStatisticsParameter(r);
		if(!PoolManager.getInstance().isCollectStatistics())
			return;

		//-- Make sure a global store exists.
		synchronized(getClass()) {
			if(m_globalStore == null) {
				m_globalStore = new GlobalPerformanceStore();
			}
		}

		if(m_enableSessionStatisticsForEveryone) {					// volatile
			threadData.m_sessionStatistics = createSessionStats(r);
		} else {
			String val = r.getParameter("__session");
			if(null != val) {
				//-- Requesting to collect stats for this session?
				if("on".equals(val) || val.startsWith("t"))
					threadData.m_sessionStatistics = createSessionStats(r);
				else if("off".equals(val) || val.startsWith("f"))
					destroySessionStats(r);
			} else {
				threadData.m_sessionStatistics = getSessionStatistics(r);
			}
		}

		threadData.m_id = nextID();
		StatisticsCollector ic = new StatisticsCollector(r.getRequestURI(), r.getQueryString(), threadData.m_sessionStatistics != null);
		threadData.m_collector = ic;
		PoolManager.getInstance().startCollecting(getClass().getName(), ic);
	}

	private void checkEnableStatisticsParameter(HttpServletRequest r) {
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
		val = r.getParameter("__trace");								// Not shown in UI: enable session trace
		if(null != val) {
			val = val.toLowerCase();
			OracleStatisticsCreator.enableSessionTrace("on".equals(val) || val.startsWith("t"));
		}
	}

	private void updateEncoding(HttpServletRequest r) {
		//-- If needed, force charset encoding on request, sigh.
		String enc = r.getCharacterEncoding();
		if(null == enc || enc.trim().length() == 0) {
			enc = getForceEncoding();
			if(enc != null) {
				try {
					r.setCharacterEncoding(enc);
				} catch(UnsupportedEncodingException x) {
					throw new RuntimeException(x); // Deep, deep sigh
				}
			}
		}
	}

	static private synchronized String nextID() {
		return Long.toString(nextIDNr(), 36);
	}
	static private long nextIDNr() {
		return ++m_nextId;
	}

	static public void setSessionStatistics(boolean on) {
		m_enableSessionStatisticsForEveryone = on;
	}

	/**
	 * Get the current counts for SQL statements for the current thread, if enabled/available. These
	 * are the statistics <b>so far</b> of course.
	 * @return
	 */
	@Nullable
	public static final StatisticsCollectorBase getThreadStatistics() {
		PerThreadData perThreadData = m_perThreadData.get();
		if(null == perThreadData)
			return null;

		StatisticsCollector collector = perThreadData.m_collector;
		if(null == collector)
			return null;
		long duration = System.nanoTime() - collector.getStartTS(); // Store duration now, before any other action.
		return new StatisticsCollectorBase(collector, duration);
	}

	/**
	 * Return the unique ID for this request/response cycle which can be used to identify the metrics for
	 * this request after it finished.
	 * @return
	 */
	@Nullable
	public static final String getRequestID() {
		PerThreadData perThreadData = m_perThreadData.get();
		if(null == perThreadData)
			return null;
		return perThreadData.m_id;
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
	@Nonnull
	public static SessionStatistics createSessionStats(HttpServletRequest r) {
		HttpSession hs = r.getSession(true); 					// Get/create session.
		SessionStatistics ss;
		synchronized(hs) {
			ss = (SessionStatistics) hs.getAttribute(StatisticsRequestListener.class.getName());
			if(null != ss)
				return ss;
			ss = new SessionStatistics(MAX_SESSION_REQUESTS);
			hs.setAttribute(StatisticsRequestListener.class.getName(), ss); // Store in session.
			return ss;
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




	/**
	 * Advanced horror mode: Internet Exploder, who else, does not send the charset it encoded
	 * the parameters with in it's content type for input received. Because of this, when
	 * content is sent to the server from IE that is encoded in UTF-8, the server does not
	 * <i>know</i> that and will decode the parameters as the default encoding (iso-8859-1 probably).
	 * This will cause parameter values to be wrong. And because these are decoded only once by
	 * Tomcat and then stored- using the getParameter() call here will cause encoding problems.
	 * Parameters are decoded as iso-8859-1 and a later call to use UTF-8 encoding is silently
	 * ignored, because otherwise the problem would be clear. The workaround here is to force
	 * input decoding to a specified encoding (usually UTF-8) always when the charset header is
	 * missing.
	 *
	 * @param forceEncoding
	 */
	synchronized public static void setForceEncoding(String forceEncoding) {
		m_forceEncoding = forceEncoding;
		m_encodingSet = true;
	}

	synchronized private String getForceEncoding() {
		if(! m_encodingSet)
			//-- Lets not fail silently and cause horror.
			throw new RuntimeException("**** INPUT ENCODING NOT DEFINED FOR DBPOOL'S STATISTICS FILTER!!\n" //
				+ "You have added the to.etc.dbpool.StatisticsRequestListener to your web.xml.\n"//
				+ "There is of course a bug in Internet Explorer where it does not sent proper encoding\n"//
				+ "information (the charset header) in data it sends back to the server. If that happens the\n"//
				+ "server guesses the encoding- usually wrong. This would lead to encoding errors (strange\n"//
				+ "characters) in input from the browser. The only way to prevent this is to add a call in your\n"//
				+ "web app initialization: StatisticsRequestListener.setForceEncoding(\"utf-8\");\n"//
			);
		return m_forceEncoding;
	}

	synchronized static public void setUnclosedListener(UnclosedListener ucl) {
		m_unclosedListener = ucl;
		PoolManager.getInstance().setCheckCloseConnections(true);
	}

	private static synchronized UnclosedListener getUnclosedListener() {
		return m_unclosedListener;
	}

}
