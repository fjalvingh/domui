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

	private List<ITreeModelChangedListener<T>> m_listeners = Collections.EMPTY_LIST;

	public TreeNodeModelBase(T root) {
		m_root = root;
	}

	@Override
	public void addChangeListener(ITreeModelChangedListener<T> l) {
		if(m_listeners == Collections.EMPTY_LIST) {
			m_listeners = new ArrayList<ITreeModelChangedListener<T>>();
		}
		if(m_listeners.contains(l))
			return;
		m_listeners.add(l);
	}

	@Override
	public void removeChangeListener(ITreeModelChangedListener<T> l) {
		m_listeners.remove(l);
	}

	protected List<ITreeModelChangedListener<T>> getListeners() {
		return m_listeners;
	}

	@Override
	public T getChild(T parent, int index) throws Exception {
		return parent.getChild(index);
	}

	@Override
	public int getChildCount(T item) throws Exception {
		return item.getChildCount();
	}

	@Override
	public T getParent(T child) throws Exception {
		return child.getParent();
	}

	@Override
	public T getRoot() throws Exception {
		return m_root;
	}

	@Override
	public boolean hasChildren(T item) throws Exception {
		return item.hasChildren();
	}

	@Override
	public void expandChildren(T item) throws Exception {
	}

	@Override
	public void collapseChildren(T item) throws Exception {
	}
}
