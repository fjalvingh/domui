package to.etc.domui.webdriver.poproxies;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import to.etc.domui.webdriver.core.WebDriverConnector;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@NonNullByDefault
abstract public class AbstractCpComponent extends AbstractCpBase {
	private final Supplier<String> m_selectorSupplier;

	public AbstractCpComponent(WebDriverConnector wd, Supplier<String> selectorSupplier) {
		super(wd);
		m_selectorSupplier = selectorSupplier;

	}

	/**
	 * Returns a selector for the primary element in this component. The default
	 * implementation returns the component element itself.
	 */
	@NonNull
	protected By getInputSelector() {
		return By.cssSelector(getInputSelectorCss());
	}

	/**
	 * Returns a selector for the primary element in this component. The default
	 * implementation returns the component element itself.
	 */
	@NonNull
	protected String getInputSelectorCss() {
		return getSelectorSupplier().get();
	}

	/**
	 * Returns the primary element, if present.
	 */
	@Nullable
	public WebElement getInputElement() {
		return wd().findElement(getInputSelector());
	}

	public boolean isPresent() {
		return wd().isPresent(getSelector());
	}

	/**
	 * Returns true if the control is being displayed (should be visible) on
	 * the page. WARNING: This depends on WebDrivers' implementation which
	 * handles basic concepts of visibility but not things like parts obscured
	 * by css positioning.
	 */
	public boolean isDisplayed() {
		WebElement element = wd().findElement(getSelector());
		if(null == element)
			return false;
		return element.isDisplayed();
	}

	public Supplier<String> getSelectorSupplier() {
		return m_selectorSupplier;
	}

	public By selector(String extra) {
		return By.cssSelector(getSelectorSupplier().get() + " " + extra);
	}

	public String selectorCss(String extra) {
		return getSelectorSupplier().get() + " " + extra;
	}

	public By getSelector() {
		return By.cssSelector(getSelectorSupplier().get());
	}


	/**
	 * Waits until the component is present.
	 */
	public void waitTillPresent() {
		wd().wait(getSelector());
	}

	/**
	 * Waits until the component is present with a timeout.
	 */
	public void waitTillPresent(long time, TimeUnit unit) {
		wd().wait(getSelector(), time, unit);
	}



	/**
	 * Return the text inside the control.
	 */
	public String getText() {
		return wd().getText(getSelector());
	}

	public void waitForElementVisible() {
		WebDriverWait wait = new WebDriverWait(wd().driver(), 5, 100);
		wait.until(ExpectedConditions.visibilityOfElementLocated(getSelector()));
	}

	public void waitForElementInvisible() {
		WebDriverWait wait = new WebDriverWait(wd().driver(), 5, 100);
		wait.until(ExpectedConditions.invisibilityOfElementLocated(getSelector()));
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
