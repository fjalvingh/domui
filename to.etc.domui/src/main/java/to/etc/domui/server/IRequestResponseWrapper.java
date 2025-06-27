package to.etc.domui.server;

import org.eclipse.jdt.annotation.NonNull;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface IRequestResponseWrapper {
	@NonNull HttpServletRequest getWrappedRequest(@NonNull HttpServletRequest request);

	@NonNull HttpServletResponse getWrappedResponse(@NonNull HttpServletResponse response);

}
