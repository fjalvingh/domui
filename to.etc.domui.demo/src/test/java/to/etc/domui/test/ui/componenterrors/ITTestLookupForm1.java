package to.etc.domui.test.ui.componenterrors;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import to.etc.domui.webdriver.core.AbstractWebDriverTest;
import to.etc.domuidemo.pages.test.componenterrors.LookupForm1TestPage;
import to.etc.domuidemo.pages.test.componenterrors.LookupForm2TestPage;
import to.etc.webapp.ProgrammerErrorException;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 27-9-17.
 */
public class ITTestLookupForm1 extends AbstractWebDriverTest {
	/**
	 * Github issue #6: mandatory lookup controls must have clearInput(), and in that case should work correctly.
	 * @throws Exception
	 */
	@Test
	public void testClearButtonWithClearInputShouldNotError() throws Exception {
		wd().openScreen(LookupForm1TestPage.class);

		//-- Clicking "clear" then search should not give an error
		wd().cmd().click().on("clearButton");
		wd().cmd().click().on("searchButton");

		String cssClass = wd().getAttribute("album", "class");
		System.out.println("css = " + cssClass);
		Assert.assertFalse("The error class should not be set on the album combolookup2", cssClass.contains("ui-input-err"));
	}

	@Test
	public void testMandControlShouldExceptionWhenClickedWithoutClearInput() throws Exception {
		wd().openScreen(LookupForm2TestPage.class);

		//-- Clicking "clear" then search should not give an error
		wd().cmd().click().on("clearButton");

		String txt = wd().getHtmlText(By.className("exc-exception-type"));
		Assert.assertEquals("Must have thrown the correct exception", ProgrammerErrorException.class.getCanonicalName(), txt);
	}

}
