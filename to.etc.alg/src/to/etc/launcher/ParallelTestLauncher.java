package to.etc.launcher;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

import javax.annotation.*;
import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import to.etc.launcher.collector.*;
import to.etc.launcher.misc.*;
import to.etc.launcher.runner.*;
import to.etc.util.*;

/*

Plan how to do this:

1. finds /.maven/source/pom.xml in project location
	parse it and collect dependencies references
2. runs through dependencies references list, and assamble class path by referencing jars found relative to -m2.repo
3. runs trought java files under -project and identify test files to run (match with Test testng annotation - uses URLClassLoader for this).
4. depending on -parallel value, group tests to run, and genarate suite files per thread
5. start running threads queue that consumes genearted suites with every browser string specified.
6. at the end, if set, remove suites that are generated. Also collect all generated reports into single xml report.

 */

/**
 * Launch parallel test execution, based on command line arguments.
 * See help.txt for details.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Jul 31, 2013
 */
public class ParallelTestLauncher implements IRunnableArgumentsProvider {

	public static final String	ARG_PROJECT	= "project";

	public static final String	ARG_M2REPO	= "m2.repo";

	public static final String	ARG_PARALLEL	= "parallel";

	public static final String	ARG_SUITES		= "suiteFiles";

	public static final String	ARG_ROOT		= "root";

	public static final String	ARG_THREADS		= "threads";

	public static final String	ARG_REPORTER	= "testng.reporter";

	public static final String	ARG_REPORTER_ROOT		= "testng.reporter.root";

	public static final String	ARG_BROWSER			= "testng.browser";

	public static final String	ARG_HUB				= "testng.remote.hub";

	public static final String	ARG_URL				= "testng.server.url";

	public static final String	ARG_USERNAME		= "testng.username";

	public static final String	ARG_PASSWORD		= "testng.password";

	public static final String	ARG_UNIT_TEST_PROPERTIES	= "testProperties";

	public static final String	ARG_REMOVE_GENERATED		= "remove.generated";

	public static final String	ARG_SINGLE_REPORT			= "single.report";

	public static final String	ARG_HELP					= "help";

	public static final int		DEFAULT_THREADS				= 5;

	private static final String	TIMESTAMP_FORMAT			= "yyyyMMddHHmm";

	private enum ParallelStartegyType {
		CLASS, PACKAGE, TEST
	};

	private static class Dependency {
		private final String	m_groupId;

		private final String	m_artifactId;

		private final String	m_version;

		private final String	m_type;

		private final String	m_scope;

		Dependency(String groupId, String artifactId, String version, String type, String scope) {
			super();
			m_groupId = groupId;
			m_artifactId = artifactId;
			m_version = version;
			m_type = type;
			m_scope = scope;
		}

		String getGroupId() {
			return m_groupId;
		}

		String getArtifactId() {
			return m_artifactId;
		}

		String getVersion() {
			return m_version;
		}

		String getType() {
			return m_type;
		}

		String getScope() {
			return m_scope;
		}

		@Override
		public String toString() {
			return "groupId:" + m_groupId + " ,artifactId:" + m_artifactId + " ,version:" + m_version;
		}

		public static @Nonnull
		Dependency factory(@Nonnull NodeList childNodes) {
			Map<String, String> map = new HashMap<String, String>();
			for(int index = 0; index < childNodes.getLength(); index++) {
				Node node = childNodes.item(index);
				if(node.getNodeType() == Node.ELEMENT_NODE) {
					String nodeName = node.getNodeName();
					String nodeText = node.getTextContent();
					if(nodeText != null) {
						nodeText = nodeText.trim();
					}
					map.put(nodeName, nodeText);
				}
			}
			return new Dependency(map.get("groupId"), map.get("artifactId"), map.get("version"), map.get("type"), map.get("scope"));
		}
	}

	private ArgumentsUtil	m_argUtil;

	private String			m_runnableClassPath;

	private File			m_reportRoot;

	public static void main(String[] args) {
		try {
			new ParallelTestLauncher().run(args);
		} catch(Exception x) {
			x.printStackTrace();
		}
	}

