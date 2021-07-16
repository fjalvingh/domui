package to.etc.domui.webdriver.core.base;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openqa.selenium.By;
import to.etc.domui.webdriver.core.WebDriverConnector;

@NonNullByDefault
public class SelectPO extends BasePO {

	private final String m_locator;

	public SelectPO(WebDriverConnector connector, String locator) {
		super(connector);
		m_locator = locator;
	}

	public void select(int idx) {
		wd().cmd().click().on(By.cssSelector(m_locator));
		By locator = By.cssSelector(m_locator + " option:nth-child(" + idx + ")");
		wd().wait(locator);
		wd().cmd().click().on(locator);
	}

	public String getValue() {
		return wd().getElement(By.cssSelector(m_locator + " option[selected='selected']")).getText();
	}
}
