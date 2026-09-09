package to.etc.domuidemo.pages.components.graph;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.maxgraph.MaxGraphPanel;
import to.etc.domui.maxgraph.model.GraphEdgeStyle;
import to.etc.domui.maxgraph.model.GraphModel;
import to.etc.domui.maxgraph.model.GraphNode;
import to.etc.domui.maxgraph.model.GraphShape;

/**
 * A diagram built as a Java model and drawn by {@link MaxGraphPanel}.
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

		MaxGraphPanel panel = new MaxGraphPanel();
		cp.add(panel);
		panel.size("100%", "420px").setModel(createModel());
	}

	/**
	 * How an order is handled, as a small flow chart. Everything about the drawing is
	 * decided here, in Java; the browser is only told what the result looks like.
	 */
	private GraphModel createModel() {
		GraphModel model = new GraphModel();

		GraphNode start = model.addNode("Order arrives", 60, 20, 160, 40)
			.styled(s -> s.shape(GraphShape.Ellipse).fillColor("#d5e8d4").strokeColor("#82b366"));

		GraphNode check = model.addNode("In stock?", 60, 110, 160, 60)
			.styled(s -> s.shape(GraphShape.Rhombus).fillColor("#ffe6cc").strokeColor("#d79b00"));

		GraphNode ship = model.addNode("Ship it", 320, 120, 140, 40)
			.styled(s -> s.rounded(true).fillColor("#dae8fc").strokeColor("#6c8ebf"));

		GraphNode order = model.addNode("Order from supplier", 60, 240, 160, 40)
			.styled(s -> s.rounded(true).fillColor("#dae8fc").strokeColor("#6c8ebf"));

		GraphNode done = model.addNode("Done", 320, 250, 140, 40)
			.styled(s -> s.shape(GraphShape.Ellipse).fillColor("#d5e8d4").strokeColor("#82b366"));

		model.addEdge(start, check);
		model.addEdge(check, ship, "yes")
			.styled(s -> s.edgeStyle(GraphEdgeStyle.Orthogonal));
		model.addEdge(check, order, "no")
			.styled(s -> s.edgeStyle(GraphEdgeStyle.Orthogonal));
		model.addEdge(ship, done)
			.styled(s -> s.edgeStyle(GraphEdgeStyle.Orthogonal));
		model.addEdge(order, done)
			.styled(s -> s.edgeStyle(GraphEdgeStyle.Orthogonal).dashed(true));

		return model;
	}
}
