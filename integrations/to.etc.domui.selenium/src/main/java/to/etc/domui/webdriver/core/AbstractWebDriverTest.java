package to.etc.domui.webdriver.core;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
			//System.out.println("OPENING WEBDRIVERCONNECTOR");
			try {
				m_wd = wd = WebDriverConnector.get();
			} catch(Exception x) {
				throw WrappedException.wrap(x);
			}
		}
		return wd;
	}

	/**
	 * We use this method to reset the WebDriver connector after a
	 * test finishes, so that the TestRule has a chance to take a
	 * snapshot using the original WebDriver instance before it
	 * gets closed.
	 */
	@Override
	public void internalClosePerTestResources() {
		cleanupWebDriver();
		super.internalClosePerTestResources();
	}

	/**
	 * Called after every test, this completely resets the WebDriver connector.
	 * This includes things like cookies, so that each test sees a new login
	 * state.
	 *
	 * DO NOT DEFINE WITH @After, as this resets the webdriver connector before
	 * the failed test screenshot is taken, and the screenshot then fails oddly.
	 */
	//@After
	public void cleanupWebDriver() {
		WebDriverConnector wd = m_wd;
		if(null != wd) {
			//System.out.println(">> CLOSING WEBDRIVERCONNECTOR");
			wd.reset();
		}
	}
}
