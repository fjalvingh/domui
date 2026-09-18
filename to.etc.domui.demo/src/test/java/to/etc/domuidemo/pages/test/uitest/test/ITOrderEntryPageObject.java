package to.etc.domuidemo.pages.test.uitest.test;

import org.junit.Assert;
import org.junit.Test;
import to.etc.domui.webdriver.core.AbstractWebDriverTest;

/**
 * The order entry page, tested through its page object: the test says what it
 * does, and every selector sits in the generated page object instead of in the
 * test. Compare with {@link ITOrderEntry}, which does the same without one.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class ITOrderEntryPageObject extends AbstractWebDriverTest {
	@Test
	public void orderingSaysWhatWasOrdered() throws Exception {
		POOrderEntryTestPage page = new POOrderEntryTestPage(wd());
		page.open();

		page.customer().setValue("Ozymandias");
		page.shipping().setValue("Express");
		page.copies().setValue("2");
		page.order("Revolver");

		Assert.assertEquals("Ozymandias ordered 2 x Revolver, Express, total 34.95", page.answer().getText());
	}

	@Test
	public void theBasketShowsWhatIsForSale() throws Exception {
		POOrderEntryTestPage page = new POOrderEntryTestPage(wd());
		page.open();

		Assert.assertEquals(3, page.basket().getVisibleRowCount());
		Assert.assertEquals("Rubber Soul", page.basket().row(0).album().getText());
		Assert.assertEquals("14.95", page.basket().row(0).priceEach().getText());
	}

	@Test
	public void anEmptyCustomerStopsTheOrder() throws Exception {
		POOrderEntryTestPage page = new POOrderEntryTestPage(wd());
		page.open();

		page.order("Revolver");

		Assert.assertEquals("Nothing ordered yet", page.answer().getText());
	}
}
