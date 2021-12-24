package to.etc.domui.webdriver.poproxies;

import org.eclipse.jdt.annotation.NonNull;
import org.openqa.selenium.By;
import to.etc.domui.webdriver.core.WebDriverConnector;

import java.util.function.Supplier;

/**
 * The base class for controls that encapsulate HTML input tags.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 24-12-21.
 */
abstract public class AbstractInputControlProxy<T> extends AbstractCpComponent implements ICpControl<T> {
	public AbstractInputControlProxy(WebDriverConnector wd, Supplier<String> selectorProvider) {
		super(wd, selectorProvider);
	}

	@NonNull
	abstract protected By getInputSelector();

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

