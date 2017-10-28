package to.etc.domui.sass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 29-10-17.
 */
final public class SassCompilerFactory {
	static private List<ISassCompiler> m_compilerList = Collections.emptyList();

	private SassCompilerFactory() {}

	public static ISassCompiler createCompiler() {
		List<ISassCompiler> compilerList = getCompilerList();

		for(ISassCompiler compiler : compilerList) {
			if(compiler.available()) {
				return compiler;
			}
		}

		throw new IllegalStateException("There is no SASS/SCSS compiler available");
	}

	public static synchronized List<ISassCompiler> getCompilerList() {
		return m_compilerList;
	}

	static public synchronized void register(ISassCompiler compiler) {
		List<ISassCompiler> list = new ArrayList<>(m_compilerList);
		list.add(compiler);
		m_compilerList = list;
	}

	static {
		register(new JSassCompiler());
		register(new VaadinSassCompiler());
	}
}
