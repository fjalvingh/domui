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

import javax.servlet.*;
import javax.servlet.http.*;

import to.etc.iocular.*;

/**
 * Singleton utility class to access thread-based request and application contexts
 * without having to pass crud around. This is a public utility class, but using
 * it in user code binds your code tightly to the Iocular framework which is a no-no.
 * Consequently, using this from user code is an indication of a design flaw. The only proper
 * use for this code is from within framework code.
 *
 * @author jal
 * Created on Mar 25, 2007
 */
public class Iocular {
	/** Refers to the single ServletContext instance for the application. Gets valid when the WebApplicationListener starts. */
	static private ServletContext m_appContext;

	//	static private Container						m_appContainer;

	/**
	 * Refers to the current session for the application. This is valid only after the request listener
	 * has activated.
	 */
	static private final ThreadLocal<HttpSession> m_currentSession = new ThreadLocal<HttpSession>();

	/**
	 * Refers to the current session for the application. This is valid only after the request listener
	 * has activated.
	 */
	static private final ThreadLocal<HttpServletRequest> m_currentRequest = new ThreadLocal<HttpServletRequest>();

	static private final ThreadLocal<Container> m_requestContainer = new ThreadLocal<Container>();

	/**
	 * This class cannot be constructed.
	 */
	private Iocular() {}

	/**
	 * Return the web application's context.
	 * @return
	 */
	static synchronized public final ServletContext getApplication() {
		if(m_appContext == null)
			throw new IllegalStateException("The application context is not yet set. Have you configured WebApplicationListener as a Servlet Listener in web.xml?");
		return m_appContext;
	}

	static synchronized final void _setApplication(final ServletContext ctx, final Container c) {
		if(m_appContext != null)
			throw new IllegalStateException("The application context is *already* set - internal error?");
		//		m_appContainer = c;
		m_appContext = ctx;
	}

	static public final HttpSession getCurrentSession() {
		HttpSession ses = m_currentSession.get();
		if(ses == null)
			throw new IllegalStateException("The 'current session' is unknown. Have you configured WebApplicationListener as a Servlet Listener in web.xml?");
		return ses;
	}

	static public final HttpServletRequest getCurrentRequest() {
		HttpServletRequest req = m_currentRequest.get();
		if(req == null)
			throw new IllegalStateException("The 'current request' is unknown. Have you configured WebApplicationListener as a Servlet Listener in web.xml?");
		return req;
	}

	static final void _setRequest(final HttpServletRequest req, final Container c) {
		m_currentRequest.set(req);
		m_currentSession.set(req.getSession(true));
		m_requestContainer.set(c);
	}

	static final public Container findApplicationContainer(final ServletContext ctx) {
		return (Container) ctx.getAttribute(Keys.APP_CONTAINER);
	}

	static final public WebConfiguration getConfiguration(final ServletContext ctx) {
		WebConfiguration wc = (WebConfiguration) ctx.getAttribute(Keys.APP_CONFIG);
		if(wc == null)
			throw new IllegalStateException("No web configuration: Have you configured WebApplicationListener as a Servlet Listener in web.xml?");
		return wc;
	}

	static final public Container findSessionContainer(final HttpSession ses) {
		return (Container) ses.getAttribute(Keys.SESSION_CONTAINER);
	}

	static final public Container findRequestContainer(final HttpServletRequest ses) {
		return (Container) ses.getAttribute(Keys.REQUEST_CONTAINER);
	}

	static final public Container getRequestContainer() {
		Container c = m_requestContainer.get();
		if(c == null)
			throw new IllegalStateException("No request executing");
		return c;
	}
}
