package to.etc.domui.databinding.list;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.databinding.*;
import to.etc.domui.databinding.list2.*;

/**
 * This event describes what happened to an observed List property. In addition to the normal list events (like elements being added
 * or deleted to the thing) this also observes changes to the property <b>itself</b>, meaning when the property is set to contain
 * a <b>new</b> list.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 1, 2013
 */
public class ListValueChangeEvent<E> extends ObservableEvent<List<E>, ListValueChangeEvent<E>, IListValueChangeListener<E>> {
	@Nonnull
	final private List<ListChange<E>> m_changeList;

	public ListValueChangeEvent(@Nonnull IObservableListValue<E> source, @Nonnull List<ListChange<E>> changeList) {
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
