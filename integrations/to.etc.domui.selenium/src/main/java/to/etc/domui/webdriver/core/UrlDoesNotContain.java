package to.etc.domui.webdriver.core;

import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;

public class UrlDoesNotContain implements ExpectedCondition<Boolean> {

	private String m_part;

	public UrlDoesNotContain(String part){
		m_part = part;
	}

	@Override
	@NotNull
	public Boolean apply(WebDriver webDriver) {
		if(webDriver == null) {
			return Boolean.FALSE;
		}
		return Boolean.valueOf(!webDriver.getCurrentUrl().contains(m_part));
	}
}
