package to.etc.domui.webdriver.poproxies;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openqa.selenium.By;
import to.etc.domui.webdriver.core.WebDriverConnector;

import java.util.function.Supplier;

@NonNullByDefault
public class CpText2 extends AbstractCpInputControl<String> implements ICpControl<String> {
	public CpText2(WebDriverConnector connector, Supplier<String> selectorProvider) {
		super(connector, selectorProvider);
	}

	@Override
	public void setValue(@Nullable String value) {
		wd().cmd().type(value == null ? "" : value).on(getInputSelector());
		wd().wait(getSelector());
	}

	@Nullable
	@Override
	public String getValue() {
		var elem = wd().getElement(getInputSelector());
		return wd().getValue(getInputSelector());
	}

	@Override
	@NonNull
	protected By getInputSelector() {
		return selector("INPUT");
	}
}
