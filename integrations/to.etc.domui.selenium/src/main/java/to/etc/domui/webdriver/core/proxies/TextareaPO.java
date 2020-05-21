package to.etc.domui.webdriver.core.proxies;

import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.webdriver.core.WebDriverConnector;

@NonNullByDefault
public class TextareaPO extends ComponentPO implements IControlPO<String> {
	public TextareaPO(WebDriverConnector connector, String testId) {
		super(connector, testId);
	}

	@Override
	public void setValue(String value) {
		wd().cmd().type(value).on(getTestId());
	}

	@Override
	public String getValue() {
		return wd().getElement(getTestId()).getText();
	}

	@Override
	public boolean isReadonly() {
		return wd().isReadonly(getTestId());
	}

	@Override
	public boolean isDisabled() {
		return !wd().isEnabled(getTestId());
	}
}
