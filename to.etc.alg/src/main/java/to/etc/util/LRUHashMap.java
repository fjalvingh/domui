/*
 * DomUI Java User Interface - shared code
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.util;

import java.util.*;

/**
 * Hashmap which can use LRU removal of items.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 25, 2010
 */
@SuppressWarnings("unchecked")
public class LRUHashMap<K, V> implements Map<K, V> {
	static final Object			NULL	= new Object();

	static private final float	LOAD	= 0.75f;

	static private class Entry<K, V> implements Map.Entry<K, V> {
		final Object	m_key;

		V				m_value;

		final int		m_hashCode;

		Entry<K, V>		m_bucketNext, m_lruNext, m_lruPrev;

		Entry(int hashCode, Object k, V v) {
			m_value = v;
			m_key = k;
			m_hashCode = hashCode;
		}

		public K getKey() {
			return (K) decodeKey(m_key);
		}

		public V getValue() {
			return m_value;
		}

		public V setValue(V value) {
			V old = m_value;
			m_value = value;
			return old;
		}

		@Override
		public boolean equals(Object o) {
			if(!(o instanceof Map.Entry< ? , ? >))
				return false;
			Map.Entry<K, V> e = (Map.Entry<K, V>) o;
			Object k1 = getKey();
			Object k2 = e.getKey();
			if(k1 == k2 || (k1 != null && k1.equals(k2))) {
				Object v1 = getValue();
				Object v2 = e.getValue();
				return v1 == v2 || (v1 != null && v1.equals(v2));
			}
			return false;
		}

		@Override
		public int hashCode() {
			return (m_key == NULL ? 0 : m_key.hashCode()) ^ (m_value == null ? 0 : m_value.hashCode());
		}

		@Override
		public String toString() {
			return getKey() + "=" + getValue();
		}
	}

	/** The bucket table. */
	transient Entry<K, V>[]					m_buckets;

	/** Head of the LRU chain for this map. */
	private Entry<K, V>						m_lruFirst, m_lruLast;

	/** The current #of entries in the cache. */
	transient int							m_currentSize;

	/** The max. #of entries in the cache. */
	private transient int					m_maxSize;

	/**
	 * The next size value at which to resize (capacity * load factor).
	 * @serial
	 */
	private int								m_threshold;

	/**
	 * The number of times this HashMap has been modified
	 */
	transient volatile int					m_updateCounter;

	private transient Set<Map.Entry<K, V>>	m_entrySet	= null;

	private transient Set<K>				m_keySet	= null;

	private transient Collection<V>			m_values	= null;

	public LRUHashMap(int maxsize) {
		this(maxsize, 16);
	}

	public LRUHashMap(int maxsize, int initial) {
		m_maxSize = maxsize;
		int capacity = 1;
		while(capacity <= initial)
			capacity <<= 1;
		capacity--;
		m_threshold = (int) (capacity * LOAD);
		m_buckets = new Entry[capacity];
	}

	static private <K> Object encodeKey(K key) {
		return key == null ? (K) NULL : key;
	}

	/**
	 * Returns key represented by specified internal representation.
	 */
	static <K> K decodeKey(Object key) {
		return (key == NULL ? null : (K) key);
	}

	/**
	 * Hash function stolen from HashMap impl.
	 * @param x
	 * @return
	 */
	static private int hash(Object x) {
		int h = x.hashCode();
		h += ~(h << 9);
		h ^= (h >>> 14);
		h += (h << 4);
		h ^= (h >>> 10);
		return (h & 0x7fffffff); // Make sure hash is +ve
	}

	/**
	 * Returns the current #elements in the map.
	 */
	public int size() {
		return m_currentSize;
	}

	public int getMaxSize() {
		return m_maxSize;
	}

	public boolean isEmpty() {
		return m_currentSize == 0;
	}

	/**
	 * Clear the map.
	 */
	public void clear() {
		m_updateCounter++;
		Entry<K, V>[] tab = m_buckets;
		for(int i = 0; i < tab.length; i++)
			tab[i] = null;
		m_currentSize = 0;
	}

