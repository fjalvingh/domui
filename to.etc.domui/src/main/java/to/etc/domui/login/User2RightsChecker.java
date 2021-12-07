package to.etc.domui.login;

import org.eclipse.jdt.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.domui.annotations.UIRights;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.state.UIContext;
import to.etc.domui.util.IRightsCheckedManually;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 17-12-18.
 */
public class User2RightsChecker implements IUserRightChecker<IUser> {
	private static final Logger LOG = LoggerFactory.getLogger(User2RightsChecker.class);

	@Override
	public boolean hasRight(IUser user, String rightName) {
		if(!(user instanceof IUser2))
			throw new IllegalStateException("Expecting an IUser2 instance; this is a " + user.getClass() + ". Set another userRightsChecker.");
		return ((IUser2) user).hasRight(rightName);
	}

	@Override
	public <T> boolean hasRight(IUser user, String rightName, T dataElement) {
		if(!(user instanceof IUser2))
			throw new IllegalStateException("Expecting an IUser2 instance; this is a " + user.getClass() + ". Set another userRightsChecker.");
		return ((IUser2) user).hasRight(rightName, dataElement);
	}

	/**
	 * Checks whether the currently logged in (or not logged in) user has rights on
	 * the specified page, based on the class only. This does not implement either
	 * page-specific rights checking nor datapath based checking.
	 */
	public static boolean hasRightsOn(Class<? extends UrlPage> pageClass) {
		UIRights rann = pageClass.getAnnotation(UIRights.class);// Get class annotation
		if(rann == null) {                                        // Any kind of rights checking is required?
			return true;                                        // No -> allow access.
		}
		if(IRightsCheckedManually.class.isAssignableFrom(pageClass))
			throw new IllegalArgumentException("This is unsupported for " + pageClass + " as this implements IRightsCheckedManually");

		//-- Get user's IUser; if not present we need to log in.
		IUser2 user = (IUser2) UIContext.getCurrentUser();                // Currently logged in?
		if(user == null) {
			return false;                                        // No -> no access
		}

		//-- Start access checks, in order. First call the interface, if applicable
		String failureReason = null;
		try {
			return checkRightsAnnotation(pageClass, rann, user);
		} catch(Exception x) {
			LOG.error("Right check failed: " + x, x);
			return false;
		}
	}

	static private boolean checkRightsAnnotation(@NonNull Class<? extends UrlPage> pageClass, @NonNull UIRights rann, @NonNull IUser2 user) throws Exception {
		//-- No special data context - we just check plain general rights
		if(rann.value().length == 0)                        // No rights specified means -> just log in
			return true;

		for(String right : rann.value()) {
			if(user.hasRight(right)) {
				return true;
			}
		}
		return false;                                        // All worked, so we have access.
	}
}
