package to.etc.domui.test.ui.componenterrors;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
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

	@Test
	public void testInitialLayout() throws Exception {
		wd().openScreen(LookupInputTestPage.class);

		//Properties properties = System.getProperties();
		//properties.forEach((k, v) -> System.out.println("    " + k + " = " + v));


		//-- Both one and two must use only one line
		WebElement one = wd().getElement("one");
		Assert.assertTrue("Control one must span one line, it now uses " + one.getSize().height + "px", one.getSize().height < 25);

		ScreenInspector screenInspector = wd().screenInspector();
		if(null != screenInspector) {
			BufferedImage bi = screenInspector.elementScreenshot(one);
			ImageIO.write(bi, "png", new File("/tmp/input-1.png"));
		}

		WebElement two = wd().getElement("two");
		Assert.assertTrue("Control two must span one line", two.getSize().height < 25);

		//-- One must not contain an input
		Assert.assertTrue("One cannot have 'input' because it has no QuickSearch", one.findElements(By.tagName("input")).size() == 0);

		//-- Two MUST have an input
		Assert.assertTrue("Two must have 'input' because it HAS QuickSearch", two.findElements(By.tagName("input")).size() == 1);
		snapshot();
	}

}
