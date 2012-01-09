package to.etc.util;


/**
 * A Vector class holding only int's, not objects. This is more optimal than
 * the basic Vector class from java.util which has to create an Integer
 * wrapper for each int stored.
 *
 * <p>Title: Mumble Global Libraries - Non-database tools</p>
 * <p>Description: Small tools for Java programs</p>
 * <p>Copyright: Copyright (c) 2002 Frits Jalvingh; released under the LGPL licence.</p>
 * <p>Website <a href="http://www.mumble.to/">Mumble</a></p>
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 * @version 1.0
 */
public class IntVector {
	/// The array of ints stored.
	protected int[]	m_int_ar;

	/// The current size of the thing (#items stored).
	protected int	m_sz;

	/// The default value for "unset" entries.
	protected int	m_defval;

	public IntVector() {
		m_sz = 0;
		m_int_ar = new int[32];
	}

	/**
	 *	Create a vector with capacity for the specified number of integers.
	 */
	public IntVector(int capacity) {
		m_sz = 0;
		m_int_ar = new int[capacity];
	}


	/**
	 *	Return the current size of the int array.
	 */
	public int size() {
		return m_sz;
	}


	/**
	 *	Returns the current capacity.
	 */
	public int capacity() {
		return m_int_ar.length;
	}


	/**
	 *	Returns the element at the position specified.
	 *  @exception java.lang.ArrayIndexOutOfBoundsException if the exception is
	 *  out of range.
	 */
	public int elementAt(int ix) {
		if(ix < 0 || ix > m_sz)
			throw new ArrayIndexOutOfBoundsException(ix);
		return m_int_ar[ix];
	}


	/**
	 *	Sets a value at the specified index. Returns the previous value. If the
	 *  current array size is smaller then the array grows to hold the element
	 *  specified. All elements between the old size and the new item will be
	 *  set to the "default value".
	 */
	public int set(int index, int value) {
		if(index >= m_sz) {
			if(index >= m_int_ar.length)
				_grow(index + 1);

			//-- Set all items in between to the def value
			for(int i = m_sz; i < index; i++)
				m_int_ar[i] = m_defval;
			m_sz = index + 1;
		}

		int ov = m_int_ar[index];
		m_int_ar[index] = value;
		return ov;
	}


	/**
	 *	Grows the array IF it is unable to hold the specified index.
	 */
	protected void _grow(int index) {
		int csz = m_int_ar.length;
		if(index < csz)
			return;

		if(csz > 16384)
			csz = 8192;
		else
			csz = m_int_ar.length / 2;

		int nsz = index + csz;
		int[] ar = new int[nsz];

		System.arraycopy(m_int_ar, 0, ar, 0, m_int_ar.length);
		m_int_ar = ar;
	}


	/**
	 *	Adds an integer to the array.
	 */
	public void add(int value) {
		set(m_sz, value);
	}

	/**
	 *	Resets to hold no values.
	 */
	public void clear() {
		m_sz = 0;
	}

	/**
	 *	Returns an int[] holding the values.
	 */
	public int[] toArray() {
		int[] ar = new int[size()];

		System.arraycopy(m_int_ar, 0, ar, 0, size());
		return ar;
	}

}
