package to.etc.domuidemo.pages.components.graph;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.maxgraph.MaxGraphPanel;
import to.etc.domui.maxgraph.model.GraphCell;
import to.etc.domui.maxgraph.model.GraphChange;
import to.etc.domui.maxgraph.model.GraphEdge;
import to.etc.domui.maxgraph.model.GraphGeometry;
import to.etc.domui.maxgraph.model.GraphModel;
import to.etc.domui.maxgraph.model.GraphNode;
import to.etc.domui.maxgraph.model.GraphOpType;
import to.etc.domui.maxgraph.model.GraphShape;

/**
 * A drawing the user may change, and a server that has the last word on it: dragging a
 * node moves it in the Java model, and deleting the hub is refused, which puts it back.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class EditableGraphPage extends UrlPage {
	private final GraphModel m_model = new GraphModel();

	private final GraphNode m_hub;

	public EditableGraphPage() {
		m_hub = m_model.addNode("The hub stays", 280, 40, 160, 50)
			.styled(s -> s.shape(GraphShape.Ellipse).fillColor("#f8cecc").strokeColor("#b85450"));
		for(int i = 1; i <= 3; i++) {
			GraphNode node = m_model.addNode("Drag me " + i, 40 + 170 * (i - 1), 240, 140, 40)
				.styled(s -> s.rounded(true).fillColor("#dae8fc").strokeColor("#6c8ebf"));
			m_model.addEdge(m_hub, node);
		}
	}

	@Override
	public void createContent() throws Exception {
		setPageTitle("A diagram the user changes");
		MaxGraphPanel.initialize(this);

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "A diagram the user changes"));
		cp.add(new Para().add("Drag a node, or click one and press Delete. Every change goes to "
			+ "the server before it counts: what the model accepts is what the drawing shows. "
			+ "Deleting the hub is refused, so it comes straight back - and its edges with it."));

		//-- Both of these are made again whenever the page is, so the handler below never
		//-- talks to a Div that is no longer on the screen.
		Div log = new Div("dm-tut-q");
		say(log, "Nothing has happened yet.");

		MaxGraphPanel panel = new MaxGraphPanel();
		cp.add(panel);
		panel.size("100%", "420px").setEditable(true).setModel(m_model)
			.setChangeHandler(change -> handleChange(log, change));

		cp.add(log);
		cp.add(new Para().add("The handler is asked about each change before the model is "
			+ "touched. Refusing one leaves the model as it was, and the browser is then told "
			+ "what the cell really is - which is what puts the drawing back."));
	}

	/**
	 * Say what happened, and refuse to lose the hub. Refusing is returning false; nothing
	 * has to be undone, because nothing was done yet.
	 */
	private boolean handleChange(Div log, GraphChange change) {
		if(change.getType() == GraphOpType.Remove && change.getCell() == m_hub) {
			say(log, "Refused: the hub cannot be deleted");
			return false;
		}
		switch(change.getType()) {
			default:
				say(log, "Changed: " + change);
				break;

			case Geometry:
				GraphGeometry to = change.getGeometry();
				say(log, "Moved " + name(change.getCell()) + " to "
					+ (null == to ? "?" : Math.round(to.getX()) + "," + Math.round(to.getY())));
				break;

			case Remove:
				say(log, "Deleted " + name(change.getCell()));
				break;
		}
		return true;
	}

	/** An edge has no label of its own, so it is named after the node it hangs on. */
	private static String name(GraphCell cell) {
		String label = cell.getLabel();
		if(null != label) {
			return label;
		}
		GraphNode target = cell instanceof GraphEdge edge ? edge.getTarget() : null;
		return "the edge to " + (null == target ? "?" : target.getLabel());
	}

	/**
	 * One line per change, because one gesture is usually several: deleting a node deletes
	 * the edges that hang on it, and each of those is a change of its own.
	 */
	private static void say(Div log, String what) {
		if(log.getChildCount() > 8) {
			log.removeChild(0);
		}
		Div line = new Div();
		line.add(what);
		log.add(line);
	}
}
