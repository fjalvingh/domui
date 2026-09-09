package to.etc.domui.maxgraph.model;

/**
 * How an edge finds its way from source to target: the edge styles maxGraph registers
 * by default. Without one an edge is a straight line.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public enum GraphEdgeStyle {
	Orthogonal("orthogonalEdgeStyle"),
	Elbow("elbowEdgeStyle"),
	EntityRelation("entityRelationEdgeStyle"),
	Loop("loopEdgeStyle"),
	Manhattan("manhattanEdgeStyle"),
	Segment("segmentEdgeStyle"),
	SideToSide("sideToSideEdgeStyle"),
	TopToBottom("topToBottomEdgeStyle");

	private final String m_name;

	GraphEdgeStyle(String name) {
		m_name = name;
	}

	/** The name maxGraph registers this edge style under. */
	public String getName() {
		return m_name;
	}
}
