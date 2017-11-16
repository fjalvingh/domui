package to.etc.domui.login;

import to.etc.domui.server.HttpServerRequestResponse;
import to.etc.domui.server.ILoginListener;
import to.etc.domui.server.IRequestContext;
import to.etc.domui.server.IServerSession;
import to.etc.domui.server.RequestContextImpl;
import to.etc.domui.state.UIContext;

import javax.annotation.Nullable;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 10-11-17.
 */
public class UILogin {
	static private volatile ILoginHandler	m_loginHandler = new DefaultLoginHandler();

	static private ThreadLocal<IUser> m_currentUser = new ThreadLocal<IUser>();

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
	}

	public static ILoginHandler getLoginHandler() {
		return m_loginHandler;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Login page logic. Temporary location.				*/
	/*--------------------------------------------------------------*/
	static public final String LOGIN_KEY = IUser.class.getName();

	/**
	 * UNSTABLE INTERFACE. This tries to retrieve an IUser context for the user. It tries to
	 * retrieve a copy from the HttpSession. The AppSession is not used; this allows a login
	 * to persist when running in DEBUG mode, where AppSessions are destroyed when a class
	 * is changed.
	 */
	static public IUser internalGetLoggedInUser(final IRequestContext rx) throws Exception {
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
	 * Logs in a user. If he was logged in before he is logged out.
	 * @param userid
	 * @param password
	 * @return
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

}
