package to.etc.server.misc;

import to.etc.log.*;
import to.etc.server.janitor.*;


/**
 *	This implements a table where items can be stored and retrieved by name. The
 *  table OWNS the inserted items. The items MUST implement iTimedTableEntry.
 *  The TimedTable uses the Janitor to scan the table on a regular basis, and
 *  it will remove items that are defined as "removed" or "too old".
 *  The time interval for removal is not very strict, and should be relatively
 *  large: items are removed by scanning the entire table, and this takes time;
 *  a typical scan interval would be 5 to 10 minutes.
 *  All actions on this table (and it's members) are synchronized except where
 *  noted specifically.
 *  To control timing, this does not USE a hashtable but it IMPLEMENTS one.
 */
public class TimedTable {
	private static final Category	JAN			= LogMaster.getCategory("timedtable", "janitor");

	private static final Category	DEL			= LogMaster.getCategory("timedtable", "del");

	/// The hash table!
	private HashEntry				m_table[];

	/// The number of entries in the table.
	private int						m_count;

	/// Treshold: rehash when count is larger than this
	private int						m_threshold;

	/// The load factor for the hashtable.
	private float					m_loadfactor;

	/// The proposed scan interval, in seconds
	private int						m_scan_interval;

	/// The default expiry time, in seconds.
	private int						m_expiry;

	/// The default expiry period for an entry, in mils. Used if an entry doesn't implement TableEntry.

	/// The current bucket to clean up.
	private int						m_cl_bucket	= 0;


	/**
	 * Construct a new hashtable with a specified initial capacity.
	 *  @param scaniv	The scan interval, in seconds,
	 *	@param timeout	The live time of the object, in seconds.
	 *
	 */
	public TimedTable(int scaniv, int timeout, int cap) throws Exception {
		this(scaniv, timeout, cap, 0.80f);
	}


	/**
	 *	Construct a new hashtable.
	 *  @param scaniv	The scan interval, in seconds,
	 *	@param timeout	The live time of the object, in seconds.
	 */
	public TimedTable(int scaniv, int timeout) throws Exception {
		this(scaniv, timeout, 1023, 0.80f);
	}


	/**
	 *	Construct a new TimedTable with a specified initial capacity and a spec'd
	 *  load factor, and a scan interval.
	 *  @param scaniv	The scan interval, in seconds,
	 *  @param cap The initial number of enties in the table
	 *  @param loadfactor	Between 0 and 1. Defines the treshold above which the
	 *  					table is resized.
	 *	@param timeout	The live time of the object, in seconds.
	 */
	public TimedTable(int scaniv, int timeout, int cap, float loadfactor) throws Exception {
		if(cap <= 0 || loadfactor <= 0.0)
			throw new IllegalArgumentException();
		m_scan_interval = scaniv;
		m_expiry = timeout;
		m_loadfactor = loadfactor;
		m_table = new HashEntry[cap];
		m_threshold = (int) (cap * loadfactor);
		addJanitorTask();
	}


