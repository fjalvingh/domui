package to.etc.domui.test.ui.componenterrors;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import to.etc.domui.webdriver.core.AbstractWebDriverTest;
import to.etc.domui.webdriver.core.ScreenInspector;
import to.etc.domuidemo.pages.test.componenterrors.HtmlEditorTestPage;

import javax.annotation.Nonnull;
import java.awt.image.BufferedImage;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-8-17.
 */
public class ITTestHtmlEditorComponent extends AbstractWebDriverTest {
	/**
	 * The page contains 2 mandatory html editors. The 1st one with a text, the second without,
	 * plus a validate button to check data.
	 *
	 * @throws Exception
	 */
	@Test
	public void testShowBindingError() throws Exception {
		wd().openScreen(HtmlEditorTestPage.class);

		// Pressing validate should make the 2nd editor be with an error background
		wd().cmd().click().on("button_validate");

		ScreenInspector inspector = wd().screenInspector();
		if(null == inspector)
			return;
		WebElement two = findEditorElement("twoText");
		BufferedImage bi = inspector.elementScreenshot(two);
		//ImageIO.write(bi, "png", new File("/tmp/test.png"));
		Assert.assertTrue("The background of the control should be red because it is in error", TestHelper.isReddish(bi));

		//-- Reload the screen, and it should remain red
		wd().refresh();

		inspector = wd().screenInspector();
		if(null == inspector)
			throw new IllegalStateException();
		two = findEditorElement("twoText");
		bi = inspector.elementScreenshot(two);
		//ImageIO.write(bi, "png", new File("/tmp/test.png"));
		Assert.assertTrue("The background of the control should be red because it is in error after screen refresh", TestHelper.isReddish(bi));
	}

	@Nonnull
	private WebElement findEditorElement(String testid) {
		WebElement two = wd().findElement(testid);
		if(null == two)
			throw new IllegalStateException("Cannot find element with testid " + testid);
		String id = two.getAttribute("id");
		WebElement lay = wd().findElement(By.id(id + "-wysiwyg-iframe"));
		if(null == lay)
			throw new IllegalStateException("Cannot find the htmleditor's iframe for testid=" + testid);
		return lay;
	}

}
