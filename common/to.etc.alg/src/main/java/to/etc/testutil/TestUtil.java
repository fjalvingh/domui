package to.etc.testutil;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 15-04-22.
 */
final public class TestUtil {
	private static Boolean m_mavenTest;

	private static Boolean m_ciRun;

	private TestUtil() {
	}

	public static void nonTestPrintln(String text) {
		if(!inTest())
			System.out.println(text);
	}

	static public boolean inTest() {
		return System.getProperty("surefire.real.class.path") != null || System.getProperty("surefire.test.class.path") != null;
	}

	static public String getGitBranch() {
		try {
			Process process = Runtime.getRuntime().exec("git rev-parse --abbrev-ref HEAD");
			process.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			return reader.readLine();
		} catch(Exception x) {
			return null;
		}
	}

	static public void main(String[] args) {
		System.out.println("branch: " + getGitBranch());
	}

	public static synchronized boolean isMavenTest() {
		Boolean mavenTest = m_mavenTest;
		if(null == mavenTest) {
			mavenTest = m_mavenTest = System.getProperty("surefire.real.class.path") != null
				|| System.getProperty("surefire.test.class.path") != null
				|| System.getProperty("failsafe.test.class.path") != null
				|| System.getProperty("failsafe.real.class.path") != null
				|| System.getProperty("domui.selenium") != null
			;

		}
		return mavenTest;
	}

	/**
	 * Returns T if the build is done from the CI (the -DCI parameter is present).
	 */
	public static synchronized boolean isCIRun() {
		Boolean ciRun = m_ciRun;
		if(null == ciRun) {
			ciRun = m_ciRun = System.getProperty("CI") != null;
		}
		return ciRun;
	}

}
