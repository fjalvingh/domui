package to.etc.domui.databinding.list2;

import to.etc.domui.databinding.ObservableEvent;
import to.etc.domui.databinding.list.ListChange;
import to.etc.domui.databinding.observables.IObservableList;

import javax.annotation.Nonnull;
import java.util.List;

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

	public void visit(@Nonnull IListChangeVisitor<T> visitor) throws Exception {
		for(ListChange<T> lc : m_changeList)
			lc.visit(visitor);
	}
}
