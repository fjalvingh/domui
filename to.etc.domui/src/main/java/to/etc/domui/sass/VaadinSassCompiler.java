package to.etc.domui.sass;

import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.ScssContext.UrlMode;
import com.vaadin.sass.internal.ScssStylesheet;
import com.vaadin.sass.internal.handler.SCSSDocumentHandlerImpl;
import com.vaadin.sass.internal.parser.LexicalUnitImpl;
import com.vaadin.sass.internal.parser.Variable;
import com.vaadin.sass.internal.visitor.ExtendNodeHandler;
import to.etc.domui.parts.ParameterInfoImpl;
import to.etc.domui.server.DomApplication;
import to.etc.domui.trouble.ThingyNotFoundException;
import to.etc.domui.util.resources.IResourceDependencyList;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Writer;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 29-10-17.
 */
public class VaadinSassCompiler implements ISassCompiler {
	@Override
	public void compiler(String rurl, Writer output, @Nonnull ParameterInfoImpl params, @Nonnull IResourceDependencyList rdl) throws Exception {
		SassCapturingErrorHandler errorHandler = new SassCapturingErrorHandler();

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

		ScssStylesheet parent = new ScssStylesheet();
		VaadinSassResolver resolver = new VaadinSassResolver(rdl, basePath, params);
		parent.addResolver(resolver);
		parent.setCharset("utf-8");
		parent.setFile(new File(rurl));				// jal 20170702 So bad, but there is no other reliable way to present parentage

		// Parse stylesheet
		ScssStylesheet scss = ScssStylesheet.get(rurl, parent, new SCSSDocumentHandlerImpl(), errorHandler);
		if(scss == null) {
			throw new ThingyNotFoundException("The scss file " + rurl + " could not be found.");
		}

		// Compile scss -> css
		compile(scss, UrlMode.RELATIVE);

		if(errorHandler.hasError()) {
			throw new RuntimeException("SASS compilation failed:\n" + errorHandler.toString());
		}
		String s = errorHandler.toString();
		if(s != null && s.length() > 0)
			System.err.println("SASS error on " + rurl + ":\n" + s);

		//pr.setMime("text/css");
		scss.write(output, ! DomApplication.get().inDevelopmentMode());
		resolver.close();
	}

	/**
	 * Alternative to the compile method from the sass code, this should allow
	 * adding variables into the compilation context so that theme compilation
	 * can be done with it.
	 *
	 * @param scss
	 * @param urlMode
	 * @throws Exception
	 */
	private void compile(ScssStylesheet scss, UrlMode urlMode) throws Exception {
		ScssContext context = new ScssContext(urlMode);

		LexicalUnitImpl lxu = LexicalUnitImpl.createIdent(0, 0, "#abc");

		Variable var = new Variable("inputColor", lxu);
		context.addVariable(var);

		scss.traverse(context);
		ExtendNodeHandler.modifyTree(context, scss);
	}

	@Override public boolean available() {
		return true;
	}
}
