package to.etc.domui.test.componenterrors;

import org.junit.Assert;
import org.junit.Test;
import to.etc.domui.webdriver.core.AbstractWebDriverTest;
import to.etc.domuidemo.pages.test.componenterrors.HtmlEditorTestPage;

import java.io.File;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-8-17.
 */
public class ITHtmlEditorComponentTest extends AbstractWebDriverTest {
	/**
	 * The page contains 2 mandatory html editors. The 1st one with a text, the second without,
	 * plus a validate button to check data.
	 *
	 * @throws Exception
	 */
	@Test
	public void testBinding1() throws Exception {
		try {
			wd().openScreen(HtmlEditorTestPage.class);

			// Pressing validate should make the 2nd editor be with an error background
			wd().cmd().click().on("button_validate");
		} catch(Exception x) {
			x.printStackTrace();
		}
		File out = File.createTempFile("test-ss-", ".png");
		boolean screenshot = wd().screenshot(out);
		Assert.assertTrue("Could not make screenshot", screenshot);

		System.out.println("Screenshot is " + out);
	}

}
