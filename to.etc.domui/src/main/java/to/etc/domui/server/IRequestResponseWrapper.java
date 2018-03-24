package to.etc.domui.server;

import javax.annotation.*;
import javax.servlet.http.*;

public interface IRequestResponseWrapper {
	@Nonnull HttpServletRequest getWrappedRequest(@Nonnull HttpServletRequest request);

	@Nonnull HttpServletResponse getWrappedResponse(@Nonnull HttpServletResponse response);

}
