package to.etc.domui.login;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 17-12-18.
 */
@NonNullByDefault
public interface IUserRightChecker<U extends IUser> {
	boolean hasRight(U user, String rightName);

	<T> boolean hasRight(U user, String r, @Nullable T dataElement);

}
