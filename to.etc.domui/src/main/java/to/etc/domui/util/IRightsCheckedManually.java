package to.etc.domui.util;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.annotations.UIRights;
import to.etc.domui.login.IUser;
import to.etc.webapp.nls.CodeException;

/**
 * When implemented on an UrlPage class, this indicates that that page handles (part of) it's
 * access rights checking by itself, to override or extend the existing {@link UIRights} based
 * rights management.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 24, 2013
 */
public interface IRightsCheckedManually {
	/**
	 * This method gets called when a page has been created, but not yet rendered. If this method
	 * returns TRUE it allows access to the resource, and no further checks are done. If this returns
	 * FALSE it means <i>this</i> check does not allow access- but other checks (the UIRights and datapath
	 * related ones) can allow access.
	 * If this method throws an Exception it explicitly forbids access to the page; the exception message
	 * will be shown to the user as the reason. This exception can be a {@link CodeException} or anything
	 * else.
	 */
	boolean isAccessAllowedBy(@NonNull IUser user) throws Exception;
}
