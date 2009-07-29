package to.etc.domui.trouble;

import to.etc.domui.util.*;
import to.etc.webapp.nls.*;

public class ValidationException extends UIException {
	public ValidationException(BundleRef bundle, String code, Object... parameters) {
		super(bundle, code, parameters);
	}

	public ValidationException(String code, Object... param) {
		super(Msgs.BUNDLE, code, param);
	}
}
