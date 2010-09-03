package to.etc.domui.component.tbl;

import java.util.*;

abstract public class TableModelBase<T> implements ITableModel<T> {
	private List<ITableModelListener<T>> m_listeners = Collections.EMPTY_LIST;

	abstract public T getItem(int ix) throws Exception;

	/**
	 * Add a change listener to this model. Don't forget to remove it at destruction time.
	 */
	@Override
	public void addChangeListener(ITableModelListener<T> l) {
		synchronized(this) {
			if(m_listeners.contains(l))
				return;
			m_listeners = new ArrayList<ITableModelListener<T>>(m_listeners);
			m_listeners.add(l);
		}
	}

	/**
	 * Remove a change listener from the model.
	 * @see to.etc.domui.component.tbl.ITableModel#removeChangeListener(to.etc.domui.component.tbl.ITableModelListener)
	 */
	@Override
	public void removeChangeListener(ITableModelListener<T> l) {
		synchronized(this) {
			m_listeners = new ArrayList<ITableModelListener<T>>();
			m_listeners.remove(l);
		}
	}

	protected synchronized List<ITableModelListener<T>> getListeners() {
		return m_listeners;
	}

	public void fireAdded(int index) throws Exception {
		T o = getItem(index);
		for(ITableModelListener<T> l : getListeners())
			l.rowAdded(this, index, o);
	}

	public void fireDeleted(int index, T deleted) throws Exception {
		for(ITableModelListener<T> l : getListeners())
			l.rowDeleted(this, index, deleted);
	}

	public void fireModified(int index) throws Exception {
		T o = getItem(index);
		for(ITableModelListener<T> l : getListeners())
			l.rowModified(this, index, o);
	}

	public void fireModelChanged() throws Exception {
		for(ITableModelListener<T> l : getListeners())
			l.modelChanged(this);
	}

	@Override
	public void refresh() {}
}
