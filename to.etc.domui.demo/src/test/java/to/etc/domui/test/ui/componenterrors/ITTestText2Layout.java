package to.etc.domui.test.ui.componenterrors;

import org.junit.Ignore;
import org.junit.Test;
import to.etc.domuidemo.pages.test.componenterrors.Text2LayoutTestPage;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 6-10-17.
 */
public class ITTestText2Layout extends AbstractLayoutTest {
	@Override
	protected void initializeScreen() throws Exception {
		wd().openScreenIf(this, Text2LayoutTestPage.class);
		//wd().cmd().type("aaaaaaaaa").on("two", "input");
		//wd().wait(By.className("ui-lui-popup"));
		//wd().cmd().type("aaaaaaaaa").on("four", "input");
	}

	@Ignore("While redesigning")
	@Test
	public void testBaseLine1() throws Exception {
		checkBaseLine("t21", "input");
	}

	@Ignore("While redesigning")
	@Test
	public void testBaseLine2() throws Exception {
		checkBaseLine("t22", "input");
	}

}
