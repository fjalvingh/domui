package to.etc.domuidemo.pages.components.graph;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.maxgraph.MaxGraphPanel;
import to.etc.domui.maxgraph.model.GraphEdge;
import to.etc.domui.maxgraph.model.GraphModel;
import to.etc.domui.maxgraph.model.GraphNode;
import to.etc.domui.maxgraph.model.GraphPaletteItem;
import to.etc.domui.maxgraph.model.GraphShape;
import to.etc.domui.maxgraph.model.IGraphCreateHandler;

/**
 * A drawing the user builds: drag a shape in from the palette, connect two of them,
 * rename one, bend an edge. Nothing is made in the browser - it says what the user did and
 * this page decides what the model becomes, which is also where it says what may not be
 * drawn.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class GraphEditorPage extends UrlPage {
	private final GraphModel m_model = new GraphModel();

	private final GraphPaletteItem m_task = new GraphPaletteItem("task", "Task", 140, 40)
		.styled(s -> s.rounded(true).fillColor("#dae8fc").strokeColor("#6c8ebf"));

	private final GraphPaletteItem m_decision = new GraphPaletteItem("decision", "Decision?", 140, 60)
		.styled(s -> s.shape(GraphShape.Rhombus).fillColor("#ffe6cc").strokeColor("#d79b00"));

	private final GraphPaletteItem m_done = new GraphPaletteItem("done", "Done", 120, 40)
		.styled(s -> s.shape(GraphShape.Ellipse).fillColor("#d5e8d4").strokeColor("#82b366"));

	public GraphEditorPage() {
		GraphNode start = m_task.create(m_model, 60, 40);
		start.setLabel("Start here");
		start.setUserObject(m_task.getKey());
	}

	@Override
	public void createContent() throws Exception {
		setPageTitle("A diagram the user draws");
		MaxGraphPanel.initialize(this);

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "A diagram the user draws"));
		cp.add(new Para().add("Drag a shape from the strip at the top into the drawing. Point at "
			+ "a shape and a green dot appears in the middle of it: drag that onto another shape "
			+ "to connect them. Double click a shape to rename it, drag the handle in the middle "
			+ "of an edge to bend it, and select anything and press Delete to remove it."));

		Div log = new Div("dm-tut-q");
		say(log, "Nothing has been drawn yet.");

		MaxGraphPanel panel = new MaxGraphPanel();
		cp.add(panel);
		panel.size("100%", "460px").setEditable(true).setModel(m_model)
			.addPaletteItem(m_task)
			.addPaletteItem(m_decision)
			.addPaletteItem(m_done)
			.setCreateHandler(createHandler(log))
			.setChangeHandler(change -> {
				say(log, "Changed: " + change.getType().getName() + " of "
					+ (null == change.getCell().getLabel() ? "an edge" : change.getCell().getLabel()));
				return true;
			});

		cp.add(log);
		cp.add(new Para().add("A cell the browser made up would have no id this side knows it "
			+ "by, so the browser never makes one: it says the user dropped a Task here, or drew "
			+ "a connection from this node to that one, and the page answers with the cell it "
			+ "wants. Answering with nothing is how it says no: try drawing a connection out of "
			+ "a Done and see. What kind of thing a node is lives in its user object, which is "
			+ "this side's own data and never goes to the browser."));
	}

	/**
	 * What the drawing gains from what the user asks for. Every rule about what may be
	 * drawn lives here: returning nothing is a refusal, and there is nothing to take back
	 * because nothing was made.
	 */
	private IGraphCreateHandler createHandler(Div log) {
		return new IGraphCreateHandler() {
			@Override
			public GraphNode createNode(GraphModel model, GraphPaletteItem item, double x, double y) {
				say(log, "Added a " + item.getKey() + " at " + Math.round(x) + "," + Math.round(y));
				GraphNode node = item.create(model, x, y);
				//-- What kind of thing this node stands for. It stays here; the browser is never told.
				node.setUserObject(item.getKey());
				return node;
			}

			@Override
			public GraphEdge createEdge(GraphModel model, GraphNode source, GraphNode target) {
				if("done".equals(source.getUserObject())) {
					say(log, "Refused: nothing leads out of a Done");
					return null;
				}
				say(log, "Connected " + source.getLabel() + " to " + target.getLabel());
				return model.addEdge(source, target);
			}
		};
	}

	/** One line per thing that happened, because one gesture is often several. */
	private static void say(Div log, String what) {
		if(log.getChildCount() > 8) {
			log.removeChild(0);
		}
		Div line = new Div();
		line.add(what);
		log.add(line);
	}
}
