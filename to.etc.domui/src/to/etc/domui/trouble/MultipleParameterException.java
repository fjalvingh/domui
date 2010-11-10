package to.etc.domui.trouble;

import to.etc.domui.util.*;

public class MultipleParameterException extends UIException {
	public MultipleParameterException(String name) {
		super(Msgs.BUNDLE, Msgs.X_MULTIPLE_PARAMETER, name);
	}
}
