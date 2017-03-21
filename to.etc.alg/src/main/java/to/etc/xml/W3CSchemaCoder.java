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
package to.etc.xml;

import java.io.*;
import java.math.*;
import java.util.*;

import javax.annotation.*;

import to.etc.util.*;

/**
 * Helper class which encodes data using the w3c schema definitions; see
 * http://www.w3.org/TR/xmlschema-2
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 29, 2008
 */
public class W3CSchemaCoder {
	static public String encodeBoolean(final boolean val) {
		return val ? "true" : "false";
	}

	static public boolean decodeBoolean(String in) {
		in = in.trim().toLowerCase();
		if("true".equals(in) || "1".equals(in))
			return true;
		if("false".equals(in) || "0".equals(in))
			return false;
		throw new W3CEncodingException("Invalid 'boolean' value", in);
	}

	static public String encodeDouble(final double v) {
		return Double.toString(v);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Date/Time encoding.									*/
	/*--------------------------------------------------------------*/
	static private final ThreadLocal<GregorianCalendar> m_calendar = new ThreadLocal<GregorianCalendar>();

	static private final ThreadLocal<MiniParser>		m_parser	= new ThreadLocal<MiniParser>();

	/**
	 * Get the per-thread shared copy of a Calendar; this prevents lots of garbage while scanning XML.
	 * @return
	 */
	@Nonnull
	static private GregorianCalendar calendar() {
		GregorianCalendar c = m_calendar.get();
		if(c == null) {
			c = (GregorianCalendar) DateUtil.getCalendar(Locale.US);
			m_calendar.set(c);
		}
		c.setTimeZone(TimeZone.getDefault());
		return c;
	}

	@Nonnull
	static private GregorianCalendar calendar(@Nonnull Date in) {
		GregorianCalendar c = calendar();
		c.setTime(in);
		return c;
	}

	static private MiniParser parser() {
		MiniParser mp = m_parser.get();
		if(mp == null) {
			mp = new MiniParser();
			m_parser.set(mp);
		}
		return mp;
	}

	static private MiniParser parser(final String txt) {
		MiniParser p = parser();
		p.init(txt.trim());
		return p;
	}

	/**
	 * This encodes the date/time. If sourceZone is specified the date gets converted
	 * from that zone to UTC as specified by the standard. If zone is null no conversion
	 * will be done.
	 *
	 * @param in
	 * @param sourceZone
	 * @return
	 */
	static public String encodeDateTimeToUTC(final Date in, final TimeZone sourceZone) {
		Calendar cal;
		if(sourceZone != null) {
			//-- Get a calendar in the specified zone,
			cal = calendar();
			cal.setTime(in); // Set it using this date,
			long utc = cal.getTimeInMillis(); // Get UTC time,
			cal.setTimeZone(TimeZone.getTimeZone("UTC"));
			cal.setTimeInMillis(utc); // Set in milli's UTC
		} else {
			cal = calendar();
			cal.setTime(in);
		}

		StringBuilder sb = new StringBuilder(32);
		int yr = cal.get(Calendar.YEAR);
		if(yr < 0)
			sb.append('-');
		appendInt(sb, yr, 4); // Year, min 4 digits
		sb.append('-');
		appendInt(sb, cal.get(Calendar.MONTH) + 1, 2);
		sb.append('-');
		appendInt(sb, cal.get(Calendar.DATE), 2);
		sb.append('T');
		appendInt(sb, cal.get(Calendar.HOUR_OF_DAY), 2);
		sb.append(':');
		appendInt(sb, cal.get(Calendar.MINUTE), 2);
		sb.append(':');
		appendInt(sb, cal.get(Calendar.SECOND), 2);
		int ms = cal.get(Calendar.MILLISECOND);
		if(ms != 0) {
			sb.append('.');
			appendInt(sb, ms, 4);
		}

		return sb.toString();
	}

	/**
	 * Outputs the date specified (which is a time in the specified timezone) and add
	 * the timezone identifier if present.
	 *
	 * @param in
	 * @param timezone
	 * @return
	 */
	static public String encodeDateTime(final Date in, final TimeZone timezone) {
		Calendar cal;
		cal = calendar();
		if(timezone != null)
			cal.setTimeZone(timezone);
		cal.setTime(in);

		StringBuilder sb = new StringBuilder(32);
		int yr = cal.get(Calendar.YEAR);
		if(yr < 0)
			sb.append('-');
		appendInt(sb, yr, 4); // Year, min 4 digits
		sb.append('-');
		appendInt(sb, cal.get(Calendar.MONTH) + 1, 2);
		sb.append('-');
		appendInt(sb, cal.get(Calendar.DATE), 2);
		sb.append('T');
		appendInt(sb, cal.get(Calendar.HOUR_OF_DAY), 2);
		sb.append(':');
		appendInt(sb, cal.get(Calendar.MINUTE), 2);
		sb.append(':');
		appendInt(sb, cal.get(Calendar.SECOND), 2);
		int ms = cal.get(Calendar.MILLISECOND);
		if(ms != 0) {
			sb.append('.');
			appendInt(sb, ms, 4);
		}
		addTimezone(sb, timezone, in.getTime());
		return sb.toString();
	}

	static private void addTimezone(final StringBuilder sb, final TimeZone timezone, final long atdate) {
		if(timezone == null)
			return;
		int offset = atdate != -1 ? timezone.getOffset(atdate) : timezone.getRawOffset();
		if(offset == 0)
			sb.append('Z');
		else {
			if(offset > 0)
				sb.append('+');
			else {
				sb.append('-');
				offset = -offset;
			}
			offset /= 60 * 1000; // In minutes
			appendInt(sb, offset / 60, 2); // Add hours
			sb.append(':');
			appendInt(sb, offset % 60, 2);
		}
	}

	/**
	 * Outputs the time-only specified (which is a time in the specified timezone) and add
	 * the timezone identifier if present.
	 *
	 * @param in
	 * @param timezone
	 * @return
	 */
	static public String encodeTime(final Date in, final TimeZone timezone) {
		Calendar cal = calendar(in);

		StringBuilder sb = new StringBuilder(32);
		appendInt(sb, cal.get(Calendar.HOUR_OF_DAY), 2);
		sb.append(':');
		appendInt(sb, cal.get(Calendar.MINUTE), 2);
		sb.append(':');
		appendInt(sb, cal.get(Calendar.SECOND), 2);
		int ms = cal.get(Calendar.MILLISECOND);
		if(ms != 0) {
			sb.append('.');
			appendInt(sb, ms, 4);
		}
		addTimezone(sb, timezone, in.getTime());
		return sb.toString();
	}

	/**
	 * Outputs the date specified (which is a date in the specified timezone) using only
	 * date fields, no time.
	 *
	 * @param in
	 * @param timezone
	 * @return
	 */
	static public String encodeDate(final Date in, final TimeZone timezone) {
		Calendar cal = calendar(in);

		StringBuilder sb = new StringBuilder(32);
		int yr = cal.get(Calendar.YEAR);
		if(yr < 0)
			sb.append('-');
		appendInt(sb, yr, 4); // Year, min 4 digits
		sb.append('-');
		appendInt(sb, cal.get(Calendar.MONTH) + 1, 2);
		sb.append('-');
		appendInt(sb, cal.get(Calendar.DATE), 2);
		addTimezone(sb, timezone, in.getTime());
		return sb.toString();
	}

	static private final void appendInt(final StringBuilder sb, final int val, final int minlen) {
		String s = Integer.toString(val);
		int len = s.length();
		while(len++ < minlen)
			sb.append('0');
		sb.append(s);
	}

	static public final String encodeInteger(final long value) {
		return Long.toString(value);
	}

	static public final String encodeInteger(final int value) {
		return Integer.toString(value);
	}

	static public final String encodeDecimal(final BigDecimal value) {
		return value.toString();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Date and time decoding.								*/
	/*--------------------------------------------------------------*/
	/**
	 * Decodes a xsd:date format value; lexical representation is [['-'? yyyy '-' mm '-' dd zzzzzz?]].
	 *
	 * WARNING: This current version decodes the version for "http://www.w3.org/TR/xmlschema-2/" and as such
	 * disallows yyyy=0000. Newer versions of the standard may allow this.
	 * @param in
	 * @return	a Calendar holding the date. This calendar gets OVERWRITTEN the next time a date/time gets decoded!!
	 */
	static public final GregorianCalendar decodeDate(final String in) {
		try {
			MiniParser p = parser(in);
			GregorianCalendar cal = calendar();
			cal.setLenient(false);
			parseDate(cal, p, true); // Date  fragment
			TimeZone tz = parseTimeZone(p); // Optional timezone

			if(tz != null)
				cal.setTimeZone(tz);
			DateUtil.clearTime(cal);
			return cal;
		} catch(W3CEncodingException x) {
			x.setReason("Invalid xsd:date: " + x.getReason());
			throw x;
		}
	}

	/**
	 * Decodes a xsd:dateTime value; lexical representation is [['-'? yyyy '-' mm '-' dd 'T' hh ':' mm ':' ss ('.' s+)? (zzzzzz)?]].
	 * @param in
	 * @return	a Calendar holding the date. This calendar gets OVERWRITTEN the next time a date/time gets decoded!!
	 */
	static public final GregorianCalendar decodeDateTime(final String in) {
		try {
			MiniParser p = parser(in);
			GregorianCalendar cal = calendar();
			cal.setLenient(false);
			parseDate(cal, p, true); // Date  fragment
			if(!p.curIs("T") && !p.curIs("t"))
				throw new W3CEncodingException("Missing 'T' in dateTime", in);
			parseTime(cal, p);
			TimeZone tz = parseTimeZone(p); // Optional timezone
			if(tz != null)
				cal.setTimeZone(tz);
			return cal;
		} catch(W3CEncodingException x) {
			x.setReason("Invalid xsd:dateTime: " + x.getReason());
			throw x;
		}
	}

	/**
	 * XML/RPC datetime type.
	 * @param in
	 * @return
	 */
	static public final GregorianCalendar decodeDateTime_iso8601(final String in) {
		try {
			MiniParser p = parser(in);
			GregorianCalendar cal = calendar();
			parseDate(cal, p, false); // Date  fragment
			if(!p.curIs("T") && !p.curIs("t"))
				throw new W3CEncodingException("Missing 'T' in dateTime", in);
			parseTime(cal, p);
			return cal;
		} catch(W3CEncodingException x) {
			x.setReason("Invalid iso8601 datetime: " + x.getReason());
			throw x;
		}
	}

	/**
	 *
	 * @param in
	 * @return
	 */
	static public final GregorianCalendar decodeTime(final String in) {
		try {
			GregorianCalendar cal = calendar();
			MiniParser p = parser(in);
			parseTime(cal, p);
			TimeZone tz = parseTimeZone(p); // Optional timezone
			if(tz != null)
				cal.setTimeZone(tz);
			return cal;
		} catch(W3CEncodingException x) {
			x.setReason("Invalid xsd:time: " + x.getReason());
			throw x;
		}
	}

	static private void parseDate(final Calendar cal, final MiniParser p, boolean dashed) {
		int year = parseYear(p);
		if(dashed)
			p.require("-");
		int month = p.parseFixedInt(2);
		if(dashed)
			p.require("-");
		int day = p.parseFixedInt(2);
		DateUtil.setDate(cal, year, month - 1, day);
	}

	/**
	 * Parses the fragment [[hh ':' mm ':' ss ('.' s+)?]].
	 * @param cal
	 * @param p
	 */
	static private void parseTime(final Calendar cal, final MiniParser p) {
		int hour = p.parseFixedInt(2);
		p.require(":");
		int min = p.parseFixedInt(2);
		p.require(":");
		int sec = p.parseFixedInt(2);
		int msec = 0;
		if(p.curIs(".")) {
			double frac = p.parseFraction();
			msec = (int) (1000 * frac);
			if(msec > 1000)
				msec = 999;
		}
		DateUtil.setTime(cal, hour, min, sec, msec);
	}

	/**
	 * Parses a fragment [['-' yyyy]]. If this fragment is not present it throws an exception.
	 * @param p
	 * @return
	 */
	static private int parseYear(final MiniParser p) {
		p.skipWs();
		int sign = 1;
		if(p.curIs("-"))
			sign = -1;
		int year = p.parseFixedInt(4);
		if(year == 0)
			throw new W3CEncodingException("'yyyy' date fragment cannot be zeroes.", p.getInput());
		return year * sign;
	}

	/**
	 * Parses a timezone. This allows the pattern: [[(('+' | '-') hh ':' mm) | 'Z']].
	 * @param p
	 * @return
	 */
	static private TimeZone parseTimeZone(final MiniParser p) {
		if(p.atEnd())
			return null;
		if(p.curIs("Z") || p.curIs("z")) { // Z means UTC.
			return TimeZone.getTimeZone("UTC"); // Return UTC timezone
		}

		//-- Expecting a valid timezone spec.
		int sign;
		if(p.curIs("+"))
			sign = 1;
		else if(p.curIs("-"))
			sign = -1;
		else
			throw new W3CEncodingException("Missing +/- in timezone", p.getInput());

		int hh = p.parseFixedInt(2);
		p.require(":");
		int mm = p.parseFixedInt(2);
		return new SimpleTimeZone((hh * 60 + mm) * 60 * 1000, (sign < 0 ? '-' : '+') + hh + ":" + mm);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Binary representations (SHOULD NOT BE USED)			*/
	/*--------------------------------------------------------------*/
	/**
	 * DO NOT USE: Bulk-encodes data into base64-encoding.
	 * @param out
	 * @param data
	 * @throws IOException
	 */
	static public final void encodeBase64(final Writer out, final byte[] data) throws IOException {
		if(data == null)
			return;
		int sidx, didx, olen = 0;
		char[] dest = new char[512]; // Must be multiple of 4.

		//-- 3-byte to 4-byte conversion + 0-63 to ascii printable conversion
		for(sidx = 0, didx = 0; sidx < data.length - 2;) {
			byte v1 = data[sidx++];
			byte v2 = data[sidx++];
			byte v3 = data[sidx++];
			dest[didx++] = (char) BASE64MAP[(v1 >> 2) & 0x3f]; // upper 6 bits of byte 1
			dest[didx++] = (char) BASE64MAP[(v1 << 4) & 0x30 | (v2 >> 4) & 0xf]; // lower 2 of b1 and upper 4 of b2
			dest[didx++] = (char) BASE64MAP[(v2 << 2) & 0x3c | (v3 >> 6) & 0x3]; // lower 4 bits of b2 and topmost 2 bits of v3
			dest[didx++] = (char) BASE64MAP[v3 & 0x3f];
			if(didx >= dest.length) {
				out.write(dest);
				olen += didx;
				didx = 0;
			}
		}
		if(didx > 0) {
			out.write(dest, 0, didx);
			olen += didx;
			didx = 0;
		}
		if(sidx < data.length) {
			dest[didx++] = (char) BASE64MAP[(data[sidx] >>> 2) & 077];
			olen++;
			if(sidx < data.length - 1) {
				dest[didx++] = (char) BASE64MAP[(data[sidx + 1] >>> 4) & 017 | (data[sidx] << 4) & 077];
				dest[didx++] = (char) BASE64MAP[(data[sidx + 1] << 2) & 077];
				olen += 2;
			} else {
				dest[didx++] = (char) BASE64MAP[(data[sidx] << 4) & 077];
				olen++;
			}
		}
		int flen = ((data.length + 2) / 3) * 4; // add padding
		while(olen++ < flen)
			dest[didx++] = '=';

		if(didx > 0) {
			out.write(dest, 0, didx);
			olen += didx;
			didx = 0;
		}

	}


	//-- rfc-2045: Base64 Alphabet
	static private final byte[]	BASE64MAP	= {(byte) 'A', (byte) 'B', (byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F', (byte) 'G', (byte) 'H', (byte) 'I', (byte) 'J', (byte) 'K', (byte) 'L',
		(byte) 'M', (byte) 'N', (byte) 'O', (byte) 'P', (byte) 'Q', (byte) 'R', (byte) 'S', (byte) 'T', (byte) 'U', (byte) 'V', (byte) 'W', (byte) 'X', (byte) 'Y', (byte) 'Z', (byte) 'a', (byte) 'b',
		(byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f', (byte) 'g', (byte) 'h', (byte) 'i', (byte) 'j', (byte) 'k', (byte) 'l', (byte) 'm', (byte) 'n', (byte) 'o', (byte) 'p', (byte) 'q', (byte) 'r',
		(byte) 's', (byte) 't', (byte) 'u', (byte) 'v', (byte) 'w', (byte) 'x', (byte) 'y', (byte) 'z', (byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4', (byte) '5', (byte) '6', (byte) '7',
		(byte) '8', (byte) '9', (byte) '+', (byte) '/'};

	private static class MiniParser {
		private String	m_input;

		private int		m_ix;

		private int		m_len;

		public MiniParser() {
		}

		public void init(final String s) {
			m_input = s;
			m_ix = 0;
			m_len = s.length();
		}

		public String getInput() {
			return m_input;
		}

		public void skipWs() {
			while(m_ix < m_len) {
				char c = m_input.charAt(m_ix);
				if(!Character.isWhitespace(c))
					return;
				m_ix++;
			}
		}

		public boolean atEnd() {
			return m_ix >= m_len;
		}

		/**
		 * Returns T if the current location starts with the specified string, and if so
		 * skips the string too.
		 * @param s
		 * @return
		 */
		public boolean curIs(final String s) {
			if(m_ix + s.length() > m_len || s.length() == 0)
				return false; // String longer than remains-> no match, or input is empty
			if(m_input.charAt(m_ix) != s.charAt(0))
				return false;
			for(int i = s.length(); --i >= 1;) {
				if(m_input.charAt(m_ix + i) != s.charAt(i))
					return false;
			}
			m_ix += s.length();
			return true;
		}

		public void require(final String s) {
			if(!curIs(s))
				throw new W3CEncodingException("Missing '" + s + "'", m_input);
		}

		public int parseFixedInt(int ndigits) {
			if(m_ix + ndigits > m_len)
				throw new W3CEncodingException("Input too short for " + ndigits + " digits", m_input);
			int value = 0;
			while(ndigits-- > 0) {
				char c = m_input.charAt(m_ix++);
				if(!Character.isDigit(c))
					throw new W3CEncodingException("Invalid digit: '" + c + "'", m_input);
				value = value * 10 + (c - '0');
			}
			return value;
		}

		//		public int parseInt() {
		//			int value = 0;
		//			int nc = 0;
		//			while(m_ix < m_len) {
		//				char c = m_input.charAt(m_ix);
		//				if(!Character.isDigit(c)) {
		//					if(nc == 0)
		//						throw new W3CEncodingException("Missing digits in integer number", m_input);
		//					return value;
		//				}
		//				value = value * 10 + (c - '0');
		//				nc++;
		//				m_ix++;
		//			}
		//			if(nc == 0)
		//				throw new W3CEncodingException("Missing digits in integer number", m_input);
		//			return value;
		//		}

		public double parseFraction() {
			double value = 0.0d;
			int nc = 0;
			double fix = 0.1d;
			while(m_ix < m_len) {
				char c = m_input.charAt(m_ix);
				if(!Character.isDigit(c)) {
					if(nc == 0)
						throw new W3CEncodingException("Missing digits in integer number", m_input);
					return value;
				}
				value = value + (fix * (c - '0'));
				fix /= 10;
				nc++;
				m_ix++;
			}
			if(nc == 0)
				throw new W3CEncodingException("Missing digits in integer number", m_input);
			return value;
		}
	}


}
