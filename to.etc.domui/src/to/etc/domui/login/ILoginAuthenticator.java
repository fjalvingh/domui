package to.etc.domui.login;

/**
 * Checks the user's access and if granted returns a IUser for the credentials
 * passed. If not correct this returns null. The IUser returned will be cached
 * in the session.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 14, 2009
 */
public interface ILoginAuthenticator {
	IUser			authenticateUser(final String userid, final String credentials);
}
