package to.etc.domui.test.ui.componenterrors;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import to.etc.domui.webdriver.core.ScreenInspector;
import to.etc.domuidemo.pages.test.componenterrors.Text2LayoutTestPage;

import java.awt.image.BufferedImage;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 6-10-17.
 */
public class ITTestText2Behavior extends AbstractLayoutTest {
	@Override
	protected void initializeScreen() throws Exception {
		wd().openScreenIf(this, Text2LayoutTestPage.class);
		//wd().cmd().type("aaaaaaaaa").on("two", "input");
		//wd().wait(By.className("ui-lui-popup"));
		//wd().cmd().type("aaaaaaaaa").on("four", "input");
	}


	@Test
	public void testBigDecimalValue() {
		WebElement input = wd().getElement("t31", "input");
		String value = input.getAttribute("value");
		Assert.assertEquals("123.45", value);
	}

	@Test
	public void testBigDecimalShouldNotAcceptLetters() {
		wd().cmd().type("abc").on("t31", "input");
		WebElement input = wd().getElement("t31", "input");
		String value = input.getAttribute("value");
		Assert.assertEquals("", value);				// Value is cleared by input
	}

	@Test
	public void testIntegerShouldNotAcceptLetters() {
		WebElement input = wd().getElement("t22", "input");
		wd().cmd().type("abc").on("t22", "input");
		String value = input.getAttribute("value");
		Assert.assertEquals("", value);
	}

	@Test
	public void testIntegerMustAcceptNumbers() {
		WebElement input = wd().getElement("t22", "input");
		wd().cmd().type("8754").on("t22", "input");
		String value = input.getAttribute("value");
		Assert.assertEquals("8754", value);
	}

	@Test
	public void mandatoryMustShowError() throws Exception {
		wd().cmd().click().on("button_validate");
		String cssClass = wd().getAttribute("t20", "class");
		System.out.println("css = " + cssClass);
		Assert.assertTrue("The error class should be set on the mandatory field", cssClass.contains("ui-input-err"));

		ScreenInspector inspector = wd().screenInspector();
		if(null == inspector)
			throw new IllegalStateException();
		BufferedImage bi = inspector.elementScreenshot("t20");
		//ImageIO.write(bi, "png", new File("/tmp/test.png"));
		Assert.assertTrue("The background of the control should be red because it is in error after screen refresh", TestHelper.isReddish(bi));
	}
}
