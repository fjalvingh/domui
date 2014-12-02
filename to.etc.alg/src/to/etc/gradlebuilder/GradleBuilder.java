package to.etc.gradlebuilder;

import java.io.*;
import java.util.*;

import javax.annotation.*;

import to.etc.gradlebuilder.EclipseProject.JarRef;
import to.etc.util.*;


/**
 * Generates the heaps of crap Maven needs to build a simple webapp.
 *
 * Documentation:
 * <ul>
 *	<li>http://blog.miloszikic.com/2010/04/maven-war-plugin-and-non-default-webapp.html: configure WebContent for war packaging in only 12 lines for a single parameter 8-(</li>
 *	<li>http://i-proving.com/2007/03/29/split-your-project-into-sub-projects/: a way to handle sub-projects</li>
 * </ul>
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 5, 2013
 */
final public class GradleBuilder {
	private int			m_argc;

	private String[]	m_argv;

	private GradleBuilder() {
	}


	public static void main(String[] args) {
		try {
			new GradleBuilder().run(args);
		} catch(Exception x) {
			x.printStackTrace();
			System.exit(10);
		}
	}

	@Nonnull
	private String arg(@Nonnull String what) {
		if(m_argc >= m_argv.length)
			error("Missing parameter after " + what);
		return m_argv[m_argc++].trim();
	}

	private void run(String[] args) throws Exception {
		List<String> projectList = new ArrayList<String>();
		File prjroot = null;
		m_argv = args;
		m_argc = 0;
		while(m_argc < args.length) {
			String s = args[m_argc++];

			if(!s.startsWith("-")) {
				if(null != prjroot)
					throw error("Duplicate workspace directory specified.");
				prjroot = new File(s);
			} else if("-p".equals(s) || "-project".equals(s)) {
				s = arg(s);
				projectList.add(s);
			} else {
				throw error("Unknown option: " + s);
			}
		}
		if(null == prjroot)
			prjroot = new File(".").getAbsoluteFile();

		EclipseWorkspace ew = EclipseWorkspace.create(prjroot);
		System.out.println("Loaded workspace " + prjroot + " with " + ew.getAllProjects().size() + " projects");

		//-- Get a list of all projects that are actually used
		List<EclipseProject> projList = new ArrayList<EclipseProject>();		// All projects to scan..
		for(String projectName : projectList) {
			EclipseProject ep = ew.findProject(projectName);
			if(null == ep)
				throw new RuntimeException(projectName + ": project not found");

			//-- Add project deps first
			for(EclipseProject depep : ep.getFullDepList()) {
				addProjectOnce(projList, depep);
			}
			addProjectOnce(projList, ep);
		}

		System.out.println("Got " + projList.size() + " projects actually used");

		renderGradleBuild(prjroot);								// build.gradle in workspace root
		renderSettings(prjroot, ew, projList);					// settings.gradle enumerating all projects
		renderProjectSettings(prjroot, ew, projList);			// per-project build.gradle
	}

	private void renderProjectSettings(File prjroot, EclipseWorkspace ew, List<EclipseProject> projList) throws Exception {
		for(EclipseProject ep : projList) {
			renderProjectSetting(ep, ew);
		}
	}


	private void renderProjectSetting(EclipseProject ep, EclipseWorkspace ew) throws Exception {
		File tgt = new File(ep.getRoot(), "build.gradle");
		//		if(tgt.exists())
		//			return;

		StringBuilder sb = new StringBuilder();

		if(ep.getRoot().getName().contains("itext"))
			System.out.println("GOTCHA");

		sb.append("apply plugin: 'java'\n");

		//-- Source
		renderSourceSets(sb, ep);

		String enc = ep.getEncoding();
		if(null != enc && !("utf-8".equalsIgnoreCase(enc) || "utf8".equalsIgnoreCase(enc))) {
			sb.append("compileJava.options.encoding = '").append(enc).append("'\n");
		}

		//-- Render project-level dependencies
		List<EclipseProject> fullDepList = ep.getFullDepList();
		fullDepList.remove(ep);
		if(fullDepList.size() > 0) {
			sb.append("dependencies {\n");
			for(EclipseProject dp : fullDepList) {
				sb.append("\tcompile project(':").append(dp.getName()).append("')\n");
			}
			sb.append("}\n");
		}

		//-- Output jars
		renderJarRefs(sb, ep);

		FileTool.writeFileFromString(tgt, sb.toString(), "utf-8");

	}


