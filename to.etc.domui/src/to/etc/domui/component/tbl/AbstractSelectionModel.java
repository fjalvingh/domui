package to.etc.domui.component.tbl;

import java.util.*;

import javax.annotation.*;

/**
 * Abstract base class for selection models which handles the registration and calling of
 * listeners. It implements none of the selection logic: use one of the subclasses
 * for that.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 17, 2011
 */
abstract public class AbstractSelectionModel<T> implements ISelectionModel<T> {
	final List<ISelectionListener<T>> m_listeners = new ArrayList<ISelectionListener<T>>();

	@Override
	public void addListener(@Nonnull ISelectionListener<T> l) {
		m_listeners.add(l);
	}

	@Override
	public void removeListener(@Nonnull ISelectionListener<T> l) {
		m_listeners.remove(l);
	}

	protected void callChanged(@Nonnull T item, boolean on) throws Exception {
		for(ISelectionListener<T> sl : m_listeners)
			sl.selectionChanged(item, on);
	}

	protected void callSelectionAllChanged() throws Exception {
		for(ISelectionListener<T> sl : m_listeners)
			sl.selectionAllChanged();
	}
}
