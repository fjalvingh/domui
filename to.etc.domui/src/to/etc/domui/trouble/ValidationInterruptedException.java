package to.etc.domui.trouble;

import to.etc.domui.util.*;

/**
 * Thrown when validation is delayed because some component needs a question answered 1st.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 14, 2013
 */
public class ValidationInterruptedException extends ValidationException {
	public ValidationInterruptedException() {
		super(Msgs.BUNDLE, Msgs.UI_VALIDATION_INTERRUPTED);
	}

}