	private void renderJarRefs(StringBuilder sb, EclipseProject ep) {
		List<JarRef> list = ep.getJarList();
		if(list.size() == 0)
			return;

		List<JarRef> act = new ArrayList<JarRef>();
		for(JarRef jr: list) {
			if(jr.getJar().exists()) {
				act.add(jr);
			}
		}
		if(act.size() == 0)
			return;

		sb.append("dependencies {\n");

		sb.append("\truntime files(");
		int ct = 0;
		for(JarRef jr : list) {
			if(ct++ > 0)
				sb.append(',');
			sb.append("\"").append(jr.getJarName()).append("\"");
		}
		sb.append(")\n");

		sb.append("}\n");
	}


	private void renderSourceSets(StringBuilder sb, EclipseProject ep) {
		List<String> sourceList = ep.getSourceList();
		if(sourceList.size() == 0) {
			return;
		}

		boolean rendered = false;
		List<String> testSets = new ArrayList<String>();
		List<String> srcSets = new ArrayList<String>();

		for(String sl : sourceList) {
			File dir = new File(ep.getRoot(), sl);
			if(dir.exists() && dir.isDirectory()) {
				if(sl.toLowerCase().contains("test")) {
					testSets.add(sl);
				} else {
					srcSets.add(sl);
				}
			}
		}

		if(srcSets.size() > 0) {
			sb.append("sourceSets {\n");
			sb.append("\tmain {\n");
			sb.append("\t\tjava {\n");
			sb.append("\t\t\tsrcDirs = [");
			int ct = 0;
			for(String ss : srcSets) {
				if(ct++ > 0)
					sb.append(",");
				sb.append("\"").append(ss).append("\"");
			}
			sb.append("]\n");
			sb.append("\t\t}\n");

			sb.append("\t\tresources {\n");
			sb.append("\t\t\tsrcDirs = [");
			ct = 0;
			for(String ss : srcSets) {
				if(ct++ > 0)
					sb.append(",");
				sb.append("\"").append(ss).append("\"");
			}
			sb.append("]\n");
			sb.append("\t\t}\n");


			sb.append("\t}\n");
			sb.append("}\n");

		}

	}


	private void renderSettings(File root, EclipseWorkspace ws, List<EclipseProject> list) throws Exception {
		File tgt = new File(root, "settings.gradle");
		if(tgt.exists())
			return;

		StringBuilder sb = new StringBuilder();
		for(EclipseProject ep : list) {
			sb.append("include \"").append(ep.getRoot().getName()).append("\"\n");
		}
		FileTool.writeFileFromString(tgt, sb.toString(), "utf-8");
	}

	private void renderGradleBuild(File root) throws Exception {
		File tgt = new File(root, "build.gradle");
		if(tgt.exists())
			return;
		String bld = FileTool.readResourceAsString(GradleBuilder.class, "rootbuild.gradle", "utf-8");
		FileTool.writeFileFromString(tgt, bld, "utf-8");
	}


	static private void addProjectOnce(@Nonnull List<EclipseProject> list, @Nonnull EclipseProject ep) {
		if(list.contains(ep))
			return;
		list.add(ep);
	}

	static private RuntimeException error(@Nonnull String msg) {
		System.err.println(msg);
		System.exit(10);
		throw new RuntimeException();
	}
}
