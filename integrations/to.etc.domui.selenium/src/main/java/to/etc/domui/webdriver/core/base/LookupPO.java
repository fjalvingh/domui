package to.etc.domui.webdriver.core.base;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import to.etc.domui.webdriver.core.WebDriverConnector;

@NonNullByDefault
public class LookupPO extends BasePO {

	private final String m_id;

	public LookupPO(WebDriverConnector connector, String id) {
		super(connector);
		m_id = id;
	}

	public void setValue(String val, String colName) {
		var locator = wd().byId(m_id + "-lookup");
		var wait = new WebDriverWait(wd().driver(), 10);
		wd().wait(ExpectedConditions.elementToBeClickable(locator));
		wd().cmd().click().on(locator);
		wd().wait(locator);

		var inputLocator = By.cssSelector(".ui-lui2-dlg *[testid='" + colName + "'] INPUT");
		if(!wd().isVisible(inputLocator)) {
			wd().cmd().click().on(locator);
		}
		wd().cmd().type(val).on(inputLocator);
		wd().cmd().click().on(By.cssSelector(".ui-lui2-dlg *[testid='searchButton']"));
		wd().cmd().click().on(By.cssSelector(".ui-lui2-dlg table td:first-child"));
		wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".ui-lui2-dlg")));
	}


	public String getValue() {
		return wd().getElement(By.cssSelector("[testid='" + m_id + "'] .ui-lui-vcell")).getText();
	}

	public boolean isDisabled() {
		var locator = wd().byId(m_id + "-lookup");
		try {
			var disabled = wd().getAttribute(locator, "disabled");
			return "true".equalsIgnoreCase(disabled);
		} catch(IllegalStateException e) {
			if(e.getMessage().contains("No value for expected attribute disabled at locator")) {
				return false;
			}
			throw e;
		}
	}
}
