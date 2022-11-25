package to.etc.domui.trouble;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.dom.errors.MsgType;
import to.etc.webapp.nls.IBundleCode;

/**
 * Special localized exception, that would be shown as MsgBox, by default.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Jun 11, 2013
 */
public class UIMsgException extends UIException {
	@NonNull
	private final MsgType m_type;

	public UIMsgException(@NonNull MsgType type, @NonNull IBundleCode code, Object... parameters) {
		super(code, parameters);
		m_type = type;
	}

	@NonNull
	public MsgType getType() {
		return m_type;
	}
}
