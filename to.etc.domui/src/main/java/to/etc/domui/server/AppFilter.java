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
import org.slf4j.MDC;
import to.etc.domui.util.DomUtil;
import to.etc.log.EtcLoggerFactory;
import to.etc.util.ClassUtil;
import to.etc.util.DeveloperOptions;
import to.etc.util.FileTool;
import to.etc.util.StringTool;
import to.etc.util.WrappedException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Enumeration;

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

	@Nullable
	static private IRequestResponseWrapper m_ioWrapper;

	/**
	 * If a reloader is needed for debug/development pps this will hold the reloader.
	 */
	private IContextMaker m_contextMaker;

	/** If client logging is enabled this contains the registry. */
	private ServerClientRegistry m_clientRegistry;

	private ILoginDeterminator m_loginDeterminator;

	static public synchronized void setIoWrapper(@Nonnull IRequestResponseWrapper ww) {
		m_ioWrapper = ww;
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
		try {
			HttpServletRequest rq = (HttpServletRequest) req;
			if(LOG.isDebugEnabled()) {
				System.out.println("--- Request entering the server");

				Enumeration<String> enu = rq.getHeaderNames();
				while(enu.hasMoreElements()) {
					String name = enu.nextElement();
					Enumeration<String> henu = rq.getHeaders(name);
					while(henu.hasMoreElements()) {
						String val = henu.nextElement();
						System.out.println("header: " + name + ": " + val);
					}
				}
				System.out.println("uri " + rq.getRequestURI());
				System.out.println("url " + rq.getRequestURL());
				System.out.println("localName " + rq.getLocalName());
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
			DomUtil.dumpExceptionIfSevere(x);
			throw x;
		} catch(IOException x) {
			if(x.getClass().getName().endsWith("ClientAbortException")) // Do not log these.
				throw x;
			DomUtil.dumpExceptionIfSevere(x);
			throw x;
		} catch(Exception x) {
			DomUtil.dumpExceptionIfSevere(x);
			throw new WrappedException(x); // checked exceptions are idiotic
		} catch(Error x) {
			x.printStackTrace();
			throw x;
		}
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

	@Nullable
	static private String readDefaultConfiguration(@Nullable String logConfigLocation) {
		if(logConfigLocation != null) {
			try {
				File configFile = new File(logConfigLocation);
				if(!(configFile.exists() && configFile.isFile())) {
					//-- Try to find this as a class-relative resource;
					if(!logConfigLocation.startsWith("/")) {
						String res = FileTool.readResourceAsString(AppFilter.class, "/" + logConfigLocation, "utf-8");
						if(res != null) {
							System.out.println("DomUI: using user-specified log config file from classpath-resource " + logConfigLocation);
							return res;
						}
					}
				} else {
					String res = FileTool.readFileAsString(configFile, "utf-8");
					if(res != null) {
						System.out.println("DomUI: using logging configuration file " + configFile.getAbsolutePath());
						return res;
					}
				}
			} catch(Exception ex) {
			}
		}

		/*
		 * Try root resource
		 */


		try {
			String res = FileTool.readResourceAsString(AppFilter.class, "/etcLoggerConfig.xml", "utf-8");
			if(res != null)
				System.out.println("DomUI: using /etcLoggerConfig.xml java resource");
			return res;
		} catch(Exception ex) {}

		try {
			String res = FileTool.readResourceAsString(AppFilter.class, "etcLoggerConfig.xml", "utf-8");
			if(res != null)
				System.out.println("DomUI: using internal etcLoggerConfig.xml");
			return res;
		} catch(Exception ex) {}

		return null;
	}

	/**
	 * Initialize by reading config from the web.xml.
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	@Override
	public synchronized void init(final FilterConfig config) throws ServletException {
		File approot = new File(config.getServletContext().getRealPath("/"));

		initLogConfig(approot, config.getInitParameter("logpath"));

		try {
			m_logRequest = DeveloperOptions.getBool("domui.logurl", false);

			//-- Get the root for all files in the webapp
			System.out.println("WebApp root=" + approot);
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
			String autoload = domUiReload != null ? domUiReload : m_config.getString("auto-reload"); 			// Allow override of web.xml values.

			//these patterns will be only watched not really reloaded. It makes sure the reloader kicks in. Found bundles and MetaData will be reloaded only.
			String autoloadWatchOnly = m_config.getString("auto-reload-watch-only");

			if(DeveloperOptions.isDeveloperWorkstation() && DeveloperOptions.getBool("domui.developer", true) && autoload != null && autoload.trim().length() > 0)
				m_contextMaker = new ReloadingContextMaker(m_applicationClassName, m_config, autoload, autoloadWatchOnly);
			else
				m_contextMaker = new NormalContextMaker(m_applicationClassName, m_config);
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

	static public void initLogConfig(@Nullable File appRoot, String logConfig) throws Error {
		try {
			if(null == appRoot) {
				appRoot = new File(System.getProperty("user.home"));
			} else {
				appRoot = new File(appRoot, "WEB-INF");
			}

			if(logConfig == null) {
				logConfig = LOGCONFIG_NAME;
			}

			//-- Resolve path(s)
			File configFile = null;
			if(logConfig.startsWith(File.separator)) {
				//-- Absolute path - obey
				configFile = new File(logConfig);
			} else {
				configFile = new File(appRoot, logConfig);
			}

			//-- Does this exist?
			if(configFile.isDirectory()) {
				configFile = new File(configFile, LOGCONFIG_NAME);
			}

			File configFolder = configFile.getParentFile();

			//-- Load the config, if present, or use the default config
			String xmlContent = null;
			if(configFile.exists() && configFile.isFile()) {
				try {
					xmlContent = FileTool.readFileAsString(configFile);
				} catch(Exception x) {
					System.err.println("etclog: failed to read " + configFile);
				}
			}

			//-- 3. If user-changed one failed- load a default one.
			if(null == xmlContent) {
				// -- Where to get log config from?
				String logspec = DeveloperOptions.getString("domui.logconfig");
				if(logspec == null) {
					logspec = System.getProperty("domui.logconfig");
					if(null == logspec)
						logspec = logConfig;
				}
				xmlContent = readDefaultConfiguration(logspec);
				if(null == xmlContent)
					throw new IllegalStateException("no logger configuration found at all");
			}

			EtcLoggerFactory.getSingleton().initialize(configFile, xmlContent);
		} catch(Exception x) {
			x.printStackTrace();
			throw WrappedException.wrap(x);
		} catch(Error x) {
			x.printStackTrace();
			throw x;
		}
	}

	public String getApplicationClassName(final ConfigParameters p) {
		return p.getString("application");
	}
}
