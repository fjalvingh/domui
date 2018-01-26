package to.etc.domui.login;

import to.etc.domui.server.ILoginListener;
import to.etc.domui.server.IRequestContext;
import to.etc.domui.server.IServerSession;
import to.etc.domui.server.RequestContextImpl;
import to.etc.domui.state.UIContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 10-11-17.
 */
public class DefaultLoginHandler implements ILoginHandler {
	/** After this amount of failed logins just pretend we're logging in */
	private volatile int m_maxFailLogins = 10;

	private volatile long m_failLoginTimeout = 5*60*1000;

	static final private class IgnoredHash {
		final private long m_ts;

		final private String m_hash;

		public IgnoredHash(long ts, String hash) {
			m_ts = ts;
			m_hash = hash;
		}

		public long getTs() {
			return m_ts;
		}

		public String getHash() {
			return m_hash;
		}
	}

	private List<IgnoredHash> m_ignoredHashList = new ArrayList<IgnoredHash>();


	static private final Map<String, LastLogin> m_lastLoginMap = new ConcurrentHashMap<>();

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
	@Override
	public IUser decodeCookie(final RequestContextImpl rci, final String cookie) {
		if(cookie == null)
			return null;
		String[] car = cookie.split(":");
		if(car.length != 3)
			return null;
		try {
			if(isIgnoredHash(car[2]))
				return null;
			String uid = car[0];
			long ts = Long.parseLong(car[1]); // Timestamp

			//-- Lookup userid;
			ILoginAuthenticator la = rci.getApplication().getLoginAuthenticator();
			if(null == la)
				return null;

			return la.authenticateByCookie(uid, ts, car[2]);// Authenticate by cookie
		} catch(Exception x) {
			return null;									// All cookie format exceptions mean no login
		}
	}


	/**
	 * Register a hash value to ignore because it was logged out.
	 * @param hash
	 */
	@Override
	public synchronized void registerIgnoredHash(String hash) {
		m_ignoredHashList.add(new IgnoredHash(System.currentTimeMillis(), hash));
	}

	/**
	 * If the hash is an ignored hash then return false. In the process clean up "old" hashes.
	 * @param hash
	 * @return
	 */
	public synchronized boolean isIgnoredHash(String hash) {
		long cts = System.currentTimeMillis() - 1000 * 60;
		for(int i = m_ignoredHashList.size(); --i >= 0;) {
			IgnoredHash ih = m_ignoredHashList.get(i);
			if(ih.getHash().equalsIgnoreCase(hash)) {
				return true;
			}
			if(ih.getTs() < cts)
				m_ignoredHashList.remove(i);
		}
		return false;
	}

	/**
	 * Logs in a user. If he was logged in before he is logged out.
	 * @param userid
	 * @param password
	 * @return
	 */
	@Override
	public LoginResult login(final String userid, final String password) throws Exception {
		IRequestContext rcx = UIContext.getRequestContext();
		if(rcx == null)
			throw new IllegalStateException("You can login from a server request only");
		if(!(rcx instanceof RequestContextImpl))
			return LoginResult.FAILED;

		LastLogin ll = m_lastLoginMap.get(userid);

		IServerSession hs = rcx.getServerSession(false);
		if(hs == null)
			return LoginResult.FAILED;
		synchronized(hs) {
			//-- Force logout
			hs.setAttribute(UILogin.LOGIN_KEY, null);

			//-- Am I still in failed login state?
			long cts = System.currentTimeMillis();
			if(ll != null) {
				if(ll.getFirstAttempt() < cts - m_failLoginTimeout) {
					ll = null;
				}
			}
			if(ll != null) {
				if(ll.getFailCount() >= m_maxFailLogins) {
					//Thread.sleep(4000);				// Don't: this can allow a DDOS attack.
					return LoginResult.IGNORED;
				}
			}

			//-- Check credentials,
			ILoginAuthenticator la = rcx.getApplication().getLoginAuthenticator();
			if(la == null)
				throw new IllegalStateException("There is no login authenticator set in the Application!");
			IUser user = la.authenticateUser(userid, password);
			if(user == null) {
				if(ll == null) {
					ll = new LastLogin(userid);
					m_lastLoginMap.put(userid, ll);
				}
				ll.setFailCount(ll.getFailCount() + 1);
				return LoginResult.FAILED;
			}

			//-- Login succeeded: save the user in the session context
			if(null != ll)
				ll.setFailCount(0);
			hs.setAttribute(UILogin.LOGIN_KEY, user); 					// This causes the user to be logged on.
			UIContext.setCurrentUser(user);

			List<ILoginListener> li = rcx.getApplication().getLoginListenerList();
			for(ILoginListener l : li)
				l.userLogin(user);
			return LoginResult.SUCCESS;
		}
	}



}
