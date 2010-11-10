package to.etc.domui.trouble;

import to.etc.domui.util.*;

public class UnusableParameterException extends UIException {
	public UnusableParameterException(String name, String type, String value) {
		super(Msgs.BUNDLE, Msgs.X_INVALID_PARAMTYPE, name, type, value);
	}
}
