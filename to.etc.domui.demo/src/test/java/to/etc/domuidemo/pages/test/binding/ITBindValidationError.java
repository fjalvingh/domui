package to.etc.domuidemo.pages.test.binding;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import to.etc.domui.webdriver.core.AbstractWebDriverTest;
import to.etc.domuidemo.pages.test.binding.binderrors.BindvalidationErrorPage;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 10-11-22.
 */
final public class ITBindValidationError extends AbstractWebDriverTest {
	@Test
	public void testValidationErrorIsShown() throws Exception {
		wd().openScreen(BindvalidationErrorPage.class);

		//-- Initially the value shown must be "bad"
		String value = wd().getValue("text");
		Assert.assertEquals("The initial value in the control must be correct", "bad", value);

		//-- Now: enter another (incorrect) value
		String incorrectValue = "incorrect";
		wd().cmd().type(incorrectValue).on("text");
		wd().cmd().click().on("click");

		//-- We should have a failure
		wd().wait(By.className("test-failed"), 2, TimeUnit.SECONDS);

		String newval = wd().getValue("text");
		Assert.assertEquals("The incorrect value entered should still be seen in the control", incorrectValue, newval);
	}
}
