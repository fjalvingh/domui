package to.etc.util;

import java.util.*;

/**
 *	This hashtable uses ints as the key, preventing the new Integer of a normal
 *  hashtable. This is way faster (and uses less memory).
 *
 * <p>Title: Mumble Global Libraries - Non-database tools</p>
 * <p>Description: Small tools for Java programs</p>
 * <p>Copyright: Copyright (c) 2002 Frits Jalvingh; released under the LGPL licence.</p>
 * <p>Website <a href="http://www.mumble.to/">Mumble</a></p>
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 * @version 1.0
 */
public class IntHashtable extends Dictionary implements Cloneable {
	/** The hash table! */
	HashEntry		m_table[];

	/** The number of entries in the table. */
	private int		m_count;

	/** Treshold: rehash when count is larger than this */
	private int		m_threshold;

	/** The load factor for the hashtable. */
	private float	m_loadfactor;


	/**
	 *	Construct a new hashtable with a specified initial capacity and a spec'd
	 *  load factor.
	 *  @param cap The initial number of enties in the table
	 *  @param loadfactor	Between 0 and 1. Defines the treshold above which the
	 *  					table is resized.
	 */
	public IntHashtable(int cap, float loadfactor) {
		if(cap <= 0 || loadfactor <= 0.0)
			throw new IllegalArgumentException();
		m_loadfactor = loadfactor;
		m_table = new HashEntry[cap];
		m_threshold = (int) (cap * loadfactor);
	}

	/**
	 *	Construct a new hashtable with a specified initial capacity.
	 */
	public IntHashtable(int cap) {
		this(cap, 0.80f);
	}


	/**
	 *	Construct a new hashtable.
	 */
	public IntHashtable() {
		this(101, 0.80f);
	}


	/**
	 *	Returns the number of elements contained in the hashtable.
	 */
	@Override
	public int size() {
		return m_count;
	}

	/**
	 *	True if the hashtable contains no elements.
	 */
	@Override
	public boolean isEmpty() {
		return m_count == 0;
	}

	/**
	 *	Returns an enumeration of the hashtable's keys.
	 */
	@Override
	public Enumeration keys() {
		return new IntHashtableEnumerator(m_table, true);
	}

	/**
	 *	Returns an enumeration of the elements. Use the Enumeration methods
	 *  on the returned object to fetch the elements sequentially.
	 */
	@Override
	public Enumeration elements() {
		return new IntHashtableEnumerator(m_table, false);
	}

	public void addTo(Collection col) {
		for(int i = m_table.length; i-- > 0;) // For each bucket,
		{
			for(HashEntry e = m_table[i]; e != null; e = e.m_next)
				col.add(e.m_value);
		}
	}

	/**
	 *	Returns true if the specified object is an element of the hashtable.
	 *  WARNING: This SEQUENTIALLY evaluates ALL buckets, as the stored object
	 *  is NOT hashed!!!
	 */
	public boolean contains(Object v) {
		if(v == null)
			throw new NullPointerException(); // Cannot store nulls.
		for(int i = m_table.length; i-- > 0;) // For each bucket,
		{
			//-- Now walk each bucket,
			for(HashEntry e = m_table[i]; e != null; e = e.m_next) {
				if(e.m_value.equals(v))
					return true;
			}
		}
		return false;
	}


	/*
	 *	Returns true if the key is in the hashtable.
	 */
	public boolean containsKey(int key) {
		return get(key) != null; // Ah well
	}


	/**
	 *	Finds the object stored for the key passed. Returns null if no object
	 *  was found.
	 */
	public Object get(int key) {
		//-- Walk all elements in the appropriate bucket,
		for(HashEntry e = m_table[(key & 0x7FFFFFFF) % m_table.length]; e != null; e = e.m_next) {
			if(e.m_key == key)
				return e.m_value;
		}
		return null;
	}

	/**
	 *	Finds the object stored for the key passed. Returns null if no object
	 *  was found. Uses an Integer as the key.
	 */
	@Override
	public Object get(Object key) {
		if(!(key instanceof Integer))
			throw new InternalError("Integer expected");
		return get(((Integer) key).intValue());
	}


	/**
	 *	Rehash the table into a bigger one.
	 */
	protected void rehash() {
		int osz = m_table.length;
		int nsz = osz * 2 + 1;
		HashEntry oldar[] = m_table; // Save current content,
		m_table = new HashEntry[nsz]; // Create a new table,
		m_threshold = (int) (nsz * m_loadfactor); // New treshold,

		//-- Now rehash by reassigning all stuff
		for(int i = osz; i-- > 0;) {
			for(HashEntry oe = oldar[i]; oe != null;) {
				HashEntry e = oe; // Save current & move to next,
				oe = oe.m_next;

				//-- Now link this into the new table.
				int index = (e.m_key & 0x7FFFFFFF) % nsz;
				e.m_next = m_table[index];
				m_table[index] = e;
			}
		}
	}

	public static abstract class DeleteHandler {
		public abstract boolean mustDelete(int key, Object val);
	}


	public void checkForDelete(DeleteHandler dh) {
		for(int i = m_table.length; --i >= 0;) // For all buckets
		{
			HashEntry last_he = null;
			for(HashEntry he = m_table[i]; he != null;) {
				boolean del = dh.mustDelete(he.m_key, he.m_value);
				if(del) {
					if(last_he == null)
						m_table[i] = he.m_next;
					else
						last_he.m_next = he.m_next;
				}
				he = he.m_next;
			}
		}
	}


