package to.etc.domui.sass;

import com.sass_lang.embedded_protocol.InboundMessage.ImportResponse.ImportSuccess;
import com.sass_lang.embedded_protocol.Syntax;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.state.IPageParameters;
import to.etc.domui.themes.ThemeResourceFactory;
import to.etc.domui.util.resources.IResourceDependencyList;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves the names that dart-sass asks for to DomUI webapp resources.
 *
 * <p>Dart-sass works with canonical urls: every resource handed to it must have an unique url
 * containing a scheme, and dart-sass itself resolves a relative import inside a sheet against
 * the url of that sheet before asking us to canonicalize the result. Every DomUI resource is
 * therefore presented as "domui:/" plus its resource name, which means all relative path
 * handling is done by dart-sass; what arrives here is always a complete resource name, so only
 * the partial (_name) and suffix conventions remain to be handled.</p>
 *
 * <p>Two names are virtual and are recognised by their basename, from whatever directory they
 * are asked for: <code>parameters</code> is the generated file holding the request's variables
 * (see {@link AbstractSassResolver#generateParameterFile()}), and <code>theme</code> is the
 * theme's own module, <code>$THEME/[variant]/_index.scss</code> for the variant the sheet is
 * being compiled for. So a partial inside the theme, an application's <code>_userstyle.scss</code>
 * and an application sheet outside the theme directory all reach the theme with the same
 * <code>@use "theme" as *;</code>. A resource that is itself called <code>_parameters.scss</code>
 * or <code>_theme.scss</code> is consequently unreachable.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
final class DartSassResolver extends AbstractSassResolver<ImportSuccess> {
	/** The url scheme under which all DomUI resources are presented to dart-sass. */
	static private final String SCHEME = "domui";

	/** The suffixes tried, in order, when the name asked for has none. */
	static private final List<String> SUFFIXES = Arrays.asList(".scss", ".sass", ".css");

	/** All names under which the generated, virtual parameter file can be imported. */
	static private final List<String> PARAMETER_NAMES = Arrays.asList("parameters", "_parameters", "parameters.scss", "_parameters.scss", "parameters.sass", "_parameters.sass");

	/** All names under which the theme's module is imported. */
	static private final List<String> THEME_NAMES = Arrays.asList("theme", "_theme", "theme.scss", "_theme.scss", "theme.sass", "_theme.sass");

	/** The theme module: the index file of the theme directory. */
	static private final String THEME_INDEX = "_index.scss";

	private final Map<String, ImportSuccess> m_byUrl = new HashMap<>();

	private final Map<ImportSuccess, String> m_urlByImport = new IdentityHashMap<>();

	DartSassResolver(IPageParameters params, IResourceDependencyList rdl) {
		super(params, rdl);
	}

	/**
	 * Locate the resource for the url asked for, and return the canonical url it is known by, or
	 * null if it does not exist. The content itself is loaded here too, and returned by {@link #load(String)}
	 * when dart-sass asks for the canonicalized url.
	 */
	@Nullable
	String canonicalize(String url) {
		String name = toResourceName(url);
		if(null == name)
			return null;
		ImportSuccess is = resolveName(name);
		return null == is ? null : m_urlByImport.get(is);
	}

	/**
	 * Return the content for a url returned earlier by {@link #canonicalize(String)}.
	 */
	@Nullable
	ImportSuccess load(String url) {
		return m_byUrl.get(url);
	}

	@Nullable
	private ImportSuccess resolveName(String name) {
		//-- The virtual parameter file is known by name only, from whatever directory it is imported.
		String lastName = name.substring(name.lastIndexOf('/') + 1);
		if(PARAMETER_NAMES.contains(lastName))
			return resolve("_parameters.scss", "");

		//-- The theme module is the index of the theme directory of the variant this sheet is compiled for.
		if(THEME_NAMES.contains(lastName))
			return resolve(ThemeResourceFactory.PREFIX + getThemeVariantName() + "/" + THEME_INDEX, "");

		if(hasSuffix(name))
			return resolve(name, "");

		for(String suffix : SUFFIXES) {
			ImportSuccess is = resolve(name + suffix, "");
			if(null != is)
				return is;
		}
		return null;
	}

	@Override
	protected ImportSuccess createInput(String name, String content) {
		String url = toUrl(name);
		ImportSuccess is = ImportSuccess.newBuilder()
			.setContents(content)
			.setSyntax(syntaxOf(name))
			.setSourceMapUrl(url)
			.build();
		m_byUrl.put(url, is);
		m_urlByImport.put(is, url);
		return is;
	}

	/**
	 * Present a DomUI resource name as an url with our own scheme.
	 */
	static private String toUrl(String name) {
		try {
			return new URI(SCHEME, "/" + name, null).toString();
		} catch(URISyntaxException x) {
			throw new SassException("Cannot create an url for the scss resource '" + name + "': " + x, x);
		}
	}

	/**
	 * The reverse of {@link #toUrl(String)}: get the DomUI resource name from the url dart-sass asks
	 * for, or null if that url is not one of ours.
	 */
	@Nullable
	static private String toResourceName(String url) {
		String path;
		if(url.regionMatches(true, 0, SCHEME + ":", 0, SCHEME.length() + 1)) {
			try {
				path = new URI(url).getSchemeSpecificPart();
			} catch(URISyntaxException x) {
				return null;
			}
		} else if(url.indexOf(':') >= 0) {
			return null;								// file:, http:, data: ... -> not ours.
		} else {
			path = url;									// A relative name: we are being asked as a fallback importer.
		}

		int ix = 0;
		while(ix < path.length() && path.charAt(ix) == '/')
			ix++;
		return path.substring(ix);
	}

	static private boolean hasSuffix(String name) {
		String lc = name.toLowerCase();
		for(String suffix : SUFFIXES) {
			if(lc.endsWith(suffix))
				return true;
		}
		return false;
	}

	static private Syntax syntaxOf(String name) {
		String lc = name.toLowerCase();
		if(lc.endsWith(".sass"))
			return Syntax.INDENTED;
		if(lc.endsWith(".css"))
			return Syntax.CSS;
		return Syntax.SCSS;
	}
}
