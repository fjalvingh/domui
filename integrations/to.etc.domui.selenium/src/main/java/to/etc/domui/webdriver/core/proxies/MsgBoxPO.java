package to.etc.domui.webdriver.core.proxies;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openqa.selenium.By;
import to.etc.domui.webdriver.core.WebDriverConnector;

@NonNullByDefault
public class MsgBoxPO extends ComponentPO {

	public MsgBoxPO(WebDriverConnector connector, String testId) {
		super(connector, testId);
	}

	public String getText() {
		var el = wd().findElement(createLocator());
		if(el == null) {
			throw new IllegalStateException("Can't find MessageBox with id" + getTestId());
		}
		return el.getText();
	}

	private By createLocator() {
		return By.cssSelector(createTestIdSelector() + " .ui-mbx-mc");
	}
}
