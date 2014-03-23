package to.etc.domui.server;

import javax.annotation.*;
import javax.servlet.http.*;

public interface IRequestResponseWrapper {
	@Nonnull
	public HttpServletRequest getWrappedRequest(@Nonnull HttpServletRequest request);

	@Nonnull
	public HttpServletResponse getWrappedResponse(@Nonnull HttpServletResponse response);

}
