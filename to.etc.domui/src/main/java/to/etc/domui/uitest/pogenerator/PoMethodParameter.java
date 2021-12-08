package to.etc.domui.uitest.pogenerator;

import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.util.Pair;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 08-12-21.
 */
@NonNullByDefault
final public class PoMethodParameter {
	private final String m_packageName;

	private final String m_typeName;

	private final String m_parameterName;

	public PoMethodParameter(String packageName, String typeName, String parameterName) {
		m_packageName = packageName;
		m_typeName = typeName;
		m_parameterName = parameterName;
	}

	public String getPackageName() {
		return m_packageName;
	}

	public String getTypeName() {
		return m_typeName;
	}

	public String getParameterName() {
		return m_parameterName;
	}

	public Pair<String, String> getType() {
		return new Pair<>(getPackageName(), getTypeName());
	}
}
