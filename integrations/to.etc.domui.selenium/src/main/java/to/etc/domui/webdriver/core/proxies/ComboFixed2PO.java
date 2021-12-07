package to.etc.domui.webdriver.core.proxies;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import to.etc.domui.webdriver.core.WebDriverConnector;

import java.util.ArrayList;
import java.util.List;

@NonNullByDefault
public class ComboFixed2PO extends ComponentPO implements IControlPO<String> {

	public ComboFixed2PO(WebDriverConnector connector, String testId) {
		super(connector, testId);
	}

	@Override
	public void setValue(String value) throws Exception {
		var available = getAvailable();
		var option = available.stream().filter(x->x.getLabel().equals(value)).findFirst().orElse(null);
		var index = available.indexOf(option);
		if(index >= 0) {
			select(index);
		}
	}

	@Override
	public String getValue() {
		return wd().getElement(By.cssSelector(createTestIdSelector() + " option[selected='selected']")).getText();
	}

	@Override
	public boolean isReadonly() throws Exception {
		return wd().isReadonly(By.cssSelector(createTestIdSelector() + " select"));
	}

	@Override
	public boolean isDisabled() throws Exception {
		return wd().isEnabled(By.cssSelector(createTestIdSelector() + " select"));
	}

	private void select(int idx) {
		wd().cmd().click().on(By.cssSelector(createTestIdSelector() + " select"));
		By locator = By.cssSelector(createTestIdSelector() + " option:nth-child(" + (idx + 1) + ")");
		wd().wait(locator);
		wd().cmd().click().on(locator);
	}

	public List<ComboPOValue> getAvailable() {
		var elements = wd().findElements(By.cssSelector((createTestIdSelector()) + " option"));
		var list = new ArrayList<ComboPOValue>();

		for(WebElement element : elements) {
			list.add(new ComboPOValue(element.getAttribute("testId"), element.getText()));
		}

		return  list;
	}

	public static class ComboPOValue {
		@Nullable
		private final String m_testId;

		private final String m_label;

		public ComboPOValue(String testId, String label) {
			m_testId = testId;
			m_label = label;
		}

		@Nullable
		public String getTestId() {
			return m_testId;
		}

		public String getLabel() {
			return m_label;
		}
	}
}