	/**
	 *	Put the specified element in the table, where it can be retrieved using
	 *  get with the specified integer key. If the key already exists the new
	 *  value is stored and it's previous value is returned.
	 */
	public Object put(int key, Object val) {
		if(val == null)
			throw new NullPointerException(); // Cannot store null for some silly reason
		int index = (key & 0x7FFFFFFF) % m_table.length; // Get index for bucket it would be stored in,

		//-- Search this bucket to check for dups.
		for(HashEntry e = m_table[index]; e != null; e = e.m_next) {
			if(e.m_key == key) {
				Object ov = e.m_value;
				e.m_value = val;
				return ov;
			}
		}

		//-- Newone- create and link,
		HashEntry e = new HashEntry();
		e.m_key = key;
		e.m_value = val;
		e.m_next = m_table[index];
		m_table[index] = e;
		m_count++;
		if(m_count >= m_threshold) // Grow and rehash if treshold exceeded,
			rehash();
		return null;
	}


	/**
	 *	Store the object, using an Integer...
	 */
	@Override
	public Object put(Object key, Object val) {
		if(!(key instanceof Integer))
			throw new InternalError("Integer expected");
		return put(((Integer) key).intValue(), val);
	}


	/**
	 * 	Removes the spec'd key. Returns it's assoc value. If the key does not
	 *  exist nothing happens and null is returned.
	 */
	public Object remove(int key) {
		int index = (key & 0x7FFFFFFF) % m_table.length;
		for(HashEntry e = m_table[index], prev = null; e != null; prev = e, e = e.m_next) {
			if(e.m_key == key) // Element found?
			{
				if(prev == null)
					m_table[index] = e.m_next;
				else
					prev.m_next = e.m_next;
				m_count--;
				return e.m_value;
			}
		}
		return null;
	}

	@Override
	public Object remove(Object key) {
		if(!(key instanceof Integer))
			throw new InternalError("Integer expected");
		return remove(((Integer) key).intValue());
	}

	/// Clears the hash table so that it has no more elements in it.
	public void clear() {
		for(int i = m_table.length; --i >= 0;)
			m_table[i] = null;
		m_count = 0;
	}

	/**
	 *	Creates a clone of the hashtable. A shallow copy is made, the contained
	 *  elements are not cloned.
	 */
	@Override
	public Object clone() {
		try {
			IntHashtable t = (IntHashtable) super.clone();
			t.m_table = new HashEntry[m_table.length];
			for(int i = m_table.length; i-- > 0;) {
				//-- Make all new bucket entries.
				HashEntry nes = null;
				for(HashEntry e = m_table[i]; e != null; e = e.m_next) {
					HashEntry ne = new HashEntry();
					ne.m_key = e.m_key;
					ne.m_value = e.m_value;
					ne.m_next = nes;
					nes = ne;
				}
				t.m_table[i] = nes;
			}
			return t;
		} catch(CloneNotSupportedException e) {
			// This shouldn't happen, since we are Cloneable.
			throw new InternalError();
		}
	}

	/// Converts to a rather lengthy String.
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(m_count * 8);
		sb.append("{");
		boolean first = true;
		for(int i = m_table.length; i-- > 0;) // Traverse all buckets,
		{
			for(HashEntry e = m_table[i]; e != null; e = e.m_next) {
				if(first) {
					sb.append(", ");
					first = false;
				}
				sb.append(Integer.toString(e.m_key));
				sb.append("=");
				sb.append(e.m_value.toString());
			}
		}
		sb.append("}");
		return sb.toString();
	}

	public IntKeyIterator getIntKeyIterator() {
		return new IntKeyIterator();
	}

	public class IntKeyIterator {
		//		int			m_index;
		int			m_index	= m_table.length;

		HashEntry	m_entry;

		public boolean hasMoreElements() {
			if(m_entry != null)
				return true;
			while(m_index-- > 0) {
				if((m_entry = m_table[m_index]) != null)
					return true;
			}
			return false;
		}

		public int nextKey() {
			if(!hasMoreElements()) // Move to 1st viable element,
				throw new NoSuchElementException("IntHashtableEnumerator");
			HashEntry e = m_entry;
			m_entry = m_entry.m_next;
			return e.m_key;
		}
	}

}


class HashEntry {
	/// The key value hashed upon.
	int			m_key;

	/// The object associated with key.
	Object		m_value;

	/// The next entry in the same bucket,
	HashEntry	m_next;
}


class IntHashtableEnumerator implements Enumeration {
	boolean		m_keys;

	int			m_index;

	HashEntry	m_table[];

	HashEntry	m_entry;

	IntHashtableEnumerator(HashEntry table[], boolean keys) {
		m_table = table;
		m_keys = keys;
		m_index = table.length;
	}

	public boolean hasMoreElements() {
		if(m_entry != null)
			return true;
		while(m_index-- > 0) {
			if((m_entry = m_table[m_index]) != null)
				return true;
		}
		return false;
	}

	public Object nextElement() {
		if(!hasMoreElements()) // Move to 1st viable element,
			throw new NoSuchElementException("IntHashtableEnumerator");
		HashEntry e = m_entry;
		m_entry = m_entry.m_next;
		return m_keys ? new Integer(e.m_key) : e.m_value;
	}
}
