package to.etc.domui.webdriver.poproxies;

import to.etc.domui.webdriver.core.WebDriverConnector;

import java.util.function.Supplier;

/**
 * The base class for controls that encapsulate HTML input tags.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 24-12-21.
 */
abstract public class AbstractCpInputControl<T> extends AbstractCpComponent implements ICpControl<T> {
	public AbstractCpInputControl(WebDriverConnector wd, Supplier<String> selectorProvider) {
		super(wd, selectorProvider);
	}

	public void click() {
		wd().cmd().click().on(getInputSelector());
	}

	@Override
	public boolean isDisabled() {
		return !wd().isEnabled(getInputSelector());
	}

	@Override
	public boolean isReadonly() throws Exception {
		return wd().isReadonly(getInputSelector());
	}
}

