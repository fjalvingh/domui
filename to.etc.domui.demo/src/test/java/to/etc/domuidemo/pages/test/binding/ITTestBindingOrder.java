package to.etc.domuidemo.pages.test.binding;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import to.etc.domuidemo.pages.test.binding.conversion.BindingConversionTestForm;
import to.etc.domuidemo.pages.test.binding.order1.BindingTypeForm1;
import to.etc.domuidemo.pages.test.binding.order1.DoNotBindControlDottedTestPage;
import to.etc.domuidemo.pages.test.binding.order1.TestBindingOrder1;
import to.etc.domui.webdriver.core.AbstractWebDriverTest;

/**
 * Integration Test to check for binding order being obeyed.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 *         Created on 15-3-17.
 */
final public class ITTestBindingOrder extends AbstractWebDriverTest {

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


	/*----------------------------------------------------------------------*/
	/*	CODING:	BindingConversionTestForm									*/
	/*----------------------------------------------------------------------*/

	/**
	 * When we just press "click" without entering anything we should have an empty "value". This caused a NPE
	 * before.
	 *
	 * @throws Exception
	 */
	@Test
	public void testBindingConverter1() throws Exception {
		wd().openScreen(BindingConversionTestForm.class);
		wd().cmd().click().on("button_click");
		String result = wd().getHtmlText("result");
		wd().assertEquals(result, "");
	}

	/**
	 * Enter 123 must properly get a new value 123 after binding.
	 * @throws Exception
	 */
	@Test
	public void testBindingConverter2() throws Exception {
		wd().openScreen(BindingConversionTestForm.class);
		wd().cmd().type("123").on("value");
		wd().cmd().click().on("button_click");
		String result = wd().getHtmlText("result");
		wd().assertEquals(result, "123");
	}

	/**
	 * Enter 123abc must cause a binding error.
	 * @throws Exception
	 */
	@Test
	public void testBindingConverter3() throws Exception {
		wd().openScreen(BindingConversionTestForm.class);
		wd().cmd().type("123abc").on("value");
		wd().cmd().click().on("button_click");
		String result = wd().getHtmlText(By.className("ui-emd-error"));
		wd().assertTrue("There must be an error message", result.contains("123abc"));
		System.out.println(">> " + result);
	}

	/**
	 * When logic moves a new value to the property the control must show that value.
	 * @throws Exception
	 */
	@Test
	public void testBindingConverter4() throws Exception {
		wd().openScreen(BindingConversionTestForm.class);
		wd().cmd().click().on("button_setvalue");
		String result = wd().getValue("value");
		wd().assertEquals(result, "987");
	}

	/**
	 * When logic moves null as a value to the property the control must clear itself.
	 * @throws Exception
	 */
	@Test
	public void testBindingConverter5() throws Exception {
		wd().openScreen(BindingConversionTestForm.class);

		//-- First enter 123
		wd().cmd().type("123").on("value");
		wd().cmd().click().on("button_click");
		String result = wd().getHtmlText("result");
		wd().assertEquals(result, "123");
		wd().cmd().click().on("button_setnull");

		result = wd().getValue("value");
		wd().assertEquals(result, "");
	}

	/**
	 * See https://etc.to/confluence/x/GYA-/
	 */
	@Test
	public void testDoNotBindDottedControlPath() throws Exception {
		wd().openScreen(DoNotBindControlDottedTestPage.class);
		String text = wd().getHtmlText(By.cssSelector(".exc-exception-type"));
		wd().assertTrue("Should have thrown an exception", text.contains("ProgrammerErrorException"));
	}

}
