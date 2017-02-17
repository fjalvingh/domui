package to.etc.util;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 *         Created on 6/29/16.
 */

import javax.annotation.*;

/**
 * <p>A hash map that uses primitive ints for the key rather than objects.</p>
 *
 * <p>Note that this class is for internal optimization purposes only, and may
 * not be supported in future releases of Apache Commons Lang.  Utilities of
 * this sort may be included in future releases of Apache Commons Collections.</p>
 *
 * @author Apache Software Foundation
 * @author Justin Couch
 * @author Alex Chaffee (alex@apache.org)
 * @since 2.0
 * @version $Revision: 905857 $
 * @see java.util.HashMap
 */
public final class IntHashMap<T> {
	/**
	 * The hash table data.
	 */
	private Entry<T> m_table[];

	/**
	 * The total number of entries in the hash table.
	 */
	private int m_count;

	/**
	 * The table is rehashed when its size exceeds this threshold.  (The
	 * value of this field is (int)(capacity * loadFactor).)
	 */
	private int m_threshold;

	/**
	 * The load factor for the hashtable.
	 */
	private final float m_loadFactor;

	final private static class Entry<T> {
		final int m_hash;
		private T m_value;
		private Entry<T> m_next;

		protected Entry(int hash, T value, Entry<T> next) {
			m_hash = hash;
			m_value = value;
			m_next = next;
		}
	}

	/**
	 * <p>Constructs a new, empty hashtable with a default capacity and load
	 * factor, which is <code>20</code> and <code>0.75</code> respectively.</p>
	 */
	public IntHashMap() {
		this(20, 0.75f);
	}

	/**
	 * <p>Constructs a new, empty hashtable with the specified initial capacity
	 * and default load factor, which is <code>0.75</code>.</p>
	 *
	 * @param  initialCapacity the initial capacity of the hashtable.
	 * @throws IllegalArgumentException if the initial capacity is less
	 *   than zero.
	 */
	public IntHashMap(int initialCapacity) {
		this(initialCapacity, 0.75f);
	}

	/**
	 * <p>Constructs a new, empty hashtable with the specified initial
	 * capacity and the specified load factor.</p>
	 *
	 * @param initialCapacity the initial capacity of the hashtable.
	 * @param loadFactor the load factor of the hashtable.
	 * @throws IllegalArgumentException  if the initial capacity is less
	 *             than zero, or if the load factor is nonpositive.
	 */
	public IntHashMap(int initialCapacity, float loadFactor) {
		if(initialCapacity < 0) {
			throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
		}
		if(loadFactor <= 0) {
			throw new IllegalArgumentException("Illegal Load: " + loadFactor);
		}
		if (initialCapacity == 0) {
			initialCapacity = 1;
		}

		m_loadFactor = loadFactor;
		m_table = new Entry[initialCapacity];
		m_threshold = (int) (initialCapacity * loadFactor);
	}

	/**
	 * Returns the number of keys in this hashtable.
	 */
	public int size() {
		return m_count;
	}

	/**
	 * Tests if this hashtable maps no keys to values.
	 */
	public boolean isEmpty() {
		return m_count == 0;
	}

	/**
	 * Tests if some key maps into the specified value in this hashtable.
	 * This operation is more expensive than the <code>containsKey</code>
	 * method.
	 */
	public boolean containsValue(T value) {
		if(value == null) {
			throw new NullPointerException();
		}

		Entry<T> tab[] = m_table;
		for(int i = tab.length; i-- > 0;) {
			for(Entry<T> e = tab[i]; e != null; e = e.m_next) {
				if(e.m_value.equals(value)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Tests if the specified object is a key in this hashtable.
	 */
	public boolean containsKey(int key) {
		Entry<T> tab[] = m_table;
		int hash = key;
		int index = (hash & 0x7FFFFFFF) % tab.length;
		for(Entry<T> e = tab[index]; e != null; e = e.m_next) {
			if(e.m_hash == hash) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the value to which the specified key is mapped in this map.
	 */
	@Nullable
	public T get(int key) {
		Entry<T> tab[] = m_table;
		int hash = key;
		int index = (hash & 0x7FFFFFFF) % tab.length;
		for(Entry<T> e = tab[index]; e != null; e = e.m_next) {
			if(e.m_hash == hash) {
				return e.m_value;
			}
		}
		return null;
	}

	/**
	 * Increases the capacity of and internally reorganizes this
	 * hashtable, in order to accommodate and access its entries more
	 * efficiently.
	 */
	protected void rehash() {
		int oldCapacity = m_table.length;
		Entry<T> oldMap[] = m_table;

		int newCapacity = oldCapacity * 2 + 1;
		Entry<T> newMap[] = new Entry[newCapacity];

		m_threshold = (int) (newCapacity * m_loadFactor);
		m_table = newMap;

		for(int i = oldCapacity; i-- > 0;) {
			for(Entry<T> old = oldMap[i]; old != null;) {
				Entry<T> e = old;
				old = old.m_next;

				int index = (e.m_hash & 0x7FFFFFFF) % newCapacity;
				e.m_next = newMap[index];
				newMap[index] = e;
			}
		}
	}

	/**
	 * Maps the specified <code>key</code> to the specified
	 * <code>value</code> in this hashtable. The key cannot be
	 * <code>null</code>
	 */
	@Nullable
	public T put(int key, T value) {
		// Makes sure the key is not already in the hashtable.
		Entry<T> tab[] = m_table;
		int index = (key & 0x7FFFFFFF) % tab.length;
		for(Entry<T> e = tab[index]; e != null; e = e.m_next) {
			if(e.m_hash == key) {
				T old = e.m_value;
				e.m_value = value;
				return old;
			}
		}

		if(m_count >= m_threshold) {
			// Rehash the table if the threshold is exceeded
			rehash();

			tab = m_table;
			index = (key & 0x7FFFFFFF) % tab.length;
		}

		// Creates the new entry.
		Entry<T> e = new Entry<>(key, value, tab[index]);
		tab[index] = e;
		m_count++;
		return null;
	}

	/**
	 * Removes the key (and its corresponding value) from this
	 * map.
	 */
	@Nullable
	public T remove(int key) {
		Entry<T> tab[] = m_table;
		int hash = key;
		int index = (hash & 0x7FFFFFFF) % tab.length;
		for(Entry<T> e = tab[index], prev = null; e != null; prev = e, e = e.m_next) {
			if(e.m_hash == hash) {
				if(prev != null) {
					prev.m_next = e.m_next;
				} else {
					tab[index] = e.m_next;
				}
				m_count--;
				T oldValue = e.m_value;
				e.m_value = null;
				return oldValue;
			}
		}
		return null;
	}

	/**
	 * Clears this Map so that it contains no keys.
	 */
	public void clear() {
		Entry<T> tab[] = m_table;
		for(int index = tab.length; --index >= 0;) {
			tab[index] = null;
		}
		m_count = 0;
	}
}
