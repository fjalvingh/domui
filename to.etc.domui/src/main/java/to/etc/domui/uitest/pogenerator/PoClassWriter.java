package to.etc.domui.uitest.pogenerator;

import to.etc.util.IndentWriter;
import to.etc.util.Pair;

import java.io.StringWriter;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 08-12-21.
 */
public class PoClassWriter implements IPoModelVisitor {
	private final IndentWriter m_writer;

	private final StringWriter m_sw;

	private PoClass m_currentClass;

	static private final Pair<String, String> GENERATED = new Pair<>("javax.annotation.processing", "Generated");

	public PoClassWriter() {
		m_sw = new StringWriter(8192);
		m_writer = new IndentWriter(m_sw);
	}

	@Override
	public void visitClass(PoClass n) throws Exception {
		m_currentClass = n;
		append("package ").append(n.getPackageName()).append(";\n");

		//-- Collect all needed imports
		for(PoField poField : n.getFieldList()) {
			n.addImport(poField.getPackageName(), poField.getTypeName());
		}
		for(PoMethod poMethod : n.getMethodList()) {
			for(Pair<String, String> pair : poMethod.getImportList()) {
				n.addImport(pair.get1(), pair.get2());
			}
		}
		if(n.isMarkGenerated()) {
			n.addImport(GENERATED);
		}

		nl();
		for(String s : n.getImportSet()) {
			append("import ").append(s).append(";").nl();
		}

		//-- Write the class
		if(n.isMarkGenerated()) {
			append("@").appendType(n, "javax.annotation.processing", "Generated").nl();
		}

		//-- Class header
		append("public class ").append(n.getClassName()).append(" ");
		PoClass baseClass = n.getBaseClass();
		if(null != baseClass) {
			append("extends ").appendType(n, baseClass.getPackageName(), baseClass.getClassName()).append(" ");
		}
		if(n.getInterfaceList().size() > 0) {
			append("implements ");
			List<Pair<String, String>> interfaceList = n.getInterfaceList();
			for(int i = 0; i < interfaceList.size(); i++) {
				Pair<String, String> s = interfaceList.get(i);
				if(i > 0)
					append(", ");
				appendType(n, s);
			}
		}
		append("{").nl();
		inc();

		//-- Fields
		for(PoField poField : n.getFieldList()) {
			append("private final ").appendType(n, poField.getPackageName(), poField.getFieldName()).append(" ").append(poField.getFieldName()).append(";").nl();
			nl();
		}



		//-- end of class
		dec();
		append("}").nl().nl();
	}

	@Override
	public void visitMethod(PoMethod n) throws Exception {

	}

	@Override
	public void visitField(PoField n) throws Exception {

	}

	public PoClassWriter append(String s) throws Exception {
		m_writer.append(s);
		return this;
	}

	public PoClassWriter nl() throws Exception {
		m_writer.append("\n");
		return this;
	}


	public PoClassWriter inc() {
		m_writer.inc();
		return this;
	}

	public PoClassWriter dec() {
		m_writer.dec();
		return this;
	}

	private PoClassWriter appendType(PoClass clz, Pair<String, String> type) throws Exception {
		append(getTypeName(clz, type.get1(), type.get2()));
		return this;
	}

	private PoClassWriter appendType(PoClass clz, String packageName, String typeName) throws Exception {
		append(getTypeName(clz, packageName, typeName));
		return this;
	}

	private String getTypeName(PoClass clz, String packageName, String typeName) {
		if(packageName.length() == 0)
			return typeName;
		String fullName = packageName + "." + typeName;
		if(clz.hasImport(fullName))
			return typeName;
		return fullName;
	}

	public String getResult() {
		return m_sw.getBuffer().toString();
	}
}
