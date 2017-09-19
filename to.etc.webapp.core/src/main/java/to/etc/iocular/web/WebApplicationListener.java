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
package to.etc.iocular.web;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.iocular.Container;
import to.etc.iocular.ioccontainer.BasicContainer;
import to.etc.iocular.util.ClassUtil;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

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
	static private final Logger LOG = LoggerFactory.getLogger(WebApplicationListener.class);

	/**
	 * A webapp is starting. This retrieves the configuration for all containers,
	 * and creates the Application container.
	 *
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextInitialized(ServletContextEvent cxe) {
		try {
			LOG.debug("Starting web application.");
			createConfiguration(cxe.getServletContext());
			LOG.debug("Starting web application succeeded.");
		} catch(Throwable x) {
			x.printStackTrace();
		}
	}

	@Override
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
	@Override
	public void sessionCreated(HttpSessionEvent se) {
		LOG.debug("Session created");
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

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		LOG.info("Session destroyed");
		Container c = Iocular.findSessionContainer(se.getSession());
		if(c != null)
			c.destroy();
	}

	@Override
	public void requestInitialized(ServletRequestEvent e) {
		LOG.debug("Request entered");

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

	@Override
	public void requestDestroyed(ServletRequestEvent e) {
		LOG.debug("Request destroyed");
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
