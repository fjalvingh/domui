package to.etc.domui.component.tree3;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.Li;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.Span;
import to.etc.domui.dom.html.Ul;

import java.util.Objects;

/**
 * Visible counterpart of a tree node.
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2019-10-05.
 */
@NonNullByDefault
final public class Tree3Node<V> extends Li {
	final private V m_value;

	private final boolean m_isRoot;

	/** Container for the expand/collapse button and its rendition */
	final private Div m_foldingIcon = new Div("ui-tree3-fbtn");

	final private NodeContainer m_content;

	@Nullable
	private Ul m_childRoot;

	private Tree3NodeType m_treeNodeType = Tree3NodeType.LEAF;

	private boolean m_selected;

	private boolean m_expanded;

	private boolean m_unExpandable;

	public Tree3Node(V item, boolean isRoot) {
		m_value = item;
		m_isRoot = isRoot;
		m_content = new Span();
	}

	@Override public void createContent() throws Exception {
		updateCssClass();
		if(isUnExpandable()) {
			m_content.addCssClass("ui-tree3-leaf ui-tree3-label");
		} else {
			m_content.addCssClass("ui-tree3-branch ui-tree3-label");
		}
		add(m_foldingIcon);
		add(m_content);
		Ul childRoot = m_childRoot;
		if(null != childRoot)
			add(childRoot);
	}

	NodeContainer getContent() {
		return m_content;
	}

	Tree3NodeType getType() {
		return m_treeNodeType;
	}

	void setType(Tree3NodeType treeNodeType) {
		if(Objects.equals(treeNodeType, m_treeNodeType))
			return;
		m_treeNodeType = treeNodeType;
		updateCssClass();
	}

	private void updateCssClass() {
		setCssClass("ui-tree3-item ui-tree3-"
			+ m_treeNodeType.name().toLowerCase().replace("_", "-")
			+ (m_treeNodeType == Tree3NodeType.OPENED_LAST ? " ui-tree3-opened" : "")
			+ (m_treeNodeType == Tree3NodeType.CLOSED_LAST ? " ui-tree3-closed" : "")
			+ (m_selected ? " ui-tree3-selected" : "")
			+ (m_isRoot ? " ui-tree3-rootitem" : "")
		);
		m_foldingIcon.setText(m_expanded ? "-" : "+");
		m_foldingIcon.setCssClass(m_expanded ? "ui-tree3-fbtn ui-tree3-opened" : "ui-tree3-fbtn ui-tree3-closed");
	}

	void setFoldingClicked(@Nullable IClicked<? extends NodeBase> clicked) {
		m_foldingIcon.setClicked(clicked);
	}

	void internalSetSelected(boolean selected) {
		m_selected = selected;
		if(selected)
			m_content.addCssClass("ui-tree3-selected");
		else
			m_content.removeCssClass("ui-tree3-selected");
	}

	@Nullable Ul getChildRoot() {
		return m_childRoot;
	}

	void setChildRoot(@Nullable Ul childRoot) {
		m_childRoot = childRoot;
	}

	public V getValue() {
		return m_value;
	}

	boolean isExpanded() {
		return m_expanded;
	}

	void setExpanded(boolean expanded) {
		m_expanded = expanded;
		m_foldingIcon.setText(expanded ? "-" : "+");
		m_foldingIcon.setCssClass(expanded ? "ui-tree3-fbtn ui-tree3-opened" : "ui-tree3-fbtn ui-tree3-closed");
	}

	boolean isUnExpandable() {
		return m_unExpandable;
	}

	void setUnExpandable(boolean unExpandable) {
		this.m_unExpandable = unExpandable;
	}
}
