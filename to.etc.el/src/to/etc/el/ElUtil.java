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
package to.etc.el;

import java.math.*;

import javax.servlet.jsp.el.*;

import to.etc.util.*;

@SuppressWarnings("deprecation")
public class ElUtil {
	private ElUtil() {}


	/*--------------------------------------------------------------*/
	/*	CODING:	Type checkers.										*/
	/*--------------------------------------------------------------*/
	/**
	 * Returns T if the class is any number.
	 */
	static public boolean isNumberClass(Class< ? > cl) {
		return cl == Byte.class || cl == Byte.TYPE || cl == Short.class || cl == Short.TYPE || cl == Integer.class || cl == Integer.TYPE || cl == Long.class || cl == Long.TYPE || cl == Float.class
			|| cl == Float.TYPE || cl == Double.class || cl == Double.TYPE || cl == BigInteger.class || cl == BigDecimal.class;
	}

	public static boolean isBigInteger(Object o) {
		return o instanceof BigInteger;
	}

	public static boolean isBigDecimal(Object o) {
		return o instanceof BigDecimal;
	}

	static public boolean isBigDecimalType(Class< ? > cl) {
		return cl == BigDecimal.class;
	}

	static public boolean isBigIntegerType(Class< ? > cl) {
		return cl == BigInteger.class;
	}

	public static boolean isAnyIntegerObject(Object o) {
		if(o == null)
			return false;
		Class< ? > cl = o.getClass();
		return cl == Byte.TYPE || cl == Short.TYPE || cl == Character.TYPE || cl == Integer.TYPE || cl == Long.TYPE;
	}

	/**
	 * Returns T if the given class is of an integer type
	 **/
	public static boolean isAnyIntegerType(Class< ? > clz) {
		return clz == Byte.class || clz == Byte.TYPE || clz == Short.class || clz == Short.TYPE || clz == Character.class || clz == Character.TYPE || clz == Integer.class || clz == Integer.TYPE
			|| clz == Long.class || clz == Long.TYPE;
	}

	/**
	 * Returns T if the given string might contain a floating point
	 * number - i.e., it contains ".", "e", or "E"
	 **/
	static public boolean isFloatingPointString(Object o) {
		if(o instanceof String) {
			String str = (String) o;
			int len = str.length();
			for(int i = 0; i < len; i++) {
				char ch = str.charAt(i);
				if(ch == '.' || ch == 'e' || ch == 'E')
					return true;
			}
		}
		return false;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Converting primitives to other primitives.			*/
	/*--------------------------------------------------------------*/
	/**
	 * Converts a long to some number class.
	 **/
	static Number coerceToPrimitiveNumber(long pValue, Class< ? > clz) throws ELException {
		if(clz == Byte.class || clz == Byte.TYPE)
			return WrapperCache.getByte((byte) pValue);
		else if(clz == Short.class || clz == Short.TYPE)
			return WrapperCache.getShort((short) pValue);
		else if(clz == Integer.class || clz == Integer.TYPE)
			return WrapperCache.getInteger((int) pValue);
		else if(clz == Long.class || clz == Long.TYPE)
			return WrapperCache.getLong(pValue);
		else if(clz == Float.class || clz == Float.TYPE)
			return WrapperCache.getFloat(pValue);
		else if(clz == Double.class || clz == Double.TYPE)
			return WrapperCache.getDouble(pValue);
		else
			return WrapperCache.getInteger(0);
	}

	/**
	 * Converts any Number class to some other number class.
	 **/
	static Number convNumberToNumber(Number pValue, Class< ? > clz) throws ELException {
		if(clz == Byte.class || clz == Byte.TYPE)
			return WrapperCache.getByte(pValue.byteValue());
		else if(clz == Short.class || clz == Short.TYPE)
			return WrapperCache.getShort(pValue.shortValue());
		else if(clz == Integer.class || clz == Integer.TYPE)
			return WrapperCache.getInteger(pValue.intValue());
		else if(clz == Long.class || clz == Long.TYPE)
			return WrapperCache.getLong(pValue.longValue());
		else if(clz == Float.class || clz == Float.TYPE)
			return WrapperCache.getFloat(pValue.floatValue());
		else if(clz == Double.class || clz == Double.TYPE)
			return WrapperCache.getDouble(pValue.doubleValue());
		else if(clz == BigInteger.class) {
			if(pValue instanceof BigDecimal)
				return ((BigDecimal) pValue).toBigInteger();
			else
				return BigInteger.valueOf(pValue.longValue());
		} else if(clz == BigDecimal.class) {
			if(pValue instanceof BigInteger)
				return new BigDecimal((BigInteger) pValue);
			else
				return new BigDecimal(pValue.doubleValue());
		} else
			return WrapperCache.getInteger(0);
	}

	static public BigDecimal coerceToBigDecimal(Object in) {
		if(in != null) {
			if(in instanceof BigDecimal)
				return (BigDecimal) in;
			if(in instanceof BigInteger)
				return new BigDecimal((BigInteger) in);
			if(in instanceof Number)
				return new BigDecimal(((Number) in).doubleValue());
		}
		converr(in, "BigDecimal");
		return new BigDecimal(0);
	}

	static public BigInteger coerceToBigInteger(Object in) {
		if(in != null) {
			if(in instanceof String) // String containing #
				return new BigInteger((String) in);
			if(in instanceof BigInteger)
				return (BigInteger) in;
			if(in instanceof BigDecimal)
				return ((BigDecimal) in).toBigInteger();
			else if(in instanceof Number)
				return BigInteger.valueOf(((Number) in).longValue());
		}
		converr(in, "BigInteger");
		return BigInteger.valueOf(0);
	}

	static public double coerceToDouble(Object a) {
		if(a != null) {
			if(a instanceof Number)
				return ((Number) a).doubleValue();
			if(a instanceof String) {
				try {
					return Double.parseDouble((String) a);
				} catch(Exception x) {}
			}
		}
		converr(a, "double");
		return 0.0;
	}

	static public long coerceToLong(Object a) {
		if(a != null) {
			if(a instanceof Number)
				return ((Number) a).longValue();
		}
		converr(a, "long");
		return 0;
	}


	static private void converr(Object a, String to) {
		System.out.print("el: cannot convert " + a + " ");
		if(a != null) {
			System.out.print(" [class " + a.getClass().getName() + "]");
		}
		System.out.println(" to " + to);
	}

	/**
	 * Converts the specified value to a string.
	 **/
	static public String coerceToString(Object o) throws ELException {
		if(o == null)
			return "";
		else if(o instanceof String)
			return (String) o;
		else {
			try {
				return o.toString();
			} catch(Exception exc) {
				exc.printStackTrace();
				return "";
			}
		}
	}

	/**
	 * Converts the input object to the primitive number class specified.
	 * @param val
	 * @param cl
	 * @return
	 */
	static public Number convToNumber(Object val, Class< ? > cl) {
		return null;
	}
}
