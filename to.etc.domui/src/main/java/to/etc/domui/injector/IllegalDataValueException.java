package to.etc.domui.injector;

import to.etc.domui.util.Msgs;
import to.etc.util.PropertyInfo;
import to.etc.webapp.nls.CodeException;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-12-18.
 */
public class IllegalDataValueException extends CodeException {
	public IllegalDataValueException(PropertyInfo info) {
		super(Msgs.dataValueAccessDenied, info.getName());
	}
}