	/**
	 *	Called from the constructor this adds this table's cleanup function to
	 *  the janitor.
	 */
	private void addJanitorTask() throws Exception {
		Janitor j = Janitor.getJanitor(); // Get the janitor,
		j.addTask(m_scan_interval, false, "TimedTable", new JanitorTask() {
			@Override
			public void run() throws Exception {
				runJanitor();
			}
		});
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Basic Hashtable-derived stuff...					*/
	/*--------------------------------------------------------------*/
	/**
	 *	Returns the number of elements contained in the hashtable.
	 */
	public final synchronized int size() {
		return m_count;
	}

	/**
	 *	True if the table contains no elements.
	 */
	public final synchronized boolean isEmpty() {
		return m_count == 0;
	}

	/**
	 *	Returns true if the specified object is an element of the hashtable.
	 *  WARNING: This SEQUENTIALLY evaluates ALL buckets, as the stored object
	 *  is NOT hashed!!!
	 */
	public final boolean contains(Object v) {
		if(v == null)
			throw new NullPointerException(); // Cannot store nulls.

		synchronized(this) {
			for(int i = m_table.length; i-- > 0;) // For each bucket,
			{
				//-- Now walk each bucket,
				for(HashEntry e = m_table[i]; e != null; e = e.m_next) {
					if(e.m_value.equals(v))
						return true;
				}
			}
		}
		return false;
	}


	/*
	 *	Returns true if the key is in the hashtable. It does NOT update the
	 *	timestamp!
	 */
	public final synchronized boolean containsKey(Object key) {
		return getHashEntry(key) != null;
	}


	/**
	 *	Rehash the table into a bigger one. Expensive!
	 */
	private synchronized void rehash() {
		int osz = m_table.length;
		int nsz = osz * 2 + 1;
		HashEntry oldar[] = m_table; // Save current content,
		m_table = new HashEntry[nsz]; // Create a new table,
		if(m_cl_bucket >= nsz)
			m_cl_bucket = 0; // Set cleaner start pos,
		m_threshold = (int) (nsz * m_loadfactor); // New treshold,

		//-- Now rehash by reassigning all stuff
		for(int i = osz; i-- > 0;) {
			for(HashEntry oe = oldar[i]; oe != null;) {
				HashEntry e = oe; // Save current & move to next,
				oe = oe.m_next;

				//-- Now link this into the new table.
				int index = (e.m_hash & 0x7FFFFFFF) % nsz;
				e.m_next = m_table[index];
				m_table[index] = e;
			}
		}
	}


	/**
	 *	Clears the hash table so that it has no more elements in it, and does it
	 *  as quickly as possible, without calling the wasRemoved() entry of the
	 *  contained items.
	 */
	public synchronized void _clear() {
		for(int i = m_table.length; --i >= 0;)
			m_table[i] = null;
		m_count = 0;
	}

	/**
	 *	Clears the hash table so that it has no more elements in it. Each object
	 *  that implements TableEntry will have it's wasRemoved() entry called. If
	 *  that is not wanted use _clear() instead.
	 */
	public synchronized void clear() {
		for(int i = m_table.length; --i >= 0;) {
			for(HashEntry e = m_table[i]; e != null; e = e.m_next) {
				removeElement(e.m_value);
			}
			m_table[i] = null;
		}
		m_count = 0;
	}


	/**
	 *	Converts to a rather lengthy String ;-)
	 */
	@Override
	public synchronized String toString() {
		//		int max = size() - 1;
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
				sb.append(e.m_key.toString());
				sb.append("=");
				sb.append(e.m_value.toString());
			}
		}
		sb.append("}");
		return sb.toString();
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Adding and obtaining elements.						*/
	/*--------------------------------------------------------------*/
	/**
	 *	Add the specified element in the table, where it can be retrieved using
	 *  find with the specified key. If the key already exists it's new
	 *  value is stored and it's previous value is returned.
	 *  @param	key	The object used as a key value
	 *  @param	val	The value to be stored
	 *  @param	expiry	The #of <b>seconds</b> that the item must reside in the table
	 *  				after it's last accessed.
	 */
	private Object add(Object key, Object val, int expiry, boolean replace) {
		if(val == null)
			throw new NullPointerException(); // Cannot store null for some silly reason
		int hash = key.hashCode();
		int index = (hash & 0x7FFFFFFF) % m_table.length; // Get index for bucket it would be stored in,
		long ct = System.currentTimeMillis();

		//-- Search this bucket to check for existing entry;
		Object ov = null;
		synchronized(this) {
			for(HashEntry e = m_table[index]; e != null; e = e.m_next) {
				if(e.m_hash == hash && e.m_key.equals(key)) // Is same key?
				{
					ov = e.m_value; // Get previous item
					e.m_ts_used = ct; // Set new time accessed
					e.m_lifetime = 1000l * expiry; // Set new expiry
					if(!replace)
						return e.m_value; // Return existing node (and keep it) if not replacing.
					e.m_value = val; // Set new'un.
					break; // Done; call wasRemoved outside monitor.
				}
			}

			if(ov == null) // No older one found?
			{
				HashEntry e = new HashEntry(); // Then create a new one.
				e.m_lifetime = expiry * 1000l;
				e.m_key = key;
				e.m_hash = hash;
				e.m_value = val;
				e.m_next = m_table[index];
				e.m_ts_used = ct;
				m_table[index] = e;
				m_count++;
				if(m_count >= m_threshold) // Grow and rehash if treshold exceeded,
					rehash();
				return replace ? null : val;
			}
		}

		//-- An older one was found. If it implements ... call it's wasRemoved method
		if(val != ov && replace)
			removeElement(ov);
		return ov;
	}

