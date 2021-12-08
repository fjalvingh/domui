package to.etc.domui.webdriver.poproxies;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import to.etc.domui.webdriver.core.WebDriverConnector;

@NonNullByDefault
public class RadioButtonPO extends ComponentPO {

	public RadioButtonPO(WebDriverConnector wd, String id) {
		super(wd, id);
	}

	public void click(int order) {
		var list = getRatioButton().findElements(By.cssSelector("label"));
		list.get(order).click();
	}

	private WebElement getRatioButton() {
		return wd().getElement(getTestId());
	}

	public void selectWithLabel(String label) {
		var el = getRatioButton().findElement(By.partialLinkText(label));
		el.click();
	}
}
