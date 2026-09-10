package to.etc.domui.test.ui.maxgraph;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Point;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.WebElement;
import to.etc.domui.webdriver.core.AbstractWebDriverTest;
import to.etc.domuidemo.pages.components.graph.BasicGraphPage;
import to.etc.domuidemo.pages.components.graph.ChangingGraphPage;
import to.etc.domuidemo.pages.components.graph.EditableGraphPage;
import to.etc.domuidemo.pages.components.graph.ExportGraphPage;
import to.etc.domuidemo.pages.components.graph.GraphEditorPage;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

	/*----------------------------------------------------------------------*/
	/*	CODING:	The user adding to a drawing                                */
	/*----------------------------------------------------------------------*/

	/**
	 * A palette item dropped in the drawing is a request, not a cell: what appears is what
	 * the page made of it, with the id the model gave it.
	 */
	@Test
	public void aDroppedPaletteItemBecomesTheNodeThePageMakes() throws Exception {
		wd().openScreen(GraphEditorPage.class);
		wd().wait(label("Start here"));

		drag(paletteItem("Done"), 300, 220);
		wd().wait(logLineContaining("Added a done"));
		Assert.assertTrue("The node the page made must be in the drawing", wd().isPresent(label("Done")));
	}

	/**
	 * And a connection drawn between two nodes is a request too. It starts from the dot in
	 * the middle of a node, which maxGraph only puts there once the pointer is over it.
	 */
	@Test
	public void aDrawnConnectionBecomesTheEdgeThePageMakes() throws Exception {
		wd().openScreen(GraphEditorPage.class);
		wd().wait(label("Start here"));
		drag(paletteItem("Done"), 300, 220);
		wd().wait(label("Done"));
		int edges = wd().findElements(EDGES).size();

		connect(label("Start here"), label("Done"));
		wd().wait(logLineContaining("Connected Start here to Done"));
		Assert.assertTrue("The edge the page made must be in the drawing", wd().findElements(EDGES).size() > edges);
	}

	/**
	 * And a connection the page will not make is not drawn: there is nothing to take back,
	 * because the browser never made anything.
	 */
	@Test
	public void aConnectionThePageRefusesIsNotDrawn() throws Exception {
		wd().openScreen(GraphEditorPage.class);
		wd().wait(label("Start here"));
		drag(paletteItem("Done"), 300, 220);
		wd().wait(label("Done"));

		connect(label("Done"), label("Start here"));
		wd().wait(logLineContaining("Refused"));
		Assert.assertTrue("Nothing may have been drawn", wd().findElements(EDGES).isEmpty());
	}

	/** Renaming a cell in place is an ordinary change, and goes the ordinary way. */
	@Test
	public void renamingACellInPlaceReachesTheModel() throws Exception {
		wd().openScreen(GraphEditorPage.class);
		wd().wait(label("Start here"));

		Actions actions = new Actions(wd().driver());
		actions.doubleClick(wd().findElement(label("Start here")))
			.keyDown(Keys.CONTROL).sendKeys("a").keyUp(Keys.CONTROL)
			.sendKeys("Renamed").perform();
		//-- The editor commits when the drawing is clicked outside of it.
		new Actions(wd().driver()).moveToElement(wd().findElement(CANVAS), 20, 20).click().perform();

		wd().wait(logLineContaining("label"));
		Assert.assertTrue("The drawing must show the new label", wd().isPresent(label("Renamed")));
	}

	/**
	 * Bending an edge is a change to its waypoints. The handle to bend it by sits in the
	 * middle of the edge, where the edge itself is, so selecting and bending are the same
	 * point twice.
	 */
	@Test
	public void bendingAnEdgeReachesTheModel() throws Exception {
		wd().openScreen(EditableGraphPage.class);
		wd().wait(label("The hub stays"));

		WebElement edge = wd().findElements(EDGES).get(0);
		new Actions(wd().driver()).click(edge).perform();
		new Actions(wd().driver()).dragAndDropBy(edge, -60, 30).perform();

		wd().wait(logLineContaining("points"));
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	Taking it back again                                        */
	/*----------------------------------------------------------------------*/

	/**
	 * The point of undo living on the server: the browser threw the node away and has
	 * nothing left to make one from, while the model still has the cell itself. So the node
	 * comes back as the same cell - and, because deleting it deleted the edge that hung on
	 * it, the edge comes back with it, in one step.
	 */
	@Test
	public void undoBringsBackADeletedNodeAndItsEdge() throws Exception {
		wd().openScreen(GraphEditorPage.class);
		wd().wait(label("Start here"));
		drag(paletteItem("Done"), 300, 220);
		wd().wait(label("Done"));
		connect(label("Start here"), label("Done"));
		wd().wait(logLineContaining("Connected Start here to Done"));
		int edges = wd().findElements(EDGES).size();

		delete(label("Done"));
		wd().notPresent(label("Done"));
		Assert.assertTrue("The edge must have gone with it", wd().findElements(EDGES).size() < edges);

		wd().cmd().click().on("button_Undo");
		wd().wait(label("Done"));
		Assert.assertEquals("And both must come back together", edges, wd().findElements(EDGES).size());
	}

	/** What the page made of a palette drop is undone like anything else, and redone again. */
	@Test
	public void undoAndRedoOfAPaletteDrop() throws Exception {
		wd().openScreen(GraphEditorPage.class);
		wd().wait(label("Start here"));

		drag(paletteItem("Decision?"), 300, 220);
		wd().wait(label("Decision?"));

		wd().cmd().click().on("button_Undo");
		wd().notPresent(label("Decision?"));

		wd().cmd().click().on("button_Redo");
		wd().wait(label("Decision?"));
		Assert.assertTrue("The drawing the user started from is never undone away",
			wd().isPresent(label("Start here")));
	}

	/**
	 * ctrl-Z in the drawing is the same undo: the browser only says the key was pressed, and
	 * what comes back is the ordinary list of changes.
	 */
	@Test
	public void theCtrlZKeystrokeUndoes() throws Exception {
		wd().openScreen(GraphEditorPage.class);
		wd().wait(label("Start here"));
		drag(paletteItem("Done"), 300, 220);
		wd().wait(label("Done"));

		//-- The drawing only gets key events while it has the focus, which a click in it gives it.
		new Actions(wd().driver()).click(wd().findElement(label("Start here")))
			.keyDown(Keys.CONTROL).sendKeys("z").keyUp(Keys.CONTROL).perform();

		wd().notPresent(label("Done"));
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	Arranging the drawing                                       */
	/*----------------------------------------------------------------------*/

	/**
	 * A layout runs in the browser, but what it moves has to reach the model - so the page
	 * hears about the same geometry changes it hears about when a user drags a node.
	 */
	@Test
	public void arrangingReportsWhereEverythingEndedUp() throws Exception {
		wd().openScreen(GraphEditorPage.class);
		wd().wait(label("Start here"));
		drag(paletteItem("Task"), 320, 240);
		wd().wait(logLineContaining("Added a task"));

		wd().cmd().click().on("button_Arrange");
		wd().wait(logLineContaining("Changed: geometry"));
	}

	/**
	 * The drawing on {@link ChangingGraphPage} may not be touched by the user at all, and is
	 * arranged all the same - and the model keeps where the layout put things, which is what
	 * a full refresh proves: it rebuilds the drawing from the model.
	 */
	@Test
	public void aReadOnlyDrawingIsArrangedAndTheModelKeepsIt() throws Exception {
		wd().openScreen(ChangingGraphPage.class);
		wd().wait(label("Hub"));
		Point before = locationOf(label("Hub"));

		wd().cmd().click().on("button_Arrange");
		wd().wait(() -> !before.equals(locationOf(label("Hub"))));
		Point arranged = locationOf(label("Hub"));

		wd().refresh();
		wd().wait(label("Hub"));
		Point rebuilt = locationOf(label("Hub"));
		Assert.assertTrue("The drawing must come back where the layout left it, not where it started"
				+ " (was " + before + ", arranged to " + arranged + ", rebuilt at " + rebuilt + ")",
			Math.abs(arranged.getX() - rebuilt.getX()) <= 3 && Math.abs(arranged.getY() - rebuilt.getY()) <= 3);
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	A picture of the drawing                                    */
	/*----------------------------------------------------------------------*/

	/**
	 * A picture is made in the browser and posted back, so what proves it arrived is the
	 * page showing what it got: a file this server now has, served back to the browser as
	 * an image. If the bytes were not a png, that image would not load.
	 */
	@Test
	public void aPngArrivesAtTheServer() throws Exception {
		wd().openScreen(ExportGraphPage.class);
		wd().wait(label("Order arrives"));

		wd().cmd().click().on("button_Send_the_PNG_here");
		wd().wait(exportedLineContaining("The png arrived here"));
		wd().wait(() -> naturalWidthOfExported() > 0);
	}

	/** The same, in the other format: an svg document, served back and drawn. */
	@Test
	public void anSvgArrivesAtTheServer() throws Exception {
		wd().openScreen(ExportGraphPage.class);
		wd().wait(label("Order arrives"));

		wd().cmd().click().on("button_Send_the_SVG_here");
		wd().wait(exportedLineContaining("The svg arrived here"));
		wd().wait(() -> naturalWidthOfExported() > 0);
	}

	/**
	 * The picture is of the drawing as it is at that moment, not of the model as the page
	 * built it: dragging a node further out makes the next picture wider.
	 */
	@Test
	public void aPictureIsOfTheDrawingAsItIsNow() throws Exception {
		wd().openScreen(ExportGraphPage.class);
		wd().wait(label("Ship it"));

		wd().cmd().click().on("button_Send_the_PNG_here");
		wd().wait(exportedLineContaining("The png arrived here"));
		int before = exportedWidth();
		Assert.assertTrue("The picture must have a size", before > 0);

		drag(label("Ship it"), 200, 0);
		wd().cmd().click().on("button_Send_the_PNG_here");
		wd().wait(() -> exportedWidth() > before);
	}

	static private final By EXPORTED_IMAGE = By.cssSelector("*[testid='exported'] img");

	static private By exportedLineContaining(String text) {
		return By.xpath("//div[@testid='exported']//p[contains(text(),'" + text + "')]");
	}

	/** How wide the picture that came back is, as the page reports it, or 0 while there is none. */
	private int exportedWidth() {
		try {
			WebElement element = wd().findElement(By.cssSelector("*[testid='exported'] p"));
			if(null == element) {
				return 0;
			}
			Matcher matcher = Pattern.compile("(\\d+) by ").matcher(element.getText());
			return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
		} catch(Exception x) {
			return 0;                                      // Being replaced by the next picture.
		}
	}

	/**
	 * How wide the browser found the image the server served to be: a picture that did not
	 * arrive, or arrived broken, is zero wide.
	 */
	private int naturalWidthOfExported() {
		if(!wd().isPresent(EXPORTED_IMAGE)) {
			return 0;
		}
		String width = wd().executeScript("return document.querySelector(\"*[testid='exported'] img\").naturalWidth");
		return width.isEmpty() ? 0 : Integer.parseInt(width);
	}

	/** Where what this points at sits on the screen. */
	private Point locationOf(By what) {
		WebElement element = wd().findElement(what);
		if(null == element) {
			throw new IllegalStateException("Nothing found for " + what);
		}
		return element.getLocation();
	}

	static private final By CANVAS = By.cssSelector(".ui-mxgr-canvas");

	static private By paletteItem(String label) {
		return By.xpath("//div[contains(@class,'ui-mxgr-pi')][normalize-space(text())='" + label + "']");
	}


	/**
	 * Draw a connection from one node to another. The dot that starts it appears only while
	 * the pointer is over the node, and maxGraph needs a move onto the dot itself before it
	 * counts as the thing being dragged.
	 */
	private void connect(By from, By to) {
		WebElement source = wd().findElement(from);
		new Actions(wd().driver()).moveToElement(source).moveToElement(source, 1, 1).perform();
		WebElement icon = wd().findElement(By.xpath("//div[contains(@class,'ui-mxgr-canvas')]//*[local-name()='image']"));
		new Actions(wd().driver()).dragAndDrop(icon, wd().findElement(to)).perform();
	}
}