	/**
	 *	Add the specified element in the table, where it can be retrieved using
	 *  find with the specified key. If the key already exists it's new
	 *  value is stored and it's previous value is returned.
	 *  @param	key	The object used as a key value
	 *  @param	val	The value to be stored
	 *  @param	expiry	The #of <b>seconds</b> that the item must reside in the table
	 *  				after it's last accessed.
	 */
	public Object add(Object key, Object val, int expiry) {
		return add(key, val, expiry, true);
	}

	/**
	 *	This puts a data value into the table but ONLY if no object
	 *  with that key exists(!). If an object with the same key exists no
	 *  object is inserted and the existing object is returned.
	 *  If the object doesn't exist the new object is inserted and returned.
	 *  This allows for add-then-initialize semantics.
	 *  @returns	the new object OR the existing object.
	 */
	public Object addIf(Object key, Object val, int expiry) {
		return add(key, val, expiry, false);
	}

	/**
	 *	Add the specified element in the table, where it can be retrieved using
	 *  find with the specified key. If the key already exists it's new
	 *  value is stored and it's previous value is returned. This version uses
	 *  the table's default timeout period.
	 */
	public Object add(Object key, Object val) {
		return add(key, val, m_expiry);
	}

	/**
	 *	Returns the internal HashEntry for a given key.
	 */
	private HashEntry getHashEntry(Object key) {
		int hash = key.hashCode();

		//-- Walk all elements in the appropriate bucket,
		for(HashEntry e = m_table[(hash & 0x7FFFFFFF) % m_table.length]; e != null; e = e.m_next) {
			if(e.m_hash == hash && e.m_key.equals(key))
				return e;
		}
		return null;
	}


	/**
	 *	Finds the object stored for the key passed. Returns null if no object
	 *  was found. Updates the "last time used" thing.
	 */
	public Object find(Object key) {
		long ct = System.currentTimeMillis();

		//-- Walk all elements in the appropriate bucket,
		synchronized(this) {
			HashEntry he = getHashEntry(key); // Get HashEntry,
			if(he == null)
				return null; // Exit if not found;
			he.m_ts_used = ct;
			return he.m_value;
		}
	}


	public Object findNoTouch(Object key) {
		synchronized(this) {
			HashEntry he = getHashEntry(key); // Get HashEntry,
			if(he == null)
				return null; // Exit if not found;
			return he.m_value;
		}
	}


