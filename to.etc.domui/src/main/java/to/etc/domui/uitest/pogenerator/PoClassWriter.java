package to.etc.domui.uitest.pogenerator;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 08-12-21.
 */
public class PoClassWriter extends BodyWriter<PoClassWriter> implements IPoModelVisitor {

	public static final String IMPORT_MARKER = "$*$IMPORT*$*";

	private PoClass m_currentClass;

	static private final RefType GENERATED = new RefType("javax.annotation.processing", "Generated");

	public PoClassWriter() {
	}

	@Override
	public void visitClass(PoClass n) throws Exception {
		m_currentClass = n;
		append("package ").append(n.getPackageName()).append(";\n");

		//-- Collect all needed imports
		//for(PoField poField : n.getFieldList()) {
		//	n.addImport(poField.getPackageName(), poField.getTypeName());
		//}
		//for(PoMethod poMethod : n.getMethodList()) {
		//	for(Pair<String, String> pair : poMethod.getImportList()) {
		//		n.addImport(pair.get1(), pair.get2());
		//	}
		//}
		if(n.isMarkGenerated()) {
			n.addImport(GENERATED);
		}

		nl();
		append(IMPORT_MARKER);

		//for(String s : n.getImportSet()) {
		//	append("import ").append(s).append(";").nl();
		//}
		nl();

		//-- Write the class
		if(n.isMarkGenerated()) {
			append("@").appendType(n, GENERATED);
			append("(\"").append("Generated on " + new Date() + "\")").nl();
		}

		//-- Class header
		append("public class ").append(n.getClassName()).append(" ");
		RefType baseClass = n.getBaseClass();
		if(null != baseClass) {
			append("extends ").appendType(n, baseClass);
			append(" ");
		}
		List<RefType> interfaceList = n.getInterfaceList();
		if(interfaceList.size() > 0) {
			interfaceList.sort(Comparator.comparing(RefType::asTypeString));
			append("implements ");
			for(int i = 0; i < interfaceList.size(); i++) {
				RefType s = interfaceList.get(i);
				if(i > 0)
					append(", ");
				appendType(n, s);
			}
		}
		append("{").nl();
		inc();

		//-- Fields
		List<PoField> fieldList = n.getFieldList();
		fieldList.sort(Comparator.comparing(PoField::getFieldName));
		for(PoField poField : fieldList) {
			append("private ")
				.appendType(n, poField.getType())
				.append(" ")
				.append(poField.getFieldName())
				.append(";").nl();
			nl();
		}
		nl();

		//-- Constructor
		List<PoMethod> constructorList = n.getConstructorList();
		for(PoMethod poMethod : constructorList) {
			renderConstructor(n, poMethod);
		}

		//-- Methods.
		List<PoMethod> methodList = n.getMethodList();
		methodList.sort(Comparator.comparing(PoMethod::getMethodName));
		for(PoMethod poMethod : methodList) {
			renderMethod(n, poMethod);
		}

		//-- end of class
		dec();
		append("}").nl().nl();
	}

	private void renderConstructor(PoClass n, PoMethod poMethod) throws Exception {
		appendModifiers(poMethod.getModifierSet());

		append(poMethod.getMethodName());
		append("(");
		List<PoMethodParameter> parameterList = poMethod.getParameterList();
		for(int i = 0; i < parameterList.size(); i++) {
			PoMethodParameter parameter = parameterList.get(i);
			if(i > 0)
				append(", ");
			appendType(n, parameter.getType());
			append(" ");
			append(parameter.getParameterName());
		}
		append(") {").nl();
		inc();
		append(poMethod.getResult());
		dec();
		append("}").nl();
		nl();
	}

	private void renderMethod(PoClass n, PoMethod poMethod) throws Exception {
		appendModifiers(poMethod.getModifierSet());
		RefType returnType = poMethod.getReturnType();
		if(null == returnType) {
			append("void ");
		} else {
			appendType(n, returnType).append(" ");
		}
		append(poMethod.getMethodName());
		append("(");
		List<PoMethodParameter> parameterList = poMethod.getParameterList();
		for(int i = 0; i < parameterList.size(); i++) {
			PoMethodParameter parameter = parameterList.get(i);
			if(i > 0)
				append(", ");
			appendType(n, parameter.getType());
			append(" ");
			append(parameter.getParameterName());
		}

		append(") throws Exception {").nl();
		inc();
		append(poMethod.getResult());
		dec();
		append("}").nl().nl();
	}

	private PoClassWriter appendModifiers(Set<Modifier> modifierSet) throws Exception {
		if(modifierSet.size() == 0) {
			append("public ");
			return this;
		}

		if(modifierSet.contains(Modifier.Final)) {
			append("final ");
		}
		if(modifierSet.contains(Modifier.Public)) {
			append("public ");
		} else if(modifierSet.contains(Modifier.Protected)) {
			append("protected ");
		} else if(modifierSet.contains(Modifier.Private)) {
			append("private ");
		}
		return this;
	}

	@Override
	public void visitMethod(PoMethod n) throws Exception {

	}

	@Override
	public void visitField(PoField n) throws Exception {

	}

	@Override
	public String getResult() {
		String result = super.getResult();

		StringBuilder sb = new StringBuilder();
		for(String s : m_currentClass.getImportSet()) {
			sb.append("import ").append(s).append(";\n");
		}
		return result.replace(IMPORT_MARKER, sb.toString());
	}
}
