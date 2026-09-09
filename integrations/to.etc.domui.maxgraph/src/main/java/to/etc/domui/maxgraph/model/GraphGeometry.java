package to.etc.domui.maxgraph.model;

/**
 * Where a node sits and how big it is. A node without a size is not drawn, so both
 * are always set.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public final class GraphGeometry {
	private double m_x;

	private double m_y;

	private double m_width;

	private double m_height;

	public GraphGeometry(double x, double y, double width, double height) {
		m_x = x;
		m_y = y;
		m_width = width;
		m_height = height;
	}

	public double getX() {
		return m_x;
	}

	public double getY() {
		return m_y;
	}

	public double getWidth() {
		return m_width;
	}

	public double getHeight() {
		return m_height;
	}

	public void setBounds(double x, double y, double width, double height) {
		m_x = x;
		m_y = y;
		m_width = width;
		m_height = height;
	}

	public void setPosition(double x, double y) {
		m_x = x;
		m_y = y;
	}

	public void setSize(double width, double height) {
		m_width = width;
		m_height = height;
	}

	@Override
	public String toString() {
		return m_x + "," + m_y + " " + m_width + "x" + m_height;
	}
}
