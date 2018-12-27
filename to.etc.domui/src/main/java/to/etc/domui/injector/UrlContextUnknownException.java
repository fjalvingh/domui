package to.etc.domui.injector;

import to.etc.webapp.nls.CodeException;
import to.etc.webapp.nls.IBundleCode;

/**
 * Thrown when no value could be found for a property marked as {@link to.etc.domui.annotations.UIUrlContext}.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 16-12-18.
 */
public class UrlContextUnknownException extends CodeException {
	public UrlContextUnknownException(IBundleCode code, Object... parameters) {
		super(code, parameters);
	}
}