	/**
	 * Links the entry at the most recently used position of the LRU chain.
	 * @param e
	 */
	private void link(Entry<K, V> e) {
		unlink(e); // Make sure we're unlinked
		if(m_lruFirst == null) // Empty initial list?
		{
			m_lruFirst = e;
			m_lruLast = e;
			e.m_lruNext = e;
			e.m_lruPrev = e;
			return;
		}
		e.m_lruPrev = m_lruFirst; // Previous first is my previous
		e.m_lruNext = m_lruLast; // After me I wrap back to the end
		m_lruLast.m_lruPrev = e;
		m_lruFirst.m_lruNext = e; // I'm his next
		m_lruFirst = e; // I'm the 1st one now;
	}

	private void unlink(Entry<K, V> e) {
		if(e.m_lruNext == null) // Already unlinked?
			return;
		if(e.m_lruNext == e.m_lruPrev) // I'm the only one?
		{
			m_lruFirst = null;
			m_lruLast = null;
			e.m_lruNext = null;
			e.m_lruPrev = null;
			return;
		}

		if(m_lruFirst == e)
			m_lruFirst = e.m_lruPrev;
		if(m_lruLast == e)
			m_lruLast = e.m_lruNext;

		e.m_lruPrev.m_lruNext = e.m_lruNext;
		e.m_lruNext.m_lruPrev = e.m_lruPrev;
		e.m_lruNext = e.m_lruPrev = null;
	}

	/**
	 * Returns the entry associated with a key, or null if not found.
	 */
	Entry<K, V> getEntry(Object key) {
		Object k = encodeKey(key);
		int hash = hash(k);
		Entry<K, V> e = m_buckets[hash % m_buckets.length];
		while(e != null && !(e.m_hashCode == hash && (k == e.m_key || k.equals(e.m_key))))
			e = e.m_bucketNext;
		return e;
	}

	/**
	 * Retrieve a value by key. If a value is found it gets moved to the
	 * most recently used position.
	 */
	public V get(Object key) {
		Entry<K, V> e = getEntry(key);
		if(e == null)
			return null;

		//-- Link as mru
		unlink(e);
		link(e);
		return e.getValue();
	}

	/**
	 * Returns T if this map contains the keyed object. If the element
	 * is found it is <b>not</b> touched as used.
	 */
	public boolean containsKey(Object key) {
		return getEntry(key) != null;
	}

	/**
	 * Associates the specified value with the specified key in this map.
	 * If the map previously contained a mapping for this key, the old
	 * value is replaced.
	 *
	 * @param key key with which the specified value is to be associated.
	 * @param value value to be associated with the specified key.
	 * @return previous value associated with specified key, or <tt>null</tt>
	 *	       if there was no mapping for key.  A <tt>null</tt> return can
	 *	       also indicate that the HashMap previously associated
	 *	       <tt>null</tt> with the specified key.
	 */
	public V put(K key, V value) {
		Object k = encodeKey(key);
		int hash = hash(k);
		int index = hash % m_buckets.length;
		for(Entry<K, V> e = m_buckets[index]; e != null; e = e.m_bucketNext) {
			if(e.m_hashCode == hash && (k == e.m_key || k.equals(e.m_key))) {
				V old = e.m_value;
				e.m_value = value;
				link(e);
				return old;
			}
		}

		m_updateCounter++;
		m_currentSize++;
		if(m_currentSize >= m_threshold) {
			resize(2 * m_buckets.length - 1);
			index = hash % m_buckets.length;
		}
		Entry<K, V> e = new Entry<K, V>(hash, k, value);
		e.m_bucketNext = m_buckets[index];
		m_buckets[index] = e;
		link(e);

		//-- If the map has become too big then release the LRU item from it.
		while(m_currentSize > m_maxSize) {
			e = m_lruLast;
			int ocs = m_currentSize;
			removeEntry(e);
			if(m_currentSize == ocs)
				throw new IllegalStateException("Fail to remove entry: " + e);
		}
		return null;
	}

	/**
	 * Rehash the table when it has overflown.
	 */
	private void resize(int newsize) {
		m_threshold = (int) (newsize * LOAD);
		Entry<K, V>[] oldt = m_buckets;
		m_buckets = new Entry[newsize];

		//-- Move all thingies to their new location.
		for(int i = oldt.length; --i >= 0;) // All buckets in the old geezer
		{
			for(Entry<K, V> curr = oldt[i]; curr != null;) // Walk the list
			{
				Entry<K, V> e = curr;
				curr = e.m_bucketNext; // Move to next bucket before remapping
				int index = e.m_hashCode % newsize; // Get bucket pos
				e.m_bucketNext = m_buckets[index];
				m_buckets[index] = e;
			}
		}
	}

