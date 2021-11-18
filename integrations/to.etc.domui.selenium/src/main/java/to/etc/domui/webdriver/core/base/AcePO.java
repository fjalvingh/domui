package to.etc.domui.webdriver.core.base;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openqa.selenium.JavascriptExecutor;
import to.etc.domui.webdriver.core.WebDriverConnector;

@NonNullByDefault
public class AcePO extends BasePO {

	private final String m_testId;

	public AcePO(WebDriverConnector wd, String testId){
		super(wd);
		m_testId = testId;
	}

	public void setValue(String value) {
		String actualId = getActualId();
		jsExecutor().executeScript("window['"+actualId+"'].setValue('"+value+"')");
	}

	private String getActualId() {
		var editor = wd().findElement(m_testId);
		if(editor == null) {
			throw new IllegalStateException("Can't find Ace Editor with testid: "+ m_testId);
		}
		return editor.getAttribute("id");
	}

	protected JavascriptExecutor jsExecutor() {
		if(wd().driver() instanceof JavascriptExecutor) {
			return (JavascriptExecutor) wd().driver();
		}
		throw new IllegalStateException("Browser does not support javascript");
	}

	public String getValue() {
		String actualId = getActualId();
		Object val = jsExecutor().executeScript("return window['"+actualId+"'].getSession().getValue();");
		if(val instanceof String) {
			return (String) val;
		}
		throw new IllegalStateException("Value is not a string?");
	}
}
