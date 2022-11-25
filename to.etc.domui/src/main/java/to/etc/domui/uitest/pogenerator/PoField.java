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
	private final RefType m_type;

	private final String m_fieldName;

	public PoField(PoClass poClass, RefType type, String fieldName) {
		m_poClass = poClass;
		m_type = type;
		m_fieldName = fieldName;
	}

	public PoClass getPoClass() {
		return m_poClass;
	}

	public String getFieldName() {
		return m_fieldName;
	}

	public RefType getType() {
		return m_type;
	}

	public void visit(IPoModelVisitor v) throws Exception {
		v.visitField(this);
	}
}
