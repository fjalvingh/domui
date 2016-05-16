package to.etc.domui.databinding.observables;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.databinding.*;
import to.etc.domui.databinding.list.*;
import to.etc.domui.databinding.list2.*;

/**
 * A list that generates change events for it's content, and that does <b>not allow null elements</b>. This
 * observes the list <i>itself</i>, not the property containing a list value.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 4, 2013
 */
public class ObservableList<T> extends ListenerList<T, ListChangeEvent<T>, IListChangeListener<T>> implements IObservableList<T> {
	private final List<T> m_list;

	/** When set this becomes an ordered model. */
	@Nullable
	private Comparator<T> m_comparator;

	@Nullable
	private List<ListChange<T>> m_currentChange;

	private int m_changeNestingCount;

	public ObservableList() {
		m_list = new ArrayList<T>();
	}

	public ObservableList(@Nonnull Collection<T> list) {
		m_list = new ArrayList<>(list);
	}

	public ObservableList(int size) {
		m_list = new ArrayList<>(size);
	}

	private List<ListChange<T>> startChange() {
		List<ListChange<T>> currentChange = m_currentChange;
		if(null == currentChange) {
			m_changeNestingCount = 1;
			m_currentChange = currentChange = new ArrayList<>();
		} else
			m_changeNestingCount++;
		return currentChange;
	}

	private void finishChange() {
		if(m_changeNestingCount <= 0)
			throw new IllegalStateException("Unbalanced startChange/finishChange");
		m_changeNestingCount--;

		if(m_changeNestingCount == 0) {
			List<ListChange<T>> currentChange = m_currentChange;
			m_currentChange = null;
			if(null == currentChange)
				throw new IllegalStateException("Logic error: no change list");
			fireEvent(new ListChangeEvent<>(this, currentChange));
		}
	}

	@Override
	public int size() {
		return m_list.size();
	}

	@Override
	public boolean isEmpty() {
		return m_list.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return m_list.contains(o);
	}

	@Nonnull
	@Override
	public Iterator<T> iterator() {
		return m_list.iterator();
	}

	@Override
	public Object[] toArray() {
		return m_list.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return m_list.toArray(a);
	}

	@Override
	public boolean add(T e) {
		int index;
		Comparator<T> comparator = m_comparator;
		if(null == comparator) {
			index = size();
			m_list.add(e);
		} else {
			index = Collections.binarySearch(m_list, e, comparator);
			if(index < 0)
				index = -(index + 1);
			m_list.add(index, e);
		}
		List<ListChange<T>> el = startChange();
		el.add(new ListChangeAdd<>(index, e));
		finishChange();
		return true;
	}

	@Override
	public boolean remove(Object o) {
		int indx = m_list.indexOf(o);
		if(indx < 0)
			return false;
		m_list.remove(indx);

		List<ListChange<T>> el = startChange();
		el.add(new ListChangeDelete<T>(indx, (T) o));
		finishChange();
		return true;
	}

	@Override
	public boolean containsAll(Collection< ? > c) {
		return m_list.containsAll(c);
	}

	@Override
	public boolean addAll(Collection< ? extends T> c) {
		List<ListChange<T>> el = startChange();
		Comparator<T> comparator = m_comparator;
		if(null == comparator) {
			m_list.addAll(c);
			int indx = size();
			for(T v : c) {
				el.add(new ListChangeAdd<T>(indx++, v));
			}
		} else {
			for(T v : c) {
				add(v);
			}
		}
		finishChange();
		return c.size() > 0;
	}

	@Override
	public boolean addAll(int index, Collection< ? extends T> c) {
		if(m_comparator != null)
			throw new IllegalStateException("Cannot add by index on a sorted model: the sorting order determines the insert index");
		boolean res = m_list.addAll(index, c);

		List<ListChange<T>> el = startChange();
		int indx = index;
		for(T v : c) {
			el.add(new ListChangeAdd<>(indx++, v));
		}
		finishChange();
		return res;
	}

	@Override
	public boolean removeAll(Collection< ? > c) {
		List<ListChange<T>> el = startChange();
		boolean done = false;
		for(Object v : c) {
			int indx = indexOf(v);
			if(indx >= 0) {
				m_list.remove(indx);
				el.add(new ListChangeDelete<T>(indx, (T) v));
				done = true;
			}
		}
		finishChange();
		return done;
	}

	@Override
	public boolean retainAll(Collection< ? > c) {
		List<ListChange<T>> el = startChange();
		boolean done = false;
		for(int i = size(); --i >= 0;) {
			T cv = m_list.get(i);
			if(!c.contains(cv)) {
				m_list.remove(i);
				el.add(new ListChangeDelete<T>(i, cv));
				done = true;
			}
		}
		finishChange();
		return done;
	}

	@Override
	public void clear() {
		List<ListChange<T>> el = startChange();
		for(int i = size(); --i >= 0;) {
			T cv = m_list.get(i);
			el.add(new ListChangeDelete<T>(i, cv));
		}
		finishChange();
		m_list.clear();
	}

	@Override
	public boolean equals(Object o) {
		return m_list.equals(o);
	}

	@Override
	public int hashCode() {
		return m_list.hashCode();
	}

	@Nonnull
	@Override
	public T get(int index) {
		return m_list.get(index);
	}

	@Override
	public T set(int index, T element) {
		if(m_comparator != null)
			throw new IllegalStateException("Cannot set by index on a sorted model: the sorting order determines the insert index");

		T res = m_list.set(index, element);

		List<ListChange<T>> el = startChange();
		el.add(new ListChangeModify<T>(index, res, element));
		finishChange();
		return res;
	}

	@Override
	public void add(int index, T element) {
		if(m_comparator != null)
			throw new IllegalStateException("Cannot add by index on a sorted model: the sorting order determines the insert index");
		m_list.add(index, element);

		List<ListChange<T>> el = startChange();
		el.add(new ListChangeAdd<T>(index, element));
		finishChange();
	}

	@Override
	public T remove(int index) {
		T res = m_list.remove(index);

		List<ListChange<T>> el = startChange();
		el.add(new ListChangeDelete<>(index, res));
		finishChange();
		return res;
	}

	@Override
	public int indexOf(Object o) {
		return m_list.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return m_list.lastIndexOf(o);
	}

	@Override
	public ListIterator<T> listIterator() {
		return m_list.listIterator();
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		return m_list.listIterator(index);
	}

	@Nonnull
	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		return m_list.subList(fromIndex, toIndex);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Sortable handling.									*/
	/*--------------------------------------------------------------*/
	/**
	 * When set the list will be kept ordered.
	 * @return
	 */
	@Nullable
	public Comparator<T> getComparator() {
		return m_comparator;
	}

	/**
	 * Sets a new comparator to use. This resorts the model, if needed, causing a full model update.
	 * @param comparator
	 * @throws Exception
	 */
	public void setComparator(@Nullable Comparator<T> comparator) throws Exception {
		if(m_comparator == comparator)
			return;
		m_comparator = comparator;
		if(comparator != null) {
			resort();
		}
	}

	private void resort() throws Exception {
		startChange();
		Collections.sort(m_list, m_comparator);
		finishChange();
	}
}
