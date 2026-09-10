package to.etc.domui.maxgraph.model;

import org.eclipse.jdt.annotation.Nullable;

import java.util.function.Consumer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A vertex of the drawing: a shape at a place, with a size and a label.
 *
 * <p>A node can contain other nodes. A child's geometry is relative to its parent, which
 * is how a group moves its contents with it.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public final class GraphNode extends GraphCell {
	@Nullable
	private final GraphNode m_parent;

	private final List<GraphNode> m_children = new ArrayList<>();

	private final GraphGeometry m_geometry;

	GraphNode(GraphModel model, String id, @Nullable String label, @Nullable GraphNode parent, double x, double y, double width, double height) {
		super(model, id, label);
		m_parent = parent;
		m_geometry = new GraphGeometry(this, x, y, width, height);
		if(null != parent) {
			parent.m_children.add(this);
		}
	}

	/** The node this one sits in, or null when it sits in the drawing itself. */
	@Nullable
	public GraphNode getParent() {
		return m_parent;
	}

	public List<GraphNode> getChildren() {
		return Collections.unmodifiableList(m_children);
	}

	public GraphGeometry getGeometry() {
		return m_geometry;
	}

	public GraphNode at(double x, double y) {
		m_geometry.setPosition(x, y);
		return this;
	}

	public GraphNode size(double width, double height) {
		m_geometry.setSize(width, height);
		return this;
	}

	public GraphNode label(@Nullable String label) {
		setLabel(label);
		return this;
	}

	/** Fluent access to the style: {@code node.styled().shape(GraphShape.Ellipse)}. */
	public GraphNode styled(Consumer<GraphStyle> what) {
		what.accept(style());
		return this;
	}

	void internalRemoveChild(GraphNode child) {
		m_children.remove(child);
	}

	/** Called when a removed child is put back by an undo; it kept its parent all along. */
	void internalAddChild(GraphNode child) {
		if(!m_children.contains(child)) {
			m_children.add(child);
		}
	}
}
