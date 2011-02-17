package to.etc.domui.component.tbl;

import java.util.*;

abstract public class AbstractSelectionModel<T> implements ISelectionModel<T> {
	final List<ISelectionListener<T>> m_listeners = new ArrayList<ISelectionListener<T>>();

	@Override
	public void addListener(ISelectionListener<T> l) {
		m_listeners.add(l);
	}

	@Override
	public void removeListener(ISelectionListener<T> l) {
		m_listeners.remove(l);
	}

	protected void callChanged(T item, boolean on) throws Exception {
		for(ISelectionListener<T> sl : m_listeners)
			sl.selectionChanged(item, on);
	}

	protected void callSelectionCleared() throws Exception {
		for(ISelectionListener<T> sl : m_listeners)
			sl.selectionCleared();
	}
}
