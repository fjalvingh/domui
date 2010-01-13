package to.etc.domui.trouble;

import to.etc.webapp.nls.*;

/**
 * Base of the exception class for user interface trouble.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 12, 2008
 */
public class UIException extends CodeException {
	public UIException(final BundleRef bundle, final String code, final Object... parameters) {
		super(bundle, code, parameters);
	}

	public UIException(final Throwable t, final BundleRef bundle, final String code, final Object... parameters) {
		super(t, bundle, code, parameters);
	}
}
