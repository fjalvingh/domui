package to.etc.server.misc;

import javax.servlet.http.*;

public class StupidServletStandardRoleRetrieverImpl implements StupidServletStandardRoleRetriever {
	private HttpServletRequest	m_rq;

	public StupidServletStandardRoleRetrieverImpl(HttpServletRequest rq) {
		m_rq = rq;
	}

	public boolean hasRole(String name) {
		return m_rq.isUserInRole(name);
	}
}
