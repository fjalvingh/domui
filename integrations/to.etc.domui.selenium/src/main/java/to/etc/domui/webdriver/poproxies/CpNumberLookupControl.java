package to.etc.domui.webdriver.poproxies;

import to.etc.domui.webdriver.core.WebDriverConnector;

import java.util.function.Supplier;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 24-12-21.
 */
public class CpNumberLookupControl extends CpText2 {
	public CpNumberLookupControl(WebDriverConnector connector, Supplier<String> selectorProvider) {
		super(connector, selectorProvider);
	}
}
