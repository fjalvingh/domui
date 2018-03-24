package to.etc.domui.test.ui.componenterrors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import to.etc.domui.webdriver.core.AbstractWebDriverTest;
import to.etc.domuidemo.pages.test.componenterrors.LookupInput2TestPage;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-8-17.
 */
public class ITTestLookupInput2Layout extends AbstractWebDriverTest {
	public ITTestLookupInput2Layout() {
	}

	@Before
	public void initializeScreen() throws Exception {
		wd().openScreenIf(this, LookupInput2TestPage.class);
	}

	@Ignore("While redesigning")
	@Test
	public void testOneLineForOne() throws Exception {
		//-- Both one and two must use only one line
		WebElement one = wd().getElement("one");
		Assert.assertTrue("Control one must span one line", one.getSize().height < 30);
	}

	@Ignore("While redesigning")
	@Test
	public void testOneLineForTwo() throws Exception {
		WebElement two = wd().getElement("two");
		Assert.assertTrue("Control two must span one line", two.getSize().height < 30);
	}

	@Ignore("Fails currently")
	@Test
	public void testOneCannotHaveInput() throws Exception {
		//-- One must not contain an input
		WebElement one = wd().getElement("one");
		Assert.assertTrue("One cannot have 'input' because it has no QuickSearch", one.findElements(By.tagName("input")).size() == 0);
	}

	@Test
	public void testTwoMustHaveInput() throws Exception {
		//-- Two MUST have an input
		WebElement two = wd().getElement("two");
		Assert.assertTrue("Two must have 'input' because it HAS QuickSearch", two.findElements(By.tagName("input")).size() == 1);
	}

	@Ignore("While redesigning")
	@Test
	public void labelMustBeAlignedOne() throws Exception {
		WebElement one = wd().getElement("one");
		WebElement label = AbstractLayoutTest.findFormLabelFor(one);
		Assert.assertEquals("Label and control for ONE must be on same Y", label.getLocation().getY(), one.getLocation().getY());
	}

	@Ignore("While redesigning")
	@Test
	public void labelMustBeAlignedTwo() throws Exception {
		WebElement two = wd().getElement("two");
		WebElement label = AbstractLayoutTest.findFormLabelFor(two);

		Assert.assertEquals("Label and control for TWO must be on same Y", label.getLocation().getY(), two.getLocation().getY());
	}

	@Ignore("While redesigning")
	@Test
	public void labelMustBeAlignedFontTwo() throws Exception {
		WebElement two = wd().getElement("two");
		WebElement label = AbstractLayoutTest.findFormLabelFor(two);
		Assert.assertEquals("Label and control for TWO must be on same Y", label.getLocation().getY(), two.getLocation().getY());
	}

}
