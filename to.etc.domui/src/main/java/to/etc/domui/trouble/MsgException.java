package to.etc.domui.trouble;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.util.Msgs;
import to.etc.webapp.nls.BundleRef;
import to.etc.webapp.nls.CodeException;

/**
 * This exception, when thrown from the DomUI framework, will cause a MsgBox to be displayed with the error
 * message in it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 12, 2013
 */
final public class MsgException extends CodeException {
	public MsgException(@NonNull BundleRef bundle, @NonNull String code, @NonNull Object... parameters) {
		super(bundle, code, parameters);
	}

	public MsgException(@NonNull Throwable t, @NonNull BundleRef bundle, @NonNull String code, @NonNull Object... parameters) {
		super(t, bundle, code, parameters);
	}

	public MsgException(@NonNull String message, @NonNull Object... parameters) {
		this(Msgs.BUNDLE, Msgs.VERBATIM, message, parameters);
	}
}
