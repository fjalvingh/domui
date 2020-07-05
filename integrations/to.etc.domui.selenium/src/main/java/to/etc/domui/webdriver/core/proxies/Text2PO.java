package to.etc.domui.webdriver.core.proxies;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openqa.selenium.By;
import to.etc.domui.webdriver.core.WebDriverConnector;

@NonNullByDefault
public class Text2PO extends ComponentPO implements IControlPO<String> {

	public Text2PO(WebDriverConnector connector, String testId) {
		super(connector, testId);
	}

	@Override
	public void setValue(String value) {
		wd().cmd().type(value).on(getInputLocator());
		wd().wait(getTestId());
	}

	@Override
	public String getValue() {
		var elem = wd().getElement(getInputLocator());
		return elem.getText();
	}

	@Override
	public boolean isReadonly() {
		return wd().isReadonly(getInputLocator());
	}

	@Override
	public boolean isDisabled() {
		return !wd().isEnabled(getTestId());
	}

	@NonNull
	private By getInputLocator() {
		return By.cssSelector(createTestIdSelector() + " INPUT");
	}
}
