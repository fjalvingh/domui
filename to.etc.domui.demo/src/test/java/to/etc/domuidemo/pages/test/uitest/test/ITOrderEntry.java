package to.etc.domuidemo.pages.test.uitest.test;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import to.etc.domui.webdriver.core.AbstractWebDriverTest;
import to.etc.domuidemo.pages.test.uitest.OrderEntryTestPage;

/**
 * The order entry page, tested with the WebDriver connector alone: every
 * element is addressed by its testid or by a css selector written here. The
 * same two tests written against the generated page object are in
 * {@link ITOrderEntryPageObject}.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class ITOrderEntry extends AbstractWebDriverTest {
	/** The Order link of the second row of the basket. */
	static private final By ORDER_REVOLVER = By.cssSelector("*[testid='basket'] tbody tr:nth-child(2) a");

	@Test
	public void orderingSaysWhatWasOrdered() throws Exception {
		wd().openScreen(OrderEntryTestPage.class);

		wd().cmd().type("Ozymandias").on("customer", "input");
		wd().cmd().click().on(ORDER_REVOLVER);

		Assert.assertEquals("Ozymandias ordered 1 x Revolver, Standard, total 16.45", wd().getText("answer"));
	}

	@Test
	public void anEmptyCustomerStopsTheOrder() throws Exception {
		wd().openScreen(OrderEntryTestPage.class);

		wd().cmd().click().on(ORDER_REVOLVER);

		//-- The mandatory customer field reports itself, and the handler never got past it.
		Assert.assertTrue("The customer field should be marked as being in error",
			wd().getAttribute("customer", "class").contains("ui-input-err"));
		Assert.assertEquals("Nothing ordered yet", wd().getText("answer"));
	}
}
