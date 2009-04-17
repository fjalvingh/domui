package to.etc.domui.login;

/**
 * Represents a logged-in user. This base interface only knows data that must be known about
 * any logged-in user. Extras can be obtained if you know the implementation.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 14, 2009
 */
public interface IUser {
	/**
	 * The user's login ID.
	 * @return
	 */
	String			getLoginID();

	/**
	 * Return a display name for the user; this usually is the full formal name.
	 * @return
	 */
	String			getDisplayName();

//	/**
//	 * The set of names representing the rights the user has.
//	 * @return
//	 */
//	Set<String>		getRightNames();

	boolean			hasRight(String r);
}
