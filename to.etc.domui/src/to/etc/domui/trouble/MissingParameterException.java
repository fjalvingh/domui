package to.etc.domui.trouble;

import to.etc.domui.util.*;

public class MissingParameterException extends UIException {
	public MissingParameterException(String name) {
		super(Msgs.X_MISSING_PARAMETER, name);
	}
}
