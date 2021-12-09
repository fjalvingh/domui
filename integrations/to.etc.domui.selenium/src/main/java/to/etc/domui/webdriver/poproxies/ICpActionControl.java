package to.etc.domui.webdriver.poproxies;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public interface ICpActionControl {
	void click() throws Exception;

	boolean isDisabled() throws Exception;
}
