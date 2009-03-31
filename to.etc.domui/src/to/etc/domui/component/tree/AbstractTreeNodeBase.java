package to.etc.domui.component.tree;

import java.util.*;

public abstract class AbstractTreeNodeBase<T extends ITreeNode<?>> implements ITreeNode<T> {
	private T			m_parent;
	private List<T>		m_childList;

	abstract public List<T>	loadChildren() throws Exception;

	public AbstractTreeNodeBase(T dad) {
		m_parent = dad;
	}
	
	public T getChild(int index) throws Exception {
		if(m_childList == null)
			m_childList = loadChildren();
		return m_childList.get(index);
	}
	public int getChildCount() throws Exception {
		if(m_childList == null)
			m_childList = loadChildren();
		return m_childList.size();
	}
	public boolean hasChildren() throws Exception {
		if(m_childList == null)
			return true;
		return m_childList.size() != 0;
//		if(m_childList == null)
//			m_childList = loadChildren();
//		return m_childList.size() != 0;
	}
	public T getParent() throws Exception {
		return m_parent;
	}
	protected List<T> getChildList() {
		return m_childList;
	}
	protected void setChildList(List<T> childList) {
		m_childList = childList;
	}
}
