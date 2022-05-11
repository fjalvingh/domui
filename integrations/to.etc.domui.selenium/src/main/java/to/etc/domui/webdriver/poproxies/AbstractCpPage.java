package to.etc.domui.webdriver.poproxies;

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
public abstract class AbstractCpPage<T extends UrlPage> implements ICpDriverSource {
	private WebDriverConnector m_wd;

	private final Class<T> m_pageClass;

	public AbstractCpPage(WebDriverConnector wd, Class<T> clazz) {
		m_wd = wd;
		m_pageClass = clazz;
	}

	public void open(Object... parameters) throws Exception {
		wd().openScreen(m_pageClass, parameters);
	}

	public void waitTillPresent() {
		wd().wait(By.tagName("body"));
	}

	public boolean isBrowserOnThisPage() {
		var body = wd().findElement(By.tagName("body"));
		if(body == null) {
			return false;
		}
		String pagename = body.getAttribute("pagename");
		if(null != pagename)
			return pagename.equalsIgnoreCase(getPageClass().getName());
		String currentURL = wd().getCurrentURL();
		if(null == currentURL)
			return false;
		return currentURL.contains(m_pageClass.getName() + ".ui");
	}

	public Map<String, String> getPageParameters() throws Exception {
		wd().driver().getCurrentUrl();
		var list = URLEncodedUtils.parse(new URI(wd().driver().getCurrentUrl()), StandardCharsets.UTF_8);
		return list.stream().collect(toMap(NameValuePair::getName, NameValuePair::getValue));
	}

	public void waitUntilPageUrlContains(String part) {
		var wait = new WebDriverWait(wd().driver(), wd().getWaitInterval());
		wait.until(ExpectedConditions.urlContains(part));
	}

	public void waitUntilPageUrlDoesNotContain(String part) {
		var wait = new WebDriverWait(wd().driver(), wd().getWaitInterval());
		wait.until(new UrlDoesNotContain(part));
	}

	public String getCurrentUrl() {
		return wd().driver().getCurrentUrl();
	}

	protected Class<T> getPageClass() {
		return m_pageClass;
	}

	@Override
	public WebDriverConnector wd() {
		return m_wd;
	}
}