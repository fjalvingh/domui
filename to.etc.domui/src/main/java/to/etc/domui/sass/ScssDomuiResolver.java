package to.etc.domui.sass;

import com.vaadin.sass.internal.ScssStylesheet;
import com.vaadin.sass.internal.resolver.ScssStylesheetResolver;
import org.w3c.css.sac.InputSource;
import to.etc.domui.parts.ParameterInfoImpl;
import to.etc.domui.server.DomApplication;
import to.etc.domui.util.resources.IResourceDependencyList;
import to.etc.domui.util.resources.IResourceRef;
import to.etc.util.StringTool;
import to.etc.util.WrappedException;

import javax.annotation.Nonnull;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;

/**
 * Resolves sass resources using DomUI's resolution mechanisms, and tracks
 * the resources used for auto recompile.
 */
final class ScssDomuiResolver implements ScssStylesheetResolver {
	@Nonnull
	private final IResourceDependencyList m_dependencyList;

	@Nonnull
	private final ParameterInfoImpl m_params;

	public ScssDomuiResolver(@Nonnull IResourceDependencyList dependencyList, @Nonnull String basePath, @Nonnull ParameterInfoImpl params) {
		m_dependencyList = dependencyList;
		m_params = params;
	}

	/**
	 * Resolve scss paths. Quite the mess, as this also resolves all the myriad ways that a @import xxx
	 * can mangle the xxx to get some result.
	 * <ul>
	 *	<li>If the path has </li>
	 * </ul>
	 */
	@Override public InputSource resolve(ScssStylesheet parentStylesheet, String original) {
		//Disaster based
		//String directory = parentStylesheet.getDirectory();
		//String fileName = parentStylesheet.getFileName();
		List<String> sourceUris = parentStylesheet.getSourceUris();

		//-- Make sure we have a suffix
		String identifier = original;
		if(! identifier.endsWith(".scss")) {
			identifier += ".scss";
		}

		if(identifier.equals("_parameters.scss") || identifier.equals("parameters.scss")) {
			return createParameterFile();
		}

		System.out.print("$$ scss resolve path '" + identifier + "', parenturis=" + sourceUris);
		DomApplication app = DomApplication.get();

		if(identifier.indexOf('/') != -1) {			// If it has slashes try the name as-is first
			//-- Try literal name
			IResourceRef ref = tryRef(app, identifier, m_dependencyList);
			if(null != ref)
				return createSource(ref, identifier);

			//-- Try for a "partial"
			String newName = "_" + identifier;
			ref = tryRef(app, newName, m_dependencyList);
			if(null != ref)
				return createSource(ref, newName);
		}

		//-- Try to prefix the relative path from its parent
		if(sourceUris.size() > 0) {
			String top = sourceUris.get(0);

			int pos = top.lastIndexOf('/');
			if(pos >= 0) {
				top = top.substring(0, pos + 1);	// Get path without file name but including /
				String newName = top + identifier;	// Get new path relative to parent
				IResourceRef ref = tryRef(app, newName, m_dependencyList);
				if(null != ref)
					return createSource(ref, newName);

				//-- Try for a partial
				newName = top + "_" + identifier;
				ref = tryRef(app, newName, m_dependencyList);
				if(null != ref)
					return createSource(ref, newName);
			}
		}
		System.out.println(" - FAILED");
		return null;								// Not found

		//
		//IResourceRef ref;
		//try {
		//	ref = app.getResource(identifier, m_dependencyList);
		//	m_dependencyList.add(ref);
		//	if(!ref.exists()) {
		//		return null;
		//	}
		//} catch(Exception x) {
		//	throw WrappedException.wrap(x);
		//}
		//
		//try {
		//	InputSource inputSource = new InputSource(new InputStreamReader(ref.getInputStream(), "utf-8"));
		//	inputSource.setURI(identifier);
		//	return inputSource;
		//} catch(Exception x) {
		//	throw WrappedException.wrap(x);
		//}
	}

	private InputSource createParameterFile() {
		StringBuilder sb = new StringBuilder();
		for(String name: m_params.getParameterNames()) {
			String[] values = m_params.getParameters(name);
			if(null != values && values.length == 1) {
				String value = values[0];
				if(! StringTool.isNumber(value)) {
					value = StringTool.strToJavascriptString(value, true);
				}
				sb.append("$").append(name).append(": ").append(value).append(";\n");
			}
		}

		InputSource is = new InputSource(new StringReader(sb.toString()));
		is.setURI("_parameter.scss");
		return is;
	}

	@Nonnull
	private InputSource createSource(@Nonnull IResourceRef ref, @Nonnull String name) {
		try {
			InputSource inputSource = new InputSource(new InputStreamReader(ref.getInputStream(), "utf-8"));
			inputSource.setURI(name);
			System.out.println(" - found");
			return inputSource;
		} catch(Exception x) {
			throw WrappedException.wrap(x);
		}
	}

	private IResourceRef tryRef(DomApplication app, String name, IResourceDependencyList deplist) {
		try {
			IResourceRef ref = app.getResource(name, deplist);
			if(!ref.exists()) {
				return null;
			}
			deplist.add(ref);
			return ref;
		} catch(Exception x) {
			return null;
		}
	}
}
