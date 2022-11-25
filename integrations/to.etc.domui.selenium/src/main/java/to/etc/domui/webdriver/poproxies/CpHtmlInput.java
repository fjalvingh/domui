package to.etc.domui.webdriver.poproxies;

import to.etc.domui.webdriver.core.WebDriverConnector;

import java.util.function.Supplier;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-12-21.
 */
public class CpHtmlInput extends AbstractCpComponent implements ICpControl<String> {
	public CpHtmlInput(WebDriverConnector connector, Supplier<String> selectorProvider) {
		super(connector, selectorProvider);
	}

	@Override
	public void setValue(String value) {
		wd().cmd().type(value).on(getSelector());
		wd().wait(getSelector());
	}

	@Override
	public String getValue() {
		var elem = wd().getElement(getSelector());
		return elem.getText();
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
