package to.etc.pombuilder;

import java.io.*;
import java.util.*;

import javax.annotation.*;

import org.w3c.dom.*;

import to.etc.pombuilder.PomBuilder.JarRef;
import to.etc.util.*;
import to.etc.xml.*;

public class PomBuilder {
	static private final String MAVENSHIT = ".maven";

	static private final String	PARENT_DIRECTORY		= "../";

	File								m_rootPath;

	private String m_version = "trunk-SNAPSHOT";

	@Nonnull
	final static private Set<String>	m_knownNameSet		= new HashSet<String>();

	static class JarRef {
		String	m_jarName;
		File	m_jar;

		String m_groupId;

		String m_artefactId;

		String m_pomDir;
	}

	private Map<String, Project> m_prjMap = new HashMap<String, Project>();

	public static void main(String[] args) {
		try {
			new PomBuilder().run(args);
		} catch(Exception x) {
			x.printStackTrace();
		}
	}

	private void	run(String[] args) throws Exception {
		File prj = args.length >= 1 ? new File(args[0]) : findViewpointProjectPath(PARENT_DIRECTORY);

		//File prj = new File(args[0]);
		m_rootPath = prj.getParentFile();
		Project p = loadProject(prj);
		p.generateDependencies(new HashSet<Project>());
		//		testClassFile(new File(prj, ".classpath"));

		System.out.println("Build order:");
		for(Project sub : p.m_fullDepList) {
			System.out.println(sub.getName());
		}
		for(Project sub : p.m_fullDepList) {
			System.out.println("... " + sub.getName());
			generateProjectPoms(sub);
		}
		generateProjectPoms(p); // And for vp itself

		//-- Create root
		File f = new File(m_rootPath, "pom.xml");
		XmlWriter w = createMavenXml(f, "nl.itris.viewpoint", "viewpointModuleSet", "viewpointModuleSet", "pom");

		w.tagendnl();				// plugin
		w.tagendnl();				// plugins

		w.tag("modules");
		//		int count = 0;
		for(Project sub : p.m_fullDepList) {
			w.tagfull("module", sub.getRoot().getName());
			//			if(sub.m_sourceList.size() > 0 && count++ > 4)
			//				break;
		}
		w.tagfull("module", p.getRoot().getName());

		w.tagendnl();
		w.tagendnl();
		w.close();
		System.out.println("done");
	}

	private void generateWarConfig(XmlWriter w) throws IOException {
		w.tag("plugins");
		w.tag("plugin");
		w.tagfull("groupId", "org.apache.maven.plugins");
		w.tagfull("artifactId", "maven-war-plugin");
		w.tagfull("version", "2.1-beta-1");
		w.tag("configuration");
		w.tagfull("webappDirectory", "WebContent");
		w.tagendnl();				// configuration
	}

	/**
	 * Try to find viewpoint project in regular workspace and if project
	 * is not found do one more try in split workspace setup.
	 *
	 * @param rootPath String
	 *
	 * @return Directory of viewpoint
	 *
	 * @throws FileNotFoundException if project cannot be found
	 */
	private File findViewpointProjectPath(@Nonnull String rootPath) throws FileNotFoundException {
		File root = new File(rootPath);

		try {
			return findViewpointProjectPathInDirectory(root);
		} catch(FileNotFoundException ex) {
			File rootParent = new File(PARENT_DIRECTORY + rootPath);
			File[] fileList = rootParent.listFiles();

			for(File subdirectory : fileList) {
				try {
					return findViewpointProjectPathInDirectory(subdirectory);
				} catch(FileNotFoundException exc) {
					//do nothing, skip it since it will throw FileNotFoundException which is valid search
				}
			}
		}

		throw new FileNotFoundException("Viewpoint project directory cannot be found.");
	}

	private File findViewpointProjectPathInDirectory(@Nonnull File directory) throws FileNotFoundException {
		if(directory.isDirectory()) {
			File[] fileList = directory.listFiles();

			if(fileList != null && fileList.length > 0) {
				for(File viewpointDirectory : fileList) {
					if(viewpointDirectory.isDirectory() && "viewpoint".equals(viewpointDirectory.getName())) {
						return viewpointDirectory;
					}
				}
			}
		}

		throw new FileNotFoundException("Viewpoint project directory cannot be found in directory: " + directory.getName());
	}



