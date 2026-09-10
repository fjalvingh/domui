package to.etc.domui.maxgraph.model;

/**
 * Which way a layout grows: {@link GraphLayoutType#Hierarchical} lays its layers out this
 * way, and {@link GraphLayoutType#Tree} its branches. The other layouts have no direction
 * and ignore it.
 *
 * <p>{@link #North} is the usual one: the first layer at the top, the drawing growing
 * downwards.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public enum GraphLayoutDirection {
	/** Top to bottom. */
	North("north"),

	/** Bottom to top. */
	South("south"),

	/** Right to left. */
	East("east"),

	/** Left to right. */
	West("west");

	private final String m_name;

	GraphLayoutDirection(String name) {
		m_name = name;
	}

	/** The name this direction is known by in the wire protocol. */
	public String getName() {
		return m_name;
	}
}
