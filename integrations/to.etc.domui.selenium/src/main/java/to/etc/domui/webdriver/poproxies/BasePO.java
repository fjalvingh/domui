package to.etc.domui.webdriver.poproxies;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openqa.selenium.JavascriptExecutor;
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

	protected JavascriptExecutor getJsExecutor() {
		if(wd().driver() instanceof JavascriptExecutor) {
			return (JavascriptExecutor) wd().driver();
		}
		throw new IllegalStateException("Browser does not support javascript");
	}
}
