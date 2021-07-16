package to.etc.domui.webdriver.core.base;

import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.webdriver.core.WebDriverConnector;

@NonNullByDefault
public abstract class BasePO {
	private final WebDriverConnector m_wd;

	public BasePO(WebDriverConnector wd) {
		m_wd = wd;
	}

	protected WebDriverConnector wd() {
		return m_wd;
	}

	protected static String createTestId(String testId) {
		return "*[testId='".concat(testId).concat("']");
	}
}
