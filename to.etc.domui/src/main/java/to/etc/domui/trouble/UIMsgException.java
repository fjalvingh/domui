package to.etc.domui.trouble;

import javax.annotation.*;

import to.etc.domui.dom.errors.*;
import to.etc.webapp.nls.*;

/**
 * Special localized exception, that would be shown as MsgBox, by default.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Jun 11, 2013
 */
public class UIMsgException extends UIException {
	private final @Nonnull
	MsgType m_type;

	public @Nonnull
	MsgType getType() {
		return m_type;
	}

	public UIMsgException(@Nonnull MsgType type, @Nonnull BundleRef bundle, @Nonnull String code, Object... parameters) {
		super(bundle, code, parameters);
		m_type = type;
	}

}
