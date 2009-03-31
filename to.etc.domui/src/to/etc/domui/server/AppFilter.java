package to.etc.domui.server;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.domui.util.nls.NlsContext;
import to.etc.util.*;

/**
 * Base filter which accepts requests to the dom windows. This accepts all URLs that end with a special
 * suffix and redigates them to the appropriate handler.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 22, 2008
 */
public class AppFilter implements Filter {
	static final Logger	LOG	= Logger.getLogger(AppFilter.class.getName());

	private ConfigParameters	m_config;

	private String				m_applicationClassName;

	private boolean				m_logRequest;

	/**
	 * If a reloader is needed for debug/development pps this will hold the reloader.
	 */

	private ContextMaker		m_contextMaker;
	
	public void destroy() {
	}

	static public String	minitime() {
		Calendar	cal	= Calendar.getInstance();
		return cal.get(Calendar.HOUR_OF_DAY)+StringTool.intToStr(cal.get(Calendar.MINUTE), 10, 2)+StringTool.intToStr(cal.get(Calendar.SECOND), 10, 2)+"."+cal.get(Calendar.MILLISECOND);
	}

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		try {
			HttpServletRequest rq = (HttpServletRequest)req;
			rq.setCharacterEncoding("UTF-8");						// jal 20080804 Encoding of input was incorrect?
			if(m_logRequest) {
				String rs = rq.getQueryString();
				rs = rs == null ? "" : "?"+rs;
				System.out.println(minitime()+" rq="+rq.getRequestURI()+rs);
			}
//			NlsContext.setLocale(rq.getLocale());
			NlsContext.setLocale(new Locale("nl", "NL"));

			if(m_contextMaker.handleRequest(rq, (HttpServletResponse)res))
				return;
		} catch(RuntimeException x) {
			DomUtil.dumpException(x);
			throw x;
		} catch(ServletException x) {
			DomUtil.dumpException(x);
			throw x;
		} catch(Exception x) {
			DomUtil.dumpException(x);
			throw new JamesGoslingIsAnIdiotException(x);		// James Gosling is an idiot
		}

		chain.doFilter(req, res);
	}

	/**
	 * Initialize by reading config from the web.xml.
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException {
		try {
			java.util.logging.LogManager.getLogManager().reset();
			java.util.logging.LogManager.getLogManager().readConfiguration(AppFilter.class.getResourceAsStream("logging.properties"));
		} catch(IOException x) {
			throw new JamesGoslingIsAnIdiotException(x);
		}
		System.out.println("Init logger");
//		System.out.println("QDataContext="+QDataContext.class.getClassLoader());

		m_logRequest = DeveloperOptions.getBool("domui.logurl", false);

		//-- Get the root for all files in the webapp
		File	approot = new File(config.getServletContext().getRealPath("/"));
		System.out.println("WebApp root="+approot);
		if(! approot.exists() || ! approot.isDirectory())
			throw new IllegalStateException("Internal: cannot get webapp root directory");
		
		m_config	= new ConfigParameters(config, approot);

		//-- Handle application construction
		m_applicationClassName = getApplicationClassName(m_config);
		if(m_applicationClassName == null)
			throw new UnavailableException("The application class name is not set. Use 'application' in the Filter parameters to set a main class.");

		//-- Are we running in debug mode?
		try {
			String	autoload = m_config.getString("auto-reload");
			if(autoload != null && autoload.trim().length() > 0)
				m_contextMaker = new ReloadingContextMaker(m_applicationClassName, m_config, autoload);
			else
				m_contextMaker = new NormalContextMaker(m_applicationClassName, m_config);
		} catch(RuntimeException x) {
			throw x;
		} catch(ServletException x) {
			throw x;
		} catch(Exception x) {
			throw new JamesGoslingIsAnIdiotException(x);		// James Gosling is an idiot
		}
	}

	public String		getApplicationClassName(ConfigParameters p) {
		return p.getString("application");
	}
}
