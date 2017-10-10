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
import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves sass resources using DomUI's resolution mechanisms, and tracks
 * the resources used for auto recompile.
 */
final class SassDomuiResolver implements ScssStylesheetResolver {
	@Nonnull
	private final IResourceDependencyList m_dependencyList;

	@Nonnull
	private final ParameterInfoImpl m_params;

	@Nullable
	private String m_parameterFile;

	private long m_resolveTime;

	private static class Line {
		private final String m_name;

		private final IResourceRef m_ref;

		public Line(String name, IResourceRef ref) {
			m_name = name;
			m_ref = ref;
		}

		public String getName() {
			return m_name;
		}

		public IResourceRef getRef() {
			return m_ref;
		}
	}

	final private Map<String, Line> m_map = new HashMap<>();

	public SassDomuiResolver(@Nonnull IResourceDependencyList dependencyList, @Nonnull String basePath, @Nonnull ParameterInfoImpl params) {
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
		String fileBase = parentStylesheet.getFileName();    // not a file name at all; a path. Sigh.
		int ixof = fileBase.lastIndexOf("/");
		if(ixof > 0) {
			fileBase = fileBase.substring(0, ixof);            // Base directory exclusive final slash
		}

		long ts = System.nanoTime();
		try {
			//System.out.println("base = " + fileBase);

			//-- Make sure we have a suffix
			String identifier = original;
			if(!identifier.endsWith(".scss")) {
				identifier += ".scss";
			}

			if(identifier.equals("_parameters.scss") || identifier.equals("parameters.scss")) {
				return createParameterFile();
			}

			Line rr = m_map.get(identifier);
			if(null != rr) {
				return createSource(rr.getRef(), rr.getName());
			}

			//Disaster based
			//String directory = parentStylesheet.getDirectory();
			//String fileBase = parentStylesheet.getFileName();
			List<String> sourceUris = parentStylesheet.getSourceUris();
			DomApplication app = DomApplication.get();

			if(identifier.indexOf('/') != -1) {            // If it has slashes try the name as-is first
				//-- Try literal name
				IResourceRef ref = tryRef(app, identifier, m_dependencyList);
				if(null != ref) {
					m_map.put(identifier, new Line(identifier, ref));
					return createSource(ref, identifier);
				}

				//-- Try for a "partial"
				String newName = "_" + identifier;
				ref = tryRef(app, newName, m_dependencyList);
				if(null != ref) {
					m_map.put(identifier, new Line(newName, ref));
					return createSource(ref, newName);
				}
			}

			//-- Try to prefix the relative path from its parent
			String newName = fileBase + "/_" + identifier;                    // Get new path relative to parent
			IResourceRef ref = tryRef(app, newName, m_dependencyList);
			if(null != ref) {
				m_map.put(identifier, new Line(newName, ref));
				return createSource(ref, newName);
			}

			//-- Try for a non partial
			newName = fileBase + "/" + identifier;
			ref = tryRef(app, newName, m_dependencyList);
			if(null != ref) {
				m_map.put(identifier, new Line(newName, ref));
				return createSource(ref, newName);
			}
			System.out.println(original + " - FAILED");
			return null;                                // Not found
		} finally {
			ts = System.nanoTime() - ts;
			System.out.println("$$ scss resolve path '" + original + " in " + StringTool.strNanoTime(ts)); // + "', parenturis=" + sourceUris);
			m_resolveTime += ts;
		}
	}

	public void close() {
		System.out.println("$$ scss total resolve time " + StringTool.strNanoTime(m_resolveTime));
	}

	private InputSource createParameterFile() {
		String parameters = calculateParameterFile();
		InputSource is = new InputSource(new StringReader(parameters));
		is.setURI("_parameter.scss");
		return is;
	}

	@Nonnull private String calculateParameterFile() {
		String pf = m_parameterFile;
		if(null == pf) {

			StringBuilder sb = new StringBuilder();
			for(String name : m_params.getParameterNames()) {
				String[] values = m_params.getParameters(name);
				if(null != values && values.length == 1) {
					String value = values[0];
					if(!StringTool.isNumber(value)) {
						value = StringTool.strToJavascriptString(value, true);
					}
					sb.append("$").append(name).append(": ").append(value).append(";\n");
				}
			}
			pf = m_parameterFile = sb.toString();
		}

		return pf;
	}

	@Nonnull
	private InputSource createSource(@Nonnull IResourceRef ref, @Nonnull String name) {
		try {
			InputSource inputSource = new InputSource(new InputStreamReader(ref.getInputStream(), "utf-8"));
			inputSource.setURI(name);
			return inputSource;
		} catch(Exception x) {
			throw WrappedException.wrap(x);
		}
	}

	private IResourceRef tryRef(DomApplication app, String name, IResourceDependencyList deplist) {
		try {
			IResourceRef ref = app.getResource(name, deplist);
			if(!ref.exists()) {
				//System.out.print("     try " + name + " failed");
				return null;
			}
			deplist.add(ref);
			//System.out.print("     try " + name + " found");
			return ref;
		} catch(Exception x) {
			return null;
		}
	}
}
