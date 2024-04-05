package to.etc.domuidemo.pages.test.msgbox;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import to.etc.domui.webdriver.core.AbstractWebDriverTest;

/**
 * This tests several things around MsgBox and MsgBox2.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 07-03-23.
 */
public class ITTestMsgBox extends AbstractWebDriverTest {
	/*----------------------------------------------------------------------*/
	/*	CODING:	MsgBox tests	*/
	/*----------------------------------------------------------------------*/
	/**
	 * No scroll bar on a simple auto-resize input with a text control.
	 */
	@Test
	public void noScrollBarOnSimpleInput() throws Exception {
		wd().openScreen(TestMsgBox1.class);
		Assert.assertFalse("There should not be a scrollbar", wd().isScrollbarPresent(By.className("ui-mbx-top")));
	}

	/**
	 * No scroll bar on a simple auto-resizing error dialog with a large text.
	 */
	@Test
	public void noScrollBarOnLargeText() throws Exception {
		wd().openScreen(TestMsgBox2.class);
		Assert.assertFalse("There should not be a scrollbar", wd().isScrollbarPresent(By.className("ui-mbx-top")));
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	MsgBox2 tests	*/
	/*----------------------------------------------------------------------*/

	/**
	 * No scroll bar on a simple auto-resize input with a text control.
	 */
	@Test
	public void noScrollBarOnSimpleInput2() throws Exception {
		wd().openScreen(TestMsg2Box1.class);
		Assert.assertFalse("There should not be a scrollbar", wd().isScrollbarPresent(By.className("ui-mbx-top")));
	}

	/**
	 * No scroll bar on a simple auto-resizing info dialog with a large text.
	 */
	@Test
	public void noScrollBarOnLargeText2() throws Exception {
		wd().openScreen(TestMsg2Box2.class);
		Assert.assertFalse("There should not be a scrollbar", wd().isScrollbarPresent(By.className("ui-mbx-top")));
	}

	/**
	 * No scroll bar on a simple auto-resizing error dialog with a large text and a
	 * specific WIDTH set, but height = -1.
	 */
	@Test
	public void noScrollBarOnLargeTextWithWidthSet() throws Exception {
		wd().openScreen(TestMsg2Box3.class);
		Assert.assertFalse("There should not be a scrollbar", wd().isScrollbarPresent(By.className("ui-mbx-top")));
	}

	/**
	 * No scroll bar on a fully sized box that has enough size to handle the content.
	 */
	@Test
	public void noScrollBarOnLargeTextWithSize1() throws Exception {
		wd().openScreen(TestMsg2Box4.class);
		Assert.assertFalse("There should not be a scrollbar", wd().isScrollbarPresent(By.className("ui-mbx-top")));
	}

	/**
	 * With a size and a text too large to fit we must have a scrollbar..
	 */
	@Test
	public void needScrollBarOnVeryLargeTextWithSize1() throws Exception {
		wd().openScreen(TestMsg2Box5.class);
		Assert.assertTrue("There SHOULD BE a scrollbar", wd().isScrollbarPresent(By.className("ui-mbx-top")));
	}

	/**
	 * With a size and a text too large to fit we must have a scrollbar..
	 */
	@Test
	public void needScrollBarOnVeryLargeDivWithSize1() throws Exception {
		wd().openScreen(TestMsg2Box6.class);
		Assert.assertTrue("There SHOULD BE a scrollbar", wd().isScrollbarPresent(By.className("ui-mbx-top")));
	}

}
