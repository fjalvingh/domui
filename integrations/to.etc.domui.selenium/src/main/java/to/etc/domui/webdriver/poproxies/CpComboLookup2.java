package to.etc.domui.webdriver.poproxies;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openqa.selenium.support.ui.Select;
import to.etc.domui.webdriver.core.WebDriverConnector;

import java.util.function.Supplier;

@NonNullByDefault
public class CpComboLookup2 extends AbstractCpComboComponent implements ICpControl<String> {
	public CpComboLookup2(WebDriverConnector connector, Supplier<String> selectorProvider) {
		super(connector, selectorProvider);
	}

	public Select getOptions() {
		var element = wd().getElement(getSelector());
		Select option = new Select(element);
		return option;
	}
}
