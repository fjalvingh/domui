package to.etc.domui.sass;

import io.bit3.jsass.Compiler;
import io.bit3.jsass.Options;
import io.bit3.jsass.context.FileContext;
import to.etc.domui.parts.ParameterInfoImpl;
import to.etc.domui.util.resources.IResourceDependencyList;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Writer;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 29-10-17.
 */
public class JSassCompiler implements ISassCompiler {
	@Override public void compiler(String rurl, Writer output, @Nonnull ParameterInfoImpl params, @Nonnull IResourceDependencyList rdl) throws Exception {
		/*
		 * Define resolvers: these resolve "filenames" in the scss to resources in the webapp.
		 */
		String basePath;
		int pos = rurl.lastIndexOf('/');
		if(pos == -1) {
			basePath = "";
		} else {
			basePath = rurl.substring(pos + 1);
		}

		JSassResolver jsr = new JSassResolver(params, basePath, rdl);

		File file = jsr.resolve(rurl, rurl);
		File out = File.createTempFile("sass-out-", ".css");
		Options opt = new Options();

		FileContext fc = new FileContext(file.toURI(), out.toURI(), opt);
		Compiler co = new Compiler();
		co.compile(fc);
	}


	@Override public boolean available() {
		return true;
	}
}
