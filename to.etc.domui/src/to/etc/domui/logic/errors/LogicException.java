package to.etc.domui.logic.errors;

import to.etc.domui.trouble.*;
import to.etc.domui.util.*;

final public class LogicException extends ValidationException {
	public LogicException() {
		super(Msgs.BUNDLE, Msgs.V_LOGIC_ERROR);
	}
}
