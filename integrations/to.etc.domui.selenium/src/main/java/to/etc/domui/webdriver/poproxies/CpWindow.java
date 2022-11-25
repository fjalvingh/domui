package to.etc.domui.webdriver.poproxies;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import to.etc.domui.webdriver.core.WebDriverConnector;

import java.util.function.Supplier;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 10-10-22.
 */
public class CpWindow extends AbstractCpComponent {
	public CpWindow(WebDriverConnector wd, Supplier<String> selectorSupplier) {
		super(wd, selectorSupplier);
	}

	/**
	 * Press the "close window" button top right in the title bar.
	 */
	public CpWindow close() {
		By buttonSel = selector("ui-flw-btn-close");
		WebElement button = wd().getElement(buttonSel);
		button.click();
		return this;
	}
}
