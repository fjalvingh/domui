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
	String getLoginID();

	/**
	 * Return a display name for the user; this usually is the full formal name.
	 * @return
	 */
	String getDisplayName();

	//	/**
	//	 * The set of names representing the rights the user has.
	//	 * @return
	//	 */
	//	Set<String>		getRightNames();

	boolean hasRight(String r);

	/**
	 * EXPERIMENTAL INTERFACE, DO NOT USE Determines if right r is enabled for the specified data element. The implementation
	 * will decide how to map this. The dataElement can be a "primary element" meaning something that rights are explicitly
	 * assigned on, or it can be something that can be linked to such a "priomary element". In the latter case it is the
	 * implementation's responsibility to obtain the primary element from the data passed and apply the rights check there.
	 * If data-bound permissions are not implemented this MUST return getRight(r).
	 *
	 * @param r
	 * @param dataElement
	 * @return
	 */
	boolean hasRight(String r, Object dataElement);
}
