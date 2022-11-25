package to.etc.domui.webdriver.poproxies;

import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.webdriver.core.WebDriverConnector;

import java.util.function.Supplier;

@NonNullByDefault
public class CpComboFixed2 extends AbstractCpComboComponent implements ICpControl<String> {
	public CpComboFixed2(WebDriverConnector connector, Supplier<String> selectorProvider) {
		super(connector, selectorProvider);
	}
}
