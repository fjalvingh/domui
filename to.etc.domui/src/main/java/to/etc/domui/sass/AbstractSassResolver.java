package to.etc.domui.sass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 29-10-17.
 */
abstract public class AbstractSassResolver<O> {
	static private final Logger LOG = LoggerFactory.getLogger(AbstractSassResolver.class);

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
			DomApplication app = DomApplication.get();

			//-- Split identifier in path and last name
			String idPath;
			String idName;

			int pos = identifier.lastIndexOf('/');
			if(pos < 0) {
				idPath = "";
				idName = identifier;
			} else {
				idPath = identifier.substring(0, pos + 1);
				idName = identifier.substring(pos + 1);
			}

			if(identifier.startsWith("/")) {
				//-- Root based path -> no relative replacements.
				//-- Try literal name
				O res = tryRef(app, idPath + idName);
				if(null != res)
					return res;

				//-- Try for a "partial"
				String newName = idPath + "_" + idPath;
				res = tryRef(app, newName);
				if(null != res) {
					return res;
				}
				return null;
			}

			//-- If we have a path: prepare the absolute path
			String absPath = fileBase;

			if(idPath.length() > 0) {
				String[] segs = idPath.split("/");
				for(int i = 0; i < segs.length; i++) {
					String seg = segs[i];
					if(seg.equals("..")) {
						//-- remove one segment from basePath, if still possible.
						if(absPath.length() == 0)		// Cannot go higher
							return null;

						int slp = absPath.lastIndexOf('/');
						if(slp == -1) {
							absPath = "";
						} else {
							absPath = absPath.substring(0, slp);		// Strip off last segment
						}
					} else if(seg.equals(".")) {
						// Ignore
					} else {
						if(absPath.length() > 0) {
							absPath += "/" + seg;
						} else {
							absPath = seg;
						}
					}
				}
			}

			//-- Try to prefix the relative path from its parent
			String newName = absPath + "/" + "_" + idName;		// Get new path relative to parent
			O ref = tryRef(app, newName);
			if(null != ref) {
				return ref;
			}

			//-- Try for a non partial
			newName = absPath + "/" + idName;
			ref = tryRef(app, newName);
			if(null != ref) {
				return ref;
			}
			System.out.println("SCSS resolve for " + original + " - FAILED");
			return null;                                // Not found
		} finally {
			ts = System.nanoTime() - ts;
			LOG.info("scss resolve path '" + original + " in " + StringTool.strNanoTime(ts)); // + "', parenturis=" + sourceUris);
			m_resolveTime += ts;
		}
	}

	private O tryRef(DomApplication app, String name) {
		Line<O> line = m_map.get(name);
		if(null != line) {
			return line.getRef();
		}

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
		LOG.info("scss total resolve time " + StringTool.strNanoTime(m_resolveTime));
	}

	protected String generateParameterFile() {
		StringBuilder sb = new StringBuilder();
		for(String name : m_params.getParameterNames()) {
			String[] values = m_params.getParameters(name);
			if(null != values && values.length == 1) {
				String value = values[0];
				if(isString(value)) {
					value = StringTool.strToJavascriptString(value, true);
				}
				sb.append("$").append(name).append(": ").append(value).append(";\n");
			}
		}
		return sb.toString();
	}

	static private boolean isString(String value) {
		if(StringTool.isNumber(value))
			return false;
		if("true".equals(value) || "false".equals(value))
			return false;
		return true;
	}
}
