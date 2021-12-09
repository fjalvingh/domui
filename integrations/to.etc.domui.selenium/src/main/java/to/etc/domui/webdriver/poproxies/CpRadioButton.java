package to.etc.domui.webdriver.poproxies;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import to.etc.domui.webdriver.core.WebDriverConnector;

import java.util.function.Supplier;

@NonNullByDefault
public class CpRadioButton extends AbstractCpComponent {
	public CpRadioButton(WebDriverConnector wd, Supplier<String> selectorProvider) {
		super(wd, selectorProvider);
	}

	public void click(int order) {
		var list = getRatioButton().findElements(By.cssSelector("label"));
		list.get(order).click();
	}

	private WebElement getRatioButton() {
		return wd().getElement(getSelector());
	}

	public void selectWithLabel(String label) {
		var el = getRatioButton().findElement(By.partialLinkText(label));
		el.click();
	}
}
