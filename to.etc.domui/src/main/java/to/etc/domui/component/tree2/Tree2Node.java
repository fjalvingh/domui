package to.etc.domui.component.tree2;

import to.etc.domui.dom.html.ATag;
import to.etc.domui.dom.html.Li;
import to.etc.domui.dom.html.Ul;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Visible counterpart of a tree node.
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-8-17.
 */
@DefaultNonNull
final public class Tree2Node<V> extends Li {
	final private V m_value;

	final private TreeIcon m_icon = new TreeIcon();

	final private ATag m_content;

	@Nullable
	private Ul m_childRoot;

	private TreeNodeType m_treeNodeType = TreeNodeType.LEAF;

	private boolean m_selected;

	public boolean expanded;

	public boolean unexpandable;

	public Tree2Node(V item) {
		m_value = item;
		m_content = new ATag("ui-tree2-val");
		m_content.setHref("#");
	}

	@Override public void createContent() throws Exception {
		updateCssClass();
		add(m_icon);
		add(m_content);
		Ul childRoot = m_childRoot;
		if(null != childRoot)
			add(childRoot);
	}

	public ATag getContent() {
		return m_content;
	}

	public TreeNodeType getType() {
		return m_treeNodeType;
	}

	public void setType(TreeNodeType treeNodeType) {
		if(Objects.equals(treeNodeType, m_treeNodeType))
			return;
		m_treeNodeType = treeNodeType;
		updateCssClass();
	}

	private void updateCssClass() {
		setCssClass("ui-tree2-item ui-tree2-"
			+ m_treeNodeType.name().toLowerCase().replace("_", "-")
			+ (m_selected ? " ui-tree2-selected" : "")
		);
	}

	public TreeIcon getIcon() {
		return m_icon;
	}

	void internalSetSelected(boolean selected) {
		m_selected = selected;
		if(selected)
			m_content.addCssClass("ui-tree2-selected");
		else
			m_content.removeCssClass("ui-tree2-selected");
	}

	@Nullable public Ul getChildRoot() {
		return m_childRoot;
	}

	public void setChildRoot(@Nullable Ul childRoot) {
		m_childRoot = childRoot;
	}

	public V getValue() {
		return m_value;
	}
}
