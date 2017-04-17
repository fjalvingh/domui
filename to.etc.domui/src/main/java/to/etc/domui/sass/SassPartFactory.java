package to.etc.domui.sass;

import com.vaadin.sass.internal.*;
import com.vaadin.sass.internal.ScssContext.*;
import com.vaadin.sass.internal.handler.*;
import com.vaadin.sass.internal.resolver.*;
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
		parent.addResolver(new WebAppresolver(rdl));
		parent.setCharset("utf-8");

		// Parse stylesheet
		ScssStylesheet scss = ScssStylesheet.get(rurl, parent, new SCSSDocumentHandlerImpl(), errorHandler);
		if(scss == null) {
			throw new ThingyNotFoundException("The scss file " + rurl + " could not be found.");
		}

		// Compile scss -> css
		scss.compile(UrlMode.RELATIVE);

		if(errorHandler.hasError()) {
			throw new RuntimeException("SASS compilation failed: " + errorHandler.toString());
		}

		pr.setMime("text/css");
		try(OutputStream outputStream = pr.getOutputStream()) {
			OutputStreamWriter osw = new OutputStreamWriter(outputStream, "utf-8");
			scss.write(osw, ! DomApplication.get().inDevelopmentMode());
			osw.close();
		}
	}

	final private class WebAppresolver implements ScssStylesheetResolver {
		@Nonnull
		private final IResourceDependencyList m_dependencyList;

		public WebAppresolver(IResourceDependencyList dependencyList) {
			m_dependencyList = dependencyList;
		}

		@Override public InputSource resolve(ScssStylesheet parentStylesheet, String identifier) {
			DomApplication app = DomApplication.get();

			IResourceRef ref = app.getAppFileOrResource(identifier);
			m_dependencyList.add(ref);
			if(!ref.exists()) {
				return null;
			}
			try {
				return new InputSource(new InputStreamReader(ref.getInputStream(), "utf-8"));
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
