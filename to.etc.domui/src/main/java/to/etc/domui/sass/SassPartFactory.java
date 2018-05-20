package to.etc.domui.sass;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.domui.parts.ParameterInfoImpl;
import to.etc.domui.server.DomApplication;
import to.etc.domui.server.IExtendedParameterInfo;
import to.etc.domui.server.IParameterInfo;
import to.etc.domui.server.parts.IBufferedPartFactory;
import to.etc.domui.server.parts.IUrlMatcher;
import to.etc.domui.server.parts.PartResponse;
import to.etc.domui.util.resources.IResourceDependencyList;
import to.etc.util.StringTool;

import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * This Web part accepts requests ending in .scss, and compiles them into a .css stylesheet on-the-fly.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 17-4-17.
 */
@NonNullByDefault
public class SassPartFactory implements IBufferedPartFactory<ParameterInfoImpl> {
	static private final Logger LOG = LoggerFactory.getLogger(SassPartFactory.class);

	/**
	 * Accepts .scss resources as sass stylesheets, and passes them through the
	 * sass compiler, returning the result as a normal .css stylesheet.
	 */
	static public final IUrlMatcher MATCHER = new IUrlMatcher() {
		@Override public boolean accepts(@NonNull IParameterInfo parameters) {
			return parameters.getInputPath().endsWith(".scss") || parameters.getInputPath().endsWith(".sass");
		}
	};

	@NonNull @Override public ParameterInfoImpl decodeKey(DomApplication application, @NonNull IExtendedParameterInfo param) throws Exception {
		ParameterInfoImpl ppi = new ParameterInfoImpl(param, name -> ! name.startsWith("$"));	// Ignore DomUI system parameters
		return ppi;
	}

	@Override public void generate(@NonNull PartResponse pr, @NonNull DomApplication da, @NonNull ParameterInfoImpl params, @NonNull IResourceDependencyList rdl) throws Exception {
		long ts = System.nanoTime();

		/*
		 * Define resolvers: these resolve "filenames" in the scss to resources in the webapp.
		 */
		String rurl = params.getInputPath();
		ISassCompiler compiler = SassCompilerFactory.createCompiler();

		pr.setMime("text/css");
		try(OutputStream outputStream = pr.getOutputStream()) {
			OutputStreamWriter osw = new OutputStreamWriter(outputStream, "utf-8");
			compiler.compiler(rurl, osw, params, rdl);
			osw.close();
		}
		ts = System.nanoTime() - ts;
		LOG.info("sass: script render took " + StringTool.strNanoTime(ts));
	}
}
