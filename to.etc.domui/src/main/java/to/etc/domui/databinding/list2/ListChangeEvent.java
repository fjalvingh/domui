package to.etc.domui.databinding.list2;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.databinding.ObservableEvent;
import to.etc.domui.databinding.list.ListChange;
import to.etc.domui.databinding.observables.IObservableList;

import java.util.List;

public class ListChangeEvent<T> extends ObservableEvent<T, ListChangeEvent<T>, IListChangeListener<T>> {
	@NonNull
	final private List<ListChange<T>> m_changeList;

	public ListChangeEvent(@NonNull IObservableList<T> source, @NonNull List<ListChange<T>> changeList) {
		super(source);
		m_changeList = changeList;
	}

	@NonNull
	public List<ListChange<T>> getChanges() {
		return m_changeList;
	}

	public void visit(@NonNull IListChangeVisitor<T> visitor) throws Exception {
		for(ListChange<T> lc : m_changeList)
			lc.visit(visitor);
	}
}
