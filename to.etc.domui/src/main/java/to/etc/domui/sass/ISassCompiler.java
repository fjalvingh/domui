package to.etc.domui.sass;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.state.IPageParameters;
import to.etc.domui.util.resources.IResourceDependencyList;

import java.io.Writer;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 29-10-17.
 */
public interface ISassCompiler {
	void compiler(String rurl, Writer output, @NonNull IPageParameters params, @NonNull IResourceDependencyList rdl) throws Exception;

	boolean available();
}
