package to.etc.domui.component.tree;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A tree node wrapping a T value.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2-1-18.
 */
abstract public class TreeNode {
	@Nullable
	final private TreeNode m_parent;

	@Nullable
	private List<TreeNode> m_children;

	abstract protected List<TreeNode> loadChildren() throws Exception;

	public TreeNode(TreeNode parent) {
		m_parent = parent;
	}

	public List<TreeNode> getChildren() throws Exception {
		if(m_children == null) {
			m_children = loadChildren();
		}
		return m_children;
	}

	@Nullable public TreeNode getParent() {
		return m_parent;
	}

	public <T extends TreeNode> int removeChild(T node) throws Exception {
		int index = getChildren().indexOf(node);
		if(index >= 0)
			getChildren().remove(index);
		return index;
	}
}
