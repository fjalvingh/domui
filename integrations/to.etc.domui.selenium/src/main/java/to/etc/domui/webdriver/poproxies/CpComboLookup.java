package to.etc.domui.webdriver.poproxies;

import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.webdriver.core.WebDriverConnector;

import java.util.function.Supplier;

@NonNullByDefault
public class CpComboLookup extends AbstractCpComboComponent implements ICpControl<String> {
	public CpComboLookup(WebDriverConnector connector, Supplier<String> selectorProvider) {
		super(connector, selectorProvider);
	}
}
