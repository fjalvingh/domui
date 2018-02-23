package my.domui.app.core.authentication;

import my.domui.app.core.db.DbPermission;
import to.etc.webapp.nls.BundleRef;

/**
 * Constants for right names as stored in the {@link DbPermission} table.
 */
final public class Rights {
	static public final BundleRef BUNDLE = BundleRef.create(Rights.class, "rights");

	static public final String ADMIN = "admin";
}
