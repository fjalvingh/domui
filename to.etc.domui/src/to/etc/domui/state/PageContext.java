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
package to.etc.domui.state;

import java.util.*;

import javax.annotation.*;
import javax.servlet.http.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.login.*;
import to.etc.domui.server.*;
import to.etc.domui.trouble.*;

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
	static private ThreadLocal<RequestContextImpl> m_current = new ThreadLocal<RequestContextImpl>();

	static private ThreadLocal<Page> m_page = new ThreadLocal<Page>();

	static private ThreadLocal<IUser> m_currentUser = new ThreadLocal<IUser>();

	static public IRequestContext getRequestContext() {
		IRequestContext rc = m_current.get();
		if(rc == null)
			throw new IllegalStateException("No current request!");
		return rc;
	}

	/**
	 * Called when a new request is to be made current, or when the request has
	 * ended.
	 * @param rc
	 */
	static public void internalSet(@Nonnull final RequestContextImpl rc) throws Exception {
		m_current.set(rc);
		boolean ok = false;
		try {
			m_currentUser.set(internalGetLoggedInUser(rc));
			ok = true;
		} finally {
			if(!ok)
				internalClear();
		}
	}

	/**
	 * This CLEARS all "current state" threadlocals.
	 */
	static public void internalClear() {
		m_current.set(null);
		m_currentUser.set(null);
		m_page.set(null);
	}

	static public void internalSet(final Page pg) {
		m_page.set(pg);
	}

	static public Page getCurrentPage() {
		Page pg = m_page.get();
		if(pg == null)
			throw new IllegalStateException("No current page");
		return pg;
	}

	static public ConversationContext getCurrentConversation() {
		return getCurrentPage().getConversation();
	}

	/**
	 * Return the currently-known logged in user, or null if unknown/not logged in.
	 * FIXME Should be named findCurrentUser().
	 * @return
	 */
	@Nullable
	static public IUser getCurrentUser() {
		return m_currentUser.get();
	}

	/**
	 * This returns the currently logged in user. If the user is not logged in this throws
	 * a login exception which should cause the user to log in.
	 * @return
	 */
	@Nonnull
	static public IUser getLoggedInUser() {
		IUser u = getCurrentUser();
		if(u == null)
			throw NotLoggedInException.create(getRequestContext(), getCurrentPage());
		return u;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Login page logic. Temporary location.				*/
	/*--------------------------------------------------------------*/
	static private final String LOGIN_KEY = IUser.class.getName();

	/**
	 * UNSTABLE INTERFACE. This tries to retrieve an IUser context for the user. It tries to
	 * retrieve a copy from the HttpSession. The AppSession is not used; this allows a login
	 * to persist when running in DEBUG mode, where AppSessions are destroyed when a class
	 * is changed.
	 *
	 * @param rci
	 * @return
	 */
	static public IUser internalGetLoggedInUser(final RequestContextImpl rci) throws Exception {
		HttpSession hs = rci.getRequest().getSession(false);
		if(hs == null)
			return null;
		synchronized(hs) {
			Object sval = hs.getAttribute(LOGIN_KEY); // Try to find the key,
			if(sval != null) {
				if(sval instanceof IUser) {
					//-- Proper IUser structure- return it.
					return (IUser) sval;
				}
			}

			/*
			 * If a LOGINCOOKIE is found check it's usability..
			 */
			Cookie[] car = rci.getRequest().getCookies();
			if(car != null) {
				for(Cookie c : car) {
					if(c.getName().equals("domuiLogin")) {
						String domval = c.getValue();
						IUser user = decodeCookie(rci, domval);
						if(user != null) {
							//-- Store the user in the HttpSession.
							hs.setAttribute(LOGIN_KEY, user);
							return user;
						}
						break;
					}
				}
			}

			/*
			 * If a remoteUser is set the user IS authenticated using Tomcat; get it's credentials.
			 */
			String ruser = rci.getRequest().getRemoteUser();
			if(ruser != null) {
				//-- Ask login provider for an IUser instance.
				ILoginAuthenticator la = rci.getApplication().getLoginAuthenticator();
				if(null == la)
					return null;

				IUser user = la.authenticateUser(ruser, null); // Tomcat authenticator has no password.
				if(user == null)
					throw new IllegalStateException("Internal: container has logged-in user '" + ruser + "', but authenticator class=" + la + " does not return an IUser for it!!");

				//-- Store the user in the HttpSession.
				hs.setAttribute(LOGIN_KEY, user);
				return user;
			}
			return null;
		}
	}

	/**
	 * Decode and check the cookie. It has the format:
	 * <pre>
	 * userid:timestamp:authhash
	 * </pre>
	 * The userid, timestamp as an yyyymmdd string and the password are hashed as an MD5 string and
	 * must be the same as the authhash for cookie auth to succeed.
	 * @param rci
	 * @param cookie
	 */
	static private IUser decodeCookie(final RequestContextImpl rci, final String cookie) {
		if(cookie == null)
			return null;
		String[] car = cookie.split(":");
		if(car.length != 3)
			return null;
		try {
			String uid = car[0];
			long ts = Long.parseLong(car[1]); // Timestamp

			//-- Lookup userid;
			ILoginAuthenticator la = rci.getApplication().getLoginAuthenticator();
			if(null == la)
				return null;

			return la.authenticateByCookie(uid, ts, car[2]); // Authenticate by cookie
		} catch(Exception x) {
			return null; // All cookie format exceptions mean no login
		}
	}

	/**
	 * Logs in a user. If he was logged in before he is logged out.
	 * @param userid
	 * @param password
	 * @return
	 */
	static public boolean login(final String userid, final String password) throws Exception {
		RequestContextImpl ci = m_current.get();
		if(ci == null)
			throw new IllegalStateException("You can login from a server request only");

		HttpSession hs = ci.getRequest().getSession(false);
		if(hs == null)
			return false;
		synchronized(hs) {
			//-- Force logout
			hs.removeAttribute(LOGIN_KEY);

			//-- Check credentials,
			ILoginAuthenticator la = ci.getApplication().getLoginAuthenticator();
			if(la == null)
				throw new IllegalStateException("There is no login authenticator set in the Application!");
			IUser user = la.authenticateUser(userid, password);
			if(user == null)
				return false;

			//-- Login succeeded: save the user in the session context
			hs.setAttribute(LOGIN_KEY, user); // This causes the user to be logged on.
			m_currentUser.set(user);

			List<ILoginListener> ll = ci.getApplication().getLoginListenerList();
			for(ILoginListener l : ll)
				l.userLogin(user);
			return true;
		}
	}

	/**
	 * Logs out a user.
	 * @throws Exception
	 */
	static public void logout() throws Exception {
		RequestContextImpl ci = m_current.get();
		if(ci == null)
			throw new IllegalStateException("You can logout from a server request only");

		HttpSession hs = ci.getRequest().getSession(false);
		if(hs == null)
			return;
		synchronized(hs) {
			IUser user = internalGetLoggedInUser(ci);
			if(user == null)
				return;

			//-- Call logout handlers BEFORE actual logout
			List<ILoginListener> ll = ci.getApplication().getLoginListenerList();
			for(ILoginListener l : ll) {
				try {
					l.userLogout(user);
				} catch(Exception x) {
					x.printStackTrace();
				}
			}

			//-- Force logout
			hs.removeAttribute(LOGIN_KEY);
			m_currentUser.set(null);
			try {
				hs.invalidate();
			} catch(Exception x) {
				//-- Invalidating 2ce causes a useless exception.
				x.printStackTrace();
			}
		}
	}

	public static Cookie createLoginCookie(final long l) throws Exception {
		IUser user = m_currentUser.get();
		if(user == null)
			return null;
		RequestContextImpl ci = m_current.get();
		if(ci == null)
			throw new IllegalStateException("You can login from a server request only");
		String auth = ci.getApplication().getLoginAuthenticator().calcCookieHash(user.getLoginID(), l);
		if(auth == null)
			return null;
		String value = user.getLoginID() + ":" + l + ":" + auth;
		Cookie k = new Cookie("domuiLogin", value);
		k.setMaxAge((int) ((l - System.currentTimeMillis()) / 1000)); // #seconds before expiry
		k.setPath(ci.getRequest().getContextPath());
		ci.getResponse().addCookie(k);
		return k;
	}
}
