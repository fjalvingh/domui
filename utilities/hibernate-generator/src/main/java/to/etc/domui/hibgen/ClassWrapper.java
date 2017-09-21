package to.etc.domui.hibgen;

import com.github.javaparser.ast.CompilationUnit;

import java.io.File;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-9-17.
 */
class ClassWrapper {
	private final AbstractGenerator m_generator;

	private final File m_file;

	private CompilationUnit m_unit;


	public ClassWrapper(AbstractGenerator generator, File file, CompilationUnit unit) {
		m_generator = generator;
		m_file = file;
		m_unit = unit;
	}


	public String getClassName() {
		String pkg = m_unit.getPackageDeclaration().get().getName().asString();
		String name = m_file.getName();
		name = name.substring(0, name.lastIndexOf("."));                // Strip .java
		return pkg + "." + name;
	}
}
