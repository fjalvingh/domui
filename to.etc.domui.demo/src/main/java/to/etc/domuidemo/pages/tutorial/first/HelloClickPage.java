package to.etc.domuidemo.pages.tutorial.first;

import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.UrlPage;

/**
 * Tutorial, "building your first page", step 3: a click handler on a tag. The
 * handler is server side Java; changing the node inside it is enough to change
 * the browser screen.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class HelloClickPage extends UrlPage {
	static private final String OFF = "#d9e8ff";

	static private final String ON = "#ffd9a0";

	private boolean m_on;

	@Override
	public void createContent() throws Exception {
		setPageTitle("Clicking a tag");

		Div box = new Div("dm-tut");
		add(box);
		box.add("Click me to change my color");
		box.setBackgroundColor(m_on ? ON : OFF);

		box.setClicked(clickedNode -> {
			m_on = !m_on;
			clickedNode.setBackgroundColor(m_on ? ON : OFF);
		});
	}
}
