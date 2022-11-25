package to.etc.domui.webdriver.poproxies;

import to.etc.domui.webdriver.core.WebDriverConnector;

import java.util.function.Supplier;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 24-12-21.
 */
public class CpDisplaySpan extends CpNodeAsText {
	public CpDisplaySpan(WebDriverConnector wd, Supplier<String> selectorProvider) {
		super(wd, selectorProvider);
	}
}
