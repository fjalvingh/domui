package to.etc.domui.maxgraph.model;

import org.eclipse.jdt.annotation.Nullable;

import java.util.function.Consumer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A connection between two nodes. Both ends may be missing: an edge with a loose end is
 * drawn to wherever its waypoints leave it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public final class GraphEdge extends GraphCell {
	@Nullable
	private GraphNode m_source;

	@Nullable
	private GraphNode m_target;

	private final List<GraphPoint> m_waypoints = new ArrayList<>();

	GraphEdge(GraphModel model, String id, @Nullable String label, @Nullable GraphNode source, @Nullable GraphNode target) {
		super(model, id, label);
		m_source = source;
		m_target = target;
	}

	@Nullable
	public GraphNode getSource() {
		return m_source;
	}

	@Nullable
	public GraphNode getTarget() {
		return m_target;
	}

	public void setSource(@Nullable GraphNode source) {
		GraphNode old = m_source;
		getModel().record(() -> setSource(old));
		m_source = source;
		getModel().changed(GraphOp.terminal(this));
	}

	public void setTarget(@Nullable GraphNode target) {
		GraphNode old = m_target;
		getModel().record(() -> setTarget(old));
		m_target = target;
		getModel().changed(GraphOp.terminal(this));
	}

	/** The points the edge is routed through, in order. */
	public List<GraphPoint> getWaypoints() {
		return Collections.unmodifiableList(m_waypoints);
	}

	public GraphEdge waypoint(double x, double y) {
		recordWaypoints();
		m_waypoints.add(new GraphPoint(x, y));
		getModel().changed(GraphOp.points(this));
		return this;
	}

	/**
	 * Route the edge through these points instead of the ones it has. Which is what the
	 * browser sends when the user bends an edge: the whole route, not a difference.
	 */
	public GraphEdge setWaypoints(List<GraphPoint> points) {
		recordWaypoints();
		m_waypoints.clear();
		m_waypoints.addAll(points);
		getModel().changed(GraphOp.points(this));
		return this;
	}

	public void clearWaypoints() {
		setWaypoints(Collections.emptyList());
	}

	/** The route the edge takes now, as the change that puts it back. */
	private void recordWaypoints() {
		List<GraphPoint> old = new ArrayList<>(m_waypoints);
		getModel().record(() -> setWaypoints(old));
	}

	public GraphEdge label(@Nullable String label) {
		setLabel(label);
		return this;
	}

	/** Fluent access to the style: {@code edge.styled(s -> s.dashed(true))}. */
	public GraphEdge styled(Consumer<GraphStyle> what) {
		what.accept(style());
		return this;
	}
}
