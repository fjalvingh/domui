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

import java.sql.*;
import java.util.*;

public final class ByteArrayUtil {
	private ByteArrayUtil() {
	}

	/**
	 * Put a long value "val" in a byte array "ba" starting at the offset "offset"
	 *
	 * @param ba
	 * @param offset
	 * @param val
	 */
	public static void setLong(byte[] ba, int offset, long val) {
		setInt(ba, offset, (int) val); // 4 bytes less significant
		setInt(ba, offset + 4, (int) (val >> 32)); // 4 bytes most significant
	}

	/**
	 * Return a long value from bytearray "ar" starting at the offset "offset".
	 *
	 * @param ar
	 * @param offset
	 * @param length
	 * @return
	 */
	public static long getLong(byte[] ba, int offset) {
		long a = ByteArrayUtil.getInt(ba, offset + 4); // Get the 4 most significant bytes (int).
		long b = ByteArrayUtil.getInt(ba, offset); // Get the 4 less significant byted (int).
		a = a << 32; // Make them the 4 most significant bytes.
		b = b & 0x00000000ffffffffl; // Make sure the sign is removed (possible sign for the 4 most significant bytes).
		b |= a; // Merge the two values

		return b; // Return the original long.
	}

	/**
	 * Put a Timestamp value "val" in a byte array "ba" starting at the offset "offset"
	 *
	 * @param ba
	 * @param offset
	 * @param val
	 */
	public static void setTimestamp(byte[] ba, int offset, Timestamp val) {
		setLong(ba, offset, val == null ? 0 : val.getTime());
	}


	/**
	 * Return a Timestamp value from bytearray "ar" starting at the offset "offset".
	 *
	 * @param ar
	 * @param offset
	 * @param length
	 * @return
	 */
	public static Timestamp getTimestamp(byte[] ar, int offset) {
		long l = getLong(ar, offset);
		return l == 0 ? null : new Timestamp(l);
	}

	public static void setShort(byte[] ba, int offset, short val) {
		ba[offset] = (byte) ((val >> 8) & 0xff);
		ba[offset + 1] = (byte) (val & 0xff);
	}

	public static short getShort(byte[] ba, int offset) {
		return (short) (((ba[offset] << 8) | (ba[offset + 1]) & 0xff) & 0xffff);
	}

	/**
	 *
	 */
	public static void setInt(byte[] ba, int offset, int val) {
		//		int ia[] = new int[4];
		int x;
		byte b;

		x = ((val >> 24) & 0xff);
		b = (byte) x;
		ba[offset] = b;

		x = ((val >> 16) & 0xff);
		b = (byte) x;
		ba[offset + 1] = b;

		x = ((val >> 8) & 0xff);
		b = (byte) x;
		ba[offset + 2] = b;

		x = (val & 0xff);
		b = (byte) x;
		ba[offset + 3] = b;
	}


	/**
	 *	Returns an integer packed into a byte array.
	 */
	public static int getInt(byte[] ba, int offset) {
		int val = (ba[offset + 3] & 0xff);

		val |= (ba[offset] & 0xff) << 24;
		val |= (ba[offset + 1] & 0xff) << 16;
		val |= (ba[offset + 2] & 0xff) << 8;
		return val;
	}


	/**
	 *	Sets a string converted to 8-bit ascii into an array. If the string is
	 *  smaller than the allotted length the rest is padded with binary zeroes.
	 */
	public static void setString(byte[] ba, int offset, String s, int length) {
		int i;
		int l = s.length();
		if(l > length)
			l = length;

		for(i = 0; i < l; i++)
			ba[offset++] = (byte) (s.charAt(i) & 0xff); // Convert to 8-bit ascii

		while(i++ < length)
			ba[offset++] = 0;
	}

	/**
	 *	Returns a string from the thingy.
	 */
	static public String getString(byte[] ar, int offset, int length) {
		char[] c = new char[length];
		int ix;

		//-- Move bytes.
		ix = 0;
		while(ix < length) {
			if(ar[offset] == 0)
				break; // End of string
			c[ix] = (char) (ar[offset] & 0xff);
			ix++;
			offset++;
		}
		return new String(c, 0, ix);
	}

	/**
	 *	Compares a byte array part with an array.
	 */
	public static int compare(byte[] a, int from, int to, byte[] b) {
		//		if(b.length != to-from) throw new IllegalArgumentException("Bad size array passed to compare.");

		int sx = 0;
		int rv = 0;
		for(int i = from; i < to; i++, sx++) {
			if(sx < b.length)
				rv = (a[i] & 0xff) - (b[sx] & 0xff);
			else
				rv = a[i];
			if(rv != 0) {
				return rv > 0 ? -1 : 1;
			}
		}
		return 0;
	}

	static public int compare(byte[] a, int astart, byte[] b, int bstart, int len) {
		while(len-- > 0) {
			int rv = a[astart++] - b[bstart++];
			if(rv < 0)
				return -1;
			else if(rv > 0)
				return 1;
		}
		return 0;
	}

	/**
	 *  Find a key by means of a binary search.
	 *  post: index if found, -1 if not
	 *
	 * @param v     vector containing the element to search for
	 * @param start location where to start comparing the bytes
	 * @param key   The key to look for.
	 */
	public static int findKey(List<byte[]> v, int start, byte[] key) {
		byte[] ba;
		int mid, rv;
		int low, high;

		//-- Init,
		low = 0;
		high = v.size();

		//-- Loop till found.
		for(;;) {
			if(low >= high)
				return -1;

			//-- Get middle element,
			mid = (low + high) / 2;
			ba = v.get(mid); // Get middle element,
			rv = ByteArrayUtil.compare(ba, start, key.length, key); // Compare,
			if(rv == 0)
				return mid;
			if(rv < 0) {
				high = mid;
			} else {
				low = mid + 1;
			}
		}
	}
}
