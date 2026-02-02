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

/**
 * DO NOT USE- LOOKS EVIL.
 * Cached values for value objects.
 **/
@Deprecated
public class WrapperCache {
	private static int			BYTE_LOWER_BOUND		= 0;

	private static int			BYTE_UPPER_BOUND		= 255;

	private static int			CHARACTER_LOWER_BOUND	= 0;

	private static int			CHARACTER_UPPER_BOUND	= 255;

	private static int			SHORT_LOWER_BOUND		= -1000;

	private static int			SHORT_UPPER_BOUND		= 1000;

	private static int			INTEGER_LOWER_BOUND		= -1000;

	private static int			INTEGER_UPPER_BOUND		= 1000;

	private static int			LONG_LOWER_BOUND		= -1000;

	private static int			LONG_UPPER_BOUND		= 1000;

	private static Byte[]		m_bytes					= createBytes();

	private static Character[]	m_chars					= createCharacters();

	private static Short[]		m_shorts				= createShorts();

	private static Integer[]	m_ints					= createIntegers();

	private static Long[]		m_longs					= createLongs();

	public static Boolean getBoolean(boolean pValue) {
		return pValue ? Boolean.TRUE : Boolean.FALSE;
	}

	public static Byte getByte(byte pValue) {
		if(pValue >= BYTE_LOWER_BOUND && pValue <= BYTE_UPPER_BOUND)
			return m_bytes[pValue - BYTE_LOWER_BOUND];
		else
			return new Byte(pValue);
	}

	public static Character getCharacter(char pValue) {
		if(pValue >= CHARACTER_LOWER_BOUND && pValue <= CHARACTER_UPPER_BOUND)
			return m_chars[pValue - CHARACTER_LOWER_BOUND];
		else
			return new Character(pValue);
	}

	public static Short getShort(short pValue) {
		if(pValue >= SHORT_LOWER_BOUND && pValue <= SHORT_UPPER_BOUND)
			return m_shorts[pValue - SHORT_LOWER_BOUND];
		else
			return new Short(pValue);
	}

	public static Integer getInteger(int pValue) {
		if(pValue >= INTEGER_LOWER_BOUND && pValue <= INTEGER_UPPER_BOUND)
			return m_ints[pValue - INTEGER_LOWER_BOUND];
		else
			return new Integer(pValue);
	}

	public static Long getLong(long pValue) {
		if(pValue >= LONG_LOWER_BOUND && pValue <= LONG_UPPER_BOUND)
			return m_longs[((int) pValue) - LONG_LOWER_BOUND];
		else
			return new Long(pValue);
	}

	public static Float getFloat(float pValue) {
		return new Float(pValue);
	}

	public static Double getDouble(double pValue) {
		return new Double(pValue);
	}

	//	/**
	//	 * If the given class is a primitive class, returns the object
	//	 * version of that class.  Otherwise, the class is just returned.
	//	 **/
	//	public static Class getPrimitiveObjectClass(Class pClass)
	//	{
	//		if(pClass == Boolean.TYPE)
	//		{
	//			return Boolean.class;
	//		}
	//		else if(pClass == Byte.TYPE)
	//		{
	//			return Byte.class;
	//		}
	//		else if(pClass == Short.TYPE)
	//		{
	//			return Short.class;
	//		}
	//		else if(pClass == Character.TYPE)
	//		{
	//			return Character.class;
	//		}
	//		else if(pClass == Integer.TYPE)
	//		{
	//			return Integer.class;
	//		}
	//		else if(pClass == Long.TYPE)
	//		{
	//			return Long.class;
	//		}
	//		else if(pClass == Float.TYPE)
	//		{
	//			return Float.class;
	//		}
	//		else if(pClass == Double.TYPE)
	//		{
	//			return Double.class;
	//		}
	//		else
	//		{
	//			return pClass;
	//		}
	//	}
	//
	/*--------------------------------------------------------------*/
	/*	CODING:	Initialization.										*/
	/*--------------------------------------------------------------*/
	private static Short[] createShorts() {
		int len = SHORT_UPPER_BOUND - SHORT_LOWER_BOUND + 1;
		Short[] ret = new Short[len];
		short val = (short) SHORT_LOWER_BOUND;
		for(int i = 0; i < len; i++, val++)
			ret[i] = new Short(val);
		return ret;
	}

	private static Integer[] createIntegers() {
		int len = INTEGER_UPPER_BOUND - INTEGER_LOWER_BOUND + 1;
		Integer[] ret = new Integer[len];
		int val = INTEGER_LOWER_BOUND;
		for(int i = 0; i < len; i++, val++)
			ret[i] = new Integer(val);
		return ret;
	}

	private static Long[] createLongs() {
		int len = LONG_UPPER_BOUND - LONG_LOWER_BOUND + 1;
		Long[] ret = new Long[len];
		long val = LONG_LOWER_BOUND;
		for(int i = 0; i < len; i++, val++)
			ret[i] = new Long(val);
		return ret;
	}

	private static Byte[] createBytes() {
		int len = BYTE_UPPER_BOUND - BYTE_LOWER_BOUND + 1;
		Byte[] ret = new Byte[len];
		byte val = (byte) BYTE_LOWER_BOUND;
		for(int i = 0; i < len; i++, val++)
			ret[i] = new Byte(val);
		return ret;
	}

	private static Character[] createCharacters() {
		int len = CHARACTER_UPPER_BOUND - CHARACTER_LOWER_BOUND + 1;
		Character[] ret = new Character[len];
		char val = (char) CHARACTER_LOWER_BOUND;
		for(int i = 0; i < len; i++, val++)
			ret[i] = new Character(val);
		return ret;
	}
}
