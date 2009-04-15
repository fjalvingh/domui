package to.etc.domui.state;

import javax.servlet.http.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.login.*;
import to.etc.domui.server.*;

/**
 * A class which allows access to the page's context and related information. This
 * is experimental. The PageContext is the root for all navigational information,
 * and interfaces the pages and the server. This would usually be the task of the
 * RequestContext, but that's an interface and I want the primary accessor to be
 * in the same class.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 22, 2008
 */
public class PageContext {
	static private ThreadLocal<RequestContextImpl>	m_current = new ThreadLocal<RequestContextImpl>();
	static private ThreadLocal<Page>				m_page	= new ThreadLocal<Page>();
	static private ThreadLocal<IUser>				m_currentUser = new ThreadLocal<IUser>();

	static public RequestContext	getRequestContext() {
		RequestContext	rc = m_current.get();
		if(rc == null)
			throw new IllegalStateException("No current request!");
		return rc;
	}

	/**
	 * Called when a new request is to be made current, or when the request has
	 * ended.
	 * @param rc
	 */
	static public void	internalSet(final RequestContextImpl rc) {
		m_current.set(rc);
		if(rc == null)
			m_currentUser.set(null);
		else
			m_currentUser.set(internalGetLoggedInUser(rc));
	}

	static public void	internalSet(final Page pg) {
		m_page.set(pg);
	}
	static public Page	getCurrentPage() {
		Page	pg = m_page.get();
		if(pg == null)
			throw new IllegalStateException("No current page");
		return pg;
	}
	static public ConversationContext	getCurrentConversation() {
		return getCurrentPage().getConversation();
	}
	static public IUser	getCurrentUser() {
		return m_currentUser.get();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Login page logic. Temporary location.				*/
	/*--------------------------------------------------------------*/
	static private final String	LOGIN_KEY	= IUser.class.getName();

	/**
	 * UNSTABLE INTERFACE. This tries to retrieve an IUser context for the user. It tries to
	 * retrieve a copy from the HttpSession. The AppSession is not used; this allows a login
	 * to persist when running in DEBUG mode, where AppSessions are destroyed when a class
	 * is changed.
	 *
	 * @param rci
	 * @return
	 */
	static public IUser		internalGetLoggedInUser(final RequestContextImpl rci) {
		HttpSession	hs	= rci.getRequest().getSession(false);
		if(hs == null)
			return null;
		Object	sval = hs.getAttribute(LOGIN_KEY);					// Try to find the key,
		if(sval != null) {
			if(sval instanceof IUser) {
				//-- Proper IUser structure- return it.
				return (IUser) sval;
			}
		}

		/*
		 * If a remoteUser is set the user IS authenticated using Tomcat; get it's credentials.
		 */
		String	ruser = rci.getRequest().getRemoteUser();
		if(ruser != null) {
			//-- Ask login provider for an IUser instance.
			ILoginHandler	la	= rci.getApplication().getLoginAuthenticator();
			if(null == la)
				return null;

			IUser	user = la.authenticateUser(ruser, null);		// Tomcat authenticator has no password.
			if(user == null)
				throw new IllegalStateException("Internal: container has logged-in user '"+ruser+"', but authenticator class="+la+" does not return an IUser for it!!");

			//-- Store the user in the HttpSession.
			hs.setAttribute(LOGIN_KEY, user);
			return user;
		}
		return null;
	}
}
