package to.etc.domuidemo.pages.test.proxies.test;

import org.junit.Assert;
import org.junit.Test;
import to.etc.domui.webdriver.core.AbstractWebDriverTest;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 28-12-21.
 */
public class ITTestProxiesPage1 extends AbstractWebDriverTest {
	@Test
	public void testText2() throws Exception {
		POProxyTestPage1 page = new POProxyTestPage1(wd());
		page.open();
		String value = page.text2().getValue();
		Assert.assertTrue("Initial content must be empty", value != null && value.length() == 0);

		//-- Set a value
		page.text2().setValue("Hello");
		value = page.text2().getValue();
		Assert.assertEquals("Value should have been set", "Hello", value);

		Assert.assertFalse("Component must be not readonly", page.text2().isReadonly());
		Assert.assertFalse("Component must be not readonly", page.text2().isDisabled());
		Assert.assertTrue("Component must be displayed", page.text2().isDisplayed());
	}

	@Test
	public void testText() throws Exception {
		POProxyTestPage1 page = new POProxyTestPage1(wd());
		page.open();
		String value = page.text().getValue();
		Assert.assertTrue("Initial content must be empty", value != null && value.length() == 0);

		//-- Set a value
		page.text().setValue("Hello");
		value = page.text().getValue();
		Assert.assertEquals("Value should have been set", "Hello", value);

		Assert.assertFalse("Component must be not readonly", page.text().isReadonly());
		Assert.assertFalse("Component must be not readonly", page.text().isDisabled());
		Assert.assertTrue("Component must be displayed", page.text().isDisplayed());
	}

	@Test
	public void testFixed2() throws Exception {
		POProxyTestPage1 page = new POProxyTestPage1(wd());
		page.open();
		String value = page.cf2().getValue();
		Assert.assertTrue("Initial content must be empty", value != null && value.length() == 0);

		//-- Set a value
		page.cf2().setValue("Ozymandias");
		value = page.cf2().getValue();
		Assert.assertEquals("Value should have been set", "Ozymandias", value);

		Assert.assertFalse("Component must be not readonly", page.cf2().isReadonly());
		Assert.assertFalse("Component must be not readonly", page.cf2().isDisabled());
		Assert.assertTrue("Component must be displayed", page.cf2().isDisplayed());
	}

	@Test
	public void testFixed() throws Exception {
		POProxyTestPage1 page = new POProxyTestPage1(wd());
		page.open();
		String value = page.cf().getValue();
		Assert.assertTrue("Initial content must be empty", value != null && value.length() == 0);

		//-- Set a value
		page.cf().setValue("Ozymandias");
		value = page.cf().getValue();
		Assert.assertEquals("Value should have been set", "Ozymandias", value);

		Assert.assertFalse("Component must be not readonly", page.cf().isReadonly());
		Assert.assertFalse("Component must be not readonly", page.cf().isDisabled());
		Assert.assertTrue("Component must be displayed", page.cf().isDisplayed());
	}

	@Test
	public void testCheckboxButton() throws Exception {
		POProxyTestPage1 page = new POProxyTestPage1(wd());
		page.open();
		Boolean value = page.cbb().getValue();
		Assert.assertTrue("Initial content must be false", value != null && !value);

		page.cbb().setValue(true);
		value = page.cbb().getValue();
		Assert.assertTrue("New content must be true", value != null && value);

		page.cbb().click();
		value = page.cbb().getValue();
		Assert.assertTrue("Click on true must become false", value != null && !value);
	}

	@Test
	public void testCheckbox() throws Exception {
		POProxyTestPage1 page = new POProxyTestPage1(wd());
		page.open();
		Boolean value = page.checkbox().getValue();
		Assert.assertTrue("Initial content must be false", value != null && !value);

		page.checkbox().setValue(true);
		value = page.checkbox().getValue();
		Assert.assertTrue("New content must be true", value != null && value);

		page.checkbox().click();
		value = page.checkbox().getValue();
		Assert.assertTrue("Click on true must become false", value != null && !value);
	}

	@Test
	public void testDefaultButton() throws Exception {
		POProxyTestPage1 page = new POProxyTestPage1(wd());
		page.open();

		page.defbtn().click();
		String value = page.getDefaultButtonValue().getText();
		Assert.assertEquals("First click should show Ping", "Ping", value);

		page.defbtn().click();
		value = page.getDefaultButtonValue().getText();
		Assert.assertEquals("Second click should show Pong", "Pong", value);
	}




}
