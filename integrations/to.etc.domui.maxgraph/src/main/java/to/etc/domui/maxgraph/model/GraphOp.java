package to.etc.domui.maxgraph.model;

import org.eclipse.jdt.annotation.Nullable;

/**
 * One change to a {@link GraphModel}: what happened, and to which cell.
 *
 * <p>An operation names a cell, it does not copy it. What is sent for a change is read
 * from the cell at the moment the change list is rendered, so a cell changed three times
 * in one round trip is sent once, in the state it ended up in. That also means the order
 * inside a list is the only thing that matters, and that applying the same list twice
 * does nothing the second time.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public final class GraphOp {
	private final GraphOpType m_type;

	/** The cell this is about; null for {@link GraphOpType#Reload}, which is about the whole drawing. */
	@Nullable
	private final GraphCell m_cell;

	private GraphOp(GraphOpType type, @Nullable GraphCell cell) {
		m_type = type;
		m_cell = cell;
	}

	public static GraphOp add(GraphCell cell) {
		return new GraphOp(cell instanceof GraphNode ? GraphOpType.AddNode : GraphOpType.AddEdge, cell);
	}

	public static GraphOp remove(GraphCell cell) {
		return new GraphOp(GraphOpType.Remove, cell);
	}

	public static GraphOp label(GraphCell cell) {
		return new GraphOp(GraphOpType.Label, cell);
	}

	public static GraphOp style(GraphCell cell) {
		return new GraphOp(GraphOpType.Style, cell);
	}

	public static GraphOp geometry(GraphNode node) {
		return new GraphOp(GraphOpType.Geometry, node);
	}

	public static GraphOp terminal(GraphEdge edge) {
		return new GraphOp(GraphOpType.Terminal, edge);
	}

	public static GraphOp points(GraphEdge edge) {
		return new GraphOp(GraphOpType.Points, edge);
	}

	/** Tell the other side to throw its drawing away and ask for the model again. */
	public static GraphOp reload() {
		return new GraphOp(GraphOpType.Reload, null);
	}

	public GraphOpType getType() {
		return m_type;
	}

	@Nullable
	public GraphCell getCell() {
		return m_cell;
	}

	/**
	 * Two operations are the same when they say the same thing about the same cell -
	 * which is what makes "changed twice, sent once" a list membership test.
	 */
	@Override
	public boolean equals(@Nullable Object other) {
		if(this == other) {
			return true;
		}
		return other instanceof GraphOp op && op.m_type == m_type && op.m_cell == m_cell;
	}

	@Override
	public int hashCode() {
		GraphCell cell = m_cell;
		return m_type.hashCode() * 31 + (null == cell ? 0 : System.identityHashCode(cell));
	}

	@Override
	public String toString() {
		GraphCell cell = m_cell;
		return m_type.getName() + (null == cell ? "" : " " + cell.getId());
	}
}
