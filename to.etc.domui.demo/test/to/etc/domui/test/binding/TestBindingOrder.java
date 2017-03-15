package to.etc.domui.test.binding;

import org.junit.Assert;
import org.junit.Test;
import to.etc.domui.test.binding.order1.TestBindingOrder1;
import to.etc.domui.webdriver.core.AbstractWebDriverTest;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 *         Created on 15-3-17.
 */
public class TestBindingOrder extends AbstractWebDriverTest {
	@Test
	public void testBinding1() throws Exception {
		wd().openScreen(TestBindingOrder1.class);

		//-- Switching country should switch city
		wd().select("country", "Netherlands");
		String city = wd().selectGetSelectedLabel("city");
		Assert.assertEquals("Amsterdam", city);

		wd().select("country", "USA");
		city = wd().selectGetSelectedLabel("city");
		Assert.assertEquals("New York", city);

		//-- Switching city to a city in another country should  change country
		wd().select("city", "Lelystad");
		city = wd().selectGetSelectedLabel("country");
		Assert.assertEquals("Netherlands", city);

		//-- Switching city to another in the same country
		wd().select("city", "Amsterdam");
		city = wd().selectGetSelectedLabel("country");
		Assert.assertEquals("Netherlands", city);

		Thread.sleep(10000);
	}
}
