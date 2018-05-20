package to.etc.domui.server;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import javax.servlet.http.HttpServletRequest;

public interface ILoginDeterminator {
	/**
	 * If the request is a request to log AND there is a logged-in user return that user's login ID.
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@Nullable
	String getLoginData(@NonNull HttpServletRequest request) throws Exception;
}
