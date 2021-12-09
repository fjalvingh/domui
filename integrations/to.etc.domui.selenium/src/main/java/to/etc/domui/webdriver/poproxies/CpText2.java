package to.etc.domui.webdriver.poproxies;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openqa.selenium.By;
import to.etc.domui.webdriver.core.WebDriverConnector;

import java.util.function.Supplier;

@NonNullByDefault
public class CpText2 extends AbstractCpComponent implements ICpControl<String> {

	public CpText2(WebDriverConnector connector, Supplier<String> selectorProvider) {
		super(connector, selectorProvider);
	}

	@Override
	public void setValue(String value) {
		wd().cmd().type(value).on(getInputLocator());
		wd().wait(getSelector());
	}

	@Override
	public String getValue() {
		var elem = wd().getElement(getInputLocator());
		return elem.getText();
	}

	@Override
	public boolean isReadonly() {
		return wd().isReadonly(getInputLocator());
	}

	@Override
	public boolean isDisabled() {
		return !wd().isEnabled(getSelector());
	}

	/**
	 * Should obviously not exist.
	 */
	@Deprecated
	@NonNull
	private By getInputLocator() {
		return selector("INPUT");
	}
}
