package to.etc.domui.sass;

import com.vaadin.sass.internal.*;
import com.vaadin.sass.internal.ScssContext.*;
import com.vaadin.sass.internal.handler.*;
import com.vaadin.sass.internal.parser.*;
import com.vaadin.sass.internal.resolver.*;
import com.vaadin.sass.internal.visitor.*;
import org.w3c.css.sac.*;
import to.etc.domui.server.*;
import to.etc.domui.server.parts.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.resources.*;
import to.etc.util.*;

import javax.annotation.*;
import java.io.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 17-4-17.
 */
final public class SassPartFactory implements IBufferedPartFactory, IUrlPart {
	/**
	 * Accepts .scss resources as sass stylesheets, and passes them through the
	 * sass compiler, returning the result as a normal .css stylesheet.
	 */
	@Override
	public boolean accepts(@Nonnull String rurl) {
		return rurl.endsWith(".scss");
	}

	@Nonnull @Override public Object decodeKey(@Nonnull String rurl, @Nonnull IExtendedParameterInfo param) throws Exception {
		return rurl;
	}

	@Override public void generate(@Nonnull PartResponse pr, @Nonnull DomApplication da, @Nonnull Object key, @Nonnull IResourceDependencyList rdl) throws Exception {
		String rurl = (String) key;

		CapturingErrorHandler errorHandler = new CapturingErrorHandler();
		//errorHandler.setWarningsAreErrors(true);

		//-- Define resolvers
		ScssStylesheet parent = new ScssStylesheet();
		parent.addResolver(new WebAppResolver(rdl));
		parent.setCharset("utf-8");

		// Parse stylesheet
		ScssStylesheet scss = ScssStylesheet.get(rurl, parent, new SCSSDocumentHandlerImpl(), errorHandler);
		if(scss == null) {
			throw new ThingyNotFoundException("The scss file " + rurl + " could not be found.");
		}

		// Compile scss -> css
		compile(scss, UrlMode.RELATIVE);

		if(errorHandler.hasError()) {
			throw new RuntimeException("SASS compilation failed: " + errorHandler.toString());
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

	/**
	 * Resolves sass resources using DomUI's resolution mechanisms, and tracks
	 * the resources used for auto recompile.
	 */
	final private class WebAppResolver implements ScssStylesheetResolver {
		@Nonnull
		private final IResourceDependencyList m_dependencyList;

		public WebAppResolver(IResourceDependencyList dependencyList) {
			m_dependencyList = dependencyList;
		}

		@Override public InputSource resolve(ScssStylesheet parentStylesheet, String identifier) {
			DomApplication app = DomApplication.get();

			IResourceRef ref;
			try {
				ref = app.getResource(identifier, m_dependencyList);
				m_dependencyList.add(ref);
				if(!ref.exists()) {
					return null;
				}
			} catch(Exception x) {
				throw WrappedException.wrap(x);
			}

			try {
				InputSource inputSource = new InputSource(new InputStreamReader(ref.getInputStream(), "utf-8"));
				inputSource.setURI(identifier);
				return inputSource;
			} catch(Exception x) {
				throw WrappedException.wrap(x);
			}
		}
	}

	/**
	 * Captures all messages in a string.
	 */
	final private class CapturingErrorHandler extends SCSSErrorHandler {
		final private StringBuilder m_sb = new StringBuilder();

		private boolean m_hasError;

		@Override public void warning(CSSParseException e) throws CSSException {
			render("warning", e);
		}

		private void render(String type, CSSParseException e) {
			m_sb.append(e.getURI())
			.append("(")
			.append(e.getLineNumber())
			.append(':')
			.append(e.getColumnNumber())
			.append(") ")
			.append(type)
			.append(": ")
			.append(e.getMessage())
			.append("\n");
		}

		@Override public void error(CSSParseException e) throws CSSException {
			m_hasError = true;
			render("error", e);
		}

		@Override public void fatalError(CSSParseException e) throws CSSException {
			m_hasError = true;
			render("fatal error", e);
		}

		public boolean hasError() {
			return m_hasError;
		}

		@Override public String toString() {
			return m_sb.toString();
		}
	}
}
