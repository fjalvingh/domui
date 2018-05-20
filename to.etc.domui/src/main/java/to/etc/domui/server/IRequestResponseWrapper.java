package to.etc.domui.server;

import org.eclipse.jdt.annotation.NonNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface IRequestResponseWrapper {
	@NonNull HttpServletRequest getWrappedRequest(@NonNull HttpServletRequest request);

	@NonNull HttpServletResponse getWrappedResponse(@NonNull HttpServletResponse response);

}
