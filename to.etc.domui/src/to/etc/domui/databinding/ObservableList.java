package to.etc.domui.databinding;

import java.util.*;

/**
 * A list that generates change events for it's content, and that does not allow null elements.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Sep 4, 2013
 */
public class ObservableList<T> extends ListenerList<T, ListChangeEvent<T>, IListChangeListener<T>> implements IObservableList<T> {
	private final List<T> m_list = new ArrayList<T>();

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
	public boolean add(T e) {
		return m_list.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return m_list.remove(o);
	}

	@Override
	public boolean containsAll(Collection< ? > c) {
		return m_list.containsAll(c);
	}

	@Override
	public boolean addAll(Collection< ? extends T> c) {
		return m_list.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection< ? extends T> c) {
		return m_list.addAll(index, c);
	}

	@Override
	public boolean removeAll(Collection< ? > c) {
		return m_list.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection< ? > c) {
		return m_list.retainAll(c);
	}

	@Override
	public void clear() {
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
		return m_list.set(index, element);
	}

	@Override
	public void add(int index, T element) {
		m_list.add(index, element);
	}

	@Override
	public T remove(int index) {
		return m_list.remove(index);
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
