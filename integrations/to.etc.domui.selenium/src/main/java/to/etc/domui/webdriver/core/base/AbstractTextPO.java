package to.etc.domui.webdriver.core.base;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openqa.selenium.By;
import to.etc.domui.webdriver.core.WebDriverConnector;

@NonNullByDefault
abstract public class AbstractTextPO extends BasePO {

	private final By m_locator;

	protected AbstractTextPO(WebDriverConnector connector, By locator) {
		super(connector);
		m_locator = locator;
	}

	public void setValue(String value) {
		wd().cmd().type(value).on(m_locator);
		wd().wait(m_locator);
	}

	public String getValue() {
		var elem = wd().findElement(m_locator);
		if(elem == null) {
			throw new IllegalStateException("Can't find element with locator "+ m_locator);
		}
		return elem.getText();
	}
}
