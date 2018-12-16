package to.etc.domui.login;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.server.RequestContextImpl;
import to.etc.function.ConsumerEx;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 16-12-18.
 */
public interface IAccessDeniedHandler {
	void handleAccessDenied(@NonNull RequestContextImpl ctx, @NonNull AccessCheckResult result, @NonNull ConsumerEx<String> logSink) throws Exception;
}
