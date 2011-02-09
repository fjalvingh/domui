package to.etc.domui.component.tbl;

import java.util.*;

abstract public class TableModelBase<T> implements ITableModel<T> {
	final private List<ITableModelListener<T>> m_listeners = new ArrayList<ITableModelListener<T>>();

	abstract protected T getItem(int ix) throws Exception;

	/**
	 * Add a change listener to this model. Don't forget to remove it at destruction time.
	 */
	public void addChangeListener(ITableModelListener<T> l) {
		m_listeners.add(l);
	}

	/**
	 * Remove a change listener from the model.
	 * @see to.etc.domui.component.tbl.ITableModel#removeChangeListener(to.etc.domui.component.tbl.ITableModelListener)
	 */
	public void removeChangeListener(ITableModelListener<T> l) {
		m_listeners.remove(l);
	}

	protected List<ITableModelListener<T>> getListeners() {
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
}