	/**
	 * Adds all of the elements to this map.
	 */
	public void putAll(Map< ? extends K, ? extends V> m) {
		int numKeysToBeAdded = m.size();
		if(numKeysToBeAdded == 0)
			return;

		int newtotal = numKeysToBeAdded + m_currentSize; // Optimistic,
		if(newtotal > m_maxSize)
			newtotal = m_maxSize;
		if(newtotal > m_threshold) // Would the max overflow?
		{
			int newsize = m_buckets.length; // Take current size,
			while(newsize <= newtotal)
				// Grow to nearest ^2
				newsize <<= 1;
			if(newsize > m_buckets.length)
				resize(newsize);
		}

		//-- Add all of the entries
		for(Iterator< ? extends Map.Entry< ? extends K, ? extends V>> i = m.entrySet().iterator(); i.hasNext();) {
			Map.Entry< ? extends K, ? extends V> e = i.next();
			put(e.getKey(), e.getValue());
		}
	}

	/**
	 * Remove an entry by key.
	 * @see java.util.Map#remove(java.lang.Object)
	 */
	public V remove(Object key) {
		Entry<K, V> e = _remove(key);
		return (e == null ? null : e.m_value);
	}

	Entry<K, V> _remove(Object key) {
		Object k = encodeKey(key);
		int hc = hash(k);
		int index = hc % m_buckets.length;
		Entry<K, V> prev = null;

		for(Entry<K, V> e = m_buckets[index]; e != null; e = e.m_bucketNext) {
			if(e.m_hashCode == hc && (e.m_key == k || k.equals(e.m_key))) {
				//-- Gotcha
				if(prev == null)
					m_buckets[index] = e.m_bucketNext; // Unlink
				else
					prev.m_bucketNext = e.m_bucketNext;
				unlink(e); // Remove from LRU
				m_currentSize--;
				m_updateCounter++;
				return e;
			}
			prev = e;
		}
		return null;
	}

	Entry<K, V> removeEntry(Object o) {
		if(!(o instanceof Map.Entry< ? , ? >))
			return null;
		return _remove(((Map.Entry<K, V>) o).getKey());
	}

	/**
	 * Returns T if this map contains the value. This is EXPENSIVE as it needs
	 * to walk all of the objects in the map.
	 *
	 * @see java.util.Map#containsValue(java.lang.Object)
	 */
	public boolean containsValue(Object value) {
		if(value == null) {
			//-- Null has a faster comparison so handle it separately
			for(int i = m_buckets.length; --i >= 0;) {
				for(Entry<K, V> e = m_buckets[i]; e != null; e = e.m_bucketNext) {
					if(e.m_value == null)
						return true;
				}
			}
			return false;
		}

		//-- The normal, non-null case.
		for(int i = m_buckets.length; --i >= 0;) {
			for(Entry<K, V> e = m_buckets[i]; e != null; e = e.m_bucketNext) {
				if(value == e.m_value || value.equals(e.m_value))
					return true;
			}
		}
		return false;
	}

