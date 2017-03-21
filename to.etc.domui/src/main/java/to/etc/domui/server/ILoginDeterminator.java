package to.etc.domui.server;

import javax.annotation.*;
import javax.servlet.http.*;

public interface ILoginDeterminator {
	/**
	 * If the request is a request to log AND there is a logged-in user return that user's login ID.
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@Nullable
	String getLoginData(@Nonnull HttpServletRequest request) throws Exception;
}