	/**
	 * 	Removes the spec'd key. Returns it's assoc value. If the key does not
	 *  exist nothing happens and null is returned. This does NOT call the
	 *  wasRemoved() method of the object removed!
	 */
	public Object remove(Object key) {
		int hash = key.hashCode();
		int index = (hash & 0x7FFFFFFF) % m_table.length;

		synchronized(this) {
			for(HashEntry e = m_table[index], prev = null; e != null; prev = e, e = e.m_next) {
				if(e.m_hash == hash && e.m_key.equals(key)) {
					if(prev == null)
						m_table[index] = e.m_next;
					else
						prev.m_next = e.m_next;
					m_count--;
					return e.m_value;
				}
			}
		}
		return null;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	The janitor code, cleanup of stale items..			*/
	/*--------------------------------------------------------------*/
	/// The total amount of time the tables were locked (one run) by last call to cleanupPart
	private long	m_t_locked;

	/// The total #of entries removed..
	private int		m_n_removed;


	/**
	 *	Should return T if the entry under consideration has expired.
	 */
	protected boolean isExpired(HashEntry he, long currtime) {
		if(he.m_value instanceof TableEntry) {
			currtime -= ((TableEntry) he.m_value).getExpiry() * 1000;
		} else
			currtime -= he.m_lifetime;

		//		if(DEL.isOn())
		//		{
		//			DEL.msg("expiry check: ts used="+he.m_ts_used+", fencetime is "+currtime);
		//		}

		return he.m_ts_used < currtime;
	}


	/**
	 *	Called when an element is indeed removed. It calls the wasRemoved()
	 *  function if the entry implements the TableEntry interface. When called
	 *  the element is already removed from the table.
	 */
	protected void removeElement(Object o) {
		if(o instanceof TableEntry) // Knows something?
		{
			try {
				((TableEntry) o).wasRemoved();
			} catch(Exception x) {}
		}
	}


	/**
	 *	This function traverses part of the hashtable to find items that have
	 *  to be removed. It traverses only a part of the hashtable at a time to
	 *  keep the lock time for the table minimal. Each call will continue where
	 *  it left off the last time it proceeded.
	 *	We walk entire buckets at a time; the first bucket to consider will
	 *  be saved in m_cl_bucket. We start a new bucket if the total #of elements
	 *  considered is below 1000 and the total #of elements to remove is below
	 *  100.
	 *  The routine returns TRUE if the end of the table has been passed..
	 */
	private boolean cleanupPart(long curt) {
		final int MAXDEL = 100;
		final int MAXLOOP = 1000;

		HashEntry[] ar = new HashEntry[MAXDEL]; // Entries to delete,
		HashEntry he, prev_he;
		int ix, ct;
		ct = MAXLOOP;
		ix = 0;
		boolean wrapped = false;

		//-- Ok. Enter synchronized section to find-and-remove items from the table
		synchronized(this) {
			//-- Prepare walking the current bucket,
			while(ct > 0 && ix < MAXDEL) {
				//-- Do the current bucket.
				ct--; // One entry done (bucket)
				prev_he = null; // No previous
				for(he = m_table[m_cl_bucket]; he != null;) {
					ct--;
					if(!isExpired(he, curt)) // Not expired?
					{
						prev_he = he; // Set previous
						he = he.m_next; // Then move to next'un
					} else {
						//-- This one has expired. Remove & add to kill table,
						ar[ix++] = he; // Add to remove table,
						if(prev_he == null)
							m_table[m_cl_bucket] = he.m_next;
						else
							prev_he.m_next = he.m_next;
						if(ix >= MAXDEL)
							break; // Exit if maxdel items found.
						he = he.m_next; // Move to next,
					}
				}

				//-- We've either reached end-of-bucket OR the delete table is full.
				if(he == null) // End of bucket was found -> next bucket..
				{
					m_cl_bucket++;
					if(m_cl_bucket >= m_table.length) {
						m_cl_bucket = 0; // Restart at zero,
						if(wrapped)
							break; // Already been there?
						wrapped = true;
					}
				}
			}
		}
		m_t_locked = System.currentTimeMillis() - curt;
		m_n_removed += ix;

		/*
		 *	We now have a table of items to remove; they have already been
		 *	removed from the names table, but their wasRemoved() method must
		 *	be called...
		 */
		while(--ix >= 0) {
			he = ar[ix]; // Get the element,
			ar[ix] = null; // Droppit (nonsense)
			removeElement(he.m_value);
			if(DEL.isOn())
				DEL.msg("TimedTable: removing item " + he.m_value);
		}
		return wrapped;
	}


	/**
	 *	This gets called on a regular basis by the janitor. It will traverse the
	 *  table. It will lock the table in short bursts, and it will sleep in
	 *  between...
	 */
	void runJanitor() {
		long curt = System.currentTimeMillis(); // Time we've started..
		long fet = curt + (1000 * m_scan_interval / 2);// Absolute end time,
		long totused = 0;
		long totlocked = 0;

		m_n_removed = 0;
		for(;;) {
			//-- Do a single run...
			long st = curt; // Set start time,
			boolean complete = cleanupPart(curt); // Collect and go
			curt = System.currentTimeMillis(); // And next time,
			st = curt - st; // Elapsed time,
			totused += st;
			totlocked += m_t_locked;

			if(complete)
				break; // Exit when done,

			//-- Should we go again?
			if(st < 1000)
				st = 1000; // Sleep 1 sec at least
			if(curt + 2 * st >= fet) // Including sleep this would exceed end time,
				break;

			//-- Sleep sometime,
			try {
				Thread.sleep(st);
			} catch(Exception x) {}
			curt = System.currentTimeMillis(); // And next time,
		}
		JAN.msg("J: TimedTable total locked=" + totlocked + "ms, total used=" + totused + " ms; " + m_n_removed + " items removed of " + size() + "elements.");
	}


}


/*--------------------------------------------------------------*/
/*	CODING:	The entry in this table...							*/
/*--------------------------------------------------------------*/
class HashEntry {
	/** The hash value, unmod-ded. */
	int			m_hash;

	/** The requested time-out value (time after last access that the thing is invalid) in ms */
	long		m_lifetime;

	/** The last-time-used stamp. */
	long		m_ts_used;

	/** The key value hashed upon. */
	Object		m_key;

	/** The object associated with key. */
	Object		m_value;

	/** The next entry in the same bucket, */
	HashEntry	m_next;
}
