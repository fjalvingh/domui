package to.etc.domui.test.componenterrors;

import org.junit.Assert;
import org.junit.Test;
import to.etc.domui.webdriver.core.AbstractWebDriverTest;
import to.etc.domui.webdriver.core.ScreenInspector;
import to.etc.domuidemo.pages.test.componenterrors.LookupInputTestPage;

import java.awt.image.BufferedImage;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-8-17.
 */
public class ITTestLookupInput extends AbstractWebDriverTest {
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
		Assert.assertTrue("The background of the control should be red because it is in error", isReddish(bi));

		//-- Reload the screen, and it should remain red
		wd().refresh();

		inspector = wd().screenInspector();
		if(null == inspector)
			throw new IllegalStateException();
		bi = inspector.elementScreenshot("one");
		//ImageIO.write(bi, "png", new File("/tmp/test.png"));
		Assert.assertTrue("The background of the control should be red because it is in error after screen refresh", isReddish(bi));
	}

	private boolean isReddish(BufferedImage bi) {
		int[][] ints = ScreenInspector.getMostUsedColors(bi, 10);
		int pixel = ints[0][0];
		int b = pixel & 0xff;
		pixel = pixel >> 8;
		int g = pixel & 0xff;
		pixel = pixel >> 8;
		int r = pixel & 0xff;

		return r > 0xf0 && b < 0xf0 && g < 0xf0;
	}
}
