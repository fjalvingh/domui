package to.etc.domui.uitest.pogenerator;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 08-12-21.
 */
@NonNullByDefault
final public class PoMethodParameter {
	private final RefType m_type;

	private final String m_parameterName;

	public PoMethodParameter(RefType type, String parameterName) {
		m_type = type;
		m_parameterName = parameterName;
	}

	public String getParameterName() {
		return m_parameterName;
	}

	public RefType getType() {
		return m_type;
	}
}
