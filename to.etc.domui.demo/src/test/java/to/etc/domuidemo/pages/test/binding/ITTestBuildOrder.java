package to.etc.domuidemo.pages.test.binding;

import org.junit.*;
import to.etc.domui.webdriver.core.*;
import to.etc.domuidemo.pages.test.binding.buildorder.*;

/**
 * See confluence.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-6-17.
 */
public class ITTestBuildOrder extends AbstractWebDriverTest {
	@Test
	public void testBindingsWorkAnyBuildOrder() throws Exception {
		wd().openScreen(BuildOrderPage.class);

		wd().cmd().click().on("button_ClickMe");

		wd().wait("button_NextButton");
		String attribute = wd().findAttribute("button_NextButton", "disabled");
		Assert.assertEquals("The button should be disabled", "true", attribute);
	}

}
