package to.etc.domui.sass;

import to.etc.domui.parts.ParameterInfoImpl;
import to.etc.domui.server.DomApplication;
import to.etc.domui.trouble.ThingyNotFoundException;
import to.etc.domui.util.resources.IResourceDependencyList;
import to.etc.domui.util.resources.IResourceRef;
import to.etc.util.FileTool;
import to.etc.util.StringTool;
import to.etc.util.WrappedException;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 29-10-17.
 */
abstract public class AbstractSassResolver<O> {
	private final ParameterInfoImpl m_params;

	private final IResourceDependencyList m_dependencyList;

	private long m_resolveTime;

	private static class Line<O> {
		private final String m_name;

		private final O m_ref;

		public Line(String name, O ref) {
			m_name = name;
			m_ref = ref;
		}

		public String getName() {
			return m_name;
		}

		public O getRef() {
			return m_ref;
		}
	}


	final private Map<String, Line<O>> m_map = new HashMap<>();

	private O m_parameterFile;

	public AbstractSassResolver(ParameterInfoImpl params, IResourceDependencyList dependencyList) {
		m_params = params;
		m_dependencyList = dependencyList;
	}

	public O resolve(String original, String parentFile) {
		int ixof = parentFile.lastIndexOf("/");

		String fileBase = ixof > 0 ? parentFile.substring(0, ixof) : "";		// Base directory exclusive final slash

		long ts = System.nanoTime();
		try {
			//System.out.println("base = " + fileBase);

			//-- Make sure we have a suffix
			String identifier = original;
			if(!identifier.endsWith(".scss") && ! identifier.endsWith(".sass") && ! identifier.endsWith(".css")) {
				boolean isSass = parentFile.toLowerCase().endsWith(".sass");
				identifier += isSass ? ".sass" : ".scss";
			}

			if(identifier.equals("_parameters.scss") || identifier.equals("parameters.scss") || identifier.equals("_parameters.sass") || identifier.equals("parameters.sass")) {
				return calculateParameterFile();
			}
			List<String> sourceUris = Collections.emptyList(); // parentStylesheet.getSourceUris();
			DomApplication app = DomApplication.get();

			if(identifier.indexOf('/') != -1) {            // If it has slashes try the name as-is first
				//-- Try literal name
				O res = tryRef(app, identifier);
				if(null != res)
					return res;

				//-- Try for a "partial"
				String newName = "_" + identifier;
				res = tryRef(app, newName);
				if(null != res) {
					return res;
				}
			}

			//-- Try to prefix the relative path from its parent
			String newName = fileBase + "/_" + identifier;                    // Get new path relative to parent
			O ref = tryRef(app, newName);
			if(null != ref) {
				return ref;
			}

			//-- Try for a non partial
			newName = fileBase + "/" + identifier;
			ref = tryRef(app, newName);
			if(null != ref) {
				return ref;
			}
			System.out.println(original + " - FAILED");
			return null;                                // Not found
		} finally {
			ts = System.nanoTime() - ts;
			System.out.println("$$ scss resolve path '" + original + " in " + StringTool.strNanoTime(ts)); // + "', parenturis=" + sourceUris);
			m_resolveTime += ts;
		}
	}

	private O tryRef(DomApplication app, String name) {
		try {
			IResourceRef ref = app.getResource(name, m_dependencyList);
			if(!ref.exists()) {
				//System.out.print("     try " + name + " failed");
				return null;
			}
			m_dependencyList.add(ref);
			String content;
			try(InputStream is = ref.getInputStream()) {
				content = FileTool.readStreamAsString(is, "utf-8");
			}
			O imp = createInput(name, content);
			m_map.put(name, new Line<>(name, imp));
			return imp;
		} catch(ThingyNotFoundException tnf) {				// Normal exception if resource cannot be located.
			return null;
		} catch(Exception x) {
			x.printStackTrace();
			return null;
		}
	}

	abstract protected O createInput(String path, String data);

	@Nonnull private O calculateParameterFile() {
		O pf = m_parameterFile;
		if(null == pf) {
			try {
				String parameters = generateParameterFile();
				pf = m_parameterFile = createInput("_parameters.scss", parameters);
			} catch(Exception x) {
				throw WrappedException.wrap(x);
			}
		}
		return pf;
	}

	public void close() {
		System.out.println("$$ scss total resolve time " + StringTool.strNanoTime(m_resolveTime));
	}

	protected String generateParameterFile() {
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
		return sb.toString();
	}
}
