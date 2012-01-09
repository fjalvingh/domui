package to.etc.util;

import java.util.*;

/**
 * Stores integer values that can be retrieved by name. More optimal than using
 * a Hashtable storing Integer() objects.
 *
 * <p>Title: Mumble Global Libraries - Non-database tools</p>
 * <p>Description: Small tools for Java programs</p>
 * <p>Copyright: Copyright (c) 2002 Frits Jalvingh; released under the LGPL licence.</p>
 * <p>Website <a href="http://www.mumble.to/">Mumble</a></p>
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 * @version 1.0
 */
public class IntByNameTable {
	/// The hash table!
	private IntHashEntry	m_table[];

	/// The number of entries in the table.
	private int				m_count;

	/// Treshold: rehash when count is larger than this
	private int				m_threshold;

	/// The load factor for the hashtable.
	private float			m_loadfactor;


	/**
	 *	Construct a new hashtable with a specified initial capacity and a spec'd
	 *  load factor.
	 *  @param cap The initial number of enties in the table
	 *  @param loadfactor	Between 0 and 1. Defines the treshold above which the
	 *  					table is resized.
	 */
	public IntByNameTable(int cap, float loadfactor) {
		if(cap <= 0 || loadfactor <= 0.0)
			throw new IllegalArgumentException();
		m_loadfactor = loadfactor;
		m_table = new IntHashEntry[cap];
		m_threshold = (int) (cap * loadfactor);
	}

	/**
	 *	Construct a new hashtable with a specified initial capacity.
	 */
	public IntByNameTable(int cap) {
		this(cap, 0.80f);
	}


	/**
	 *	Construct a new hashtable.
	 */
	public IntByNameTable() {
		this(101, 0.80f);
	}


	/**
	 *	Returns the number of elements contained in the hashtable.
	 */
	public int size() {
		return m_count;
	}

	/**
	 *	True if the hashtable contains no elements.
	 */
	public boolean isEmpty() {
		return m_count == 0;
	}

	/**
	 *	Returns an enumeration of the hashtable's keys.
	 */
	public Enumeration keys() {
		return new IntStoreEnumerator(m_table, true);
	}

	/**
	 *	Returns an enumeration of the elements. Use the Enumeration methods
	 *  on the returned object to fetch the elements sequentially.
	 */
	public Enumeration elements() {
		return new IntStoreEnumerator(m_table, false);
	}


	/**
	 *	Returns true if the specified object is an element of the hashtable.
	 *  WARNING: This SEQUENTIALLY evaluates ALL buckets, as the stored object
	 *  is NOT hashed!!!
	 */
	public boolean contains(int val) {
		for(int i = m_table.length; i-- > 0;) // For each bucket,
		{
			//-- Now walk each bucket,
			for(IntHashEntry e = m_table[i]; e != null; e = e.m_next) {
				if(e.m_value == val)
					return true;
			}
		}
		return false;
	}


	/*
	 *	Returns true if the key is in the hashtable.
	 */
	public boolean containsKey(Object key) {
		//-- Walk all elements in the appropriate bucket,
		int hash = key.hashCode();
		for(IntHashEntry e = m_table[(hash & 0x7FFFFFFF) % m_table.length]; e != null; e = e.m_next) {
			if(hash == e.m_hash) {
				if(key.equals(e.m_key))
					return true;
			}
		}
		return false;
	}


	public int get(Object key) {
		//-- Walk all elements in the appropriate bucket,
		int hash = key.hashCode();
		for(IntHashEntry e = m_table[(hash & 0x7FFFFFFF) % m_table.length]; e != null; e = e.m_next) {
			if(hash == e.m_hash) {
				if(key.equals(e.m_key))
					return e.m_value;
			}
		}
		return -1;
	}


	/**
	 *	Finds the object stored for the key passed. Returns null if no object
	 *  was found.
	 */
	public Object getAsObject(Object key) {
		//-- Walk all elements in the appropriate bucket,
		int hash = key.hashCode();
		for(IntHashEntry e = m_table[(hash & 0x7FFFFFFF) % m_table.length]; e != null; e = e.m_next) {
			if(hash == e.m_hash) {
				if(key.equals(e.m_key))
					return new Integer(e.m_value);
			}
		}
		return null;
	}

