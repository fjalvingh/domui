package to.etc.domui.databinding.list;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.databinding.*;
import to.etc.domui.databinding.list2.*;

public class ListValueChangedEvent<E> extends ObservableEvent<List<E>, ListValueChangedEvent<E>, IListValueChangeListener<E>> {
	@Nonnull
	final private List<ListChange<E>> m_changeList;

	public ListValueChangedEvent(@Nonnull IObservableListValue<E> source, @Nonnull List<ListChange<E>> changeList) {
		super(source);
		m_changeList = changeList;
	}


	@Nonnull
	public List<ListChange<E>> getChanges() {
		return m_changeList;
	}

	public void visit(@Nonnull IListChangeVisitor<E> visitor) throws Exception {
		for(ListChange<E> lc : m_changeList)
			lc.visit(visitor);
	}

}
