package to.etc.domui.webdriver.poproxies;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import to.etc.domui.webdriver.core.WebDriverConnector;

import java.util.List;
import java.util.function.Supplier;

@NonNullByDefault
public class CpLookupInput extends AbstractCpComponent implements ICpControl<String> {
	public CpLookupInput(WebDriverConnector connector, Supplier<String> selectorProvider) {
		super(connector, selectorProvider);
	}

	public void searchBy(List<SearchPair> val) {
		By locator = By.cssSelector(getSelectorSupplier().get() +  " -lookup");			// FIXME Incorrect

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
		clickClear();
		By selector = By.cssSelector(getSelectorSupplier().get() + " INPUT");
		wd().cmd().type(value).on(selector);
		wd().waitForNoneOfElementsPresent(selector);
	}

	private void clickClear() throws Exception {
		By clearBtn = By.cssSelector(getSelectorSupplier().get() + " INPUT");
		By inputSelector = By.cssSelector(getSelectorSupplier().get() + " INPUT");

		if(wd().isEnabled(clearBtn)) {
			wd().wait(ExpectedConditions.elementToBeClickable(clearBtn));
			wd().wait(clearBtn);
			wd().cmd().click().on(clearBtn);
			if(!wd().isVisible(inputSelector)) {
				wd().cmd().click().on(clearBtn);
			}
		}
	}

	@Override
	public String getValue() {
		By selector = By.cssSelector(getSelectorSupplier().get() + " .ui-lui-vcell");
		return wd().getElement(selector).getText();
	}

	@Override
	public boolean isReadonly() {
		return false;
	}

	@Override
	public boolean isDisabled() {
		return !wd().isEnabled(selector("-lookup"));					// FIXME Incorrect
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
