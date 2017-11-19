package to.etc.domui.test.ui.componenterrors;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import to.etc.domui.test.ui.imagehelper.ImageHelper;
import to.etc.domui.webdriver.core.AbstractWebDriverTest;
import to.etc.domui.webdriver.core.ScreenInspector;
import to.etc.domuidemo.pages.test.componenterrors.LookupInputTestPage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-8-17.
 */
public class ITTestLookupInput extends AbstractWebDriverTest {
	public ITTestLookupInput() {
		System.out.println("CONSTRUCTED");
	}

	/**
	 * The page contains a mandatory LookupInput2. The 1st one with a text, the second without,
	 * plus a validate button to check data.
	 *
	 * @throws Exception
	 */
	@Test
	public void testShowBindingError() throws Exception {
		wd().openScreen(LookupInputTestPage.class);

		// Pressing validate should make the 2nd editor be with an error background
		wd().cmd().click().on("button_validate");

		ScreenInspector inspector = wd().screenInspector();
		if(null == inspector)
			return;
		BufferedImage bi = inspector.elementScreenshot("one");
		//ImageIO.write(bi, "png", new File("/tmp/test.png"));
		Assert.assertTrue("The background of the control should be red because it is in error", TestHelper.isReddish(bi));

		//-- Reload the screen, and it should remain red
		wd().refresh();

		inspector = wd().screenInspector();
		if(null == inspector)
			throw new IllegalStateException();
		bi = inspector.elementScreenshot("one");
		//ImageIO.write(bi, "png", new File("/tmp/test.png"));
		Assert.assertTrue("The background of the control should be red because it is in error after screen refresh", TestHelper.isReddish(bi));
	}

	/**
	 * Bug: when a LookupInput.value is bound and the input is mandatory, clicking the lookup
	 * button will put the component in error state.
	 * @throws Exception
	 */
	@Test
	public void testBindingShouldNotThrowErrorOnLookup() throws Exception {
		wd().openScreen(LookupInputTestPage.class);

		wd().cmd().click().on("one-lookup");
		Thread.sleep(1000);

		//-- The lookup cannot be in error state.
		WebElement one = wd().getElement("one");
		String aClass = one.getAttribute("class");
		Assert.assertFalse("The input control should not be in error state", aClass.contains("ui-input-err"));

	}

	@Ignore("While redesigning")
	@Test
	public void testInitialLayout() throws Exception {
		wd().openScreen(LookupInputTestPage.class);

		//Properties properties = System.getProperties();
		//properties.forEach((k, v) -> System.out.println("    " + k + " = " + v));


		//-- Both one and two must use only one line
		WebElement one = wd().getElement("one");
		Assert.assertTrue("Control one must span one line, it now uses " + one.getSize().height + "px", one.getSize().height < 30);

		WebElement two = wd().getElement("two");
		Assert.assertTrue("Control two must span one line (" + two.getSize().height + ")", two.getSize().height < 30);

		//-- One must not contain an input
		Assert.assertTrue("One cannot have 'input' because it has no QuickSearch", one.findElements(By.tagName("input")).size() == 0);

		//-- Two MUST have an input
		Assert.assertTrue("Two must have 'input' because it HAS QuickSearch", two.findElements(By.tagName("input")).size() == 1);

		wd().cmd().type("aaaaaaaaa").on(By.cssSelector("#" + two.getAttribute("id") + " input"));

		Thread.sleep(1000);				// IMPORTANT: wait for the popup to finish

		ScreenInspector screenInspector = wd().screenInspector();
		if(null != screenInspector) {
			BufferedImage bi = screenInspector.elementScreenshot(two);
			ImageIO.write(bi, "png", new File("/tmp/input-2.png"));
		}

		WebElement input = wd().getElement(By.cssSelector("#" + two.getAttribute("id") + " input"));
		WebElement label = wd().getElement(By.cssSelector("label[for='" + input.getAttribute("id")+"']"));

		ImageHelper.checkBaseLine("/tmp/testInitialLayout", wd(), By.id(label.getAttribute("id")), By.id(input.getAttribute("id")), By.id("_A"));
	}
}
