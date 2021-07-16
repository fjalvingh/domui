package to.etc.domui.webdriver.core.base;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openqa.selenium.By;
import to.etc.domui.webdriver.core.WebDriverConnector;

@NonNullByDefault
public class MsgBoxPO extends BasePO {

	private String m_id;

	public MsgBoxPO(WebDriverConnector connector, String id) {
		super(connector);
		m_id = id;
	}

	public String getText() {
		var el = wd().findElement(createLocator());
		if(el == null) {
			throw new IllegalStateException("Can't find MessageBox with id" + m_id);
		}
		return el.getText();
	}

	private By createLocator() {
		return By.cssSelector("*[testid='" + m_id + "'] .ui-mbx-mc");
	}
}
