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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import to.etc.domui.login.DefaultLoginDeterminator;
import to.etc.domui.login.ILoginDeterminator;
import to.etc.domui.util.DomUtil;
import to.etc.log.EtcLoggerFactory;
import to.etc.util.ClassUtil;
import to.etc.util.DeveloperOptions;
import to.etc.util.StringTool;
import to.etc.util.WrappedException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Base filter which accepts requests to the dom windows. This accepts all URLs that end with a special
 * suffix and redigates them to the appropriate handler.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 22, 2008
 */
public class AppFilter implements Filter {

	static final Logger LOG = LoggerFactory.getLogger(AppFilter.class);

	public static final String LOGCONFIG_NAME = "etclogger.config.xml";

	private ConfigParameters m_config;

	private String m_applicationClassName;

	private boolean m_logRequest;

	static private String m_appContext;

	static private boolean m_testMode;

	@Nullable
	static private IRequestResponseWrapper m_ioWrapper;

	/**
	 * If a reloader is needed for debug/development pps this will hold the reloader.
	 */
	private IContextMaker m_contextMaker;

	/** If client logging is enabled this contains the registry. */
	private ServerClientRegistry m_clientRegistry;

	private ILoginDeterminator m_loginDeterminator;

	private static List<HttpSession> m_activeSessionList = new ArrayList<>();

	public static boolean isTestMode() {
		return m_testMode;
	}

	@Override
	public void destroy() {
		//-- Pass DESTROY on to Application, if present.
		if(DomApplication.get() != null)
			DomApplication.get().internalDestroy();
	}

	static public String minitime() {
		Calendar cal = Calendar.getInstance();
		return cal.get(Calendar.HOUR_OF_DAY) + StringTool.intToStr(cal.get(Calendar.MINUTE), 10, 2) + StringTool.intToStr(cal.get(Calendar.SECOND), 10, 2) + "." + cal.get(Calendar.MILLISECOND);
	}

