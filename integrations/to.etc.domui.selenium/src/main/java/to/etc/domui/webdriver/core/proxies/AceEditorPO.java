package to.etc.domui.webdriver.core.proxies;

import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.webdriver.core.WebDriverConnector;

@NonNullByDefault
public class AceEditorPO extends ComponentPO implements IControlPO<String> {

	public AceEditorPO(WebDriverConnector wd, String testId) {
		super(wd, testId);
	}

	@Override
	public void setValue(String value) {
		getJsExecutor().executeScript(getAceJsReference() + ".setValue('" + value + "')");
	}

	@Override
	public String getValue() {
		Object val = getJsExecutor().executeScript("return " + getAceJsReference() + ".getSession().getValue();");
		if(val instanceof String) {
			return (String) val;
		}
		throw new IllegalStateException("Value is not a string?");
	}

	@Override
	public boolean isReadonly() {
		return "true".equals(getJsExecutor().executeScript("return " + getAceJsReference() + ".$readOnly"));
	}

	@Override
	public boolean isDisabled() {
		return isReadonly();
	}

	private String getAceJsReference() {
		return "window['" + getActualId() + "']";
	}

	private String getActualId() {
		var editor = wd().findElement(getTestId());
		if(editor == null) {
			throw new IllegalStateException("Can't find Ace Editor with testid: " + getTestId());
		}
		return editor.getAttribute("id");
	}
}
