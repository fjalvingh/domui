package to.etc.domui.webdriver.poproxies;

import to.etc.domui.webdriver.core.WebDriverConnector;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 08-12-21.
 */
public class AbstractPageObject {
	private final WebDriverConnector m_connector;

	public AbstractPageObject(WebDriverConnector connector) {
		m_connector = connector;
	}

	public WebDriverConnector wd() {
		return m_connector;
	}
}
