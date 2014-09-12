package to.etc.gradlebuilder;

import java.io.*;
import java.util.*;

import javax.annotation.*;

import org.w3c.dom.*;

import to.etc.util.*;
import to.etc.xml.*;

/**
 * Encapsulates a EclipseProject as read from the Eclipse configuration.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 5, 2013
 */
class EclipseProject {
	@Nonnull
	private final File m_root;

	@Nonnull
	private final String m_baseName;

	@Nonnull
	private final String m_baseGroup;

	@Nonnull
	private final String m_name;

	@Nonnull
	private final String m_encoding;

	/** T if there are actually source files in the EclipseProject that need a compile. */
	private boolean m_hasSources;

	private boolean m_dependenciesDone;

	private String m_groupId;

	private final String m_sourceArtefactId;

	@Nonnull
	private String m_sourceVersion = "1.6";

	@Nonnull
	private String m_targetVersion = "1.6";

	private final List<String> m_sourceList = new ArrayList<String>();

	private final List<EclipseProject> m_directDepList = new ArrayList<EclipseProject>();

	private final List<JarRef> m_jarList = new ArrayList<JarRef>();

	private final List<EclipseProject> m_fullDepList = new ArrayList<EclipseProject>();

	private String m_relativeOutputPath;

	@Nonnull
	final private EclipseWorkspace m_workspace;

	static class JarRef {
		private final String m_jarName;

		private final File m_jar;

		String m_groupId;

		String m_artefactId;

		String m_pomDir;

		private boolean m_exported;

		public JarRef(@Nonnull File jar, @Nonnull String jarName, boolean exported) {
			m_jar = jar;
			m_jarName = jarName;
			m_exported = exported;
		}

		public boolean isExported() {
			return m_exported;
		}

		@Nonnull
		public File getJar() {
			return m_jar;
		}

		@Nonnull
		public String getJarName() {
			return m_jarName;
		}
	}

	EclipseProject(@Nonnull EclipseWorkspace b, @Nonnull String name, @Nonnull File root, @Nonnull String baseGroup, @Nonnull String baseName, @Nonnull String encoding) {
		m_workspace = b;
		m_root = root;
		m_name = name;
		m_baseName = baseName;
		m_baseGroup = baseGroup;
		m_encoding = encoding;
		m_sourceArtefactId = getBaseName() + ".source";
	}

	/**
	 * Fully resolve the project.
	 * @throws Exception
	 */
	void load() throws Exception {
		loadClassFile();
		loadJavaPreferences();
		checkSources();
	}

	@Nonnull
	public File getOutputDirectory() {
		return new File(m_root, m_relativeOutputPath);
	}

	private void checkSources() throws Exception {
		for(String srcref : m_sourceList) {
			File path = new File(m_root, srcref);
			if(path.exists() && path.isDirectory()) {
				int nf = countFiles(path, 3);
				if(nf >= 3) {
					m_hasSources = true;
					return;
				}
			}
		}
	}

	private int countFiles(@Nonnull File root, int max) {
		File[] ar = root.listFiles();
		int count = 0;
		for(int i = ar.length; --i >= 0;) {
			File f = ar[i];
			if(f.isFile())
				count++;
			if(count >= max)
				return count;
		}
		for(int i = ar.length; --i >= 0;) {
			File f = ar[i];
			if(f.isDirectory()) {
				int tc = countFiles(f, max - count);
				count += tc;
				if(count >= max)
					return count;
			}
		}
		return count;
	}

	private void loadJavaPreferences() throws Exception {
		File enc = new File(getRoot(), ".settings/org.eclipse.jdt.core.prefs");
		if(enc.exists()) {
			Properties pf = FileTool.loadProperties(enc);
			String s = pf.getProperty("org.eclipse.jdt.core.compiler.compliance");
			if(s != null)
				setSourceVersion(s);
			s = pf.getProperty("org.eclipse.jdt.core.compiler.codegen.targetPlatform");
			if(s != null)
				setTargetVersion(s);
		}
	}

