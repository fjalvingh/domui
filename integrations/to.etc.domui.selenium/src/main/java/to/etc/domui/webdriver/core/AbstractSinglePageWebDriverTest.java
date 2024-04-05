package to.etc.domui.webdriver.core;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.Before;
import to.etc.util.WrappedException;

/**
 * Abstract base class for Selenium JUnit tests. This class is for
 * tests that open a single page and then do multiple tests on that
 * single page.
 */
@NonNullByDefault
abstract public class AbstractSinglePageWebDriverTest extends AbstractWebDriverTestBase {
	@Nullable
	private WebDriverConnector m_wd;

	@Before
	abstract public void initializeScreen() throws Exception;

	/**
	 * Get the webdriver to use for tests. This gets the connector only once!
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
}