	private String removeVersionFromFile(String in) {
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

	/*
	 * <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
	">http://maven.apache.org/maven-v4_0_0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<groupId>itris</groupId>
	<artifactId>commons-logging</artifactId>
	<name>commons-logging</name>
	<version>1.0.4</version>
	<packaging>jar</packaging>
	<build>
	<plugins>
	  <plugin>
	    <artifactId>maven-antrun-plugin</artifactId>
	    <executions>
	      <execution>
	        <id>trick maven into installing the binary jar</id>
	        <phase>verify</phase> <!-- runs after packaging, before
	        install, exactly what we need... -->
	        <goals>
	          <goal>run</goal>
	        </goals>
	        <configuration>
	          <tasks>
	             <copy overwrite="true" file="commons-logging.jar"
	             tofile="${project.build.directory}/${project.artifactId}-${project.version}.jar"/>
	          </tasks>
	        </configuration>
	      </execution>
	    </executions>
	  </plugin>
	</plugins>
	</build>
	</project>
	 */


	/**
	 *
	 */
	private void generateProjectPoms(Project sub) throws Exception {
		//-- Generate poms for all jars in this project
		for(JarRef jr : sub.m_jarList)
			generateJar(sub, jr);

		//-- Generate master pom referring all these modules.
		File f = new File(sub.getRoot(), "pom.xml");
		XmlWriter w = createMavenXml(f, sub.getBaseGroup(), sub.getBaseName() + ".BasePOM", sub.getName(), "pom");

		w.tag("modules");
		for(JarRef jr : sub.m_jarList) {
			w.tagfull("module", jr.m_pomDir);
		}

		if(sub.m_sourceList.size() > 0) {
			String srcmod = generateSourceBuild(sub);

			if(srcmod != null)
				w.tagfull("module", srcmod);
		}

		w.tagendnl();
		w.tagendnl();
		w.close();
	}

	private String generateSourceBuild(Project sub) throws Exception {
		File mv = new File(sub.getRoot(), MAVENSHIT);
		mv.mkdirs();

		File f = new File(mv, "source");
		f.mkdirs();
		f = new File(f, "pom.xml");
		sub.m_groupId = sub.getBaseGroup();
		sub.m_artefactId = sub.getBaseName() + ".source";

		XmlWriter w = createMavenXml(f, sub.getBaseGroup(), sub.m_artefactId, sub.getName() + " source", "jar");

		w.tag("properties");
		w.tagfull("cobertura.maxmem", "8292M");
		w.tagfull("maven.cobertura.instrumentation.maxmemory", "8192M");
		w.tagfull("project.build.sourceEncoding", sub.getEncoding());
		w.tagfull("maven.compile.encoding", sub.getEncoding());
		w.tagfull("maven.compile.fork", "true"); // Split compile in separate task to prevent memory leaks in javac
		w.tagendnl();

		w.tag("build");

		//-- Separate source and test source paths into separate lists
		List<String> sourceList = new ArrayList<String>();
		List<String> testList = new ArrayList<String>();
		boolean hastests = false;

		//-- Generate source
		if(sub.m_sourceList.size() > 0) {
			//-- Create a SOURCE and a TEST list.
			for(String src : sub.m_sourceList) {
				if(src.endsWith("test") || src.endsWith("test/"))
					testList.add(src);
				else
					sourceList.add(src);
			}

			//-- Generate the 1st source dir as the "sourceDirectory" if present, and remove;
			if(sourceList.size() > 0) {
				String ms = sourceList.remove(0); // Remove from todo list,
				w.tagfull("sourceDirectory", "../../" + ms);
			}

			if(testList.size() > 0) {
				String ms = testList.remove(0); // Remove from todo list,
				w.tagfull("testSourceDirectory", "../../" + ms);

				hastests = true;
				if(testList.size() > 0)
					throw new RuntimeException(sub + ": too many 'test' source paths.");
			}

			//-- Resources; thing is too stupid to do it by itself.
			w.tag("resources");
			for(String src : sub.m_sourceList) {
				w.tag("resource");

				w.tagfull("directory", "../../" + src);
				w.tag("excludes");
				w.tagfull("exclude", "**/*.java");
				w.tagendnl(); // excludes, sigh.
				w.tagendnl(); //resource
			}
			w.tagendnl(); // resources
		}

		w.tag("plugins");
		w.tag("plugin");

		w.tagfull("groupId", "org.apache.maven.plugins");
		w.tagfull("artifactId", "maven-compiler-plugin");
		w.tagfull("version", "2.5.1");
		w.tag("configuration");
		w.tagfull("compilerId", "groovy-eclipse-compiler");
		w.tagfull("source", sub.getSourceVersion());
		w.tagfull("target", sub.getSourceVersion()); // sub.m_targetVersion);
		w.tagfull("encoding", sub.getEncoding());
		w.tagendnl(); // configuration

		w.tag("dependencies");
		w.tag("dependency");
		w.tagfull("groupId", "org.codehaus.groovy");
		w.tagfull("artifactId", "groovy-eclipse-compiler");
		w.tagfull("version", "2.7.0-01");
		w.tagendnl(); // dependency
		w.tagendnl(); //dependencies


		w.tagendnl(); //plugin

		//-- Set FindBugs timeout, in only 8 lines!!!! Great!
		w.tag("plugin");

		//       <plugin>
		//        <groupId>org.codehaus.mojo</groupId>
		//        <artifactId>findbugs-maven-plugin</artifactId>
		//        <version>2.3.1</version>
		//        <configuration>
		//          <timeout>60000000</timeout>
		//        </configuration>
		//      </plugin>
		w.tagfull("groupId", "org.codehaus.mojo");
		w.tagfull("artifactId", "findbugs-maven-plugin");
		w.tagfull("version", "2.3.1");
		w.tag("configuration");
		w.tagfull("timeout", "60000000");
		w.tagendnl();

		w.tagendnl(); // plugin

		// Get this idiot piece of shit to find testcases.
		//      <plugin>
		//        <groupId>org.apache.maven.plugins</groupId>
		//        <artifactId>maven-surefire-plugin</artifactId>
		//        <configuration>
		//          <testClassesDirectory>../../bin</testClassesDirectory>
		//        </configuration>
		//      </plugin>

		if(hastests) {
			w.tag("plugin");
			w.tagfull("groupId", "org.apache.maven.plugins");
			w.tagfull("artifactId", "maven-surefire-plugin");
			w.tagfull("version", "2.14.1");

			w.tag("configuration");
			w.tagfull("argLine", "-Xmx2048m -XX:MaxPermSize=1024m");
			w.tagendnl();

			w.tag("dependencies");
			w.tag("dependency");
			w.tagfull("groupId", "org.apache.maven.surefire");
			w.tagfull("artifactId", "surefire-junit4");
			w.tagfull("version", "2.14.1");
			w.tagendnl();
			w.tagendnl();

//			w.tag("configuration");
//			w.tagfull("testClassesDirectory", "../../bin");
//			w.tagendnl();

			w.tagendnl(); // plugin
		}

		generateExtraSourcesPukefest(w, sourceList);

		w.tagendnl(); // plugins


		w.tagendnl();


		//--dependencies in downward projects
		w.tag("dependencies");
		generateJarDeps(w, sub.m_jarList);
		for(Project p : sub.m_fullDepList) {
			generateJarDeps(w, p.m_jarList);

			//-- If this project has sources add the src jar dep
			if(p.m_sourceList.size() > 0) {
				//-- Generate the 1st source dir in the usualway
				w.tag("dependency");
				w.tagfull("groupId", p.m_groupId);
				w.tagfull("artifactId", p.m_artefactId);
				w.tagfull("version", m_version);
				w.tagfull("type", "jar");
				w.tagfull("scope", "compile");

				w.tagendnl();
			}

		}
		w.tagendnl(); //dependencies

		w.tagendnl();
		w.close();

		return MAVENSHIT + "/source";
	}

	private void generateJarDeps(XmlWriter w, List<JarRef> list) throws Exception {
		for(JarRef jr : list) {
			w.tag("dependency");
			w.tagfull("groupId", jr.m_groupId);
			w.tagfull("artifactId", jr.m_artefactId);
			w.tagfull("version", m_version);
			w.tagfull("type", "jar");
			w.tagfull("scope", "compile");

			w.tagendnl();
		}
	}

	/**
	 * If we have > 1 source directories we need to puke out EVEN MORE XML to get it to bloody compile.
	 * @param w
	 * @param sub
	 */
	private void generateExtraSourcesPukefest(XmlWriter w, List<String> sourceList) throws Exception {
		if(sourceList.size() == 0)
			return;

		//-- Puke, puke
		w.tag("plugin");
		w.tagfull("groupId", "org.codehaus.mojo");
		w.tagfull("artifactId", "build-helper-maven-plugin");
		w.tagfull("version", "1.1");
		w.tag("executions");
		w.tag("execution");
		w.tagfull("id", "add-source");
		w.tagfull("phase", "generate-sources");

		w.tag("goals");
		w.tagfull("goal", "add-source");
		w.tagendnl(); // goals

		w.tag("configuration");
		w.tag("sources");

		//-- After only 18 lines of SHIT can I add source dirs!!
		for(String src : sourceList) {
			w.tagfull("source", "../../" + src);
		}

		w.tagendnl(); // sources
		w.tagendnl(); // configuration

		w.tagendnl(); // execution
		w.tagendnl(); //executions
		w.tagendnl(); // plugin
	}

	private XmlWriter createMavenXml(File pom, String group, String art, String name, String pck) throws Exception {
		System.out.println("Generate " + pom);
		XmlWriter w = new XmlWriter(new OutputStreamWriter(new FileOutputStream(pom)));
		w.tag("project", "xmlns", "http://maven.apache.org/POM/4.0.0", "xmlns:xsl", "http://www.w3.org/2001/XMLSchema-instance", "xsi:schemaLocation", "http://maven.apache.org/POM/4.0.0");
		w.tagfull("modelVersion", "4.0.0");
		w.tagfull("groupId", group);
		w.tagfull("artifactId", art);
		w.tagfull("name", name);
		w.tagfull("version", m_version);
		w.tagfull("packaging", pck);

		return w;
	}

	private void generateJar(Project sub, JarRef jr) throws Exception {
		String basename = removeVersionFromFile(jr.m_jar.getName());
		System.out.println("Jar: " + jr.m_jarName + ", basename=" + basename);
		File mroot = new File(sub.getRoot(), MAVENSHIT);

		jr.m_groupId = sub.getBaseGroup();
		jr.m_artefactId = sub.getBaseName()+"."+basename;

		File f = new File(mroot, basename);
		f.mkdirs();
		f = new File(f, "pom.xml");
		jr.m_pomDir = MAVENSHIT + "/" + basename;

		System.out.println("Generate " + f);
		XmlWriter w = new XmlWriter(new OutputStreamWriter(new FileOutputStream(f)));
		w.tag("project", "xmlns", "http://maven.apache.org/POM/4.0.0", "xmlns:xsl", "http://www.w3.org/2001/XMLSchema-instance", "xsi:schemaLocation", "http://maven.apache.org/POM/4.0.0");
		w.tagfull("modelVersion", "4.0.0");
		w.tagfull("groupId", jr.m_groupId);
		w.tagfull("artifactId", jr.m_artefactId);
		w.tagfull("name", jr.m_jarName);
		w.tagfull("version", m_version);
		w.tagfull("packaging", "jar");
		w.tag("build");
		w.tag("plugins");

		w.tag("plugin");
		w.tagfull("artifactId", "maven-antrun-plugin");

		w.tag("executions");
		w.tag("execution");

		w.tagfull("id", "Do the immensely complex task of copying a single file to the repository in only 29 lines of xml. Welcome to the joys of Maven.");
		w.tagfull("phase", "verify");

		w.tag("goals");
		w.tagfull("goal", "run");
		w.tagendnl();

		w.tag("configuration");
		w.tag("tasks");

		w.tag("copy", "overwrite", "true", "file", "../../" + jr.m_jarName, "tofile", "${project.build.directory}/${project.artifactId}-${project.version}.jar");
		w.wraw("\n");

		w.tagendnl(); // tasks
		w.tagendnl(); // configuration

		w.tagendnl(); // execution
		w.tagendnl(); // executions

		w.tagendnl();

		w.tagendnl();
		w.tagendnl();
		w.tagendnl();
		w.close();
	}

	@Nonnull
	public Project loadProject(@Nonnull File dir) throws Exception {
		String name = dir.getName().toLowerCase();
		Project p = m_prjMap.get(name);
		if(null != p)
			return p;
		p = Project.createProject(this, dir);
		m_prjMap.put(name, p);
		return p;

	}

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

/**
 * Encapsulates a project as read from the Eclipse configuration.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 5, 2013
 */
class Project {
	@Nonnull
	final private PomBuilder	m_builder;

	@Nonnull
	private final File			m_root;

	@Nonnull
	private final String		m_baseName;

	@Nonnull
	private final String		m_baseGroup;

	@Nonnull
	private final String		m_name;

	@Nonnull
	private final String		m_encoding;

	boolean	m_dependenciesDone;

	String	m_groupId;

	String	m_artefactId;

	@Nonnull
	private String						m_sourceVersion	= "1.6";

	@Nonnull
	private String						m_targetVersion	= "1.6";

	List<String>	m_sourceList	= new ArrayList<String>();

	List<Project>	m_directDepList	= new ArrayList<Project>();

	List<JarRef>	m_jarList		= new ArrayList<JarRef>();

	List<Project>	m_fullDepList	= new ArrayList<Project>();

	private Project(@Nonnull PomBuilder b, @Nonnull String name, @Nonnull File root, @Nonnull String baseGroup, @Nonnull String baseName, @Nonnull String encoding) {
		m_builder = b;
		m_root = root;
		m_name = name;
		m_baseName = baseName;
		m_baseGroup = baseGroup;
		m_encoding = encoding;
	}

	static public Project createProject(@Nonnull PomBuilder b, @Nonnull File dir) throws Exception {
		String name = dir.getName().toLowerCase();
		File classfile = new File(dir, ".classpath");
		File prjfile = new File(dir, ".project");

		if(!classfile.isFile() || !classfile.exists() || !prjfile.isFile() || !prjfile.exists())
			throw new IllegalArgumentException(dir + " is not an eclipse project directory.");

		//-- Prepare groupid and stuff.
		String base = name;
		String baseGroup = "nl.itris";
		if(base.startsWith("bin-") || base.startsWith("lib-")) {
			base = base.substring(4);							// Cut off the bin/lib prefix.
			baseGroup = "nl.itris.external";
		} else if(base.startsWith("to."))
			baseGroup = "to.etc";

		//-- Start cutting off version numbers.
		String basename = PomBuilder.removeVersionFromDir(base);
		basename = PomBuilder.generateUniqueName(basename);
		String encoding = loadEncoding(dir);

		Project p = new Project(b, name, dir, baseGroup, basename, encoding);
		p.loadClassFile(classfile);
		p.loadJavaPreferences();
		return p;
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

	private void loadClassFile(@Nonnull File classfile) throws Exception {
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
						File f = new File(m_builder.m_rootPath, path.substring(1));
						Project dp = m_builder.loadProject(f);
						m_directDepList.add(dp);
					} else {
						m_sourceList.add(path);
					}
					//					System.out.println("src: " + path);
				} else if("lib".equals(kind)) {
					String path = DomTools.strAttr(n, "path");
					File jar;
					if(path.startsWith("/")) {
						if(true)
							throw new IllegalStateException("Link to external jar not allowed in " + getName() + ": " + path);
						jar = new File(m_builder.m_rootPath, path.substring(1));
					} else
						jar = new File(getRoot(), path);
					if(!jar.exists() || !jar.isFile()) {
						System.out.println("Ignoring nonexistent " + jar);
					} else {
						JarRef j = new JarRef();
						j.m_jar = new File(getRoot(), path);
						j.m_jarName = path;
						m_jarList.add(j);
					}
				}
			}
		}
	}

	public void generateDependencies(@Nonnull HashSet<Project> stack) {
		if(m_dependenciesDone)
			return;

		if(stack.contains(this))
			throw new IllegalStateException("Circular dependency on " + getName());

		//--Depth-1st traversal of all dependencies
		stack.add(this);
		for(Project sub : m_directDepList) {
			sub.generateDependencies(stack);
			addNewTo(m_fullDepList, sub.m_fullDepList);
			if(!m_fullDepList.contains(sub))
				m_fullDepList.add(sub);
		}
		stack.remove(this);
		m_dependenciesDone = true;
	}

	static private void addNewTo(@Nonnull List<Project> target, @Nonnull List<Project> src) {
		for(Project p : src) {
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


}