	private void loadClassFile() throws Exception {
		File classfile = new File(m_root, ".classpath");
		Document doc = DomTools.getDocument(classfile, false);
		Node root = DomTools.getRootElement(doc);
		DOMDecoder d = new DOMDecoder(root);
		for(DOMDecoder child : d.getChildIterator()) {
			if(child.getCurrentRoot().getNodeName().equals("classpathentry")) {
				Node n = child.getCurrentRoot();

				String kind = DomTools.strAttr(n, "kind");
				if("src".equals(kind)) {
					String path = DomTools.strAttr(n, "path");
					if(path.startsWith("/")) {
						String projectName = path.substring(1);
						EclipseProject ep = m_workspace.findProject(projectName);
						if(null == ep)
							throw new EclipseWorkspaceException("Project " + this + ": .classpath contains unknown project '" + projectName + "'");
						getDirectDepList().add(ep);
					} else {
						getSourceList().add(path);
					}
				} else if("lib".equals(kind)) {
					boolean exported = "true".equals(DomTools.strAttr(n, "exported", null));

					String path = DomTools.strAttr(n, "path");
					File jar;
					if(path.startsWith("/")) {
						if(true)
							throw new IllegalStateException("Link to external jar not allowed in " + getName() + ": " + path);
						jar = new File(m_workspace.getRoot(), path.substring(1));
					} else
						jar = new File(getRoot(), path);
					if(!jar.exists() || !jar.isFile()) {
						System.out.println("Ignoring nonexistent " + jar);
					} else {
						JarRef j = new JarRef(new File(getRoot(), path), path, exported);
						getJarList().add(j);
					}
				} else if("output".equals(kind)) {
					m_relativeOutputPath = DomTools.strAttr(n, "path");
				}
			}
		}
	}

	void generateDependencies(@Nonnull HashSet<EclipseProject> stack) {
		if(m_dependenciesDone)
			return;

		if(stack.contains(this))
			throw new IllegalStateException("Circular dependency on " + getName());

		//--Depth-1st traversal of all dependencies
		stack.add(this);
		for(EclipseProject sub : getDirectDepList()) {
			sub.generateDependencies(stack);
			addNewTo(m_fullDepList, sub.m_fullDepList);
			if(!m_fullDepList.contains(sub))
				m_fullDepList.add(sub);
		}
		stack.remove(this);
		m_dependenciesDone = true;
	}

	static private void addNewTo(@Nonnull List<EclipseProject> target, @Nonnull List<EclipseProject> src) {
		for(EclipseProject p : src) {
			if(!target.contains(p))
				target.add(p);
		}
	}

	@Nonnull
	public File getRoot() {
		return m_root;
	}

	@Nonnull
	public String getBaseName() {
		return m_baseName;
	}

	@Nonnull
	public String getBaseGroup() {
		return m_baseGroup;
	}

	@Nonnull
	public String getName() {
		return m_name;
	}

	@Nonnull
	public String getEncoding() {
		return m_encoding;
	}

	@Nonnull
	public String getSourceVersion() {
		return m_sourceVersion;
	}

	public void setSourceVersion(@Nonnull String sourceVersion) {
		m_sourceVersion = sourceVersion;
	}

	@Nonnull
	public String getTargetVersion() {
		return m_targetVersion;
	}

	public void setTargetVersion(@Nonnull String targetVersion) {
		m_targetVersion = targetVersion;
	}

	@Nonnull
	public List<String> getSourceList() {
		return m_sourceList;
	}

	@Nonnull
	public List<EclipseProject> getDirectDepList() {
		return m_directDepList;
	}

	@Nonnull
	public List<JarRef> getJarList() {
		return m_jarList;
	}

	@Nonnull
	public List<EclipseProject> getFullDepList() {
		return new ArrayList<EclipseProject>(m_fullDepList);
	}

	@Nonnull
	public String getSourceArtefactId() {
		if(!hasSources())
			throw new IllegalStateException("There should not be an artifact ID for a module without sources (module " + getName() + ")");
		return m_sourceArtefactId;
	}

	public boolean hasSources() {
		return m_hasSources;
	}

	@Nonnull
	public String getEclipseProjectArtifactID() {
		return getBaseName() + ".BasePOM";
	}

	public List<File> getFullClassPath() {
		List<File> all = new ArrayList<File>();
		for(EclipseProject ep : getFullDepList()) {
			ep.appendClassPath(all);
		}
		appendClassPath(all);
		return all;
	}

	public void appendFullClassPath(@Nonnull List<File> all) {
		for(EclipseProject ep : getFullDepList()) {
			ep.appendClassPath(all);
		}
		appendClassPath(all);
	}

	public void appendClassPath(@Nonnull List<File> all) {
		//-- Add all my outputs
		conditionalAdd(all, getOutputDirectory());

		//-- My jars
		for(JarRef jr : getJarList()) {
			conditionalAdd(all, jr.getJar());
		}
	}

	private void conditionalAdd(@Nonnull List<File> all, @Nonnull File outputDirectory) {
		if(!all.contains(outputDirectory))
			all.add(outputDirectory);
	}

}
