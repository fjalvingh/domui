package to.etc.domui.webdriver.core;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.After;
import to.etc.util.WrappedException;

/**
 * Abstract base class for Selenium JUnit tests. This class is for tests
 * that need a clean state with every test.
 */
@NonNullByDefault
abstract public class AbstractWebDriverTest extends AbstractWebDriverTestBase {
	@Nullable
	private WebDriverConnector m_wd;

	/**
	 * Get the webdriver to use for tests.
	 */
	@Override
	@NonNull
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

	/**
	 * Called after every test, this completely resets the WebDriver connector.
	 * This includes things like cookies, so that each test sees a new login
	 * state.
	 */
	@After
	public void cleanupWebDriver() {
		WebDriverConnector wd = m_wd;
		if(null != wd) {
			wd.reset();
		}
	}
}
