package to.etc.domui.webdriver.core;

import to.etc.pater.OnTestFailure;
import to.etc.util.StringTool;
import to.etc.util.WrappedException;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;

/**
 * Abstract base class for Selenium JUnit tests.
 */
@DefaultNonNull
abstract public class AbstractWebDriverTest {
	@Nullable
	private WebDriverConnector m_wd;

	/**
	 * Get the webdriver to use for tests.
	 * @return
	 */
	@Nonnull
	final protected WebDriverConnector wd() {
		WebDriverConnector wd = m_wd;
		if(null == wd) {
			try {
				m_wd = wd = WebDriverConnector.get();
			} catch(Exception x) {
				throw WrappedException.wrap(x);
			}
		}
		return wd;
	}

	protected WebDriverCommandBuilder c() {
		return wd().cmd();
	}

	@Nonnull
	public static String prefix(int len) {
		return StringTool.getRandomStringWithPrefix(len, "wd_");
	}

	/**
	 * EXPERIMENTAL: this method gets called by the "new" test runner if a test has failed. The implementation
	 * uses the "test report" mechanism to register screenshots with the test runner so that they can be
	 * part of the result report.
	 * @param failedMethod
	 * @throws Exception
	 */
	@OnTestFailure
	public void onTestFailure(Method failedMethod) throws Exception {
		WebDriverConnector.onTestFailure(wd(), failedMethod);
	}
}
