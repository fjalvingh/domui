package to.etc.domui.login;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 17-12-18.
 */
public class User2RightsChecker implements IUserRightChecker<IUser> {
	@Override public boolean hasRight(IUser user, String rightName) {
		if(!(user instanceof IUser2))
			throw new IllegalStateException("Expecting an IUser2 instance; this is a " + user.getClass() + ". Set another userRightsChecker.");
		return ((IUser2) user).hasRight(rightName);
	}

	@Override public <T> boolean hasRight(IUser user, String rightName, T dataElement) {
		if(!(user instanceof IUser2))
			throw new IllegalStateException("Expecting an IUser2 instance; this is a " + user.getClass() + ". Set another userRightsChecker.");
		return ((IUser2) user).hasRight(rightName, dataElement);
	}
}
