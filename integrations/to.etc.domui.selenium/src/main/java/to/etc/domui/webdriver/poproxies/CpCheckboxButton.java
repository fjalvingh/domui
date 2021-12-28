package to.etc.domui.webdriver.poproxies;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.openqa.selenium.By;
import to.etc.domui.webdriver.core.WebDriverConnector;

import java.util.function.Supplier;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 24-12-21.
 */
public class CpCheckboxButton extends CpCheckbox {
	public CpCheckboxButton(WebDriverConnector wd, Supplier<String> selectorProvider) {
		super(wd, selectorProvider);
	}

	@NonNull
	@Override
	protected By getInputSelector() {
		return selector("input");
	}

	@Override
	public void setValue(@Nullable Boolean value) throws Exception {
		if(value == null)
			return;
		boolean current = isChecked();
		if(value.booleanValue() == current)
			return;
		click();
		current = isChecked();
		if(current != value.booleanValue())
			throw new IllegalStateException("Unexpected: could not set checkbox value");
	}

	@Override
	public void click() {
		wd().cmd().click().on(getSelector());
	}
}
