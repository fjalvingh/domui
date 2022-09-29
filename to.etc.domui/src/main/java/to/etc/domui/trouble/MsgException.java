package to.etc.domui.trouble;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.util.Msgs;
import to.etc.webapp.nls.CodeException;
import to.etc.webapp.nls.IBundleCode;

/**
 * This exception, when thrown from the DomUI framework, will cause a MsgBox to be displayed with the error
 * message in it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 12, 2013
 */
final public class MsgException extends CodeException {
	public MsgException(@NonNull IBundleCode code, @NonNull Object... parameters) {
		super(code, parameters);
	}

	public MsgException(@NonNull Throwable t, @NonNull IBundleCode code, @NonNull Object... parameters) {
		super(t, code, parameters);
	}

	public MsgException(@NonNull String message, @NonNull Object... parameters) {
		this(Msgs.verbatim, message, parameters);
	}
}
