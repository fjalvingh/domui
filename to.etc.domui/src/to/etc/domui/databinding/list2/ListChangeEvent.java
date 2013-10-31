package to.etc.domui.databinding.list2;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.databinding.*;
import to.etc.domui.databinding.list.*;
import to.etc.domui.databinding.observables.*;

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
