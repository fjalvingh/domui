package to.etc.domui.maxgraph.model;

import org.eclipse.jdt.annotation.Nullable;

import java.util.function.Consumer;

/**
 * One thing the user can drag onto a drawing to make a node of it: what it is called, how
 * big it is and what it looks like.
 *
 * <p>The browser uses this to show the item and to drag a preview of it around. What is
 * actually made when it is dropped is decided by the page's {@link IGraphCreateHandler},
 * which is usually {@link #create(GraphModel, double, double)} - a node just like the
 * item - and sometimes something more.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public final class GraphPaletteItem {
	private final String m_key;

	@Nullable
	private final String m_label;

	private final double m_width;

	private final double m_height;

	private final GraphStyle m_style = new GraphStyle();

	public GraphPaletteItem(String key, @Nullable String label, double width, double height) {
		m_key = key;
		m_label = label;
		m_width = width;
		m_height = height;
	}

	/** How the browser names this item when the user drops it; the page's own name for it. */
	public String getKey() {
		return m_key;
	}

	@Nullable
	public String getLabel() {
		return m_label;
	}

	public double getWidth() {
		return m_width;
	}

	public double getHeight() {
		return m_height;
	}

	public GraphStyle style() {
		return m_style;
	}

	/** Fluent access to the style: {@code item.styled(s -> s.shape(GraphShape.Ellipse))}. */
	public GraphPaletteItem styled(Consumer<GraphStyle> what) {
		what.accept(m_style);
		return this;
	}

	/**
	 * Add a node like this item to the model, at this place. The usual thing for a create
	 * handler to do.
	 */
	public GraphNode create(GraphModel model, double x, double y) {
		GraphNode node = model.addNode(m_label, x, y, m_width, m_height);
		node.style().replaceWith(m_style);
		return node;
	}

	@Override
	public String toString() {
		return "GraphPaletteItem[" + m_key + " " + m_label + "]";
	}
}
