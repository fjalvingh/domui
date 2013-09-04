package to.etc.domui.databinding;

import java.util.*;

import javax.annotation.*;

public class ListChangeEvent<T> extends ObservableEvent<T, ListChangeEvent<T>, IListChangeListener<T>> {
	@Nonnull
	final private List<ListChange<T>> m_changeList;

	public ListChangeEvent(@Nonnull IObservableList<T> source, @Nonnull List<ListChange<T>> changeList) {
		super(source);
		m_changeList = changeList;
	}

	@Nonnull
	public List<ListChange<T>> getChanges() {
		return m_changeList;
	}
}
