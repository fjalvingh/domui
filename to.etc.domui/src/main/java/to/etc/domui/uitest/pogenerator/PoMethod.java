package to.etc.domui.uitest.pogenerator;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.function.ConsumerEx;
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
final public class PoMethod extends BodyWriter<PoMethod> {
	private final PoClass m_poClass;

	private final String m_methodName;

	private final Set<Modifier> m_modifierSet = new HashSet<>();

	private final List<PoMethodParameter> m_parameterList = new ArrayList<>();

	private final List<Pair<String, String>> m_importList = new ArrayList<>();

	@Nullable
	private RefType m_returnType;

	public PoMethod(PoClass poClass, @Nullable RefType returnType, String methodName, Modifier... modifiers) {
		m_poClass = poClass;
		m_methodName = methodName;
		m_returnType = returnType;
		if(modifiers.length == 0) {
			m_modifierSet.add(Modifier.Public);
		} else {
			m_modifierSet.addAll(Arrays.asList(modifiers));
		}
	}

	public PoMethod addParameter(RefType type, String name) {
		m_parameterList.add(new PoMethodParameter(type, name));
		return this;
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

	public String getMethodName() {
		return m_methodName;
	}

	public Set<Modifier> getModifierSet() {
		return m_modifierSet;
	}

	public List<PoMethodParameter> getParameterList() {
		return m_parameterList;
	}

	@Nullable
	public RefType getReturnType() {
		return m_returnType;
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	Body writer methods											*/
	/*----------------------------------------------------------------------*/

	public void appendLazyInit(PoField field, ConsumerEx<String> writer) throws Exception {
		String var = field.getFieldName().startsWith("m_") ? field.getFieldName().substring(2) : "value";

		appendType(m_poClass, field.getType()).append(" ").append(var).append(" = ").append(field.getFieldName()).append(";").nl();
		append("if(null == ").append(var).append(") {").nl();
		inc();
		writer.accept(var);
		append(field.getFieldName()).append(" = ").append(var).append(";").nl();
		dec();
		append("}").nl();
		append("return ").append(var).append(";").nl();
	}

}
