package to.etc.domui.webdriver.core.base;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openqa.selenium.By;
import to.etc.domui.webdriver.core.WebDriverConnector;

@NonNullByDefault
public class InputPO extends AbstractTextPO {
	public InputPO(WebDriverConnector connector, String testId) {
		super(connector, By.cssSelector(createTestId(testId) + " INPUT"));
	}
}
