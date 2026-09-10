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

	private final GraphStyle m_style = new GraphStyle(this);

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
		String old = m_label;
		m_model.record(() -> setLabel(old));
		m_label = label;
		m_model.changed(GraphOp.label(this));
	}

	/**
	 * The style of this cell, changed in place: {@code cell.style().fillColor("#eef")}.
	 * Changing it is a change to the model like any other, so a cell restyled long after
	 * the drawing was sent is restyled in the browser too.
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

	/** Called by the cell's own {@link GraphStyle} when a property in it was set. */
	void internalStyleChanged() {
		m_model.changed(GraphOp.style(this));
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + m_id + " " + m_label + "]";
	}
}
