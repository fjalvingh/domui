package to.etc.domui.test.ui.maxgraph;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import to.etc.domui.webdriver.core.AbstractWebDriverTest;
import to.etc.domuidemo.pages.components.graph.BasicGraphPage;

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
}
