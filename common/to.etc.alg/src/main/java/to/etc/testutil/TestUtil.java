package to.etc.testutil;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 15-04-22.
 */
final public class TestUtil {
	private TestUtil() {}

	public static void nonTestPrintln(String text) {
		if(! inTest())
			System.out.println(text);
	}

	static public boolean inTest() {
		return System.getProperty("surefire.real.class.path") != null || System.getProperty("surefire.test.class.path") != null;
	}

}
