package to.etc.domui.maxgraph.model;

/**
 * The kinds of change the two sides of a {@link GraphModel} tell each other about. The
 * name is what goes over the wire, in the "op" field of a change.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public enum GraphOpType {
	/** A node appeared; the whole node is sent. */
	AddNode("addNode"),

	/** An edge appeared; the whole edge is sent. */
	AddEdge("addEdge"),

	/** A cell is gone. Only its id is sent. */
	Remove("remove"),

	/** A node moved or was resized. */
	Geometry("geometry"),

	/** A cell's style changed; the complete style is sent, not a difference. */
	Style("style"),

	/** A cell's label changed. */
	Label("label"),

	/** An edge got another source or target. */
	Terminal("terminal"),

	/** An edge is routed through other waypoints. */
	Points("points"),

	/**
	 * Forget the drawing and ask for the model again. This is the answer to anything that
	 * cannot be expressed as a change, and to a change list that was made against a
	 * version of the model that no longer exists.
	 */
	Reload("reload");

	private final String m_name;

	GraphOpType(String name) {
		m_name = name;
	}

	/** The name this operation is known by in the wire protocol. */
	public String getName() {
		return m_name;
	}
}
