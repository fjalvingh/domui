package to.etc.domui.databinding;

import java.util.*;

import javax.annotation.*;

/**
 * A list that generates change events for it's content, and that does <b>not allow null elements</b>. This
 * observes the list <i>itself</i>, not the property containing a list value.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 4, 2013
 */
public class ObservableList<T> extends ListenerList<T, ListChangeEvent<T>, IListChangeListener<T>> implements IObservableList<T> {
	private final List<T> m_list;

	public ObservableList() {
		m_list = new ArrayList<T>();
	}

	public ObservableList(@Nonnull List<T> list) {
		m_list = list;
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
	public boolean add(@Nonnull T e) {
		int indx = size();
		boolean res = m_list.add(e);

		List<ListChange<T>> el = new ArrayList<ListChange<T>>();
		el.add(new ListChangeAdd<T>(indx, e));
		fireEvent(new ListChangeEvent<T>(this, el));
		return res;
	}

	@Override
	public boolean remove(Object o) {
		int indx = m_list.indexOf(o);
		if(indx < 0)
			return false;
		m_list.remove(indx);

		List<ListChange<T>> el = new ArrayList<ListChange<T>>();
		el.add(new ListChangeDelete<T>(indx, (T) o));
		fireEvent(new ListChangeEvent<T>(this, el));

		return true;
	}

	@Override
	public boolean containsAll(Collection< ? > c) {
		return m_list.containsAll(c);
	}

	@Override
	public boolean addAll(Collection< ? extends T> c) {
		boolean res = m_list.addAll(c);

		List<ListChange<T>> el = new ArrayList<ListChange<T>>();
		int indx = size();
		for(T v : c) {
			el.add(new ListChangeAdd<T>(indx++, v));
		}
		fireEvent(new ListChangeEvent<T>(this, el));

		return res;
	}

	@Override
	public boolean addAll(int index, Collection< ? extends T> c) {
		boolean res = m_list.addAll(index, c);

		List<ListChange<T>> el = new ArrayList<ListChange<T>>();
		int indx = index;
		for(T v : c) {
			el.add(new ListChangeAdd<T>(indx++, v));
		}
		fireEvent(new ListChangeEvent<T>(this, el));

		return res;
	}

	@Override
	public boolean removeAll(Collection< ? > c) {
		List<ListChange<T>> el = new ArrayList<ListChange<T>>();
		boolean done = false;
		for(Object v : c) {
			int indx = indexOf(v);
			if(indx >= 0) {
				m_list.remove(indx);
				el.add(new ListChangeDelete<T>(indx, (T) v));
				done = true;
			}
		}
		fireEvent(new ListChangeEvent<T>(this, el));
		return done;
	}

	@Override
	public boolean retainAll(Collection< ? > c) {
		List<ListChange<T>> el = new ArrayList<ListChange<T>>();
		boolean done = false;
		for(int i = size(); --i >= 0;) {
			T cv = m_list.get(i);
			if(!c.contains(cv)) {
				m_list.remove(i);
				el.add(new ListChangeDelete<T>(i, cv));
				done = true;
			}
		}
		fireEvent(new ListChangeEvent<T>(this, el));
		return done;
	}

	@Override
	public void clear() {
		List<ListChange<T>> el = new ArrayList<ListChange<T>>();
		for(int i = size(); --i >= 0;) {
			T cv = m_list.get(i);
			el.add(new ListChangeDelete<T>(i, cv));
		}
		fireEvent(new ListChangeEvent<T>(this, el));
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

	@Override
	public T get(int index) {
		return m_list.get(index);
	}

	@Override
	public T set(int index, T element) {
		T res = m_list.set(index, element);

		List<ListChange<T>> el = new ArrayList<ListChange<T>>();
		el.add(new ListChangeModify<T>(index, res, element));
		fireEvent(new ListChangeEvent<T>(this, el));

		return res;
	}

	@Override
	public void add(int index, T element) {
		m_list.add(index, element);

		List<ListChange<T>> el = new ArrayList<ListChange<T>>();
		el.add(new ListChangeAdd<T>(index, element));
		fireEvent(new ListChangeEvent<T>(this, el));
	}

	@Override
	public T remove(int index) {
		T res = m_list.remove(index);

		List<ListChange<T>> el = new ArrayList<ListChange<T>>();
		el.add(new ListChangeDelete<T>(index, res));
		fireEvent(new ListChangeEvent<T>(this, el));

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

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		return m_list.subList(fromIndex, toIndex);
	}
}
