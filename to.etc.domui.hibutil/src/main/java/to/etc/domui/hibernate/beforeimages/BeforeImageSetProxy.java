package to.etc.domui.hibernate.beforeimages;

import java.util.*;

import javax.annotation.*;

class BeforeImageSetProxy<T> implements Set<T>, IBeforeImageCollectionProxy<Set<T>> {
	private Set<T> m_set;

	private void present() {
		if(null == m_set)
			throw new QBeforeCollectionNotLoadedException("The before image for this collection is not loaded because the original was not.");
	}

	@Nonnull
	private RuntimeException immutable() {
		throw new IllegalStateException("Attempt to change an immutable collection");
	}

	@Override
	public void initializeFromOriginal(@Nonnull Set<T> source) {
		m_set = new HashSet<T>(source);
	}

	@Override
	public int size() {
		present();
		return m_set.size();
	}

	@Override
	public boolean isEmpty() {
		present();
		return m_set.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		present();
		return m_set.contains(o);
	}

	@Override
	public Iterator<T> iterator() {
		present();
		return m_set.iterator();
	}

	@Override
	public Object[] toArray() {
		present();
		return m_set.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		present();
		return m_set.toArray(a);
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
		return m_set.containsAll(c);
	}

	@Override
	public boolean addAll(Collection< ? extends T> c) {
		throw immutable();
	}

	@Override
	public boolean retainAll(Collection< ? > c) {
		throw immutable();
	}

	@Override
	public boolean removeAll(Collection< ? > c) {
		throw immutable();
	}

	@Override
	public void clear() {
		throw immutable();
	}

	@Override
	public boolean equals(Object o) {
		present();
		return m_set.equals(o);
	}

	@Override
	public int hashCode() {
		present();
		return m_set.hashCode();
	}

}
