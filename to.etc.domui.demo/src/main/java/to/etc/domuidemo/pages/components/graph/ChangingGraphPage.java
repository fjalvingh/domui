package to.etc.domuidemo.pages.components.graph;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.buttons.ButtonBar2;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.maxgraph.MaxGraphPanel;
import to.etc.domui.maxgraph.model.GraphCell;
import to.etc.domui.maxgraph.model.GraphLayoutType;
import to.etc.domui.maxgraph.model.GraphModel;
import to.etc.domui.maxgraph.model.GraphNode;
import to.etc.domui.maxgraph.model.GraphShape;

/**
 * A drawing that the server changes while it is on the screen: every button changes the
 * {@link GraphModel} and nothing else, and what changed in it is what the browser is told
 * about - the rest of the drawing is not touched.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class ChangingGraphPage extends UrlPage {
	/** The drawing is state, so it lives in a field - unlike the panel that shows it. */
	private final GraphModel m_model = new GraphModel();

	private final GraphNode m_hub;

	private int m_satelliteCount;

	private boolean m_highlighted;

	private boolean m_moved;

	public ChangingGraphPage() {
		m_hub = m_model.addNode("Hub", 260, 40, 120, 50)
			.styled(s -> s.shape(GraphShape.Ellipse).fillColor("#dae8fc").strokeColor("#6c8ebf"));
		addSatellite();
		addSatellite();
	}

	@Override
	public void createContent() throws Exception {
		setPageTitle("A diagram the server changes");
		MaxGraphPanel.initialize(this);

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "A diagram the server changes"));

		//-- Made here so the Arrange button can talk to it, and added below the buttons.
		MaxGraphPanel panel = new MaxGraphPanel();

		ButtonBar2 bb = new ButtonBar2();
		cp.add(bb);
		bb.addButton("Add a satellite", this::addSatellite);
		bb.addButton("Move the hub", this::moveHub);
		bb.addButton("Recolour the hub", this::recolourHub);
		bb.addButton("Rename the hub", this::renameHub);
		bb.addButton("Remove a satellite", this::removeSatellite);
		bb.addButton("Arrange", () -> panel.layout(GraphLayoutType.Organic));

		cp.add(panel);
		panel.size("100%", "420px").setModel(m_model);

		cp.add(new Para().add("Each button changes the Java model and does nothing else: no "
			+ "forceRebuild, no redraw. What changed during the request is sent at the end of "
			+ "it as a list of changes, which the browser applies to the drawing it already "
			+ "has - so the nodes you did not touch are not even repainted."));

		cp.add(new Para().add("Arrange is the one thing that happens the other way round: the "
			+ "browser does the arranging, because that is where the drawing is, and tells this "
			+ "side where everything ended up. Nobody may drag a node in this drawing, and it "
			+ "still holds after a refresh - the model has the arranged positions now."));
	}

	/*----------------------------------------------------------------------*/
	/*	CODING:	What the buttons do									        */
	/*----------------------------------------------------------------------*/

	private void addSatellite() {
		m_satelliteCount++;
		GraphNode satellite = m_model.addNode("Satellite " + m_satelliteCount, 40 + 160 * (m_satelliteCount % 4), 230, 140, 40)
			.styled(s -> s.rounded(true).fillColor("#d5e8d4").strokeColor("#82b366"));
		m_model.addEdge(m_hub, satellite);
	}

	private void moveHub() {
		m_moved = !m_moved;
		m_hub.at(m_moved ? 420 : 260, 40);
	}

	private void recolourHub() {
		m_highlighted = !m_highlighted;
		m_hub.style().fillColor(m_highlighted ? "#f8cecc" : "#dae8fc")
			.strokeColor(m_highlighted ? "#b85450" : "#6c8ebf");
	}

	private void renameHub() {
		m_hub.setLabel("Hub".equals(m_hub.getLabel()) ? "Hub, renamed" : "Hub");
	}

	/**
	 * Remove the satellite added last. The edge to it goes with it: the model removes what
	 * cannot exist without the node.
	 */
	private void removeSatellite() {
		for(int i = m_model.getCells().size(); --i >= 0; ) {
			GraphCell cell = m_model.getCells().get(i);
			if(cell instanceof GraphNode node && node != m_hub) {
				m_model.remove(node);
				return;
			}
		}
	}
}
