package to.etc.domui.test.ui.maxgraph;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.WebElement;
import to.etc.domui.webdriver.core.AbstractWebDriverTest;
import to.etc.domuidemo.pages.components.graph.BasicGraphPage;
import to.etc.domuidemo.pages.components.graph.ChangingGraphPage;
import to.etc.domuidemo.pages.components.graph.EditableGraphPage;

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

	/*----------------------------------------------------------------------*/
	/*	CODING:	The user changing a drawing, and a server that decides       */
	/*----------------------------------------------------------------------*/

	/**
	 * Dragging a node has to reach the Java model: the page says where the model put it,
	 * which it can only know from the change the browser sent.
	 */
	@Test
	public void draggingANodeMovesItInTheModel() throws Exception {
		wd().openScreen(EditableGraphPage.class);
		wd().wait(label("Drag me 1"));

		drag(label("Drag me 1"), 260, 60);
		wd().wait(logLineContaining("Moved Drag me 1"));
	}

	/**
	 * A refused deletion has to put the drawing back - the node the browser threw away, and
	 * the edges the browser took with it.
	 */
	@Test
	public void aRefusedDeleteBringsTheCellBack() throws Exception {
		wd().openScreen(EditableGraphPage.class);
		wd().wait(label("The hub stays"));
		int edges = wd().findElements(EDGES).size();

		delete(label("The hub stays"));
		wd().wait(logLineContaining("Refused"));

		Assert.assertTrue("The hub must be back", wd().isPresent(label("The hub stays")));
		Assert.assertEquals("The edges that went with it must be back too", edges, wd().findElements(EDGES).size());
	}

	/** And a deletion that is accepted really does remove it, edge and all. */
	@Test
	public void anAcceptedDeleteRemovesTheCell() throws Exception {
		wd().openScreen(EditableGraphPage.class);
		wd().wait(label("Drag me 3"));
		int edges = wd().findElements(EDGES).size();

		delete(label("Drag me 3"));
		wd().notPresent(label("Drag me 3"));
		wd().wait(logLineContaining("Deleted the edge to Drag me 3"));
		Assert.assertTrue("The edge to it must be gone as well", wd().findElements(EDGES).size() < edges);
	}

	/** What an edge is drawn with; maxGraph uses more than one path per edge, so only counts of it compare. */
	static private final By EDGES = By.xpath("//div[contains(@class,'ui-mxgr')]//*[local-name()='path'][@fill='none']");

	/** One line of what the page says the model was told. */
	static private By logLineContaining(String text) {
		return By.xpath("//div[contains(@class,'dm-tut-q')]/div[contains(text(),'" + text + "')]");
	}

	private void drag(By what, int dx, int dy) {
		new Actions(wd().driver()).dragAndDropBy(wd().findElement(what), dx, dy).perform();
	}

	/**
	 * Delete what this locator points at: click it to select, then press Delete. The drawing
	 * only gets key events while it holds the focus, which the click gives it.
	 */
	private void delete(By what) {
		new Actions(wd().driver()).click(wd().findElement(what)).sendKeys(Keys.DELETE).perform();
	}
}
