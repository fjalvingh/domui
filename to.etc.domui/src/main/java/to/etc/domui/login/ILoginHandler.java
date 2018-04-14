package to.etc.domui.login;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.server.RequestContextImpl;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 10-11-17.
 */
@NonNullByDefault
public interface ILoginHandler {
	@Nullable
	IUser decodeCookie(RequestContextImpl rci, String cookie);

	void registerIgnoredHash(String hash);

	LoginResult login(final String userid, final String password) throws Exception;

	@Nullable
	IUser login(String userid) throws Exception;
}
