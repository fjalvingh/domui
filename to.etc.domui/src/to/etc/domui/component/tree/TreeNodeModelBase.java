package to.etc.domui.component.tree;

import java.util.*;

/**
 * Concrete implementation of a tree node model using AbstractTreeNodeBase thingies.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 21, 2008
 */
public class TreeNodeModelBase<T extends ITreeNode< ? >> implements ITreeModel<ITreeNode< ? >> {
	private ITreeNode< ? > m_root;

	private List<ITreeModelChangedListener> m_listeners = Collections.EMPTY_LIST;

	public TreeNodeModelBase(ITreeNode< ? > root) {
		m_root = root;
	}

	public void addChangeListener(ITreeModelChangedListener l) {
		if(m_listeners == Collections.EMPTY_LIST) {
			m_listeners = new ArrayList<ITreeModelChangedListener>();
		}
		if(m_listeners.contains(l))
			return;
		m_listeners.add(l);
	}

	public void removeChangeListener(ITreeModelChangedListener l) {
		m_listeners.remove(l);
	}

	protected List<ITreeModelChangedListener> getListeners() {
		return m_listeners;
	}

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

	public void fireNodeWillExpand(ITreeNode< ? > item) throws Exception {
		for(ITreeModelChangedListener listener : getListeners()) {
			listener.nodeWillExpand(item);
		}
	}

	public void fireNodeWillCollapse(ITreeNode< ? > item) throws Exception {
		for(ITreeModelChangedListener listener : getListeners()) {
			listener.nodeWillCollapse(item);
		}
	}
}
