package to.etc.domui.component.binding;

import to.etc.domui.util.*;
import to.etc.webapp.nls.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3/29/16.
 */
final public class BindingFailureException extends CodeException {
	public BindingFailureException(Exception x, String direction, String bindingDetails) {
		super(x, Msgs.eBindingFailed, direction, bindingDetails, x);
	}
}
