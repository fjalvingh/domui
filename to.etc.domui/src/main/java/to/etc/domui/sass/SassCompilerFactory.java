package to.etc.domui.sass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 29-10-17.
 */
final public class SassCompilerFactory {
	static private final Logger LOG = LoggerFactory.getLogger(SassCompilerFactory.class);

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

	/**
	 * Release the resources held by all registered compilers; called when the application terminates.
	 */
	static public void terminate() {
		for(ISassCompiler compiler : getCompilerList()) {
			try {
				compiler.close();
			} catch(Exception x) {
				LOG.error("Failed to close sass compiler " + compiler + ": " + x, x);
			}
		}
	}

	static {
		register(new DartSassCompiler());				// Dart Sass, the reference implementation, is used when its process can be started,
		register(new JSassCompiler());					// and the end-of-life libsass is the fallback for as long as it is still here.
	}
}