	@Override
	public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain) throws IOException, ServletException {
		boolean logRequired = false;
		Throwable failure = null;
		String path = null;
		try {
			HttpServletRequest rq = (HttpServletRequest) req;
			path = rq.getRequestURI();            // getPathInfo() always returns null - what an idiots.
			logRequired = isLogRequired(path);
			if(logRequired) {
				LOG.error("ENTERED " + rq.getPathInfo());
			}

			if(LOG.isDebugEnabled()) {
				LOG.debug("--- Request entering the server");

				Enumeration<String> enu = rq.getHeaderNames();
				while(enu.hasMoreElements()) {
					String name = enu.nextElement();
					Enumeration<String> henu = rq.getHeaders(name);
					while(henu.hasMoreElements()) {
						String val = henu.nextElement();
						System.out.println("header: " + name + ": " + val);
					}
				}
				LOG.debug("uri " + rq.getRequestURI());
				LOG.debug("url " + rq.getRequestURL());
				LOG.debug("localName " + rq.getLocalName());
			}

			HttpServletResponse response = (HttpServletResponse) res;
			IRequestResponseWrapper ww = m_ioWrapper;
			if(null != ww) {
				rq = ww.getWrappedRequest(rq);
				response = ww.getWrappedResponse(response);
			}

			String userid = m_loginDeterminator.getLoginData(rq);
			if(null != userid) {
				m_clientRegistry.registerRequest(rq, userid);
				MDC.put(to.etc.log.EtcMDCAdapter.LOGINID, userid);
			}
			String id = rq.getSession().getId();
			if(null != id)
				MDC.put(to.etc.log.EtcMDCAdapter.SESSION, id);
			//LOG.info(MarkerFactory.getMarker("request-uri"), rq.getRequestURI()); -- useful for developer controlled debugging
			rq.setCharacterEncoding("UTF-8"); // FIXME jal 20080804 Encoding of input was incorrect?
			//			DomUtil.dumpRequest(rq);

			if(m_logRequest) {
				String rs = rq.getQueryString();
				rs = rs == null ? "" : "?" + rs;
				System.out.println(minitime() + " rq=" + rq.getRequestURI() + rs);
			}
			m_contextMaker.handleRequest(rq, response, chain);
		} catch(RuntimeException | ServletException x) {
			failure = x;
			DomUtil.dumpExceptionIfSevere(x);
			throw x;
		} catch(IOException x) {
			failure = x;
			if(x.getClass().getName().endsWith("ClientAbortException")) // Do not log these.
				throw x;
			DomUtil.dumpExceptionIfSevere(x);
			throw x;
		} catch(Exception x) {
			failure = x;
			DomUtil.dumpExceptionIfSevere(x);
			throw new WrappedException(x); // checked exceptions are idiotic
		} catch(Error x) {
			LOG.error("Request error: " + x, x);
			failure = x;
			throw x;
		} finally {
			if(logRequired) {
				LOG.error("EXITED " + path + " exception=" + failure);
			}
		}
	}

	private boolean isLogRequired(String s) {
		if(s == null)
			return false;
		return s.contains("rest/appliance2/loadResultTable");
	}

	//static synchronized private void initContext(ServletRequest req) {
	//	if(m_appContext != null || !(req instanceof HttpServletRequest))
	//		return;
	//
	//	m_appContext = NetTools.getApplicationContext((HttpServletRequest) req);
	//}
	//
	///**
	// * Do not use: does not work when hosting parties do not proxy correctly.
	// */
	//@Deprecated
	//static synchronized public String internalGetWebappContext() {
	//	return m_appContext;
	//}

	/**
	 * Initialize by reading config from the web.xml.
	 */
	@Override
	public synchronized void init(final FilterConfig config) throws ServletException {
		File approot = new File(config.getServletContext().getRealPath("/"));

		initLogConfig(approot, config.getInitParameter("logpath"));

		if(DeveloperOptions.isDeveloperWorkstation()) {
			config.getServletContext().getSessionCookieConfig().setHttpOnly(false);
			config.getServletContext().getSessionCookieConfig().setSecure(false);
		}

		try {
			m_logRequest = DeveloperOptions.getBool("domui.logurl", false);

			//-- Get the root for all files in the webapp
			LOG.info("WebApp root=" + approot);
			if(!approot.exists() || !approot.isDirectory())
				throw new IllegalStateException("Internal: cannot get webapp root directory");

			m_config = new FilterConfigParameters(config, approot);

			//-- Handle application construction
			m_applicationClassName = getApplicationClassName(m_config);
			if(m_applicationClassName == null)
				throw new UnavailableException("The application class name is not set. Use 'application' in the Filter parameters to set a main class.");

			//-- Do we want session logging?
			String s = config.getInitParameter("login-determinator");
			if(null != s) {
				m_loginDeterminator = ClassUtil.loadInstance(getClass().getClassLoader(), ILoginDeterminator.class, s);
			} else {
				m_loginDeterminator = new DefaultLoginDeterminator();
			}
			m_clientRegistry = ServerClientRegistry.getInstance();

			//-- Are we running in development mode?
			String domUiReload = DeveloperOptions.getString("domui.reload");
			String autoload = domUiReload != null ? domUiReload : m_config.getString("auto-reload");            // Allow override of web.xml values.

			//these patterns will be only watched not really reloaded. It makes sure the reloader kicks in. Found bundles and MetaData will be reloaded only.
			String autoloadWatchOnly = m_config.getString("auto-reload-watch-only");

			if(DeveloperOptions.isDeveloperWorkstation() && DeveloperOptions.getBool("domui.developer", true) && autoload != null && !autoload.trim().isEmpty()) {
				m_contextMaker = new ReloadingContextMaker(m_applicationClassName, m_config, autoload, autoloadWatchOnly);
				m_testMode = true;
			} else {
				m_contextMaker = new NormalContextMaker(m_applicationClassName, m_config);
				m_testMode = false;
			}

			config.getServletContext().addListener(new ActiveSessionListener());
		} catch(RuntimeException x) {
			DomUtil.dumpException(x);
			throw x;
		} catch(ServletException x) {
			DomUtil.dumpException(x);
			throw x;
		} catch(Exception x) {
			DomUtil.dumpException(x);
			throw new RuntimeException(x); // checked exceptions are idiotic
		} catch(Error x) {
			x.printStackTrace();
			throw x;
		}
	}

	static private void initLogConfig(@Nullable File appRoot, String logConfigParameter) throws Error {
		try {
			if(null == appRoot) {
				appRoot = new File(System.getProperty("user.home"));
			} else {
				appRoot = new File(appRoot, "WEB-INF");
			}

			//-- Prio 0: developer.properties
			File writablePath = null;
			File logSource = null;
			String logconfig = DeveloperOptions.getString("domui.logconfig");
			if(null != logconfig) {
				logSource = trySource(appRoot, logconfig);

				//-- Always have a writable config
				writablePath = makeAbs(appRoot, logconfig);
			}

			//-- Prio 1: -DLOGCONFIG
			logconfig = System.getProperty("LOGCONFIG");
			if(null != logconfig) {
				logSource = trySource(appRoot, logconfig);
				writablePath = makeAbs(appRoot, logconfig);
			}

			//-- Prio 2: parameter - not writable
			if(logSource == null && logConfigParameter != null) {
				logSource = trySource(appRoot, logConfigParameter);
			}

			//-- Prio 3: etclogger.config.xml in web-inf
			if(null == logSource) {
				logSource = trySource(appRoot, LOGCONFIG_NAME);
			}

			//-- 2. We now have either a path or not. Load the default config.
			if(logSource != null)
				EtcLoggerFactory.getSingleton().initializeFromFile(logSource, writablePath);
		} catch(Exception x) {
			x.printStackTrace();
			throw WrappedException.wrap(x);
		} catch(Error x) {
			x.printStackTrace();
			throw x;
		}
	}

	static private File makeAbs(File appRoot, String name) {
		File f = new File(name);
		if(f.isAbsolute())
			return f;
		return new File(appRoot, name);
	}

	@Nullable
	static private File trySource(File appRoot, String name) {
		File f = new File(name);
		if(!f.isAbsolute()) {
			f = new File(appRoot, name);
		}
		if(f.exists() && f.isFile())
			return f;
		return null;
	}

	static synchronized void addSession(HttpSession session) {
		m_activeSessionList.add(session);
	}

	static synchronized void removeSession(HttpSession session) {
		m_activeSessionList.remove(session);
	}

	static public synchronized List<HttpSession> getSessions(Predicate<HttpSession> filter) {
		return m_activeSessionList.stream()
			.filter(filter)
			.collect(Collectors.toList());
	}

	static public synchronized void setIoWrapper(@NonNull IRequestResponseWrapper ww) {
		m_ioWrapper = ww;
	}

	public String getApplicationClassName(final ConfigParameters p) {
		return p.getString("application");
	}
}
