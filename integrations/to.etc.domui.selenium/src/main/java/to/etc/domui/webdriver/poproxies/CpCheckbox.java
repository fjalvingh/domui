package to.etc.domui.webdriver.poproxies;

import org.eclipse.jdt.annotation.NonNull;
import org.openqa.selenium.By;
import to.etc.domui.webdriver.core.WebDriverConnector;

import java.util.function.Supplier;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-12-21.
 */
public class CpCheckbox extends AbstractInputControlProxy<Boolean> implements ICpControl<Boolean> {
	public CpCheckbox(WebDriverConnector wd, Supplier<String> selectorProvider) {
		super(wd, selectorProvider);
	}

	@NonNull
	@Override
	protected By getInputSelector() {
		return getSelector();
	}

	@Override
	public void setValue(Boolean value) throws Exception {
		wd().cmd().check(value.booleanValue()).on(getSelector());
	}

	public boolean isChecked() throws Exception {
		return getValue() == Boolean.TRUE;
	}

	public void setChecked(boolean v) throws Exception {
		setValue(Boolean.valueOf(v));
	}

	@Override
	public Boolean getValue() throws Exception {
		return Boolean.valueOf(wd().isChecked(getSelector()));
	}
}