	/**
	 *	Rehash the table into a bigger one.
	 */
	protected void rehash() {
		int osz = m_table.length;
		int nsz = osz * 2 + 1;
		IntHashEntry oldar[] = m_table; // Save current content,
		m_table = new IntHashEntry[nsz]; // Create a new table,
		m_threshold = (int) (nsz * m_loadfactor); // New treshold,

		//-- Now rehash by reassigning all stuff
		for(int i = osz; i-- > 0;) {
			for(IntHashEntry oe = oldar[i]; oe != null;) {
				IntHashEntry e = oe; // Save current & move to next,
				oe = oe.m_next;

				//-- Now link this into the new table.
				int index = (e.m_hash & 0x7FFFFFFF) % nsz;
				e.m_next = m_table[index];
				m_table[index] = e;
			}
		}
	}


	/**
	 *	Put the specified element in the table, where it can be retrieved using
	 *  get with the specified integer key. If the key already exists the new
	 *  value is stored and it's previous value is returned.
	 */
	public int put(Object key, int val) {
		if(key == null)
			throw new NullPointerException(); // Cannot store null for some silly reason
		int hash = key.hashCode();
		int index = (hash & 0x7FFFFFFF) % m_table.length; // Get index for bucket it would be stored in,

		//-- Search this bucket to check for dups.
		for(IntHashEntry e = m_table[index]; e != null; e = e.m_next) {
			if(e.m_hash == hash) {
				if(e.m_key.equals(key)) {
					//-- Duplicate
					int i = e.m_value;
					e.m_value = val;
					return i;
				}
			}
		}

		//-- Newone- create and link,
		IntHashEntry e = new IntHashEntry();
		e.m_key = key;
		e.m_hash = hash;
		e.m_value = val;
		e.m_next = m_table[index];
		m_table[index] = e;
		m_count++;
		if(m_count >= m_threshold) // Grow and rehash if treshold exceeded,
			rehash();
		return -1;
	}


	/**
	 * 	Removes the spec'd key. Returns it's assoc value. If the key does not
	 *  exist nothing happens and null is returned.
	 */
	public int remove(Object key) {
		int hash = key.hashCode();
		int index = (hash & 0x7FFFFFFF) % m_table.length;
		for(IntHashEntry e = m_table[index], prev = null; e != null; prev = e, e = e.m_next) {
			if(e.m_hash == hash) {
				if(e.m_key.equals(key)) {
					if(prev == null)
						m_table[index] = e.m_next;
					else
						prev.m_next = e.m_next;
					m_count--;
					return e.m_value;
				}
			}
		}
		return -1;
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
			IntByNameTable t = (IntByNameTable) super.clone();
			t.m_table = new IntHashEntry[m_table.length];
			for(int i = m_table.length; i-- > 0;) {
				//-- Make all new bucket entries.
				IntHashEntry nes = null;
				for(IntHashEntry e = m_table[i]; e != null; e = e.m_next) {
					IntHashEntry ne = new IntHashEntry();
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
			for(IntHashEntry e = m_table[i]; e != null; e = e.m_next) {
				if(first) {
					sb.append(", ");
					first = false;
				}
				sb.append(e.m_key);
				sb.append("=");
				sb.append(Integer.toString(e.m_value));
			}
		}
		sb.append("}");
		return sb.toString();
	}
}


class IntHashEntry {
	/// The key value hashed upon.
	Object			m_key;

	/// The object associated with key.
	int				m_value;

	/// The stored hash value for the key
	int				m_hash;

	/// The next entry in the same bucket,
	IntHashEntry	m_next;
}


class IntStoreEnumerator implements Enumeration {
	boolean			m_keys;

	int				m_index;

	IntHashEntry	m_table[];

	IntHashEntry	m_entry;

	IntStoreEnumerator(IntHashEntry table[], boolean keys) {
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
		IntHashEntry e = m_entry;
		m_entry = m_entry.m_next;
		return m_keys ? e.m_key : new Integer(e.m_value);
	}
}
