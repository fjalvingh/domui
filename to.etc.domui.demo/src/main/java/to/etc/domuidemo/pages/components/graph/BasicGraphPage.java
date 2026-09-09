package to.etc.domuidemo.pages.components.graph;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.maxgraph.MaxGraphPanel;

/**
 * A diagram drawn by {@link MaxGraphPanel}.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class BasicGraphPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("Diagrams");
		MaxGraphPanel.initialize(this);

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Diagrams"));

		MaxGraphPanel graph = new MaxGraphPanel();
		cp.add(graph);
		graph.size("100%", "400px");
	}
}
