package to.etc.domui.webdriver.core.proxies;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface IActionControlPO {

	void click() throws Exception;

	boolean isDisabled() throws Exception;
}
