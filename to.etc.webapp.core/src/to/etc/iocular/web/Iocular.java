package to.etc.iocular.web;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import to.etc.iocular.Container;

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
	static private ServletContext					m_appContext;

	static private Container						m_appContainer;

	/**
	 * Refers to the current session for the application. This is valid only after the request listener
	 * has activated.
	 */
	static private final ThreadLocal<HttpSession>	m_currentSession = new ThreadLocal<HttpSession>();

	/**
	 * Refers to the current session for the application. This is valid only after the request listener
	 * has activated.
	 */
	static private final ThreadLocal<HttpServletRequest>	m_currentRequest = new ThreadLocal<HttpServletRequest>();

	static private final ThreadLocal<Container>	m_requestContainer = new ThreadLocal<Container>();

	/**
	 * This class cannot be constructed.
	 */
	private Iocular() {}			

	/**
	 * Return the web application's context.
	 * @return
	 */
	static synchronized public final ServletContext		getApplication() {
		if(m_appContext == null)
			throw new IllegalStateException("The application context is not yet set. Have you configured WebApplicationListener as a Servlet Listener in web.xml?");
		return m_appContext;
	}

	static synchronized final void	_setApplication(ServletContext ctx, Container c) {
		if(m_appContext != null)
			throw new IllegalStateException("The application context is *already* set - internal error?");
		m_appContainer = c;
		m_appContext = ctx;
	}

	static public final HttpSession getCurrentSession() {
		HttpSession ses = m_currentSession.get();
		if(ses == null)
			throw new IllegalStateException("The 'current session' is unknown. Have you configured WebApplicationListener as a Servlet Listener in web.xml?");
		return ses;
	}
	static public final HttpServletRequest	getCurrentRequest() {
		HttpServletRequest req = m_currentRequest.get();
		if(req == null)
			throw new IllegalStateException("The 'current request' is unknown. Have you configured WebApplicationListener as a Servlet Listener in web.xml?");
		return req;
	}
	static final void _setRequest(HttpServletRequest req, Container c) {
		m_currentRequest.set(req);
		m_currentSession.set(req.getSession(true));
		m_requestContainer.set(c);
	}

	static final public Container	findApplicationContainer(ServletContext ctx) {
		return (Container) ctx.getAttribute(Keys.APP_CONTAINER);
	}
	static final public WebConfiguration	getConfiguration(ServletContext ctx) {
		WebConfiguration	wc = (WebConfiguration)ctx.getAttribute(Keys.APP_CONFIG);
		if(wc == null)
			throw new IllegalStateException("No web configuration: Have you configured WebApplicationListener as a Servlet Listener in web.xml?");
		return wc;
	}
	static final public Container	findSessionContainer(HttpSession ses) {
		return (Container) ses.getAttribute(Keys.SESSION_CONTAINER); 
	}
	static final public Container	findRequestContainer(HttpServletRequest ses) {
		return (Container) ses.getAttribute(Keys.REQUEST_CONTAINER); 
	}

	static final public Container	getRequestContainer() {
		Container	c = m_requestContainer.get();
		if(c == null)
			throw new IllegalStateException("No request executing");
		return c;
	}
}
