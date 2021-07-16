package to.etc.domui.webdriver.core.base;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import to.etc.domui.webdriver.core.WebDriverConnector;

@NonNullByDefault
public class RadioButtonPO extends BasePO {
	private final By m_locator;

	public RadioButtonPO(WebDriverConnector wd, String id) {
		super(wd);
		m_locator = wd.byId(id);
	}

	public void select(int order) {
		var list = getRatioButton().findElements(By.cssSelector("label"));
		list.get(order).click();
	}

	private WebElement getRatioButton() {
		var rb = wd().findElement(m_locator);
		if(rb == null) {
			throw new IllegalStateException("Radio button with " + m_locator + " couldn't be found");
		}
		return rb;
	}

	public void selectWithLabel(String label) {
		var el = getRatioButton().findElement(By.partialLinkText(label));
		el.click();
	}
}
