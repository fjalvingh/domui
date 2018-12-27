package to.etc.domui.server;

import to.etc.webapp.nls.CodeException;
import to.etc.webapp.nls.IBundleCode;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 16-12-18.
 */
public class AccessDeniedException extends CodeException {
	public AccessDeniedException(IBundleCode code, Object... parameters) {
		super(code, parameters);
	}

	public AccessDeniedException(Throwable t, IBundleCode code, Object... parameters) {
		super(t, code, parameters);
	}
}
