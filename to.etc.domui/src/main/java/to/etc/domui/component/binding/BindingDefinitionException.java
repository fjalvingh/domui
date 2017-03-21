package to.etc.domui.component.binding;

import to.etc.domui.util.*;
import to.etc.webapp.nls.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3/29/16.
 */
final public class BindingDefinitionException extends CodeException {
	public BindingDefinitionException(String what, String typea, String typeb) {
		super(Msgs.BUNDLE, Msgs.E_BINDING_DEFINITION, what, typea, typeb);
	}
}
