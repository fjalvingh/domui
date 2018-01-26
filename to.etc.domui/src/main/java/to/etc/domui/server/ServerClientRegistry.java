package to.etc.domui.server;

import java.util.*;

import javax.annotation.*;
import javax.servlet.http.*;

import to.etc.domui.util.*;

public class ServerClientRegistry {
	@Nonnull
	static final private ServerClientRegistry m_instance = new ServerClientRegistry();


	/**
	 * A page that was used by a specified client.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Nov 30, 2012
	 */
	final public static class Use {
		private long m_timeStamp;

		@Nullable
		private String m_url;

		void update(@Nonnull final String url, final long ts) {
			m_url = url;
			m_timeStamp = ts;
		}

		public long getTimeStamp() {
			return m_timeStamp;
		}

		@Nonnull
		public String getUrl() {
			if(null != m_url)
				return m_url;
			throw new IllegalStateException("update() not called");
		}
	}

	/**
	 * Contains information on a logged-on client.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Jul 18, 2008
	 */
	final public class Client implements HttpSessionBindingListener {
		private String m_remoteAddress;

		private String m_remoteHost;

		private String m_remoteUser;

		private List<Use> m_lastUseList = new ArrayList<Use>();

		private long m_tsSessionStart;

		private long m_tsLastRequest;

		private int m_nRequests;

		Client() {}

		Client(final String addr, final String hn, final String user, final long ts) {
			m_remoteAddress = addr;
			m_remoteHost = hn;
			m_remoteUser = user;
			m_nRequests = 1;
			m_tsLastRequest = m_tsSessionStart = ts;
		}

		public String getRemoteAddress() {
			return m_remoteAddress;
		}

		public String getRemoteHost() {
			return m_remoteHost;
		}

		public String getRemoteUser() {
			return m_remoteUser;
		}

		public List<Use> getLastUseList() {
			return m_lastUseList;
		}

		public long getTsSessionStart() {
			return m_tsSessionStart;
		}

		public long getTsLastRequest() {
			return m_tsLastRequest;
		}

		public int getNRequests() {
			return m_nRequests;
		}

		void update(final long ts) {
			m_tsLastRequest = ts;
			m_nRequests++;
		}

		@Override
		public void valueBound(final HttpSessionBindingEvent arg0) {}

		@Override
		public void valueUnbound(final HttpSessionBindingEvent arg0) {
			unregisterUser(this);
		}
	}

	@Nonnull
	final private Map<String, List<Client>> m_userMap = new HashMap<String, List<Client>>();

	public ServerClientRegistry() {
	}

	@Nonnull
	static public ServerClientRegistry getInstance() {
		return m_instance;
	}

	/**
	 * Called from the filter to register all requests.
	 * @param req
	 */
	void registerRequest(@Nonnull final HttpServletRequest req, @Nonnull String remoteUser) {
		try {
			if(req == null)
				throw new IllegalStateException("??");
			HttpSession ses = req.getSession(false);			// Has session?
			if(ses == null)
				return;
			String rt = req.getParameter("webuia");
			if(Constants.ACMD_ASYPOLL.equals(rt))
				return;

			//-- Accept this: active user
			long ts = System.currentTimeMillis();
			synchronized(m_userMap) {
				List<Client> list = m_userMap.get(remoteUser);
				if(list == null) {
					list = new ArrayList<Client>(4);
					m_userMap.put(remoteUser, list);
				}
				Client c = findClient(list, req.getRemoteAddr());
				if(c == null) {
					//-- New client!!! Register && attach to session-pisse handler.
					c = new Client(req.getRemoteAddr(), req.getRemoteHost(), remoteUser, ts);
					list.add(c);
					ses.setAttribute(getClass().getName(), c);
				}

				//-- Register this request's data
				List<Use> ul = c.getLastUseList();
				Use u;
				if(ul.size() < 5) {
					u = new Use();
				} else {
					u = ul.remove(0);
				}
				ul.add(u);
				u.update(req.getRequestURI(), ts);
				c.update(ts);
			}
		} catch(Exception x) {
			x.printStackTrace();
		}
	}

	@Nullable
	static private Client findClient(@Nonnull final List<Client> list, @Nonnull final String a) {
		for(Client c : list) {
			if(c.getRemoteAddress().equals(a))
				return c;
		}
		return null;
	}

	/**
	 * Discards a client's data when it's session is destroyed.
	 * @param c
	 */
	void unregisterUser(@Nonnull final Client c) {
		synchronized(m_userMap) {
			List<Client> list = m_userMap.get(c.getRemoteUser());
			if(list == null)
				return;
			list.remove(c);
		}
	}

	@Nonnull
	private Client dupClient(@Nonnull final Client in) {
		Client c = new Client();
		c.m_nRequests = in.m_nRequests;
		c.m_remoteAddress = in.m_remoteAddress;
		c.m_remoteHost = in.m_remoteHost;
		c.m_remoteUser = in.m_remoteUser;
		c.m_tsLastRequest = in.m_tsLastRequest;
		c.m_tsSessionStart = in.m_tsSessionStart;

		List<Use> nul = new ArrayList<Use>(in.getLastUseList().size());
		for(Use u : in.getLastUseList()) {
			Use nu = new Use();
			nu.m_timeStamp = u.m_timeStamp;
			nu.m_url = u.m_url;
			nul.add(nu);
		}
		c.m_lastUseList = nul;
		return c;
	}

	/**
	 * Returns (a duplicate of) the list of clients that are currently connected (for whom a
	 * session exists).
	 * @return
	 */
	@Nonnull
	public List<Client> getActiveClients() {
		List<Client> res = new ArrayList<Client>();
		synchronized(m_userMap) {
			for(List<Client> list : m_userMap.values()) {
				for(Client c : list) {
					res.add(dupClient(c)); // Make a copy so we don't have to synchronize
				}
			}
		}
		return res;
	}

	/**
	 * Returns a list of login names for users that currently own a session. To get full client
	 * information call {@link ServerRuntime#getActiveClients()}.
	 * @return
	 */
	@Nonnull
	public Set<String> getActiveLogins() {
		Set<String> idmap = new TreeSet<String>();
		synchronized(m_userMap) {
			for(List<Client> list : m_userMap.values()) {
				for(Client c : list) {
					idmap.add(c.getRemoteUser());
				}
			}
		}
		return idmap;
	}
}
