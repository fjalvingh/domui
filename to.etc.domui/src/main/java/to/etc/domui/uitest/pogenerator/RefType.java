package to.etc.domui.uitest.pogenerator;

import org.eclipse.jdt.annotation.NonNullByDefault;

import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 09-12-21.
 */
@NonNullByDefault
final public class RefType {
	static public final RefType INT = new RefType("", "int");
	static public final RefType STRING = new RefType("", "String");

	private final String m_packageName;

	private final String m_typeName;

	private final List<RefType> m_genericParameterList;

	public RefType(String packageName, String typeName, RefType... genericParameters) {
		m_packageName = packageName;
		m_typeName = typeName;
		m_genericParameterList = Arrays.asList(genericParameters);
	}

	public RefType(String packageName, String typeName, List<RefType> genericParameters) {
		m_packageName = packageName;
		m_typeName = typeName;
		m_genericParameterList = genericParameters;
	}

	public RefType(Class<?> clz, RefType... genericParameters) {
		m_packageName = clz.getPackageName();
		m_typeName = clz.getSimpleName();
		m_genericParameterList = Arrays.asList(genericParameters);
	}

	public String getPackageName() {
		return m_packageName;
	}

	public String getTypeName() {
		return m_typeName;
	}

	public List<RefType> getGenericParameterList() {
		return m_genericParameterList;
	}

	private String asTypeString() {
		StringBuilder sb = new StringBuilder();
		if(!m_packageName.isEmpty()) {
			sb.append(m_packageName).append(".");
		}
		sb.append(m_typeName);
		if(!m_genericParameterList.isEmpty()) {
			sb.append("<");
			for(int i = 0; i < m_genericParameterList.size(); i++) {
				RefType s = m_genericParameterList.get(i);
				if(i > 0)
					sb.append(", ");
				sb.append(s.asTypeString());
			}

			sb.append(">");
		}

		return sb.toString();
	}

	public String asTypeString(PoClass clz) {
		clz.addImport(this);

		StringBuilder sb = new StringBuilder();
		if(! clz.hasImport(this) && !m_packageName.isEmpty()) {
			sb.append(m_packageName).append(".");
		}
		sb.append(m_typeName);
		if(!m_genericParameterList.isEmpty()) {
			sb.append("<");
			for(int i = 0; i < m_genericParameterList.size(); i++) {
				RefType s = m_genericParameterList.get(i);
				if(i > 0)
					sb.append(", ");
				sb.append(s.asTypeString(clz));
			}

			sb.append(">");
		}

		return sb.toString();
	}


	//public String asSmallTypeString() {
	//	StringBuilder sb = new StringBuilder();
	//	sb.append(m_typeName);
	//	if(m_genericParameterList.size() > 0) {
	//		sb.append("<");
	//		for(int i = 0; i < m_genericParameterList.size(); i++) {
	//			RefType s = m_genericParameterList.get(i);
	//			if(i > 0)
	//				sb.append(", ");
	//			sb.append(s.asTypeString());
	//		}
	//
	//		sb.append(">");
	//	}
	//
	//	return sb.toString();
	//}


	@Override
	public String toString() {
		return asTypeString();
	}
}
