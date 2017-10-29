package to.etc.domui.sass;

import io.bit3.jsass.importer.Import;
import io.bit3.jsass.importer.Importer;
import to.etc.domui.parts.ParameterInfoImpl;
import to.etc.domui.server.DomApplication;
import to.etc.domui.trouble.ThingyNotFoundException;
import to.etc.domui.util.resources.IResourceDependencyList;
import to.etc.domui.util.resources.IResourceRef;
import to.etc.util.FileTool;
import to.etc.util.StringTool;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 29-10-17.
 */
class JSassResolver implements Importer {
	private final ParameterInfoImpl m_params;

	private long m_resolveTime;

	private final String m_basePath;

	private final IResourceDependencyList m_dependencyList;

	private static class Line {
		private final String m_name;

		private final Import m_ref;

		public Line(String name, Import ref) {
			m_name = name;
			m_ref = ref;
		}

		public String getName() {
			return m_name;
		}

		public Import getRef() {
			return m_ref;
		}
	}

	final private Map<String, Line> m_map = new HashMap<>();


	private File m_parameterFile;

	public JSassResolver(ParameterInfoImpl params, String basePath, IResourceDependencyList rdl) {
		m_params = params;
		m_basePath = basePath;
		m_dependencyList = rdl;
	}

	@Override public Collection<Import> apply(String s, Import anImport) {
		Import resolve = resolve(s, anImport.getAbsoluteUri().toString());
		if(null == resolve)
			throw new RuntimeException(s + ": sass import not found");
		return Arrays.asList(resolve);
	}

	public Import resolve(String original, String fileBase) {
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

			//if(identifier.equals("_parameters.scss") || identifier.equals("parameters.scss")) {
			//	return createParameterFile();
			//}
			List<String> sourceUris = Collections.emptyList(); // parentStylesheet.getSourceUris();
			DomApplication app = DomApplication.get();

			if(identifier.indexOf('/') != -1) {            // If it has slashes try the name as-is first
				//-- Try literal name
				Import res = tryRef(app, identifier);
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
			Import ref = tryRef(app, newName);
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

	private Import tryRef(DomApplication app, String name) {
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
			Import imp = new Import(name, name, content);
			m_map.put(name, new Line(name, imp));
			return imp;
		} catch(ThingyNotFoundException tnf) {				// Normal exception if resource cannot be located.
			return null;
		} catch(Exception x) {
			x.printStackTrace();
			return null;
		}
	}


	//private InputSource createParameterFile() {
	//	String parameters = calculateParameterFile();
	//	InputSource is = new InputSource(new StringReader(parameters));
	//	is.setURI("_parameter.scss");
	//	return is;
	//}

	@Nonnull private File calculateParameterFile() throws IOException {
		File pf = m_parameterFile;
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
			pf = m_parameterFile = File.createTempFile("sass-params-", ".scss");
		}
		return pf;
	}

	public void close() {
		System.out.println("$$ scss total resolve time " + StringTool.strNanoTime(m_resolveTime));
	}

}
