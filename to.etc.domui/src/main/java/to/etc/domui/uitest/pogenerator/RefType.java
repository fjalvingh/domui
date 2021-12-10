package to.etc.domui.uitest.pogenerator;

import org.eclipse.jdt.annotation.NonNullByDefault;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 09-12-21.
 */
@NonNullByDefault
final public class RefType {
	private final String m_packageName;

	private final String m_typeName;

	private final List<String> m_genericParameterList;

	public RefType(String packageName, String typeName, String... genericParameters) {
		m_packageName = packageName;
		m_typeName = typeName;
		m_genericParameterList = Arrays.asList(genericParameters);
	}

	public RefType(String packageName, String typeName, List<String> genericParameters) {
		m_packageName = packageName;
		m_typeName = typeName;
		m_genericParameterList = genericParameters;
	}

	public RefType(Class<?> clz) {
		m_packageName = clz.getPackageName();
		m_typeName = clz.getSimpleName();
		m_genericParameterList = Collections.emptyList();
	}

	public String getPackageName() {
		return m_packageName;
	}

	public String getTypeName() {
		return m_typeName;
	}

	public List<String> getGenericParameterList() {
		return m_genericParameterList;
	}

	public String asTypeString() {
		StringBuilder sb = new StringBuilder();
		if(m_packageName.length() > 0) {
			sb.append(m_packageName).append(".");
		}
		sb.append(m_typeName);
		if(m_genericParameterList.size() > 0) {
			sb.append("<");
			for(int i = 0; i < m_genericParameterList.size(); i++) {
				String s = m_genericParameterList.get(i);
				if(i > 0)
					sb.append(", ");
				sb.append(s);
			}

			sb.append(">");
		}

		return sb.toString();
	}

	public String asSmallTypeString() {
		StringBuilder sb = new StringBuilder();
		sb.append(m_typeName);
		if(m_genericParameterList.size() > 0) {
			sb.append("<");
			for(int i = 0; i < m_genericParameterList.size(); i++) {
				String s = m_genericParameterList.get(i);
				if(i > 0)
					sb.append(", ");
				sb.append(s);
			}

			sb.append(">");
		}

		return sb.toString();
	}


	@Override
	public String toString() {
		return asTypeString();
	}
}
