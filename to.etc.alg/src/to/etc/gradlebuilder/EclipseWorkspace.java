package to.etc.gradlebuilder;

import java.io.*;
import java.util.*;

import javax.annotation.*;

import to.etc.util.*;

/**
 * This creates a simple model of an Eclipse workspace, either split or normal.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 1, 2014
 */
public class EclipseWorkspace {
	private Map<String, EclipseProject> m_projectMap = new HashMap<String, EclipseProject>();

	@Nonnull
	final private File m_root;

	private EclipseWorkspace(@Nonnull File root) {
		m_root = root;
	}

	@Nonnull
	static public EclipseWorkspace create(@Nonnull File root) throws Exception {
		EclipseWorkspace ws = new EclipseWorkspace(root);
		ws.load();
		return ws;
	}

	private void load() throws Exception {
		loadProjectMap(m_root, 2);
		for(EclipseProject ep : m_projectMap.values()) {
			ep.load();
		}
		for(EclipseProject ep : m_projectMap.values()) {
			ep.generateDependencies(new HashSet<EclipseProject>());
		}
	}

	@Nonnull
	public File getRoot() {
		return m_root;
	}

	@Nonnull
	public List<EclipseProject> getAllProjects() {
		return new ArrayList<EclipseProject>(m_projectMap.values());
	}

	@Nullable
	public EclipseProject	findProject(@Nonnull String projectName) {
		return m_projectMap.get(projectName);
	}

	/**
	 * Recursively find all folders with .classpath and .project files, until a max depth of 2.
	 * @param res
	 * @param dir
	 * @param depth
	 */
	private void loadProjectMap(@Nonnull File dir, int depthLeft) throws Exception {
		if(depthLeft <= 0)
			return;

		File[] far = dir.listFiles();
		if(null == far)
			return;

		Arrays.sort(far, new Comparator<File>() {
			@Override
			public int compare(File a, File b) {
				return a.getName().compareToIgnoreCase(b.getName());
			}
		});

		for(File f : far) {
			if(f.isDirectory()) {
				EclipseProject ep = loadProject(f);
				if(null != ep) {
					if(!m_projectMap.containsKey(f.getName())) {
						m_projectMap.put(f.getName(), ep);
					}
				} else {
					loadProjectMap(f, depthLeft - 1);
				}
			}
		}
	}

	@Nullable
	private EclipseProject loadProject(@Nonnull File dir) throws Exception {
		String name = dir.getName().toLowerCase();
		File classfile = new File(dir, ".classpath");
		File prjfile = new File(dir, ".project");

		if(!classfile.isFile() || !classfile.exists() || !prjfile.isFile() || !prjfile.exists())
			return null;

		//-- Prepare groupid and stuff.
		String base = name;
		String baseGroup = "nl.itris";
		if(base.startsWith("bin-") || base.startsWith("lib-")) {
			base = base.substring(4);							// Cut off the bin/lib prefix.
			baseGroup = "nl.itris.external";
		} else if(base.startsWith("to."))
			baseGroup = "to.etc";

		//-- Start cutting off version numbers.
		String basename = removeVersionFromDir(base);
		basename = generateUniqueName(basename);
		String encoding = loadEncoding(dir);

		return new EclipseProject(this, name, dir, baseGroup, basename, encoding);
	}

	private boolean isProject(@Nonnull File f) {
		File cp = new File(f, ".classpath");
		if(!cp.exists() || !cp.isFile())
			return false;
		cp = new File(f, ".project");
		return cp.exists() && cp.isFile();
	}

	@Nonnull
	static private String loadEncoding(@Nonnull File dir) throws Exception {
		File enc = new File(dir, ".settings/org.eclipse.core.resources.prefs");
		String encoding = null;
		if(enc.exists()) {
			Properties encp = FileTool.loadProperties(enc);
			encoding = encp.getProperty("encoding/<project>");
		}
		if(encoding == null)
			encoding = "UTF-8";
		return encoding;
	}

	@Nonnull
	final static private Set<String>	m_knownNameSet	= new HashSet<String>();

	@Nonnull
	static public String generateUniqueName(@Nonnull String bn) {
		//-- Start cutting off version numbers.
		int cn = 1;
		String name = bn;
		for(;;) {
			if(!m_knownNameSet.contains(name))
				break;
			name = bn + (cn++);
		}
		m_knownNameSet.add(name);
		return name;
	}

	@Nonnull
	static public String removeVersionFromDir(@Nonnull String in) {
		String[] ar = in.split("-");
		if(ar == null)
			return in;

		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < ar.length; i++) {
			if(!isDottedVersion(ar[i])) {
				if(sb.length() != 0)
					sb.append('-');
				sb.append(ar[i]);
			}
		}
		return sb.toString();
	}

	@Nonnull
	static private String removeVersionFromFile(@Nonnull String in) {
		int d = in.lastIndexOf('.');
		String ext;
		if(d != -1) {
			ext = in.substring(d + 1);
			if(!StringTool.isNumber(ext)) {
				in = in.substring(0, d);
				ext = in.substring(d); // Include dot
			} else
				ext = "";
		} else
			ext = "";

		String[] ar = in.split("-");
		if(ar == null)
			return in;

		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < ar.length; i++) {
			if(!isDottedVersion(ar[i])) {
				if(sb.length() != 0)
					sb.append('-');
				sb.append(ar[i]);
			}
		}
		sb.append(ext);
		return sb.toString();
	}

	static private boolean isDottedVersion(@Nonnull String string) {
		string = string.replace("rc", ".");

		String[] dots = string.split("\\.");
		if(dots == null) {
			dots = new String[]{string};
		}
		for(String s : dots) {
			if(!StringTool.isNumber(s))
				return false;
		}
		return true;
	}

}
