package to.etc.domui.webdriver.poproxies;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import to.etc.domui.webdriver.core.WebDriverConnector;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@NonNullByDefault
public class CpComboFixed2 extends AbstractCpComponent implements ICpControl<String> {

	public CpComboFixed2(WebDriverConnector connector, Supplier<String> selectorProvider) {
		super(connector, selectorProvider);
	}

	@Override
	public void setValue(String value) throws Exception {
		var available = getAvailable();
		var option = available.stream().filter(x -> x.getLabel().equals(value)).findFirst().orElse(null);
		var index = available.indexOf(option);
		if(index >= 0) {
			select(index);
		}
	}

	@Override
	public String getValue() {
		String topSelector = getSelectorSupplier().get();
		return wd().getElement(By.cssSelector(topSelector + " option[selected='selected']")).getText();
	}

	@Override
	public boolean isReadonly() throws Exception {
		return wd().isReadonly(By.cssSelector(getSelectorSupplier().get() + " select"));
	}

	@Override
	public boolean isDisabled() throws Exception {
		return wd().isEnabled(By.cssSelector(getSelectorSupplier().get() + " select"));
	}

	private void select(int idx) {
		wd().cmd().click().on(By.cssSelector(getSelectorSupplier().get() + " select"));
		By locator = By.cssSelector(getSelectorSupplier().get() + " option:nth-child(" + (idx + 1) + ")");
		wd().wait(locator);
		wd().cmd().click().on(locator);
	}

	public List<ComboPOValue> getAvailable() {
		var elements = wd().findElements(By.cssSelector(getSelectorSupplier().get() + " option"));
		var list = new ArrayList<ComboPOValue>();

		for(WebElement element : elements) {
			list.add(new ComboPOValue(element.getAttribute("testId"), element.getText()));
		}
		return list;
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
