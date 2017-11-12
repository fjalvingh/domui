package to.etc.domui.login;

import to.etc.domui.server.RequestContextImpl;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nullable;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 10-11-17.
 */
@DefaultNonNull
public interface ILoginHandler {
	@Nullable
	IUser decodeCookie(RequestContextImpl rci, String cookie);
	void registerIgnoredHash(String hash);
	LoginResult login(final String userid, final String password) throws Exception;
}
