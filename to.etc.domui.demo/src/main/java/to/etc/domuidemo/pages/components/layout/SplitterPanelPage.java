package to.etc.domuidemo.pages.components.layout;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.layout.SplitterPanel;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;

/**
 * SplitterPanel: two panels with a bar between them that the user can drag.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class SplitterPanelPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("SplitterPanel");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "SplitterPanel"));

		//-- A vertical bar: the two panels end up side by side.
		Div left = new Div();
		left.add("The left panel. Drag the bar between the two to change how the space is "
			+ "divided; this one may not become narrower than 100 pixels.");

		Div right = new Div();
		right.add("The right panel.");

		SplitterPanel sideBySide = new SplitterPanel(left, right, true);
		sideBySide.setHeight("150px");
		sideBySide.setMinASize(100);
		cp.add(sideBySide);

		cp.add(new HTag(2, "A horizontal bar"));

		Div top = new Div();
		top.add("The top panel.");

		Div bottom = new Div();
		bottom.add("The bottom panel. The bar between them moves up and down.");

		SplitterPanel stacked = new SplitterPanel(top, bottom, false);
		stacked.setHeight("200px");
		cp.add(stacked);

		cp.add(new Para().add("The panel takes two divs and puts a bar between them. The "
			+ "boolean is the orientation of the *bar*, not of the split: true gives a "
			+ "vertical bar and therefore two panels side by side, false a horizontal bar "
			+ "and two panels above each other. It is easy to read the wrong way round."));
		cp.add(new Para().add("Give the splitter itself a height: it has none of its own, and "
			+ "without one there is nothing to divide."));
	}
}
