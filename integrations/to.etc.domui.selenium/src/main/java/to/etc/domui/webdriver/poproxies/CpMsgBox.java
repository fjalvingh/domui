package to.etc.domui.webdriver.poproxies;

import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.webdriver.core.WebDriverConnector;

import java.util.function.Supplier;

@NonNullByDefault
public class CpMsgBox extends AbstractCpComponent {
	public CpMsgBox(WebDriverConnector connector, Supplier<String> selectorProvider) {
		super(connector, selectorProvider);
	}

	@Override
	public String getText() {
		var el = wd().findElement(selector(".ui-mbx-mc"));
		if(el == null) {
			throw new IllegalStateException("Can't find MsgBox " + getSelectorSupplier().get());
		}
		return el.getText();
	}
}
