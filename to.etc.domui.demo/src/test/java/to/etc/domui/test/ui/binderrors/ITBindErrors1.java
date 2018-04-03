package to.etc.domui.test.ui.binderrors;

import org.junit.Test;
import org.openqa.selenium.By;
import to.etc.domui.webdriver.core.AbstractWebDriverTest;
import to.etc.domuidemo.pages.test.binding.binderrors.BindError1Page;
import to.etc.domuidemo.pages.test.binding.binderrors.BindError2Page;

/**
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3-4-18.
 */
public class ITBindErrors1  extends AbstractWebDriverTest {
	/**
	 * Test for the following:
	 *
	 * - a mandatoty control bound to a model has an initial value through the model (i.e. the start value is not null)
	 * - clear the value in the control, then press the click button which validates the bindings
	 *
	 * expected result: as the control is now empty it is invalid, and a mandatory error must be shown.
	 * actual result before: the validation fails, but no message is shown.
	 */
	@Test
	public void emptyingControlMustGiveBindingError() throws Exception {
		wd().openScreen(BindError1Page.class);

		wd().cmd().type("").on("edit", " input");					// Clear the input field

		// Pressing validate should make the 2nd editor be with an error background
		wd().cmd().click().on("button_click");

		// And now we must have a "mandatory" error.
		wd().assertTrue("we must have an error panel", wd().findElement(By.className("ui-emd-error")) != null);
		String htmlText = wd().getHtmlText(By.className("ui-emd-error"));
		System.out.println(" >> " + htmlText);
		wd().assertTrue("There must be a mandatory error message", htmlText.contains("Mandatory"));
	}

	@Test
	public void emptyingLookupMustSetBindingToNull() throws Exception {
		wd().openScreen(BindError2Page.class);

		wd().cmd().click().on("edit-clear");
		wd().cmd().click().on("button_click");

		// And now we must have a "mandatory" error.
		wd().assertTrue("we must have an error panel", wd().findElement(By.className("ui-emd-error")) != null);
		String htmlText = wd().getHtmlText(By.className("ui-emd-error"));
		System.out.println(" >> " + htmlText);
		wd().assertTrue("There must be a mandatory error message", htmlText.contains("Mandatory"));

		//-- And the binding must have been set to null
		wd().assertTrue("We must have a message stating that the binding has been set to null", wd().findElement(By.className("success")) != null);

	}


}
