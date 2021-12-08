package to.etc.domui.uitest.pogenerator;

import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 08-12-21.
 */
@NonNullByDefault
final public class PoMethod {
	private final PoClass m_poClass;

	private final String m_methodName;

	private final Set<Modifier> m_modifierSet = new HashSet<>();

	private final List<Pair<String, String>> m_parameterList = new ArrayList<>();

	private final List<Pair<String, String>> m_importList = new ArrayList<>();

	public PoMethod(PoClass poClass, String methodName, Modifier... modifiers) {
		m_poClass = poClass;
		m_methodName = methodName;
		if(modifiers.length == 0) {
			m_modifierSet.add(Modifier.Public);
		} else {
			m_modifierSet.addAll(Arrays.asList(modifiers));
		}
	}

	public void visit(IPoModelVisitor v) throws Exception {
		v.visitMethod(this);
	}

	public PoMethod addImport(String packageName, String typeName) {
		m_importList.add(new Pair<>(packageName, typeName));
		return this;
	}

	public List<Pair<String, String>> getImportList() {
		return m_importList;
	}
}
