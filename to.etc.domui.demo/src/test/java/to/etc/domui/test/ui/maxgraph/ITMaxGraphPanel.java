package to.etc.domui.test.ui.maxgraph;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import to.etc.domui.webdriver.core.AbstractWebDriverTest;
import to.etc.domuidemo.pages.components.graph.BasicGraphPage;
import to.etc.domuidemo.pages.components.graph.ChangingGraphPage;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The drawing a {@link to.etc.domui.maxgraph.MaxGraphPanel} makes is not in the page's
 * HTML: the browser asks for the model and builds the SVG from it. So the thing to test
 * is that the SVG arrives, and that what it contains is what the Java model said.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class ITMaxGraphPanel extends AbstractWebDriverTest {
	/** Every label in the drawing, in document order. */
	static private final By LABELS = By.xpath("//div[contains(@class,'ui-mxgr')]//*[local-name()='text']");

	/** The drawing itself: one svg, made once and then changed in place. */
	static private final By DRAWING = By.xpath("//div[contains(@class,'ui-mxgr')]//*[local-name()='svg']");

	static private By label(String text) {
		return By.xpath("//div[contains(@class,'ui-mxgr')]//*[local-name()='text'][normalize-space(text())='" + text + "']");
	}

	static private By ellipseFilled(String color) {
		return By.xpath("//div[contains(@class,'ui-mxgr')]//*[local-name()='ellipse'][@fill='" + color + "']");
	}

	@Test
	public void theDrawingIsBuiltFromTheModel() throws Exception {
		wd().openScreen(BasicGraphPage.class);

		//-- The five nodes and the two labelled edges of the model on that page.
		wd().waitMultiplePresent(LABELS, 7);

		List<String> labels = wd().findElements(LABELS).stream()
			.map(WebElement::getText)
			.collect(Collectors.toList());
		Assert.assertTrue("The drawing should show the model's start node, got " + labels, labels.contains("Order arrives"));
		Assert.assertTrue("The drawing should show the model's decision, got " + labels, labels.contains("In stock?"));
		Assert.assertTrue("The drawing should show an edge label, got " + labels, labels.contains("yes"));
	}

	/**
	 * A full re-render throws the browser-side instance away and creates it again, so the
	 * drawing has to come back from the model unchanged.
	 */
	@Test
	public void theDrawingSurvivesAFullRefresh() throws Exception {
		wd().openScreen(BasicGraphPage.class);
		wd().waitMultiplePresent(LABELS, 7);

		wd().refresh();
		wd().waitMultiplePresent(LABELS, 7);
		Assert.assertTrue("The drawing should be there again after a full refresh",
			wd().findElements(LABELS).stream().map(WebElement::getText).anyMatch("Order arrives"::equals));
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	The server changing a drawing that is on the screen         */
	/*----------------------------------------------------------------------*/

	/**
	 * The buttons of {@link ChangingGraphPage} change the Java model and nothing else, so
	 * what has to arrive in the browser is a change to the drawing that is there - not a
	 * new drawing. The svg element being the same one afterwards is what says so: a rebuilt
	 * component would have replaced it, and Selenium would call this element stale.
	 */
	@Test
	public void theServerChangesTheDrawingInPlace() throws Exception {
		wd().openScreen(ChangingGraphPage.class);
		wd().waitMultiplePresent(LABELS, 3);               // The hub and its two satellites.
		WebElement drawing = wd().findElement(DRAWING);
		Assert.assertNotNull("The drawing should be there", drawing);

		wd().cmd().click().on("button_Add_a_satellite");
		wd().wait(label("Satellite 3"));

		Assert.assertEquals("The drawing must have been changed, not made again",
			"svg", drawing.getTagName());
	}

	@Test
	public void aCellRemovedInTheModelDisappears() throws Exception {
		wd().openScreen(ChangingGraphPage.class);
		wd().wait(label("Satellite 2"));

		wd().cmd().click().on("button_Remove_a_satellite");
		wd().notPresent(label("Satellite 2"));
		Assert.assertTrue("Only the satellite that was removed should be gone", wd().isPresent(label("Satellite 1")));
	}

	@Test
	public void aRelabelledCellShowsItsNewLabel() throws Exception {
		wd().openScreen(ChangingGraphPage.class);
		wd().wait(label("Hub"));

		wd().cmd().click().on("button_Rename_the_hub");
		wd().wait(label("Hub, renamed"));
	}

	@Test
	public void aRestyledCellIsRedrawnInItsNewStyle() throws Exception {
		wd().openScreen(ChangingGraphPage.class);
		wd().wait(ellipseFilled("#dae8fc"));

		wd().cmd().click().on("button_Recolour_the_hub");
		wd().wait(ellipseFilled("#f8cecc"));
	}
}
