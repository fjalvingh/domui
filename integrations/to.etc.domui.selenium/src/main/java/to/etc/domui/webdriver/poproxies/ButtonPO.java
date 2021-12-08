package to.etc.domui.webdriver.poproxies;

import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.webdriver.core.WebDriverConnector;

@NonNullByDefault
public class ButtonPO extends ComponentPO implements IActionControlPO {
	public ButtonPO(WebDriverConnector wd, String testId) {
		super(wd, testId);
	}

	@Override
	public void click() {
		wd().cmd().click().on(getTestId());
	}

	@Override
	public boolean isDisabled() {
		return !wd().isEnabled(getTestId());
	}
}