	/**
	 * FIXME Needs an implementation
	 */
	@Override
	public Object clone() {
		throw new UnsupportedOperationException();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Iterators.											*/
	/*--------------------------------------------------------------*/

	/**
	 * This abstract iterator walks the hash chain.
	 */
	private abstract class HashIterator<E> implements Iterator<E> {
		/** The current bucket index we're traversing. */
		int			m_bucketIndex;

		/** Next entry to return */
		Entry<K, V>	m_next;

		/** The update count at the time of construction of this iterator. */
		int			m_currentUpdateCount;

		Entry<K, V>	m_current;

		HashIterator() {
			m_currentUpdateCount = m_updateCounter;
			Entry<K, V> e = null;
			if(m_currentSize > 0) {
				for(int bucket = m_buckets.length; --bucket >= 0;) {
					e = m_buckets[bucket];
					if(e != null) {
						m_bucketIndex = bucket;
						m_next = e;
						break;
					}
				}
			}
		}

		public boolean hasNext() {
			return m_next != null;
		}

		Entry<K, V> nextEntry() {
			if(m_updateCounter != m_currentUpdateCount)
				throw new ConcurrentModificationException();
			if(m_next == null)
				throw new NoSuchElementException();
			m_current = m_next;
			m_next = m_next.m_bucketNext;
			if(m_next == null) // Chain expired?
			{
				while(--m_bucketIndex >= 0) {
					m_next = m_buckets[m_bucketIndex];
					if(m_next != null)
						break;
				}
			}
			return m_current;
		}

		public void remove() {
			if(m_current == null)
				throw new IllegalStateException();
			if(m_updateCounter != m_currentUpdateCount)
				throw new ConcurrentModificationException();
			Object k = m_current.m_key;
			m_current = null;
			_remove(k);
			m_currentUpdateCount = m_updateCounter;
		}
	}

	private class ValueIterator extends HashIterator<V> {
		public ValueIterator() {
		}

		public V next() {
			return nextEntry().m_value;
		}
	}

	private class KeyIterator extends HashIterator<K> {
		public KeyIterator() {
		}

		public K next() {
			return nextEntry().getKey();
		}
	}

	private class EntryIterator extends HashIterator<Map.Entry<K, V>> {
		public EntryIterator() {
		}

		public Map.Entry<K, V> next() {
			return nextEntry();
		}
	}

	Iterator<K> newKeyIterator() {
		return new KeyIterator();
	}

	Iterator<V> newValueIterator() {
		return new ValueIterator();
	}

	Iterator<Map.Entry<K, V>> newEntryIterator() {
		return new EntryIterator();
	}

	private class KeySet extends AbstractSet<K> {
		public KeySet() {
		}

		@Override
		public Iterator<K> iterator() {
			return newKeyIterator();
		}

		@Override
		public int size() {
			return m_currentSize;
		}

		@Override
		public boolean contains(Object o) {
			return containsKey(o);
		}

		@Override
		public boolean remove(Object o) {
			return _remove(o) != null;
		}

		@Override
		public void clear() {
			LRUHashMap.this.clear();
		}
	}

	public Set<K> keySet() {
		if(m_keySet == null)
			m_keySet = new KeySet();
		return m_keySet;
	}

	public Collection<V> values() {
		if(m_values == null)
			m_values = new ValuesCollection();
		return m_values;
	}

	private class ValuesCollection extends AbstractCollection<V> {
		public ValuesCollection() {
		}

		@Override
		public Iterator<V> iterator() {
			return newValueIterator();
		}

		@Override
		public int size() {
			return m_currentSize;
		}

		@Override
		public boolean contains(Object o) {
			return containsValue(o);
		}

		@Override
		public void clear() {
			LRUHashMap.this.clear();
		}
	}

	/**
	 * Returns a collection view of the mappings contained in this map.  Each
	 * element in the returned collection is a <tt>Map.Entry</tt>.  The
	 * collection is backed by the map, so changes to the map are reflected in
	 * the collection, and vice-versa.  The collection supports element
	 * removal, which removes the corresponding mapping from the map, via the
	 * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>,
	 * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt> operations.
	 * It does not support the <tt>add</tt> or <tt>addAll</tt> operations.
	 *
	 * @return a collection view of the mappings contained in this map.
	 * @see Map.Entry
	 */
	public Set<Map.Entry<K, V>> entrySet() {
		if(m_entrySet == null)
			m_entrySet = new EntrySet();
		return m_entrySet;
	}


	@SuppressWarnings({"unchecked", "rawtypes"})
	private class EntrySet extends AbstractSet/*<Map.Entry<K,V>>*/
	{
		public EntrySet() {
		}

		@Override
		public Iterator/*<Map.Entry<K,V>>*/iterator() {
			return newEntryIterator();
		}

		@Override
		public boolean contains(Object o) {
			if(!(o instanceof Map.Entry))
				return false;
			Map.Entry<K, V> e = (Map.Entry<K, V>) o;
			Entry<K, V> candidate = getEntry(e.getKey());
			return candidate != null && candidate.equals(e);
		}

		@Override
		public boolean remove(Object o) {
			return removeEntry(o) != null;
		}

		@Override
		public int size() {
			return m_currentSize;
		}

		@Override
		public void clear() {
			LRUHashMap.this.clear();
		}
	}


	public static void main(String[] args) {
		LRUHashMap<Integer, Integer> map = new LRUHashMap<Integer, Integer>(1000);
		try {
			for(int i = 0; i < 100000; i++) {
				Integer iv = Integer.valueOf(i);

				map.put(iv, iv);
			}
		} catch(Exception x) {
			x.printStackTrace();
		}
	}

}
