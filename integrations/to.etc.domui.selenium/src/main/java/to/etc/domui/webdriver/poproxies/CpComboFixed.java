package to.etc.domui.webdriver.poproxies;

import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.webdriver.core.WebDriverConnector;

import java.util.function.Supplier;

@NonNullByDefault
public class CpComboFixed extends AbstractCpComboComponent implements ICpControl<String> {
	public CpComboFixed(WebDriverConnector connector, Supplier<String> selectorProvider) {
		super(connector, selectorProvider);
	}

	@Override
	protected String getInputSelectorCss() {
		String s = getSelectorSupplier().get();
		return s;
	}
}