	private static void showHelp() {
		try {
			System.out.println(FileTool.readStreamAsString(ParallelTestLauncher.class.getResourceAsStream("help.txt"), "utf-8"));
		} catch(Exception e) {
			System.out.println("Error in showing help.");
			e.printStackTrace();
		}
	}

	void run(String[] args) throws Exception {
		if(args != null && args.length == 1 && (args[0] == "/?" || args[0] == "-help")) {
			showHelp();
			return;
		}
		setArgUtil(new ArgumentsUtil(args));
		File root = initializeRoot();
		File reportRoot = initializeReporterRoot(root);
		setReportRoot(reportRoot);
		List<String> projects = getArgUtil().getMandatory(ARG_PROJECT);
		for(String project : projects) {
			File location = new File(root, project);
			if(!location.exists()) {
				throw new IllegalArgumentException("Unable to locate specified project location " + location.getAbsolutePath());
			}
			runProject(location, reportRoot);
		}
	}

	private void setArgUtil(@Nonnull ArgumentsUtil argumentsUtil) {
		m_argUtil = argumentsUtil;
	}

	@Nonnull
	private ArgumentsUtil getArgUtil() {
		return m_argUtil;
	}

	private @Nonnull
	File initializeRoot() {
		String root = getArgUtil().getOptionalSingle(ARG_ROOT);
		if(!StringTool.isBlank(root)) {
			return new File(root);
		}
		return new File("bla-test-bla").getParentFile();
	}

