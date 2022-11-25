package to.etc.domui.component.menu;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.webapp.nls.IBundleCode;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 13-04-22.
 */
@NonNullByDefault
final public class BundleMessage {
	private final IBundleCode m_code;

	@Nullable
	private final Object[] m_parameters;

	public BundleMessage(IBundleCode code, @Nullable Object[] parameters) {
		m_code = code;
		m_parameters = parameters;
	}

	public IBundleCode getCode() {
		return m_code;
	}

	@Nullable
	public Object[] getParameters() {
		return m_parameters;
	}

	public String getString() {
		return m_code.format(m_parameters);
	}
}
