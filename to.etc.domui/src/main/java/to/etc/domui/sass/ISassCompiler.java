package to.etc.domui.sass;

import to.etc.domui.parts.ParameterInfoImpl;
import to.etc.domui.util.resources.IResourceDependencyList;

import javax.annotation.Nonnull;
import java.io.Writer;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 29-10-17.
 */
public interface ISassCompiler {
	void compiler(String rurl, Writer output, @Nonnull ParameterInfoImpl params, @Nonnull IResourceDependencyList rdl) throws Exception;

	boolean available();
}
