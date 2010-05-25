package to.etc.domui.component.tree;

import java.util.*;

/**
 * Concrete implementation of a tree node model using AbstractTreeNodeBase thingies.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 21, 2008
 */
public class TreeNodeModelBase<T extends ITreeNode<T>> implements ITreeModel<T> {
	private T m_root;

	private List<ITreeModelChangedListener> m_listeners = Collections.EMPTY_LIST;

	public TreeNodeModelBase(T root) {
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

	public T getChild(T parent, int index) throws Exception {
		return parent.getChild(index);
	}

	public int getChildCount(T item) throws Exception {
		return item.getChildCount();
	}

	public T getParent(T child) throws Exception {
		return child.getParent();
	}

	public T getRoot() throws Exception {
		return m_root;
	}

	public boolean hasChildren(T item) throws Exception {
		return item.hasChildren();
	}

	public void expandChildren(T item) throws Exception {
	}

	public void collapseChildren(T item) throws Exception {
	}
}
