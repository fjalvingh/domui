package to.etc.domui.webdriver.poproxies;

import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.webdriver.core.WebDriverConnector;

import java.util.function.Supplier;

@NonNullByDefault
public class CpButton extends AbstractCpComponent implements ICpActionControl {
	public CpButton(WebDriverConnector wd, Supplier<String> selectorProvider) {
		super(wd, selectorProvider);
	}

	@Override
	public void click() {
		wd().cmd().click().on(getSelector());
	}

	@Override
	public boolean isDisabled() {
		return !wd().isEnabled(getSelector());
	}
}
