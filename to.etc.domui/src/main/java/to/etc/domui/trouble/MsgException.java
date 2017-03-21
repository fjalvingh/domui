package to.etc.domui.trouble;

import javax.annotation.*;

import to.etc.domui.util.*;
import to.etc.webapp.nls.*;

/**
 * This exception, when thrown from the DomUI framework, will cause a MsgBox to be displayed with the error
 * message in it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 12, 2013
 */
final public class MsgException extends CodeException {
	public MsgException(@Nonnull BundleRef bundle, @Nonnull String code, @Nonnull Object... parameters) {
		super(bundle, code, parameters);
	}

	public MsgException(@Nonnull Throwable t, @Nonnull BundleRef bundle, @Nonnull String code, @Nonnull Object... parameters) {
		super(t, bundle, code, parameters);
	}

	public MsgException(@Nonnull String message, @Nonnull Object... parameters) {
		this(Msgs.BUNDLE, Msgs.VERBATIM, message, parameters);
	}
}
