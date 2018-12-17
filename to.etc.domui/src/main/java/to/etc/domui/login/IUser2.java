package to.etc.domui.login;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 17-12-18.
 */
@NonNullByDefault
public interface IUser2 extends IUser {
	boolean hasRight(@NonNull String r);

	/**
	 * EXPERIMENTAL INTERFACE, DO NOT USE Determines if right r is enabled for the specified data element. The implementation
	 * will decide how to map this. The dataElement can be a "primary element" meaning something that rights are explicitly
	 * assigned on, or it can be something that can be linked to such a "priomary element". In the latter case it is the
	 * implementation's responsibility to obtain the primary element from the data passed and apply the rights check there.
	 * If data-bound permissions are not implemented this MUST return getRight(r).
	 */
	<T> boolean hasRight(@NonNull String r, @Nullable T dataElement);
}
