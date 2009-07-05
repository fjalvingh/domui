package to.etc.domui.component.tree;

/**
 * Concrete implementation of a tree node model using AbstractTreeNodeBase thingies.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 21, 2008
 */
public class TreeNodeModelBase<T extends ITreeNode< ? >> implements ITreeModel<ITreeNode< ? >> {
	private ITreeNode< ? > m_root;

	public TreeNodeModelBase(ITreeNode< ? > root) {
		m_root = root;
	}

	public void addChangeListener(ITreeModelChangedListener l) {}

	public void removeChangeListener(ITreeModelChangedListener l) {}

	public ITreeNode< ? > getChild(ITreeNode< ? > parent, int index) throws Exception {
		return parent.getChild(index);
	}

	public int getChildCount(ITreeNode< ? > item) throws Exception {
		return item.getChildCount();
	}

	public ITreeNode< ? > getParent(ITreeNode< ? > child) throws Exception {
		return child.getParent();
	}

	public ITreeNode< ? > getRoot() throws Exception {
		return m_root;
	}

	public boolean hasChildren(ITreeNode< ? > item) throws Exception {
		return item.hasChildren();
	}
}
