package to.etc.launcher;

import java.io.*;

import org.junit.*;

import to.etc.launcher.misc.*;

public class ParallelTestLauncherTest {
	//@Test
	public void testClassLauncher() throws Exception {
		String[] args = new String[]{ //
		"-root", "/home/vmijic/Data/Projects/Itris/Viewpoint/bzr/vp-split-4.4-webdriver/vp-4.4", //
			"-m2.repo", "/home/vmijic/.m2", //
			"-project", "vp-selenium-webdriver-tests", //
			"-parallel", "CLASS", //
			//"-suiteFiles", "file1.xml", // still not implemented
			"-threads", "10", //
			"-testng.reporter", "nl.itris.vp.webdriver.core.report.WdVpTestXMLReporter", //
			"-testng.reporter.root", "/home/vmijic/Data/Projects/Itris/Viewpoint/testing/tests/proba/vp-suite", //
			"-testng.browser", "firefox", "ie", //
			"-testng.remote.hub", "local", //
			"-testng.server.url", "http://ws061.execom.co.yu:8080/Itris_VO02/", //
			"-testng.username", "vpc", //
			"-testng.password", "rhijnspoor", //
			"-testProperties", "wd44.properties", //
		};

		new ParallelTestLauncher().run(args);
	}

	@Test
	public void testHelp() throws Exception {
		String[] args = new String[]{ //
		"-help", //
		};

		new ParallelTestLauncher().run(args);
	}

	//@Test
	public void reportFixer() throws Exception {
		ReportFixer r = new ReportFixer();
		r.assambleSingleReports(new File("/home/vmijic/parallel201307311250/"));
	}

}
