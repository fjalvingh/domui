package to.etc.domui.maxgraph;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.maxgraph.model.GraphNode;
import to.etc.domui.maxgraph.model.GraphOpType;

/**
 * Something the user asked for that the browser cannot do by itself: a new cell, or a step
 * back through the model's history. Ids and history are the server's, so the browser says
 * what the user did and the server decides what to make of it.
 *
 * <p>Unlike a change, this is not about a cell as it is, and it only ever travels from the
 * browser to the server.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public final class GraphRequest {
	private final GraphOpType m_type;

	@Nullable
	private final String m_paletteKey;

	private final double m_x;

	private final double m_y;

	@Nullable
	private final GraphNode m_source;

	@Nullable
	private final GraphNode m_target;

	private GraphRequest(GraphOpType type, @Nullable String paletteKey, double x, double y, @Nullable GraphNode source, @Nullable GraphNode target) {
		m_type = type;
		m_paletteKey = paletteKey;
		m_x = x;
		m_y = y;
		m_source = source;
		m_target = target;
	}

	static GraphRequest node(String paletteKey, double x, double y) {
		return new GraphRequest(GraphOpType.RequestNode, paletteKey, x, y, null, null);
	}

	static GraphRequest edge(GraphNode source, GraphNode target) {
		return new GraphRequest(GraphOpType.RequestEdge, null, 0, 0, source, target);
	}

	/** The user pressed ctrl-Z; what that takes back is the model's to decide. */
	static GraphRequest undo() {
		return new GraphRequest(GraphOpType.RequestUndo, null, 0, 0, null, null);
	}

	static GraphRequest redo() {
		return new GraphRequest(GraphOpType.RequestRedo, null, 0, 0, null, null);
	}

	public GraphOpType getType() {
		return m_type;
	}

	/** The palette item the user dropped. Only for {@link GraphOpType#RequestNode}. */
	@Nullable
	public String getPaletteKey() {
		return m_paletteKey;
	}

	/** Where it was dropped, in the drawing's own coordinates. */
	public double getX() {
		return m_x;
	}

	public double getY() {
		return m_y;
	}

	/** Where the connection was drawn from. Only for {@link GraphOpType#RequestEdge}. */
	@Nullable
	public GraphNode getSource() {
		return m_source;
	}

	@Nullable
	public GraphNode getTarget() {
		return m_target;
	}

	@Override
	public String toString() {
		switch(m_type) {
			default:
				return m_type.getName();

			case RequestNode:
				return m_type.getName() + " " + m_paletteKey + " at " + m_x + "," + m_y;

			case RequestEdge:
				return m_type.getName() + " " + m_source + " -> " + m_target;
		}
	}
}
