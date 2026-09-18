package to.etc.domui.maxgraph.model;

/**
 * The shapes maxGraph knows out of the box. A shape that is not in here - a registered
 * custom one - is set with {@link GraphStyle#raw(String, Object)}.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public enum GraphShape {
	Rectangle("rectangle"),
	Ellipse("ellipse"),
	DoubleEllipse("doubleEllipse"),
	Rhombus("rhombus"),
	Triangle("triangle"),
	Hexagon("hexagon"),
	Cylinder("cylinder"),
	Actor("actor"),
	Cloud("cloud"),
	Line("line"),
	Label("label"),
	Swimlane("swimlane"),
	Image("image"),
	Arrow("arrow"),
	ArrowConnector("arrowConnector"),
	Connector("connector");

	private final String m_name;

	GraphShape(String name) {
		m_name = name;
	}

	/** The name maxGraph registers this shape under. */
	public String getName() {
		return m_name;
	}
}
