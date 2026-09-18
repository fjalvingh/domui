package to.etc.domui.maxgraph.model;

/**
 * A way of arranging a drawing, asked for with
 * {@code to.etc.domui.maxgraph.MaxGraphPanel#layout(GraphLayoutType)}.
 *
 * <p>Arranging happens in the browser, because that is where the drawing is; what it moves
 * comes back as ordinary changes and is what the model then holds - so a drawing that is
 * arranged and later rebuilt comes back arranged.</p>
 *
 * <p>Not every layout suits every drawing, and one that does not fit does nothing rather
 * than something wrong: a tree layout ignores a drawing with a cycle in it, and the ones
 * that work along the edges ignore a node that has none.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public enum GraphLayoutType {
	/**
	 * Layers, in the direction given: what a flow chart wants. The first thing to try for a
	 * drawing that has a direction to it.
	 */
	Hierarchical("hierarchical"),

	/** Force-directed, which spreads a connected drawing out evenly. Nodes without edges are left where they are. */
	Organic("organic"),

	/** Everything on a circle. The edges are not looked at, so it arranges an unconnected drawing too. */
	Circle("circle"),

	/** A compact tree, growing in the direction given. Only for a drawing that is a tree; a cycle is left alone. */
	Tree("tree"),

	/** The same tree, laid out around its root instead of below it. */
	RadialTree("radialTree"),

	/**
	 * Not an arrangement of the whole drawing but a repair of one thing in it: edges that
	 * run between the same two nodes are pulled apart so that all of them can be seen.
	 */
	ParallelEdges("parallelEdges");

	private final String m_name;

	GraphLayoutType(String name) {
		m_name = name;
	}

	/** The name this layout is known by in the wire protocol. */
	public String getName() {
		return m_name;
	}
}
