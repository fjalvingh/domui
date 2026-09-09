package to.etc.domui.maxgraph.model;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A node or an edge of a {@link GraphModel}.
 *
 * <p>The id is allocated by the model and is what both sides of the wire address a cell
 * by; it never changes. The {@link #getUserObject()} is the application's own data for
 * this cell - the entity a node stands for, usually - and it stays on the server: only
 * the label and the style are sent to the browser.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
abstract public class GraphCell {
	private final GraphModel m_model;

	private final String m_id;

	@Nullable
	private String m_label;

	private final GraphStyle m_style = new GraphStyle();

	@Nullable
	private Object m_userObject;

	protected GraphCell(GraphModel model, String id, @Nullable String label) {
		m_model = model;
		m_id = id;
		m_label = label;
	}

	public GraphModel getModel() {
		return m_model;
	}

	public String getId() {
		return m_id;
	}

	@Nullable
	public String getLabel() {
		return m_label;
	}

	public void setLabel(@Nullable String label) {
		m_label = label;
		m_model.changed();
	}

	/**
	 * The style of this cell, changed in place: {@code cell.style().fillColor("#eef")}.
	 *
	 * <p>Handing out the mutable style means the model cannot see the change, so a style
	 * edited after the drawing has been sent does not reach the browser by itself. That is
	 * what the change recording of phase 2 is for; until then, style the cells while the
	 * model is being built.</p>
	 */
	public GraphStyle style() {
		return m_style;
	}

	@Nullable
	public Object getUserObject() {
		return m_userObject;
	}

	public void setUserObject(@Nullable Object userObject) {
		m_userObject = userObject;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + m_id + " " + m_label + "]";
	}
}