	private @Nonnull
	File initializeReporterRoot(@Nonnull File root) {
		File res = null;
		String reporterRoot = getArgUtil().getOptionalSingle(ARG_REPORTER_ROOT);
		if(!StringTool.isBlank(reporterRoot)) {
			if(reporterRoot.startsWith("/") || reporterRoot.startsWith("~/") || (reporterRoot.length() > 2 && reporterRoot.charAt(1) == ':')) {
				res = new File(reporterRoot);
			}else {
				res = new File(root, reporterRoot);
			}
		} else {
			res = root;
		}
		String timestamp = new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date());
		res = new File(res, "parallel" + timestamp);
		return res;
	}

	private void runProject(@Nonnull File project, @Nonnull File reporter) throws Exception {
		File pom = new File(project, ".maven" + File.separator + "source" + File.separator + "pom.xml");
		if(!pom.exists()) {
			throw new IllegalStateException("Unable to locate " + pom.getPath() + ". Please run PomBuilder first!");
		}
		List<Dependency> dependencies = parseDependencies(pom);
		String classPath = assambleClassPath(dependencies);
		URLClassLoader onDependenciesLoader = getClassLoader(dependencies);
		List<String> xmlSuites = getArgUtil().getOptional(ARG_SUITES);
		List<File> generatedSuites = new ArrayList<File>();
		if(!getArgUtil().isEmptyArgumentValues(xmlSuites)) {
			ParallelStartegyType parallel = ParallelStartegyType.valueOf(getArgUtil().getSingleArgumentValue(ARG_PARALLEL, ParallelStartegyType.TEST.name()));
			if(ParallelStartegyType.TEST != parallel) {
				System.out.println("Predefined xmlSuites can only be run with -parallel=TESTS. Specified argument is ignored!");
			}
			generatedSuites = generateSuitesFromPreparedSuites(xmlSuites);
		} else {
			ParallelStartegyType parallel = ParallelStartegyType.valueOf(getArgUtil().getSingleArgumentValue(ARG_PARALLEL, ParallelStartegyType.CLASS.name()));
			List<String> testArtefacts = collectTestArtefacts(onDependenciesLoader, parallel, project);
			generatedSuites = generateSuitesFromArtefacts(parallel, testArtefacts, reporter);
			System.out.println("Generated suites from artefacts:" + generatedSuites.size());
		}
		try {
			runTests(classPath, generatedSuites);
		} finally {
			if("true".equals(getArgUtil().getSingleArgumentValue(ARG_SINGLE_REPORT, "true"))) {
				ReportFixer reportFixer = new ReportFixer();
				reportFixer.assambleSingleReports(getReportRoot());
			}
			if("true".equals(getArgUtil().getSingleArgumentValue(ARG_REMOVE_GENERATED, "true"))) {
				File parentLocation = generatedSuites.get(0).getParentFile();
				for(File file : generatedSuites) {
					file.delete();
				}
				parentLocation.delete();
			}
		}
	}

	private List<File> generateSuitesFromArtefacts(@Nonnull ParallelStartegyType parallel, @Nonnull List<String> testArtefacts, @Nonnull File reporter) throws Exception {
		int index = 0;
		String timestamp = new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date());
		List<String> listeners = getArgUtil().getOptional(ARG_REPORTER);
		List<File> res = new ArrayList<File>();
		for(String artefact : testArtefacts) {
			index++;
			StringBuilder sb = new StringBuilder();
			sb.append("<!DOCTYPE suite SYSTEM \"http://testng.org/testng-1.0.dtd\" >");
			sb.append("<suite name=\"genSuite" + index + "\" verbose=\"1\">");
			switch(parallel){
				case CLASS:
					String testName = artefact.substring(artefact.lastIndexOf(".") + 1);
					sb.append("<test name=\"" + testName + "\">");
					sb.append("<classes>");
					sb.append("<class name=\"" + artefact + "\"/>");
					sb.append("</classes>");
					break;
				case PACKAGE:
					sb.append("<test name=\"" + artefact + "\">");
					sb.append("<packages>");
					sb.append("<package name=\"" + artefact + "\"/>");
					sb.append("</packages>");
					break;
			}
			sb.append("</test>");
			if(!getArgUtil().isEmptyArgumentValues(listeners)) {
				sb.append("<listeners>");
				for(String listenerArtefact : listeners) {
					sb.append("<listener class-name=\"" + listenerArtefact + "\"/>");
				}
				sb.append("</listeners>");
			}
			sb.append("</suite>");
			File suite = new File(reporter, "gen" + timestamp + File.separator + "suite" + index + ".xml");
			suite.getParentFile().mkdirs();
			FileTool.writeFileFromString(suite, sb.toString(), "UTF8");
			res.add(suite);
		}
		return res;
	}

	private List<File> generateSuitesFromPreparedSuites(List<String> xmlSuites) {
		// TODO Auto-generated method stub
		return null;
	}

	private @Nonnull
	URLClassLoader getClassLoader(List<Dependency> dependencies) throws IllegalStateException {
		URL[] urls = new URL[dependencies.size()];
		for(int index = 0; index < dependencies.size(); index++) {
			try {
				urls[index] = locateDependency(dependencies.get(index)).toURI().toURL();
			} catch(MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return URLClassLoader.newInstance(urls);
	}

	private void runTests(@Nonnull String classpath, @Nonnull List<File> suites) throws InterruptedException {
		int parallelThreadsLimit = getArgUtil().getIntParam(ARG_THREADS, DEFAULT_THREADS);
		List<Thread> threads = new ArrayList<Thread>();
		List<String> browsers = getArgUtil().getOptional(ARG_BROWSER);
		if(getArgUtil().isEmptyArgumentValues(browsers)) {
			browsers.add("firefox");
		}
		if(parallelThreadsLimit > suites.size() * browsers.size()) {
			parallelThreadsLimit = suites.size() * browsers.size();
		}

		RunnableDataProvider dataProvider = new RunnableDataProvider(suites, browsers);

		setRunnableClassPath(classpath);

		for(int i = 0; i < parallelThreadsLimit; i++) {
			String name = "T" + i;
			SuiteRunner runnableJob = new SuiteRunner(this, dataProvider, name);
			Thread thread = new Thread(runnableJob, name);
			threads.add(thread);
			thread.start();
		}

		for(Thread thread : threads) {
			thread.join();
		}
	}

	private void setRunnableClassPath(@Nonnull String classpath) {
		m_runnableClassPath = classpath;
	}

	private @Nonnull
	List<String> collectTestArtefacts(@Nonnull URLClassLoader parentLoader, @Nonnull ParallelStartegyType parallel, @Nonnull File location) throws ClassNotFoundException {
		ITestArtefactsCollector collector = getTestArtefactsCollector(parallel, parentLoader);
		return collector.collectArtefacts(location);
	}

	private ITestArtefactsCollector getTestArtefactsCollector(@Nonnull ParallelStartegyType parallel, @Nonnull URLClassLoader parentLoader) {
		switch(parallel){
			case CLASS:
				return new ClassTestArtefactsCollector(parentLoader);
			case PACKAGE:
				return new PackageTestArtefactsCollector(parentLoader);
				/*
				case METHOD:
				return new MethodTestArtefactsCollector(parentLoader);
				*/default:
				throw new IllegalArgumentException("Unsupported collector for parallel mode: " + parallel.name());
		}
	}

	private @Nonnull
	String assambleClassPath(@Nonnull List<Dependency> dependencies) throws IllegalStateException {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for(Dependency dependency : dependencies) {
			if(first) {
				first = false;
			} else {
				sb.append(File.pathSeparator);
			}
			sb.append(locateDependency(dependency).getAbsolutePath());
		}
		return sb.toString();
	}

	private @Nonnull
	File locateDependency(@Nonnull Dependency dependency) throws IllegalStateException {
		StringBuilder path = new StringBuilder();
		path.append(getArgUtil().getSingleArgumentValue(ARG_M2REPO)).append(File.separator).append("repository");
		String[] groups = dependency.getGroupId().split("\\.");
		for(String group : groups) {
			path.append(File.separator);
			path.append(group);
		}
		path.append(File.separator).append(dependency.getArtifactId());
		path.append(File.separator).append(dependency.getVersion());
		path.append(File.separator).append(dependency.getArtifactId()).append("-").append(dependency.getVersion()).append(".jar");
		File file = new File(path.toString());
		if(!file.exists()) {
			throw new IllegalStateException("Unable to locate dependency " + file.getAbsolutePath());
		}
		return file;
	}

	private List<Dependency> parseDependencies(@Nonnull File pom) throws ParserConfigurationException, SAXException, IOException {
		List<Dependency> res = new ArrayList<Dependency>();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(pom);
		Node projectGroupId = XmlHelper.locateDirectChild(doc.getDocumentElement(), "groupId");
		Node projectArtifactId = XmlHelper.locateDirectChild(doc.getDocumentElement(), "artifactId");
		Node projectVersion = XmlHelper.locateDirectChild(doc.getDocumentElement(), "version");
		Dependency projectRoot = new Dependency(projectGroupId.getTextContent().trim(), projectArtifactId.getTextContent().trim(), projectVersion.getTextContent().trim(), null, null);
		res.add(projectRoot);
		Node dependencies = XmlHelper.locateDirectChild(doc.getDocumentElement(), "dependencies");
		if(null != dependencies) {
			NodeList dependencyNodes = dependencies.getChildNodes();
			for(int index = 0; index < dependencyNodes.getLength(); index++) {
				Node node = dependencyNodes.item(index);
				if(node.getNodeType() == Node.ELEMENT_NODE && "dependency".equals(node.getNodeName())) {
					res.add(Dependency.factory(node.getChildNodes()));
				}
			}
		}
		return res;
	}

	private void setReportRoot(@Nonnull File reportRoot) {
		m_reportRoot = reportRoot;
	}

	@Override
	@Nonnull
	public String getClassPath() throws IllegalStateException {
		if(StringTool.isBlank(m_runnableClassPath)) {
			throw new IllegalStateException("Class path still not resolved!");
		}
		return m_runnableClassPath;
	}

	@Override
	@Nullable
	public String getRemoteHub() {
		return getArgUtil().getOptionalSingle(ARG_HUB);
	}

	@Override
	@Nullable
	public String getServerUrl() {
		return getArgUtil().getOptionalSingle(ARG_URL);
	}

	@Override
	@Nullable
	public String getUserName() {
		return getArgUtil().getOptionalSingle(ARG_USERNAME);
	}

	@Override
	@Nullable
	public String getPassword() {
		return getArgUtil().getOptionalSingle(ARG_PASSWORD);
	}

	@Override
	@Nullable
	public String getUnitTestProperties() {
		return getArgUtil().getOptionalSingle(ARG_UNIT_TEST_PROPERTIES);
	}

	@Override
	@Nonnull
	public File getReportRoot() throws IllegalStateException {
		if(null == m_reportRoot) {
			throw new IllegalStateException("Report root still not resolved!");
		}
		return m_reportRoot;
	}

}
