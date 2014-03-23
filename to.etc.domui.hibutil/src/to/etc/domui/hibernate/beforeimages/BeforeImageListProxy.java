package to.etc.domui.hibernate.beforeimages;

import java.util.*;

import javax.annotation.*;

class BeforeImageListProxy<T> implements List<T>, IBeforeImageCollectionProxy<List<T>> {
	private List<T> m_list;

	private void present() {
		if(null == m_list)
			throw new QBeforeCollectionNotLoadedException("The before image for this collection is not loaded because the original was not.");
	}

	@Nonnull
	private RuntimeException immutable() {
		throw new IllegalStateException("Attempt to change an immutable collection");
	}

	@Override
	public void initializeFromOriginal(@Nonnull List<T> source) {
		m_list = new ArrayList<T>(source);
	}

	@Override
	public int size() {
		present();
		return m_list.size();
	}

	@Override
	public boolean isEmpty() {
		present();
		return m_list.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		present();
		return m_list.contains(o);
	}

	@Override
	public Iterator<T> iterator() {
		present();
		return m_list.iterator();
	}

	@Override
	public Object[] toArray() {
		present();
		return m_list.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		present();
		return m_list.toArray(a);
	}

	@Override
	public boolean add(T e) {
		throw immutable();
	}

	@Override
	public boolean remove(Object o) {
		throw immutable();
	}

	@Override
	public boolean containsAll(Collection< ? > c) {
		present();
		return m_list.containsAll(c);
	}

	@Override
	public boolean addAll(Collection< ? extends T> c) {
		throw immutable();
	}

	@Override
	public boolean addAll(int index, Collection< ? extends T> c) {
		throw immutable();
	}

	@Override
	public boolean removeAll(Collection< ? > c) {
		throw immutable();
	}

	@Override
	public boolean retainAll(Collection< ? > c) {
		throw immutable();
	}

	@Override
	public void clear() {
		throw immutable();
	}

	@Override
	public boolean equals(Object o) {
		present();
		return m_list.equals(o);
	}

	@Override
	public int hashCode() {
		present();
		return m_list.hashCode();
	}

	@Override
	public T get(int index) {
		present();
		return m_list.get(index);
	}

	@Override
	public T set(int index, T element) {
		throw immutable();
	}

	@Override
	public void add(int index, T element) {
		throw immutable();
	}

	@Override
	public T remove(int index) {
		throw immutable();
	}

	@Override
	public int indexOf(Object o) {
		present();
		return m_list.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		present();
		return m_list.lastIndexOf(o);
	}

	@Override
	public ListIterator<T> listIterator() {
		present();
		return m_list.listIterator();
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		present();
		return m_list.listIterator(index);
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		present();
		return m_list.subList(fromIndex, toIndex);
	}
}
