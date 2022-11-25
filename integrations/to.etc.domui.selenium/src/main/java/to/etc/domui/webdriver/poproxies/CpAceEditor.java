package to.etc.domui.webdriver.poproxies;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.webdriver.core.WebDriverConnector;

import java.util.function.Supplier;

@NonNullByDefault
public class CpAceEditor extends AbstractCpComponent implements ICpControl<String> {

	public CpAceEditor(WebDriverConnector wd, Supplier<String> selectorProvider) {
		super(wd, selectorProvider);
	}

	@Override
	public void setValue(@Nullable String value) {
		getJsExecutor().executeScript(getAceJsReference() + ".setValue('" + (value == null ? "" : value) + "')");
	}

	@Nullable
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
		var editor = wd().findElement(getSelector());
		if(editor == null) {
			throw new IllegalStateException("Can't find Ace Editor with selector: " + getSelector());
		}
		return editor.getAttribute("id");
	}
}
