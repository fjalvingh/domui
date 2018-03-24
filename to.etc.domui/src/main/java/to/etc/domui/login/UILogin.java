package to.etc.domui.login;

import to.etc.domui.server.HttpServerRequestResponse;
import to.etc.domui.server.ILoginListener;
import to.etc.domui.server.IRequestContext;
import to.etc.domui.server.IServerSession;
import to.etc.domui.server.RequestContextImpl;
import to.etc.domui.state.UIContext;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 10-11-17.
 */
@DefaultNonNull
final public class UILogin {
	static private volatile ILoginHandler	m_loginHandler = new DefaultLoginHandler();

	/**
	 * Contains, for the current request, the user that is to be considered the
	 * "actual user". When impersonating this contains the impersonated user.
	 */
	static private ThreadLocal<IUser> m_currentUser = new ThreadLocal<>();

	/**
	 * If impersonation is active this contains the user ID of the actual user,
	 * i.e. the original user that impersonated the new user.
	 */
	static private ThreadLocal<IUser> m_impersonator = new ThreadLocal<>();

	private UILogin() {
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

	static public void setCurrentUser(@Nullable IUser user) {
		m_currentUser.set(user);
		if(null == user) {
			m_impersonator.set(null);
		}
	}

	/**
	 * If impersonation is active this is the actual user that was doing the impersonation.
	 */
	@Nullable
	static public IUser getImpersonator() {
		return m_impersonator.get();
	}

	public static ILoginHandler getLoginHandler() {
		return m_loginHandler;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Login page logic. Temporary location.				*/
	/*--------------------------------------------------------------*/
	static public final String LOGIN_KEY = IUser.class.getName() + ".user";

	static public final String IMPERSONATION_KEY = IUser.class.getName() + ".impersonated";

	/**
	 * UNSTABLE INTERFACE. This tries to retrieve an IUser context for the user. It tries to
	 * retrieve a copy from the HttpSession. The AppSession is not used; this allows a login
	 * to persist when running in DEBUG mode, where AppSessions are destroyed when a class
	 * is changed. This does not handle impersonation: that is done later.
	 *
	 * @return The actual IUser if logged in (not the impersonated one) - or null if not logged in.
	 */
	@Nullable
	static private IUser internalGetLoggedInUser(final IRequestContext rx) throws Exception {
		if(!(rx instanceof RequestContextImpl))
			return null;
		RequestContextImpl rci = (RequestContextImpl) rx;
		HttpServerRequestResponse srr = null;
		if(rci.getRequestResponse() instanceof HttpServerRequestResponse) {
			srr = (HttpServerRequestResponse) rci.getRequestResponse();
		}

		if(srr != null) {
			HttpSession hs = srr.getRequest().getSession(false);
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
				 * If a LOGINCOOKIE is found check it's usability. If the cookie is part of the ignored hash set try to delete it again and again...
				 */
				Cookie[] car = srr.getRequest().getCookies();
				if(car != null) {
					for(Cookie c : car) {
						if(c.getName().equals("domuiLogin")) {
							String domval = c.getValue();
							IUser user = UILogin.getLoginHandler().decodeCookie(rci, domval);
							if(user != null) {
								//-- Store the user in the HttpSession.
								hs.setAttribute(LOGIN_KEY, user);
								return user;
							} else {
								//-- Invalid cookie: delete it.
								c.setMaxAge(10);
								c.setValue("logout");
							}
							break;
						}
					}
				}

				/*
				 * If a remoteUser is set the user IS authenticated using Tomcat; get it's credentials.
				 */
				String ruser = srr.getRequest().getRemoteUser();
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
			}

			ILoginAuthenticator la = rci.getApplication().getLoginAuthenticator();
			if(null == la)
				return null;

			IUser user = la.authenticateByRequest(rx);
			if(null != user) {
				//-- Store the user in the HttpSession.
				hs.setAttribute(LOGIN_KEY, user);
				return user;
			}
		}

		return null;
	}

	/**
	 * Returns the real user, i.e. the impersonator if impersonating.
	 */
	@Nullable
	static public IUser	getRealUser() {
		IUser impersonator = getImpersonator();
		if(null != impersonator)
			return impersonator;
		return m_currentUser.get();
	}

	/**
	 * Impersonate the specified user. The user should not be yourself. Setting to null
	 * means stop impersonating.
	 */
	static public void impersonate(@Nullable IUser user) {
		IUser real = getRealUser();
		if(null == real)
			throw new IllegalStateException("There is no currently logged in user");
		IRequestContext rc = UIContext.getRequestContext();
		IServerSession hs = rc.getServerSession(true);
		if(null == hs)
			throw new IllegalStateException("There is no http session available");

		if(real == user || real.equals(user) || user == null) {
			setCurrentUser(real);
			m_impersonator.set(null);
			hs.setAttribute(IMPERSONATION_KEY, null);
			return;
		}
		m_currentUser.set(user);
		m_impersonator.set(real);
		hs.setAttribute(IMPERSONATION_KEY, user);
	}

	static public void impersonateByLoginId(@Nonnull String userId) throws Exception {
		//-- Be sure the current user is allowed this.
		IUser realUser = getRealUser();
		if(null == realUser)
			throw new IllegalStateException("There is no currently logged in user");
		if(! realUser.canImpersonate())
			throw new ImpersonationFailedException("You have no rights to impersonate");

		//-- Ask login provider for an IUser instance.
		IRequestContext rc = UIContext.getRequestContext();
		ILoginAuthenticator la = rc.getApplication().getLoginAuthenticator();
		if(null == la)
			throw new IllegalStateException("No login authenticator is set in DomApplication");

		IUser user = la.authenticateUser(userId, null);				// Passwordless authentication
		if(user == null)
			throw new ImpersonationFailedException("Could not log in as user '" + userId + "'");
		impersonate(user);
	}

	/**
	 * Logs in a user. If he was logged in before he is logged out.
	 */
	static public boolean login(final String userid, final String password) throws Exception {
		return UILogin.getLoginHandler().login(userid, password) == LoginResult.SUCCESS;
	}

	/**
	 * Logs out a user.
	 * @throws Exception
	 */
	static public void logout() throws Exception {
		IRequestContext rcx = UIContext.getRequestContext();
		if(!(rcx instanceof RequestContextImpl))
			return;

		IServerSession hs = rcx.getServerSession(false);
		if(hs == null)
			return;

		//first we delete LOGINCOOKIE if exists, otherwise user can never logout...
		deleteLoginCookie(rcx);
		synchronized(hs) {
			IUser user = internalGetLoggedInUser(rcx);
			if(user == null)
				return;

			//-- Call logout handlers BEFORE actual logout
			List<ILoginListener> ll = rcx.getApplication().getLoginListenerList();
			for(ILoginListener l : ll) {
				try {
					l.userLogout(user);
				} catch(Exception x) {
					x.printStackTrace();
				}
			}

			//-- Force logout
			hs.setAttribute(LOGIN_KEY, null);
			hs.setAttribute(IMPERSONATION_KEY, null);
			m_currentUser.set(null);
			try {
				hs.invalidate();
			} catch(Exception x) {
				//-- Invalidating 2ce causes a useless exception.
				x.printStackTrace();
			}
		}
	}

	@Nullable
	public static Cookie createLoginCookie(long l) throws Exception {
		IUser user = m_currentUser.get();
		if(user == null)
			return null;

		IRequestContext rcx = UIContext.getRequestContext();
		if(!(rcx instanceof RequestContextImpl))
			return null;
		RequestContextImpl ci = (RequestContextImpl) rcx;

		String auth = ci.getApplication().getLoginAuthenticator().calcCookieHash(user.getLoginID(), l);
		if(auth == null)
			return null;
		String value = user.getLoginID() + ":" + l + ":" + auth;
		Cookie k = new Cookie("domuiLogin", value);
		k.setMaxAge((int) ((l - System.currentTimeMillis()) / 1000)); // #seconds before expiry
		k.setPath(ci.getRequestResponse().getWebappContext());
		ci.getRequestResponse().addCookie(k);
		return k;
	}

	public static boolean deleteLoginCookie(IRequestContext rci) throws Exception {
		if(rci == null)
			throw new IllegalStateException("You can logout from a server request only");

		Cookie[] car = rci.getRequestResponse().getCookies();
		if(car != null) {
			for(Cookie c : car) {
				if(c.getName().equals("domuiLogin")) {
					String[] var = c.getValue().split(":");
					if(var.length == 3) {
						//-- Make sure the same hash value is not used for login again. This prevents "relogin" when the browser sends some requests with the "old" cookie value (obituaries)
						UILogin.getLoginHandler().registerIgnoredHash(var[2]);
					}

					//-- Create a new cookie value containing a delete.
					Cookie k = new Cookie("domuiLogin", "logout");
					k.setMaxAge(60);
					k.setPath(rci.getRequestResponse().getWebappContext());
					rci.getRequestResponse().addCookie(k);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Called when a request enters the server to set the current login.
	 */
	public static void internalSetLoggedInUser(IRequestContext rc) throws Exception {
		IUser user = UILogin.internalGetLoggedInUser(rc);
		if(null == user) {
			setCurrentUser(null);
			return;
		}
		IServerSession hs = rc.getServerSession(false);
		if(hs != null) {
			Object o = hs.getAttribute(IMPERSONATION_KEY);
			if(o instanceof IUser) {
				//-- Impersonation is active.
				m_impersonator.set(user);						// The actual login
				user = (IUser) o;								// And the one we're impersonating is the "current user"

			}
		}

		setCurrentUser(user);
	}
}
