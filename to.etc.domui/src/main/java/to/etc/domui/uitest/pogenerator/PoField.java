package to.etc.domui.uitest.pogenerator;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 08-12-21.
 */
@NonNullByDefault
final public class PoField {
	private final PoClass m_poClass;

	/** The package name, or the empty string if not in a package */
	private final String m_packageName;

	private final String m_typeName;

	private final String m_fieldName;

	public PoField(PoClass poClass, String packageName, String typeName, String fieldName) {
		m_poClass = poClass;
		m_packageName = packageName;
		m_typeName = typeName;
		m_fieldName = fieldName;
	}

	public PoClass getPoClass() {
		return m_poClass;
	}

	public String getPackageName() {
		return m_packageName;
	}

	public String getTypeName() {
		return m_typeName;
	}

	public String getFieldName() {
		return m_fieldName;
	}

	public void visit(IPoModelVisitor v) throws Exception {
		v.visitField(this);
	}
}
