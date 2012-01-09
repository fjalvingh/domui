package to.etc.util;

/**
 *	This contains a packed array of boolean values. The array grows dynamically
 *  and each boolean only uses a single bit.
 *
 * <p>Title: Mumble Global Libraries - Non-database tools</p>
 * <p>Description: Small tools for Java programs</p>
 * <p>Copyright: Copyright (c) 2002 Frits Jalvingh; released under the LGPL licence.</p>
 * <p>Website <a href="http://www.mumble.to/">Mumble</a></p>
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 * @version 1.0
 */
public class BoolVector {
	/// The bitmap.
	private byte[]	m_ar;

	/// The current #of items in the bitmap.
	private int		m_count;


	/**
	 *	Create a default vector..
	 */
	public BoolVector() {
		this(256);
	}


	/**
	 *	Create a vector for the specified capacity.
	 */
	public BoolVector(int cap) {
		if(cap <= 0)
			cap = 256;
		m_ar = new byte[(cap + 7) / 8];
		m_count = 0;
	}

	/**
	 *	Set a value in the vector. It returns the previous value.
	 */
	public boolean set(int ix, boolean value) {
		//-- 1. Is the index past the current end?
		if(ix >= m_count)
			m_count = ix + 1; // Increment containment,
		int i = ix / 8; // Get array index
		if(i >= m_ar.length) // Index is past current array size -> may need to grow!
		{
			//-- If the value is set to FALSE we do not need to grow...
			if(!value)
				return false; // Previous was false..

			//-- We need to grow..
			byte[] ar = new byte[i + 128];
			System.arraycopy(m_ar, 0, ar, 0, m_ar.length);
			m_ar = ar;
		}

		//-- Now get & set,
		byte v = m_ar[i]; // Get entry,
		int msk = 1 << (ix & 7); // Get mask,
		boolean res = (v & msk) != 0; // Get previous value
		if(value)
			m_ar[i] |= msk;
		else
			m_ar[i] &= ~msk; // Dec old value.
		return res;
	}


	/**
	 *	Get a value from the vector.
	 */
	public boolean get(int ix) {
		//		if(ix > m_count) return false;		// Past size -> does not contain
		int i = ix / 8;
		if(i >= m_ar.length)
			return false; // Past array size means false bit,

		int msk = 1 << (ix & 7); // Get mask,
		return 0 != (m_ar[i] & msk);
	}

	/**
	 *	Returns the current #of elements.
	 */
	public int size() {
		return m_count;
	}


	/**
	 *	Returns the current capacity
	 */
	public int capacity() {
		return m_ar.length * 8;
	}

	/**
	 * Does a bitwise OR of the contents of THIS vector and the parameter
	 * vector, and store the result in this vector.
	 * @param bv
	 */
	public void or(BoolVector bv) {
		if(bv.m_count > m_count)
			m_count = bv.m_count; // Update count
		byte[] s1 = bv.m_ar; // Get source array
		if(m_ar.length < bv.m_ar.length) // Is my array too small?
		{
			byte[] s2 = m_ar; // get current array
			m_ar = new byte[bv.m_ar.length]; // Make same size as other one
			for(int i = s2.length; --i >= 0;)
				// Walk all elements of the old array
				m_ar[i] = (byte) (s1[i] | s2[i]); // Or the shit
			System.arraycopy(s1, s2.length, m_ar, s2.length, m_ar.length - s2.length);
		} else {
			for(int i = s1.length; --i >= 0;)
				// Walk all elements of the source
				m_ar[i] |= s1[i]; // Or the shit
		}
	}
}
