package to.etc.domui.webdriver.poproxies;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openqa.selenium.By;
import to.etc.domui.webdriver.core.WebDriverConnector;

import java.util.function.Supplier;

@NonNullByDefault
abstract public class AbstractCpComponent extends AbstractCpBase {
	private final Supplier<String> m_selectorSupplier;

	public AbstractCpComponent(WebDriverConnector wd, Supplier<String> selectorSupplier) {
		super(wd);
		m_selectorSupplier = selectorSupplier;

	}

	public boolean isPresent() {
		return wd().isPresent(getSelector());
	}

	public Supplier<String> getSelectorSupplier() {
		return m_selectorSupplier;
	}

	public By selector(String extra) {
		return By.cssSelector(getSelectorSupplier().get() + " " + extra);
	}

	public By getSelector() {
		return By.cssSelector(getSelectorSupplier().get());
	}

	//protected String createTestIdSelector() {
	//	var selector = m_testIdSelector;
	//	if(selector == null) {
	//		selector = m_testIdSelector = createTestIdSelector(m_testId);
	//	}
	//	return selector;
	//}

	//protected String createTestIdSelector(String id) {
	//	return "*[testId='".concat(id).concat("']");
	//}
}
