package to.etc.domui.sass;

import com.vaadin.sass.internal.*;
import com.vaadin.sass.internal.ScssContext.*;
import com.vaadin.sass.internal.handler.*;
import com.vaadin.sass.internal.parser.*;
import com.vaadin.sass.internal.visitor.*;
import to.etc.domui.parts.ParameterInfoImpl;
import to.etc.domui.server.*;
import to.etc.domui.server.parts.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.resources.*;
import to.etc.util.*;

import javax.annotation.*;
import java.io.*;

/**
 * This Web part accepts requests ending in .scss, and compiles them into a .css stylesheet on-the-fly.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 17-4-17.
 */
@DefaultNonNull
public class SassPartFactory implements IBufferedPartFactory<ParameterInfoImpl> {
	/**
	 * Accepts .scss resources as sass stylesheets, and passes them through the
	 * sass compiler, returning the result as a normal .css stylesheet.
	 */
	static public final IUrlMatcher MATCHER = new IUrlMatcher() {
		@Override public boolean accepts(@Nonnull IParameterInfo parameters) {
			return parameters.getInputPath().endsWith(".scss");
		}
	};

	@Nonnull @Override public ParameterInfoImpl decodeKey(DomApplication application, @Nonnull IExtendedParameterInfo param) throws Exception {
		ParameterInfoImpl ppi = new ParameterInfoImpl(param, name -> ! name.startsWith("$"));	// Ignore DomUI system parameters
		return ppi;
	}

	@Override public void generate(@Nonnull PartResponse pr, @Nonnull DomApplication da, @Nonnull ParameterInfoImpl params, @Nonnull IResourceDependencyList rdl) throws Exception {
		long ts = System.nanoTime();
		SassCapturingErrorHandler errorHandler = new SassCapturingErrorHandler();
		//errorHandler.setWarningsAreErrors(true);

		/*
		 * Define resolvers: these resolve "filenames" in the scss to resources in the webapp.
		 */
		String rurl = params.getInputPath();
		String basePath;
		int pos = rurl.lastIndexOf('/');
		if(pos == -1) {
			basePath = "";
		} else {
			basePath = rurl.substring(pos + 1);
		}

		ScssStylesheet parent = new ScssStylesheet();
		SassDomuiResolver resolver = new SassDomuiResolver(rdl, basePath, params);
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

		pr.setMime("text/css");
		try(OutputStream outputStream = pr.getOutputStream()) {
			OutputStreamWriter osw = new OutputStreamWriter(outputStream, "utf-8");
			scss.write(osw, ! DomApplication.get().inDevelopmentMode());
			osw.close();
		}
		ts = System.nanoTime() - ts;
		System.out.println("sass: script render took " + StringTool.strNanoTime(ts));
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
}
