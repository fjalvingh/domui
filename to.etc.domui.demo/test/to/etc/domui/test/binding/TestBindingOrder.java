package to.etc.domui.test.binding;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import to.etc.domui.test.binding.order1.BindingTypeForm1;
import to.etc.domui.test.binding.order1.TestBindingOrder1;
import to.etc.domui.webdriver.core.AbstractWebDriverTest;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 *         Created on 15-3-17.
 */
public class TestBindingOrder extends AbstractWebDriverTest {

	/**
	 * Two bindings that influence each order should work.
	 * https://etc.to/confluence/display/DOM/Tests%3A+data+binding
	 * @throws Exception
	 */
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
	}


	/**
	 * Binding between property and control of different type must result in an error.
	 * @throws Exception
	 */
	@Test
	public void testBindingTypes1() throws Exception {
		wd().openScreen(BindingTypeForm1.class);
		String text = wd().getHtmlText(By.cssSelector(".exc-exception-type"));
		wd().assertTrue("Should have thrown an exception", text.contains("BindingDefinitionException"));
	}
}
