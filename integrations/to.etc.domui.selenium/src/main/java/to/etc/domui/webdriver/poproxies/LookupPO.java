package to.etc.domui.webdriver.poproxies;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import to.etc.domui.webdriver.core.WebDriverConnector;

import java.util.List;

@NonNullByDefault
public class LookupPO extends ComponentPO implements IControlPO<String> {

	public LookupPO(WebDriverConnector connector, String testId) {
		super(connector, testId);
	}

	public void searchBy(List<SearchPair> val) {
		var locator = wd().byId(getTestId() + "-lookup");
		var wait = new WebDriverWait(wd().driver(), 10);
		wd().wait(ExpectedConditions.elementToBeClickable(locator));
		wd().cmd().click().on(locator);
		wd().wait(locator);
		for(SearchPair searchPair : val) {
			var inputLocator = By.cssSelector(".ui-lui2-dlg *[testid='" + searchPair.getColumn() + "'] INPUT");
			if(!wd().isVisible(inputLocator)) {
				wd().cmd().click().on(locator);
			}
			wd().cmd().type(searchPair.getValue()).on(inputLocator);
		}

		wd().cmd().click().on(By.cssSelector(".ui-lui2-dlg *[testid='searchButton']"));
		wd().cmd().click().on(By.cssSelector(".ui-lui2-dlg table td:first-child"));
		wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".ui-lui2-dlg")));
	}

	public void searchBy(String column, String value) {
		searchBy(List.of(new SearchPair(column, value)));
	}


	@Override
	public void setValue(String value) throws Exception {
		var clearBtn = getTestId()+ "-clear";
		By locator = By.cssSelector(createTestIdSelector() + " INPUT");
		if(wd().isEnabled(clearBtn)) {
			wd().wait(ExpectedConditions.elementToBeClickable(wd().byId(clearBtn)));
			wd().wait(wd().byId(clearBtn));
			wd().cmd().click().on(clearBtn);
			if(!wd().isVisible(locator)) {
				wd().cmd().click().on(clearBtn);
			}
		}

		wd().cmd().type(value).on(locator);
		wd().waitForNoneOfElementsPresent(locator);
	}

	@Override
	public String getValue() {
		return wd().getElement(By.cssSelector(createTestIdSelector()+" .ui-lui-vcell")).getText();
	}

	@Override
	public boolean isReadonly() {
		return false;
	}

	@Override
	public boolean isDisabled() {
		return !wd().isEnabled(getTestId()+ "-lookup");
	}

	public static class SearchPair {
		private String m_column;
		private String m_value;

		public SearchPair(String column, String value) {
			m_column = column;
			m_value = value;
		}

		public String getColumn() {
			return m_column;
		}

		public String getValue() {
			return m_value;
		}
	}
}
