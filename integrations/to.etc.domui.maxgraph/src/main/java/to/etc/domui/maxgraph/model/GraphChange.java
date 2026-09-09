package to.etc.domui.maxgraph.model;

import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A change the user made in the browser, before it is made to the model: what kind of
 * change it is, which cell it is about, and what it proposes.
 *
 * <p>This is what an {@link IGraphChangeHandler} is asked about. Nothing has happened to
 * the model yet when it is asked, so the cell still holds what it was; what the user did
 * is on this object. Refusing it leaves the model as it is, and the browser is told what
 * the cell really looks like - which puts the drawing back.</p>
 *
 * <p>It is the mirror of {@link GraphOp}, which is a change that already happened and is
 * on its way to the browser.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public final class GraphChange {
	private final GraphOpType m_type;

	private final GraphCell m_cell;

	@Nullable
	private final GraphGeometry m_geometry;

	@Nullable
	private final String m_label;

	@Nullable
	private final GraphNode m_source;

	@Nullable
	private final GraphNode m_target;

	@Nullable
	private final List<GraphPoint> m_points;

	@Nullable
	private final GraphStyle m_style;

	private GraphChange(GraphOpType type, GraphCell cell, @Nullable GraphGeometry geometry, @Nullable String label,
		@Nullable GraphNode source, @Nullable GraphNode target, @Nullable List<GraphPoint> points, @Nullable GraphStyle style) {
		m_type = type;
		m_cell = cell;
		m_geometry = geometry;
		m_label = label;
		m_source = source;
		m_target = target;
		m_points = points;
		m_style = style;
	}

	public static GraphChange geometry(GraphNode node, GraphGeometry geometry) {
		return new GraphChange(GraphOpType.Geometry, node, geometry, null, null, null, null, null);
	}

	public static GraphChange label(GraphCell cell, @Nullable String label) {
		return new GraphChange(GraphOpType.Label, cell, null, label, null, null, null, null);
	}

	public static GraphChange terminal(GraphEdge edge, @Nullable GraphNode source, @Nullable GraphNode target) {
		return new GraphChange(GraphOpType.Terminal, edge, null, null, source, target, null, null);
	}

	public static GraphChange points(GraphEdge edge, List<GraphPoint> points) {
		return new GraphChange(GraphOpType.Points, edge, null, null, null, null, new ArrayList<>(points), null);
	}

	public static GraphChange style(GraphCell cell, GraphStyle style) {
		return new GraphChange(GraphOpType.Style, cell, null, null, null, null, null, style);
	}

	public static GraphChange remove(GraphCell cell) {
		return new GraphChange(GraphOpType.Remove, cell, null, null, null, null, null, null);
	}

	public GraphOpType getType() {
		return m_type;
	}

	public GraphCell getCell() {
		return m_cell;
	}

	/** Where the node is to end up. Only for {@link GraphOpType#Geometry}. */
	@Nullable
	public GraphGeometry getGeometry() {
		return m_geometry;
	}

	/** The label the cell is to get. Only for {@link GraphOpType#Label}. */
	@Nullable
	public String getLabel() {
		return m_label;
	}

	/** The end the edge is to start at, null for none. Only for {@link GraphOpType#Terminal}. */
	@Nullable
	public GraphNode getSource() {
		return m_source;
	}

	/** The end the edge is to point at, null for none. Only for {@link GraphOpType#Terminal}. */
	@Nullable
	public GraphNode getTarget() {
		return m_target;
	}

	/** The points the edge is to be routed through. Only for {@link GraphOpType#Points}. */
	public List<GraphPoint> getPoints() {
		List<GraphPoint> points = m_points;
		return null == points ? Collections.emptyList() : Collections.unmodifiableList(points);
	}

	/** The complete style the cell is to get. Only for {@link GraphOpType#Style}. */
	@Nullable
	public GraphStyle getStyle() {
		return m_style;
	}

	/**
	 * Make this change to the model. Called for a change that was accepted; a page has no
	 * reason to call it, but nothing stops it either.
	 */
	public void apply() {
		switch(m_type) {
			default:
				throw new IllegalStateException("A " + m_type + " cannot come from the browser");

			case Geometry:
				GraphGeometry geometry = m_geometry;
				if(null != geometry) {
					((GraphNode) m_cell).getGeometry().setBounds(geometry.getX(), geometry.getY(), geometry.getWidth(), geometry.getHeight());
				}
				break;

			case Label:
				m_cell.setLabel(m_label);
				break;

			case Terminal:
				GraphEdge edge = (GraphEdge) m_cell;
				edge.setSource(m_source);
				edge.setTarget(m_target);
				break;

			case Points:
				GraphEdge routed = (GraphEdge) m_cell;
				routed.clearWaypoints();
				for(GraphPoint point : getPoints()) {
					routed.waypoint(point.getX(), point.getY());
				}
				break;

			case Style:
				GraphStyle style = m_style;
				if(null != style) {
					m_cell.style().replaceWith(style);
				}
				break;

			case Remove:
				m_cell.getModel().remove(m_cell);
				break;
		}
	}

	@Override
	public String toString() {
		return m_type.getName() + " " + m_cell.getId();
	}
}
