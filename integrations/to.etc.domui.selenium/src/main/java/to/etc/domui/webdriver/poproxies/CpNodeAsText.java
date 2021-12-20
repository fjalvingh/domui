package to.etc.domui.webdriver.poproxies;

import to.etc.domui.webdriver.core.WebDriverConnector;

import java.util.function.Supplier;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 09-12-21.
 */
public class CpNodeAsText extends AbstractCpComponent implements ICpActionControl {
	public CpNodeAsText(WebDriverConnector wd, Supplier<String> selectorProvider) {
		super(wd, selectorProvider);
	}

	@Override
	public void click() {
		wd().cmd().click().on(getSelector());
	}

	@Override
	public boolean isDisabled() {
		return !wd().isEnabled(getSelector());
	}
}
