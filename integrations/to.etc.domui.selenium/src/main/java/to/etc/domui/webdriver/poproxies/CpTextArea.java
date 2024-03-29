package to.etc.domui.webdriver.poproxies;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.webdriver.core.WebDriverConnector;

import java.util.function.Supplier;

@NonNullByDefault
public class CpTextArea extends AbstractCpComponent implements ICpControl<String> {
	public CpTextArea(WebDriverConnector connector, Supplier<String> selectorProvider) {
		super(connector, selectorProvider);
	}

	@Override
	public void setValue(@Nullable String value) {
		wd().cmd().type(value).on(getSelector());
	}

	@Nullable
	@Override
	public String getValue() {
		return wd().getElement(getSelector()).getText();
	}

	@Override
	public boolean isReadonly() {
		return wd().isReadonly(getSelector());
	}

	@Override
	public boolean isDisabled() {
		return !wd().isEnabled(getSelector());
	}
}
