package to.etc.domui.webdriver.core.base;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openqa.selenium.By;
import to.etc.domui.webdriver.core.WebDriverConnector;

@NonNullByDefault
public class ButtonPO extends BasePO {

	private final By m_locator;

	public ButtonPO(WebDriverConnector wd, String id) {
		super(wd);
		m_locator = wd.byId(id);
	}

	public void click() {
		wd().cmd().click().on(m_locator);
	}

	public boolean isDisabled() {
		var btn = wd().findElement(m_locator);
		if(btn == null) {
			throw new IllegalStateException("Cant find button "+ m_locator);
		}
		return "true".equalsIgnoreCase(btn.getAttribute("disabled"));
	}

	public boolean isPresent(){
		return wd().isPresent(m_locator);
	}
}
