package to.etc.domui.webdriver.core.base;

import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.webdriver.core.WebDriverConnector;

@NonNullByDefault
public class TextareaPO extends AbstractTextPO {
	public TextareaPO(WebDriverConnector connector, String id) {
		super(connector, connector.byId(id));
	}
}
