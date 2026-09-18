package to.etc.domui.maxgraph.model;

/**
 * A point in the drawing's coordinate system, used for the waypoints of an edge.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public final class GraphPoint {
	private final double m_x;

	private final double m_y;

	public GraphPoint(double x, double y) {
		m_x = x;
		m_y = y;
	}

	public double getX() {
		return m_x;
	}

	public double getY() {
		return m_y;
	}

	@Override
	public String toString() {
		return "(" + m_x + "," + m_y + ")";
	}
}
