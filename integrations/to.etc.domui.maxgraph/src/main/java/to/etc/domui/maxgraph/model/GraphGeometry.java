package to.etc.domui.maxgraph.model;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Where a node sits and how big it is. A node without a size is not drawn, so both
 * are always set.
 *
 * <p>The geometry of a node in a model reports every change it is given to that model, so
 * moving a node by changing its geometry is seen exactly like {@link GraphNode#at(double, double)}.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public final class GraphGeometry {
	/** The node this is the geometry of, or null for a geometry that is not part of a drawing. */
	@Nullable
	private final GraphNode m_owner;

	private double m_x;

	private double m_y;

	private double m_width;

	private double m_height;

	public GraphGeometry(double x, double y, double width, double height) {
		this(null, x, y, width, height);
	}

	GraphGeometry(@Nullable GraphNode owner, double x, double y, double width, double height) {
		m_owner = owner;
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
		changed();
	}

	public void setPosition(double x, double y) {
		m_x = x;
		m_y = y;
		changed();
	}

	public void setSize(double width, double height) {
		m_width = width;
		m_height = height;
		changed();
	}

	private void changed() {
		GraphNode owner = m_owner;
		if(null != owner) {
			owner.getModel().changed(GraphOp.geometry(owner));
		}
	}

	@Override
	public String toString() {
		return m_x + "," + m_y + " " + m_width + "x" + m_height;
	}
}
