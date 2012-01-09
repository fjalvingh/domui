package to.etc.server.misc;

/**
 * Because some braindead twit at Sun has botched the servlet spec
 * wrt declarative security we need an accessor to check if a
 * principal has a role.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 5, 2007
 */
public interface StupidServletStandardRoleRetriever {
	public boolean hasRole(String name);
}
