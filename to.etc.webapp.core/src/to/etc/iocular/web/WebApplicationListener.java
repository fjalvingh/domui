package to.etc.iocular.web;

import java.util.logging.*;

import javax.servlet.*;
import javax.servlet.http.*;


import to.etc.iocular.*;
import to.etc.iocular.container.*;
import to.etc.iocular.util.*;

/**
 * <p>This listener must be registered as a 'listener' entry in web.xml for
 * any webapp that requires the use of the iocular container. An example config
 * would be:
 * <pre><![CDATA[
 * 		<listener>
 * 			<listener-class>to.etc.iocular.web.WebApplicationListener</listener-class>
 * 		</listener>
 * ]]>
 * </p>
 * <p>This receives info when the web application starts and when it ends; this creates
 * the web- and session-scoped containers.
 *
 * @author jal
 * Created on Mar 25, 2007
 */
public class WebApplicationListener implements ServletContextListener, HttpSessionListener, ServletRequestListener {
	static private final Logger LOG = Logger.getLogger(WebApplicationListener.class.getName());

	/**
	 * A webapp is starting. This retrieves the configuration for all containers,
	 * and creates the Application container.
	 *
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent cxe) {
		try {
			LOG.fine("Starting web application.");
			createConfiguration(cxe.getServletContext());
			LOG.fine("Starting web application succeeded.");
		} catch(Throwable x) {
			x.printStackTrace();
		}
	}

	public void contextDestroyed(ServletContextEvent cxe) {
		LOG.info("Terminating web application.");
		Container c = Iocular.findApplicationContainer(cxe.getServletContext());
		if(c != null) {
			c.destroy();
		}
	}

	/**
	 * Creates the session container when a new session is registered.
	 *
	 * @see javax.servlet.http.HttpSessionListener#sessionCreated(javax.servlet.http.HttpSessionEvent)
	 */
	public void sessionCreated(HttpSessionEvent se) {
		LOG.fine("Session created");
		createSessionContainer(se.getSession());
		//		WebConfiguration	wc = Iocular.getConfiguration(se.getSession().getServletContext());
		//		Container c = Iocular.findApplicationContainer(se.getSession().getServletContext());
		//
		//		//-- Create the session container.
		//		synchronized(se.getSession()) {
		//			BasicContainer	bc = new BasicContainer(wc.getSessionDefinition(), c);
		//			bc.start();
		//			se.getSession().setAttribute(Keys.SESSION_CONTAINER, bc);
		//		}
	}

	private Container createSessionContainer(HttpSession hs) {
		synchronized(hs) {
			Container c = Iocular.findSessionContainer(hs);
			if(c != null)
				return c;

			WebConfiguration wc = Iocular.getConfiguration(hs.getServletContext());
			c = Iocular.findApplicationContainer(hs.getServletContext());
			BasicContainer bc = new BasicContainer(wc.getSessionDefinition(), c);
			bc.start();
			hs.setAttribute(Keys.SESSION_CONTAINER, bc);
			return bc;
		}
	}

	public void sessionDestroyed(HttpSessionEvent se) {
		LOG.info("Session destroyed");
		Container c = Iocular.findSessionContainer(se.getSession());
		if(c != null)
			c.destroy();
	}

	public void requestInitialized(ServletRequestEvent e) {
		LOG.finer("Request entered");

		HttpServletRequest req = (HttpServletRequest) e.getServletRequest();
		WebConfiguration wc = Iocular.getConfiguration(e.getServletContext());
		Container sc = createSessionContainer(req.getSession(true));
		if(sc == null)
			throw new IllegalStateException("No session container found!?");

		//-- Create a request container,
		BasicContainer bc = new BasicContainer(wc.getRequestDefinition(), sc);
		bc.start();
		req.setAttribute(Keys.REQUEST_CONTAINER, bc);
		Iocular._setRequest((HttpServletRequest) e.getServletRequest(), bc);
	}

	public void requestDestroyed(ServletRequestEvent e) {
		LOG.finer("Request destroyed");
		Container c = Iocular.findRequestContainer((HttpServletRequest) e.getServletRequest());
		if(c != null)
			c.destroy();
	}

	/**
	 * Entry called to configure the web app, if still needed.
	 * @param ctx
	 * @return
	 */
	static public WebConfiguration createConfiguration(ServletContext ctx) throws Exception {
		WebConfiguration conf = (WebConfiguration) ctx.getAttribute(Keys.APP_CONFIG);
		if(conf != null)
			return conf;

		//-- Is a specific configurator defined?
		WebConfigurator wc;
		String cn = ctx.getInitParameter("configurator-class");
		if(cn != null) {
			wc = ClassUtil.instanceByName(WebConfigurator.class, cn);
		} else {
			//-- No explicit configurator found. Use the default configurator.
			wc = new DefaultWebConfigurator();
		}
		conf = wc.createConfiguration(ctx);

		//-- Create the application container.
		Container c = createAppContainer(conf);
		Iocular._setApplication(ctx, c);
		ctx.setAttribute(Keys.APP_CONTAINER, c);
		ctx.setAttribute(Keys.APP_CONFIG, conf);
		return conf;
	}

	static private Container createAppContainer(WebConfiguration wc) {
		BasicContainer bc = new BasicContainer(wc.getApplicationDefinition(), null);
		bc.start();
		return bc;
	}
}
