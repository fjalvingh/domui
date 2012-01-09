package to.etc.server.servicer;

/**
 * Delegates the checking of roles for permissions.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 27, 2009
 */
public interface IServiceAuthenticator {
	/**
	 * Check to see if the currently authenticated user has the specified role.
	 *
	 * @param roleName
	 * @return	T if the user HAS the role specified (or is the ADMIN user).
	 * @throws Exception
	 */
	boolean userHasRole(String roleName) throws Exception;
}
