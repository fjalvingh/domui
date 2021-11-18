package to.etc.domui.webdriver.core.base;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.webdriver.core.UrlDoesNotContain;
import to.etc.domui.webdriver.core.WebDriverConnector;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@NonNullByDefault
public abstract class BasePagePO<T extends UrlPage> extends BasePO {

	private final Class<T> m_clazz;

	public BasePagePO(WebDriverConnector wd, Class<T> clazz) {
		super(wd);
		m_clazz = clazz;
	}

	public void open(Object... parameters) throws Exception {
		wd().openScreen(m_clazz, parameters);
	}

	public boolean isBrowserOnThisPage() {
		var body = wd().findElement(By.tagName("body"));
		if(body == null) {
			return false;
		}
		return body.getAttribute("pagename").equalsIgnoreCase(getClazz().getName());
	}

	public Map<String, String> getPageParameters() throws Exception{
		wd().driver().getCurrentUrl();
		var list = URLEncodedUtils.parse(new URI(wd().driver().getCurrentUrl()), StandardCharsets.UTF_8);
		return list.stream().collect(toMap(NameValuePair::getName, NameValuePair::getValue));
	}

	public void waitUntilPageContains(String part) {
		var wait = new WebDriverWait(wd().driver(), wd().getWaitInterval());
		wait.until(ExpectedConditions.urlContains(part));
	}

	public void waitUntilPageDoesNotContain(String part) {
		var wait = new WebDriverWait(wd().driver(), wd().getWaitInterval());
		wait.until(new UrlDoesNotContain(part));
	}

	public String getCurrentUrl() {
		return wd().driver().getCurrentUrl();
	}

	protected Class<T> getClazz() {
		return m_clazz;
	}
}
