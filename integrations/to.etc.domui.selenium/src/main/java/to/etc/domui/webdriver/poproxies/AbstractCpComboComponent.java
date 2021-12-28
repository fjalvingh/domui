package to.etc.domui.webdriver.poproxies;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import to.etc.domui.webdriver.core.WebDriverConnector;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 28-12-21.
 */
public class AbstractCpComboComponent extends AbstractCpInputControl<String> implements ICpControl<String> {
	public AbstractCpComboComponent(WebDriverConnector wd, Supplier<String> selectorSupplier) {
		super(wd, selectorSupplier);
	}

	@Override
	public void setValue(@Nullable String valueIn) throws Exception {
		String value = valueIn == null ? "" : valueIn;
		var available = getAvailable();
		var option = available.stream().filter(x -> x.getLabel().equals(value)).findFirst().orElse(null);
		var index = available.indexOf(option);
		if(index >= 0) {
			select(index);
		}
	}

	@Nullable
	@Override
	public String getValue() {
		String topSelector = getSelectorSupplier().get();
		return wd().getElement(By.cssSelector(topSelector + " option[selected='selected']")).getText();
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

	@NonNull
	@Override
	protected By getInputSelector() {
		return By.cssSelector(getInputSelectorCss());
	}

	@Override
	protected String getInputSelectorCss() {
		return selectorCss("select");
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
