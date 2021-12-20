package to.etc.domui.webdriver.poproxies;

import to.etc.domui.webdriver.core.WebDriverConnector;

import java.util.function.Supplier;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-12-21.
 */
public class CpCheckbox extends AbstractCpComponent implements ICpControl<Boolean> {
	public CpCheckbox(WebDriverConnector wd, Supplier<String> selectorProvider) {
		super(wd, selectorProvider);
	}

	public void click() {
		wd().cmd().click().on(getSelector());
	}

	@Override
	public boolean isDisabled() {
		return !wd().isEnabled(getSelector());
	}

	@Override
	public void setValue(Boolean value) throws Exception {

	}

	@Override
	public Boolean getValue() throws Exception {
		return wd().isChecked(getSelector());
	}

	@Override
	public boolean isReadonly() throws Exception {
		return wd().isReadonly(getSelector());
	}
}
