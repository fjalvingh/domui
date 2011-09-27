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

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * This static utility class contains a load of string functions. And some other
 * stuff I could not quickly find a place for ;-)
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * @version 1.0
 */
public class StringTool {
	static public boolean isValidJavaIdentifier(final String s) {
		int len = s.length();
		if(len == 0)
			return false;
		if(!Character.isJavaIdentifierStart(s.charAt(0)))
			return false;
		for(int i = 1; i < len; i++) {
			if(!Character.isJavaIdentifierPart(s.charAt(i)))
				return false;
		}
		return true;
	}


	/**
	 * Checks if the name is a valid domain name. These can contain only
	 * letters (a..z), digits (0..9), the dash and dots. Dots cannot start or
	 * end a name, nor can two dots occurs immediately next to another.
	 * @param s
	 * @return
	 */
	static public boolean isValidDomainName(final String s) {
		int len = s.length();
		if(len == 0)
			return false;
		int ix = 0;
		int lastdot = -1;
		while(ix < len) {
			char c = s.charAt(ix);
			if(c == '.') {
				//-- Dot.. Was prev char a dot also?
				if(ix - 1 == lastdot)
					return false; // Two dots in a row, or a dot at pos 0
				lastdot = ix;
			} else if(!isDomainChar(c))
				return false; // Invalid character for domain name
			ix++;
		}
		if(lastdot + 1 == len)
			return false;
		return true;
	}

	static public boolean isNumber(final String s) {
		try {
			Integer.parseInt(s.trim());
			return true;
		} catch(Exception x) {}
		return false;
	}

	static public boolean isDomainChar(final char c) {
		return c == '-' || c == '.' || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9');
	}

	static public boolean isWhiteSpaceOrNbsp(final char c) {
		return c == 0x00a0 || Character.isWhitespace(c);
	}

	static public boolean isAllSpaces(final String s) {
		for(int i = s.length(); --i >= 0;) {
			if(!isWhiteSpaceOrNbsp(s.charAt(i)))
				return false;
		}
		return true;
	}

	static public boolean isValidEmail(final String em) {
		int ix = em.indexOf('@');
		if(ix == -1)
			return false;
		//		String pre = em.substring(0, ix);
		String dom = em.substring(ix + 1);
		if(!isValidDomainName(dom))
			return false;
		return true; //isValidDottedName(pre);
	}

	/**
	 * Field name must start with ascii letter, then letters, digits or _.
	 * @param s
	 * @return
	 */
	static public boolean isValidDbFieldName(String s) {
		if(s == null || s.length() == 0)
			return false;
		if(s.length() > 30)
			return false;
		s = s.toLowerCase();
		char c = s.charAt(0);
		if(c < 'a' || c > 'z')
			return false;

		for(int i = s.length(); --i > 0;) {
			c = s.charAt(i);
			if((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_')
				;
			else
				return false;
		}
		return true;
	}


	static public boolean isValidDottedName(String s) {
		if(s == null)
			return false;
		s = s.trim();
		if(s.length() == 0)
			return false;

		//-- Now: only allow names containing ascii chars starting with a nondigit.
		for(int i = s.length(); --i >= 0;) {
			char ch = s.charAt(i);
			if(!isValidDottedChar(ch))
				return false;
		}

		return true;
	}

	static private boolean isValidDottedChar(final char c) {
		if(Character.isDigit(c))
			return true;
		if(Character.isLetter(c))
			return true;
		if(c == '.' || c == '_')
			return true;
		return false;
	}


	static public boolean isEqual(final Object a, final Object b) {
		if(a == b)
			return true;
		if(a == null || b == null)
			return false;
		return a.equals(b);
	}

	public static void stringize(final StringBuffer sb, final String s) {
		for(int i = 0; i < s.length(); i++) {
			char c = s.charAt(i); // Get the char to put,
			switch(c){
				case '"':
					sb.append("\\\"");
					break;
				case '\n':
					sb.append("\\n");
					break;
				case '\b':
					sb.append("\\b");
					break;
				case '\r':
					sb.append("\\r");
					break;
				case '\\':
					sb.append("\\\\");
					break;
				case '\t':
					sb.append("\\\t");
					break;
				default:
					if(c < ' ' || c > '\u007f')
						sb.append("\\u" + Integer.toHexString(c));
					else
						sb.append(c);
					break;
			}
		}
	}


	/**
	 *	Converts a string into a java-compilable version of a string, i.e.
	 *	surrounded by quotes, and with escape sequences escaped..
	 */
	public static StringBuffer stringize(final String s) {
		StringBuffer sb = new StringBuffer(s.length() + 20);

		sb.append("\""); // Write a quote,
		stringize(sb, s);
		sb.append("\"");
		return sb;
	}

	public static StringBuffer stringizeNQ(final String s) {
		StringBuffer sb = new StringBuffer(s.length() + 20);
		stringize(sb, s);
		return sb;
	}


	/**
	 *	Takes an input string and replaces all occurences of the backslash with
	 *	a forward slash.
	 */
	public static String strBackslashToSlash(final String s) {
		StringBuffer sb = new StringBuffer(s.length());

		int six, ix;

		six = 0;
		while(true) {
			ix = s.indexOf('\\', six);
			if(ix == -1) {
				sb.append(s.substring(six)); // Append the last part,
				return sb.toString(); // And be done!
			}
			sb.append(s.substring(six, ix)); // Copy all but slash
			sb.append('/');
			six = ix + 1;
		}
	}


	/**
	 *	Returns a string representing some size, in bytes. Depending on the size
	 *  it will be represented as KB, MB, GB or TB.
	 */
	public static String strSize(final long sz) {
		final long kb = 1024;
		final long mb = kb * 1024;
		final long gb = mb * 1024;
		final long tb = gb * 1024;

		long div = 1;
		String sf = "";
		if(sz >= tb) {
			div = tb;
			sf = "TB";
		} else if(sz >= gb) {
			div = gb;
			sf = "GB";
		} else if(sz >= mb) {
			div = mb;
			sf = "MB";
		} else if(sz >= kb) {
			div = kb;
			sf = "KB";
		}

		//-- Now do something,
		StringBuffer sb = new StringBuffer(15);

		if(div == 1) {
			return sz + " bytes";
		}

		long v = (sz / div);
		long r = (sz % div) / (div / 10);
		sb.append(Long.toString(v));
		if(r != 0) {
			sb.append(".");
			sb.append(Long.toString(r));
		}
		sb.append(" ");
		sb.append(sf);
		return sb.toString();
	}


	/**
	 *	Takes a java string, without quotes, and replaces all escape sequences
	 *	in there with their actual character representation.
	 */
	public static void parseString(final StringBuffer sb, final String s) {
		int i = 0;

		while(i < s.length()) {
			char c = s.charAt(i++);

			if(c != '\\')
				sb.append(c);
			else {
				if(i >= s.length()) {
					sb.append(c);
					return;
				}
				c = s.charAt(i++);
				switch(c){
					case '"':
						sb.append('"');
						break;
					case '\'':
						sb.append('\'');
						break;
					case 'b':
						sb.append('\b');
						break;
					case 'f':
						sb.append('\f');
						break;
					case 'n':
						sb.append('\n');
						break;
					case 'r':
						sb.append('\r');
						break;
					case 't':
						sb.append('\t');
						break;
					case 'u':
						//** Is UNICODE-
						sb.append("\\u");
						break;

					default:
						sb.append("\\");
						sb.append(c);
						break;
				}
			}
		}
	}

	/**
	 *	If the input string is too long, returns a substring containing at most
	 *	maxlen characters.
	 */
	public static String truncLength(final String s, final int maxlen) {
		if(s.length() < maxlen)
			return s;
		return s.substring(0, maxlen);
	}


	/**
	 *	Returns a string with the specified length. If the string is too long
	 *	it is truncated; if it is too short it is filled with spaces.
	 */
	public static String strToFixedLength(String s, final int l) {
		if(s == null)
			s = "null";
		String t = "                                          ";

		if(s.length() == l)
			return s; // Length already OK,
		if(s.length() > l)
			return s.substring(0, l); // Truncate,

		//** Need to add spaces.. Can we do that quickly?
		int dl = l - s.length(); // Get difference in lengths,

		while(true) {
			if(dl > t.length()) // Very big?
			{
				s = s + t; // Append whole space buffer,
				dl -= t.length();
			} else {
				s = s + t.substring(0, dl); // Just add a small part,
				return s;
			}
		}
	}

	/**
	 *	Returns a string with the specified length. If the string is too long
	 *	it is truncated; if it is too short it is filled with c.
	 */
	public static String strToFixedLength(final String s, final char c, final int l) {
		if(s.length() == l)
			return s; // Length already OK,
		if(s.length() > l)
			return s.substring(0, l); // Truncate,

		//** Need to add spaces.. Can we do that quickly?
		int dl = l - s.length(); // Get difference in lengths,
		StringBuffer sb = new StringBuffer(l);
		sb.append(s);
		while(dl-- > 0)
			sb.append(c);
		return sb.toString();
	}


	/**
	 *	Returns a coordinate pair as a string.
	 */
	static public String toXY(final int x, final int y) {
		return "(" + x + "," + y + ")";
	}


	/********************************************************************/
	/*	CODING:	Fuzzy String comparisons..								*/
	/********************************************************************/

	/**
	 * This returns the Levenshtein distance between two strings, which is the number of <i>changes</i> (adds, removes)
	 * that are needed to convert source into target. The number of changes is an indication of the difference between
	 * those strings.
	 */
	public static int getLevenshteinDistance(String s, String t, boolean ignorecase) {
		if(s == null || t == null)
			throw new IllegalArgumentException("Strings must not be null");
		if(ignorecase) {
			s = s.trim().toLowerCase();
			t = t.trim().toLowerCase();
		}
		int n = s.length();
		int m = t.length();

		if(n == 0) {
			return m;
		} else if(m == 0) {
			return n;
		} else if(m == n) {
			if(s.equals(t)) // Optimization for when strings are equal.
				return 0;
		}

		int p[] = new int[n + 1]; // 'previous' cost array, horizontally
		int d[] = new int[n + 1]; // cost array, horizontally
		int _d[]; //placeholder to assist in swapping p and d

		// indexes into strings s and t
		int i; // iterates through s
		int j; // iterates through t

		char t_j; // jth character of t

		int cost; // cost

		for(i = 0; i <= n; i++) {
			p[i] = i;
		}

		for(j = 1; j <= m; j++) {
			t_j = t.charAt(j - 1);
			d[0] = j;

			for(i = 1; i <= n; i++) {
				cost = s.charAt(i - 1) == t_j ? 0 : 1;
				//-- minimum of cell to the left+1, to the top+1, diagonally left and up +cost
				d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
			}

			//-- copy current distance counts to 'previous row' distance counts
			_d = p;
			p = d;
			d = _d;
		}

		//-- our last action in the above loop was to switch d and p, so p now
		//-- actually has the most recent cost counts
		return p[n];
	}

	/**
	 * Returns T if the string starts with the specified string, while ignoring
	 * case.
	 * @param st		the string whose start is to be checked
	 * @param with		the start string
	 * @return
	 */
	static public boolean strStartsWithIgnoreCase(final String st, final String with) {
		if(st.length() < with.length())
			return false;

		String p = st.substring(0, with.length());
		return with.equalsIgnoreCase(p);
	}

	/**
	 * Returns T if the string ends with the specified string, while ignoring
	 * case.
	 * @param st		the string whose end is to be checked
	 * @param with		the end string
	 * @return
	 */
	static public boolean strEndsWithIgnoreCase(final String st, final String with) {
		if(st.length() < with.length())
			return false;

		int l = st.length();
		String p = st.substring(l - with.length(), l);
		return with.equalsIgnoreCase(p);
	}


	/**
	 * Tries to locate a substring in a string while ignoring case.
	 * @param txt
	 * @param match
	 * @return
	 */
	static public int strIndexOfIgnoreCase(final String txt, final String match) {
		int lm = match.length();
		int sl = txt.length();
		if(lm > sl || lm == 0)
			return -1; // match > string

		//-- Find a 1st char then start the matcher
		char mc = Character.toLowerCase(match.charAt(0));
		int et = sl - lm;
		for(int i = 0; i <= et; i++) {
			char c = txt.charAt(i);
			if(c == mc || Character.toLowerCase(c) == mc) {
				//-- Try to match this segment, and return if OK
				int j = 1;
				int k = i + 1;
				for(;;) {
					if(j >= lm)
						return i; // Reached the end -> match

					char c1 = txt.charAt(k);
					char c2 = match.charAt(j);
					if(c1 != c2 && Character.toLowerCase(c1) != Character.toLowerCase(c2))
						break;
					k++;
					j++;
				}
			}
		}

		return -1;
	}


	/**
	 *	Returns a number in the specified base, and with the specified #of
	 *	positions. If the number is too large for the #positions then the
	 *	high values are cut off.
	 */
	static public String intToStr(final int val, final int radix, final int npos) {
		String v = "000000000000" + Integer.toString(val, radix);

		return v.substring(v.length() - npos, v.length());
	}

	/**
	 * Converts the integer to a string with a fixed length, adding leading zeroes
	 * if needed.
	 * @param sb
	 * @param val
	     * @param radix
	     * @param len
	     */
	static public void strAddIntFixed(final Appendable sb, final int val, final int radix, final int len) {
		try {
			String iv = Integer.toString(val, radix);
			int l = iv.length();
			if(l > len) {
				for(int i = l - len; i < l; i++)
					sb.append(iv.charAt(i));
				return;
			}
			while(l < len) {
				sb.append('0');
				l++;
			}
			sb.append(iv);
		} catch(IOException x) {
			throw new RuntimeException(x);
		}
	}


	/**
	 * Returns a properly formatted commad string for a number [english only].
	 * @param val
	 * @return
	 */
	static public String strCommad(final long val) {
		String v = Long.toString(val);
		StringBuffer sb = new StringBuffer(30);
		int pos = (v.length() % 3) + 1;
		if(pos == 0)
			pos = 3;
		for(int i = 0; i < v.length(); i++) {
			pos--;
			if(pos == 0) {
				if(i > 0)
					sb.append(',');
				pos = 3;
			}
			sb.append(v.charAt(i));
		}
		return sb.toString();
	}


	static private final long	DAYS	= 24 * 60 * 60;

	static private final long	HOURS	= 60 * 60;


	static public String strDuration(long dlt) {
		StringBuffer sb = new StringBuffer();

		if(dlt >= DAYS) {
			sb.append(Long.toString(dlt / DAYS));
			sb.append("D ");
			dlt %= DAYS;
		}
		if(dlt >= HOURS) {
			sb.append(Long.toString(dlt / HOURS));
			sb.append("u ");
			dlt %= HOURS;
		}
		if(dlt >= 60) {
			sb.append(Long.toString(dlt / 60));
			sb.append("min ");
			dlt %= 60;
		}
		sb.append(Long.toString(dlt));
		sb.append("sec");
		return sb.toString();
	}

	static public String strDurationMillis(long dlt) {
		StringBuffer sb = new StringBuffer();

		int millis = (int) (dlt % 1000); // Get milliseconds,
		dlt /= 1000; // Now in seconds,

		boolean sp = false;
		if(dlt >= DAYS) {
			sb.append(dlt / DAYS);
			sb.append("D");
			dlt %= DAYS;
			sp = true;
		}
		if(dlt >= HOURS) {
			long v = dlt / HOURS;
			if(v != 0) {
				if(sp)
					sb.append(' ');
				sb.append(v);
				sb.append("u");
				sp = true;
			}
			dlt %= HOURS;
		}
		if(dlt >= 60) {
			long v = dlt / 60;
			if(v != 0) {
				if(sp)
					sb.append(' ');
				sb.append(v);
				sb.append("m");
				sp = true;
			}
			dlt %= 60;
		}
		if(dlt != 0) {
			if(sp)
				sb.append(' ');
			sb.append(dlt);
			sb.append("s");
			sp = true;
		}
		if(millis != 0) {
			if(sp)
				sb.append(' ');
			sb.append(millis);
			sb.append("ms");
		}
		return sb.toString();
	}


	static public String strTrunc(final String s, final int len) {
		if(s == null)
			return null;
		if(s.length() <= len)
			return s;
		return s.substring(0, len);
	}


	/**
	 *	Returns a string of hex bytes for a given thing.
	 *	@parameter	ar: the array that data needs to be gotten from
	     *      @parameter      bi: The initial index where the 1st byte is in the array
	     *      @parameter      nc:     The number of bytes to decode.
	     */
	static public void arrayToHexStr(final Appendable sb, final byte[] ar, final int bi, final int nc, final boolean fillout) throws IOException {
		int i, ei;

		ei = nc + bi;
		for(i = bi; i < ei; i++) {
			if(i >= ar.length) // Past end of array?
			{
				if(!fillout) // No need to add spaces?
					return; // Then return the result
				sb.append("   "); // Add 3 spaces.
			} else {
				sb.append(intToStr((ar[i] & 0xff), 16, 2));
				sb.append(' ');
			}
		}
	}


	/**
	 *      Returns a string containing only printable chars for the given bytes.
	 */
	static public void arrayToAsciiStr(final Appendable sb, final byte[] ar, final int bi, final int nc) throws IOException {
		int i, ei;

		ei = nc + bi;
		for(i = bi; i < ei && i < ar.length; i++) {
			byte c = ar[i];
			if(c >= 32 && c < 255)
				sb.append((char) c);
			else
				sb.append('.');
		}
	}


	/**
	     *      Returns a dumpstring containing the offset, the hex bytes, and the ascii
	     *      representation of a given dump buffer.
	     */
	static public void arrayToDumpLine(final Appendable sb, final byte[] ar, final int bi, final int nc) throws IOException {
		sb.append(intToStr(bi, 16, 4)); // Buffer offset
		sb.append(": ");
		arrayToHexStr(sb, ar, bi, nc, true); // Get filled-out string of nc bytes in HEX
		sb.append("  ");
		arrayToAsciiStr(sb, ar, bi, nc);
	}

	/**
	 * Dump the data as a formatted multiline buffer: like
	 * <pre>
	 * 	0000 ff ef aa bb cc dd 99 88  ff ef aa bb cc dd 99 88 sgdfkajse
	 * </pre>
	 * @param sb
	 * @param ar
	 * @param off
	 * @param len
	 * @throws IOException
	 */
	static public void dumpData(final Appendable sb, final byte[] ar, final int off, final int len) throws IOException {
		int ix = off;
		int left = len;
		while(left > 0) {
			StringTool.arrayToDumpLine(sb, ar, ix, left > 16 ? 16 : left);
			sb.append("\n");
			left -= 16;
			ix += 16;
		}
	}

	static public void printHex(final PrintWriter pw, final byte[] arr) {
		printHex(pw, arr, 0, arr.length);
	}


	static public void printHex(final PrintWriter pw, final byte[] arr, final int start, final int end) {
		//-- Dump the data as a hex string, completely.
		for(int i = start; i < end; i++) {
			pw.print(intToStr((arr[i]) & 0xff, 16, 2));
		}
		pw.println("");
	}

	static public void printHex(final PrintStream pw, final byte[] arr) {
		printHex(pw, arr, 0, arr.length);
	}


	static public void printHex(final PrintStream pw, final byte[] arr, final int start, final int end) {
		//-- Dump the data as a hex string, completely.
		for(int i = start; i < end; i++) {
			pw.print(intToStr((arr[i]) & 0xff, 16, 2));
		}
		pw.println("");
	}

	/**
	 * Converts the byte array passed to a hex string. This converts the
	 * region [start..end&gt;.
	 * @param arr		the array containing the data to convert
	 * @param start		the first byte in the array to convert
	 * @param end		the exclusive end of the region to convert
	 * @return
	 */
	static public String toHex(final byte[] arr, final int start, final int end) {
		StringBuffer sb = new StringBuffer(arr.length * 2);

		for(int i = start; i < end; i++)
			sb.append(intToStr((arr[i]) & 0xff, 16, 2));

		return sb.toString();
	}

	/**
	 * Converts the byte array to a hex string.
	 * @param arr
	 * @return
	 */
	static public String toHex(final byte[] arr) {
		return toHex(arr, 0, arr.length);
	}

	/**
	 * Converts the byte array passed to a hex string. This converts the
	 * region [start..end&gt;.
	 * @param arr		the array containing the data to convert
	 * @param start		the first byte in the array to convert
	 * @param end		the exclusive end of the region to convert
	 * @return
	 */
	static public String toHexSp(final byte[] arr, final int start, final int end) {
		StringBuffer sb = new StringBuffer(arr.length * 2);

		for(int i = start; i < end; i++) {
			sb.append(intToStr((arr[i]) & 0xff, 16, 2));
			sb.append(' ');
		}
		return sb.toString();
	}

	/**
	 * Converts the byte array to a hex string.
	 * @param arr
	 * @return
	 */
	static public String toHexSp(final byte[] arr) {
		return toHexSp(arr, 0, arr.length);
	}


	static private int decc(final char c) throws Exception {
		int rv = Character.toUpperCase(c);
		if(rv >= 'A')
			rv = 10 + (rv - 'A');
		else
			rv -= '0';
		if(rv < 0 || rv > 15)
			throw new Exception("invalid key string (not hex)");
		return rv;
	}

	/**
	 * Decodes a hex string into a byte array.
	 * @param s		the string
	 * @return the decoded array
	 * @throws Exception if the array is malformed.
	 */
	static public byte[] fromHex(final String s) throws Exception {
		int l = s.length();
		if(l % 2 == 1)
			throw new Exception("fromHex: input string has odd length");
		l /= 2;
		byte[] ar = new byte[l];

		int six = 0;
		for(int i = 0; i < l; i++) {
			int c1 = decc(s.charAt(six++));
			int c2 = decc(s.charAt(six++));
			c1 = (c1 << 4) + c2;
			ar[i] = (byte) c1;
		}
		return ar;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Finding files and constructing search paths.		*/
	/*--------------------------------------------------------------*/
	/**
	 *	Returns the complete filename of the first file that is found along the
	 *	path specified.
	 */
	static public File findFileOnPath(final String fname, final String path) {
		int six;
		StringBuffer sb = new StringBuffer(64);

		//		System.out.println("findFileOnPath: find "+fname+" on "+path);

		six = 0;
		while(six < path.length()) {
			sb.setLength(0);
			int eix = path.indexOf(File.pathSeparatorChar, six);
			if(eix == -1)
				eix = path.length();
			String rp = path.substring(six, eix); // Get segment,
			sb.append(rp);
			if(!rp.endsWith(File.separator))
				sb.append(File.separator);
			sb.append(fname);
			File f = new File(sb.toString());

			//			System.out.println("Trying "+f.toString());

			if(f.exists())
				return f;

			//** Nonexistent- retry,
			six = eix + 1;
		}
		return null;
	}


	/**
	 *	Finds a filename along the classpath..
	 */
	static public File findFileOnEnv(final String pname, final String env) {
		String ev = System.getProperty(env);
		if(ev == null)
			return null;
		return findFileOnPath(pname, ev);
	}


	/**
	 *	Returns the extension of a file. The extension includes the . If no
	 *  extension is present then the empty string is returned ("").
	 * @deprecated
	 * @see FileTool.getFileExtension(String)
	 */
	@Deprecated
	static public String getFileExtension(final String fn) {
		int s1 = fn.lastIndexOf('/');
		int s2 = fn.lastIndexOf('\\');
		if(s2 > s1)
			s1 = s2;
		if(s1 == -1)
			s1 = 0;

		int p = fn.lastIndexOf('.');
		if(p < s1)
			return "";
		return fn.substring(p);
	}


	/**
	 *	Adds a path to the vector specified, if the path is an existing file
	 *  or directory, and it doesn't already exist in the vector.
	 */
	static public void addPathToVector(final List<String> v, String p) {
		p = p.trim();
		File f = new File(p);
		//		System.out.print("CP: Consider "+f.toString()+" - ");
		if((f.isDirectory() || f.isFile()) && f.exists()) {
			//-- No duplicates?
			for(int i = 0; i < v.size(); i++) {
				String e = v.get(i);
				if(e.equals(p)) {
					return;
				}
			}
			v.add(p);
			return;
		}
	}


	/**
	 *	Takes a search path, i.e. a list of directory/file names separated by the
	 *  system path separator and adds all files/directories specified to the
	 *  vector v, only if the pathname exists and if it is not already
	 */
	static public void addSearchPathToVector(final List<String> v, final String searchpath) {
		int six = 0;
		while(six < searchpath.length()) {
			int eix = searchpath.indexOf(File.pathSeparatorChar, six);
			if(eix == -1)
				eix = searchpath.length();
			String ss = searchpath.substring(six, eix);
			addPathToVector(v, ss);
			six = eix + 1;
		}
	}

	/**
	 *	Adds the value of a "search path environment variable" to the vector.
	 *  @see addSearchPathToVector()
	 */
	static public void addSearchEnvToVector(final List<String> v, final String envvar) {
		String ev = System.getProperty(envvar);
		if(ev == null)
			return;
		addSearchPathToVector(v, ev);
	}


	/**
	 *	Returns a string buffer containing a search path variable from the
	 *  vector passed.
	 */
	static public void makeSearchPath(final StringBuilder sb, final List<String> v) {
		for(int i = 0; i < v.size(); i++) {
			if(i > 0)
				sb.append(File.pathSeparator);
			sb.append(v.get(i));
		}
	}

	/**
	 *	Returns a string buffer containing a search path variable from the
	 *  vector passed.
	 */
	static public String makeSearchPath(final List<String> v) {
		StringBuilder sb = new StringBuilder(128);
		makeSearchPath(sb, v);
		return sb.toString();
	}

	/**
	 *	Returns a string buffer containing a search path variable from the
	 *  vector passed.
	 */
	static public void makeSearchPath(final StringBuffer sb, final String[] v) {
		for(int i = 0; i < v.length; i++) {
			if(i > 0)
				sb.append(File.pathSeparator);
			sb.append(v[i]);
		}
	}

	/**
	 *	Returns a string buffer containing a search path variable from the
	 *  vector passed.
	 */
	static public String makeSearchPath(final String v[]) {
		StringBuffer sb = new StringBuffer(128);
		makeSearchPath(sb, v);
		return sb.toString();
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Get a constant's name from a CLASS by introspection	*/
	/*--------------------------------------------------------------*/
	static public String getFinalFrom(final Class< ? > cl, final long sval) {
		return getFinalFrom(cl, sval, null);
	}

	/**
	 *	Traverses a given class and tries to find a public
	 */
	static public String getFinalFrom(final Class< ? > cl, final long sval, final String part) {
		return getFinalFrom(cl, sval, part, null);
	}

	/**
	 *	Traverses a given class and tries to find a public
	 */
	static public String getFinalFrom(final Class< ? > cl, final long sval, final String part, final String ign) {
		java.lang.reflect.Field[] far = cl.getFields();
		java.lang.reflect.Field f;

		for(int i = 0; i < far.length; i++) {
			f = far[i];
			int mod = f.getModifiers();
			if((mod & Modifier.FINAL) != 0 && (mod & Modifier.STATIC) != 0 && (mod & Modifier.PUBLIC) != 0) {
				//-- A whatever thing field. Is it a primitive?
				try {
					long val = 0;
					boolean valid = true;
					Class< ? > ty = f.getType();
					if(ty == Integer.TYPE)
						val = f.getInt(null);
					else if(ty == Long.TYPE)
						val = f.getLong(null);
					else if(ty == Byte.TYPE)
						val = f.getByte(null);
					else if(ty == Short.TYPE)
						val = f.getShort(null);
					else
						valid = false;

					if(valid) {
						if(val == sval) {
							if(ign != null && f.getName().startsWith(ign))
								continue;

							if(part == null)
								return f.getName();
							if(f.getName().toUpperCase().startsWith(part.toUpperCase()))
								return f.getName();
						}
					}
				} catch(Exception x) {}
			}
		}

		return "[? " + sval + "]";
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	URL Scanning stuff..								*/
	/*--------------------------------------------------------------*/
	//	static private String urlAIsPort(String url, int ix)
	//	{
	//		/*
	//		 *	The string is a port number if all chars between the colon and the
	//		 *	slash or eo$ are numeric.
	//		 */
	//		int	i	= ++ix;								// Past colon,
	//		while(i < url.length())
	//		{
	//			char	c = url.charAt(i);				// Get a char,
	//			if(c == '/') break;						// For slash all ok,
	//			if(c < '0' || c > '9') return null;		// No digit -> no port#
	//			i++;
	//		}
	//
	//		//** Loop ends; what was in it is numeric or empty.
	//		if(i == ix) return "";						// Empty :
	//		return url.substring(ix, i);
	//	}
	//
	/**
	 *	Returns the last element (document name?) from the url passed.
	 */
	static public String urlLastPart(final String url) {
		int sp = url.lastIndexOf('/'); // Find last slash,
		if(sp == -1)
			return url; // No slashes -> entire URL,
		return url.substring(sp + 1);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Unhtmlize											*/
	/*--------------------------------------------------------------*/
	/**
	 *	Enter with a string; it returns the same string but replaces HTML
	 *  recognised characters with their &..; equivalent. This allows parts of
	 *  HTML to be rendered neatly.
	 */
	static public String htmlStringize(final String is) {
		StringBuilder sb = new StringBuilder(is.length() + 20);
		htmlStringize(sb, is);
		return sb.toString();
	}

	/**
	 *	Enter with a string; it returns the same string but replaces HTML
	 *  recognised characters with their &..; equivalent. This allows parts of
	 *  HTML to be rendered neatly.
	 */
	static public void htmlStringize(final StringBuilder sb, final String is) {
		int len = is.length();
		for(int i = 0; i < len; i++) {
			char c = is.charAt(i);
			switch(c){
				default:
					sb.append(c);
					break;
				case '\n':
					sb.append("<br>");
					break;
				case '>':
					sb.append("&gt;");
					break;
				case '<':
					sb.append("&lt;");
					break;
				case '&':
					sb.append("&amp;");
					break;
			}
		}
	}

	/**
	 *	Enter with a string; it returns the same string but replaces HTML
	 *  recognised characters with their &..; equivalent. This allows parts of
	 *  HTML to be rendered neatly.
	 */
	static public void htmlStringize(final Appendable o, final String is) throws Exception {
		StringBuffer sb = new StringBuffer(256);
		int len = is.length();
		for(int i = 0; i < len; i++) {
			char c = is.charAt(i);
			switch(c){
				default:
					sb.append(c);
					if(sb.length() >= 256) {
						o.append(sb.toString());
						sb.setLength(0);
					}
					break;

				case '\n':
					if(sb.length() > 0)
						o.append(sb.toString());
					sb.setLength(0);
					o.append("<br>");
					break;
				case '>':
					if(sb.length() > 0)
						o.append(sb.toString());
					sb.setLength(0);
					o.append("&gt;");
					break;
				case '<':
					if(sb.length() > 0)
						o.append(sb.toString());
					sb.setLength(0);
					o.append("&lt;");
					break;
				case '&':
					if(sb.length() > 0)
						o.append(sb.toString());
					sb.setLength(0);
					o.append("&amp;");
					break;
			}
		}
		if(sb.length() > 0)
			o.append(sb.toString());
	}

	/**
	 *	Enter with a string; it returns the same string but replaces HTML
	 *  recognised characters with their &..; equivalent. This allows parts of
	 *  HTML to be rendered neatly.
	 */
	static public String xmlStringize(final String is) {
		if(is == null)
			return "null";
		StringBuffer sb = new StringBuffer(is.length() + 20);
		xmlStringize(sb, is);
		return sb.toString();
	}

	/**
	 *	Enter with a string; it returns the same string but replaces HTML
	 *  recognised characters with their &..; equivalent. This allows parts of
	 *  HTML to be rendered neatly.
	 */
	static public void xmlStringize(final StringBuffer sb, final String is) {
		if(is == null) {
			sb.append("null");
			return;
		}
		for(int i = 0; i < is.length(); i++) {
			char c = is.charAt(i);
			switch(c){
				case '>':
					sb.append("&gt;");
					break;
				case '<':
					sb.append("&lt;");
					break;
				case '&':
					sb.append("&amp;");
					break;
				default:
					sb.append(c);
					break;
			}
		}
	}


	/**
	 * Scans the input string for entities and replaces all entities that
	 * are actually found with their Unicode character code. The resulting
	 * string is appended to the string buffer.
	 * WARNING: this does not take HTML tah parameters into consideration!
	 * @param sb		the buffer to append the string to
	 * @param str		the string to copy while replacing entities.
	 */
	static public void entitiesToUnicode(final Appendable sb, final String str, final boolean ignoremarkers) throws IOException {
		int ix = 0;
		while(ix < str.length()) {
			int epos = str.indexOf('&', ix); // Find next start for entity,
			if(epos == -1) {
				sb.append(str.substring(ix, str.length())); // Add last segment,
				return;
			}

			//-- First copy all data up to the &
			sb.append(str.substring(ix, epos));
			ix = epos + 1;

			epos = str.indexOf(';', epos + 1); // Find terminating ';'
			if(epos == -1) // Missing ; means 'no entity'
			{
				sb.append('&');
			} else {
				String es = str.substring(ix, epos); // Get complete entity name
				ix = epos + 1;
				int ec = entityToUnicode(es); // Translate code;
				if(ignoremarkers && isMarker(ec)) {
					sb.append('&');
					sb.append(es);
					sb.append(';');
				} else if(ec == -1) {
					//-- Undefined code- leave
					sb.append("&#");
					sb.append(Integer.toString(ec));
					sb.append(';');
				} else {
					sb.append((char) ec);
				}
			}
		}
	}


	static private boolean isMarker(final int ec) {
		return ec == '<' || ec == '>' || ec == '&';
	}

	/**
	* Replaces all non-ascii stuff with their entities. Also replaces &lt;, &gt; and &amp;.
	* @param sb
	* @param str
	*/
	static public void unicodeToEntities(final StringBuffer sb, final String str) {
		int se = str.length();
		for(int i = 0; i < se; i++) {
			char c = str.charAt(i);
			if(c < 32 || c > 127 || c == '&' || c == '<' || c == '>') {
				String en = HtmlEntityTables.findName(c);
				if(en == null)
					sb.append(c);
				else {
					sb.append("&");
					sb.append(en);
					sb.append(';');
				}
			} else
				sb.append(c);
		}
	}

	/**
	 * Translates an entity name to unicode. The entity can also be a numeral.
	 * @param ename
	 * @return
	 */
	static public int entityToUnicode(final String ename) {
		if(ename.startsWith("#")) // Decimal code?
		{
			try {
				return Integer.parseInt(ename.substring(1));
			} catch(Exception x) {
				return -1;
			}
		}

		//-- Find in entity tables.
		return HtmlEntityTables.findCode(ename);
	}

	static public String strToJavascriptString(final String cs, final boolean dblquote) {
		if(cs == null)
			return null;
		StringBuilder sb = new StringBuilder(cs.length() + 10);
		try {
			strToJavascriptString(sb, cs, dblquote);
			return sb.toString();
		} catch(IOException x) {
			throw new RuntimeException(x.toString(), x);
		}
	}

	static public void strToJavascriptString(final Appendable w, final String cs, final boolean dblquote) throws IOException {
		int len = cs.length();
		//		if(len == 0)					jal 20090225 WTF!?!! Empty strings MUST be ""!!!!!
		//			return;
		int ix = 0;
		char quotechar;
		quotechar = dblquote ? '\"' : '\'';
		w.append(quotechar);

		while(ix < len) {
			//-- Collect a run
			int runstart = ix;
			char c = 0;
			while(ix < len) {
				c = cs.charAt(ix);
				if(c < 32 || c == '\'' || c == '\\' || c == quotechar)
					break;
				ix++;
			}
			if(ix > runstart) {
				w.append(cs, runstart, ix);
				if(ix >= len)
					break;
			}
			ix++;
			switch(c){
				default:
					w.append("\\u"); // Unicode escape
					w.append(StringTool.intToStr(c & 0xffff, 16, 4));
					break;
				case '\n':
					w.append("\\n");
					break;
				case '\b':
					w.append("\\b");
					break;
				case '\f':
					w.append("\\f");
					break;
				case '\r':
					w.append("\\r");
					break;
				case '\t':
					w.append("\\t");
					break;
				case '\'':
					w.append("\\'");
					break;
				case '\"':
					w.append("\\\"");
					break;
				case '\\':
					w.append("\\\\");
					break;
			}
		}
		w.append(quotechar);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Database field translation...						*/
	/*--------------------------------------------------------------*/

	/**
	 *	Returns a boolean value from some database field. This returns T if
	 *  the string contains T, Y, 1.
	 */
	static public boolean dbGetBool(final String fv) {
		if(fv == null)
			return false;
		if(fv.length() == 0)
			return false;
		char c = Character.toUpperCase(fv.charAt(0));
		if(c == 'Y' || c == 'T')
			return true;
		if(c == '0')
			return false;
		if(Character.isDigit(c))
			return true;
		return false;
	}

	/**
	 *	Returns a char(1) value to store in a database for booleans.
	 */
	static public String dbSetBool(final boolean v) {
		return v ? "T" : "F";
	}


	static public void main(final String[] args) {
		try {
			//			int	res	= compareStrings(args[0], args[1]);
			//			System.out.println("Result is "+res);
			//			System.out.println(strCommad(0));
			//			System.out.println(strCommad(10));
			//			System.out.println(strCommad(112));
			//			System.out.println(strCommad(1234));
			//			System.out.println(strCommad(10234));
			//			System.out.println(strCommad(101221));
			//			System.out.println(strCommad(1065432));
			//
			//			StringWriter	sw = new StringWriter();
			//			strToJavascriptString(sw, "Dit is \"de beste\" oplossing\nzij de beste 'man'; ze kost \u20a3 20,00", false);
			//			System.out.println(sw.getBuffer());

			String s = strToJavascriptString("accu\\'s vervangen", false);
			System.out.println(s);

		} catch(Throwable x) {
			System.out.println("Fatal: " + x.toString());
			x.printStackTrace();

		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Get "REAL" environment variables.					*/
	/*--------------------------------------------------------------*/
	/**
	 *	Returns the list of environment variables of the supported OS's. Because
	 *  in their infinite wizdom the Java builders deprecated the use of this
	 *  we need to do something complex..
	 */
	static public List<String> getEnvironment() {
		try {
			String opsys = System.getProperty("os.name").toLowerCase();
			//		System.out.println(OS);
			Process p = null;
			if(opsys.indexOf("windows 9") > -1)
				p = Runtime.getRuntime().exec("command.com /c set");
			else if((opsys.indexOf("nt") > -1) || (opsys.indexOf("windows 2000") > -1))
				p = Runtime.getRuntime().exec("cmd.exe /c set");
			else if(opsys.indexOf("unix") > -1 || opsys.indexOf("Linux") > -1 || File.separatorChar == '/')
				p = Runtime.getRuntime().exec("env");

			//-- Take the result..
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			List<String> v = new ArrayList<String>();
			String line;
			while((line = br.readLine()) != null) {
				v.add(line);
			}
			p.waitFor();
			return v;
		} catch(Exception x) {
			x.printStackTrace();
		}
		return null;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Base 64 encoding/decoding (rfc2045)					*/
	/*--------------------------------------------------------------*/
	// rfc-2045: Base64 Alphabet
	static private final byte[]	BASE64MAP	= {(byte) 'A', (byte) 'B', (byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F', (byte) 'G', (byte) 'H', (byte) 'I', (byte) 'J', (byte) 'K', (byte) 'L',
		(byte) 'M', (byte) 'N', (byte) 'O', (byte) 'P', (byte) 'Q', (byte) 'R', (byte) 'S', (byte) 'T', (byte) 'U', (byte) 'V', (byte) 'W', (byte) 'X', (byte) 'Y', (byte) 'Z', (byte) 'a', (byte) 'b',
		(byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f', (byte) 'g', (byte) 'h', (byte) 'i', (byte) 'j', (byte) 'k', (byte) 'l', (byte) 'm', (byte) 'n', (byte) 'o', (byte) 'p', (byte) 'q', (byte) 'r',
		(byte) 's', (byte) 't', (byte) 'u', (byte) 'v', (byte) 'w', (byte) 'x', (byte) 'y', (byte) 'z', (byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4', (byte) '5', (byte) '6', (byte) '7',
		(byte) '8', (byte) '9', (byte) '+', (byte) '/'};

	static private final byte[]	BASE64DECMAP;

	static {
		BASE64DECMAP = new byte[128];
		for(int ix = 0; ix < BASE64MAP.length; ix++)
			BASE64DECMAP[BASE64MAP[ix]] = (byte) ix;
	}

	static public final byte[] getBase64Map() {
		return BASE64MAP;
	}

	/**
	 * This method encodes the given string using the base64-encoding
	 * specified in RFC-2045 (Section 6.8). It's used for example in the
	 * "Basic" authorization scheme.
	 *
	 * @param  str the string
	 * @return the base64-encoded <var>str</var>
	 */
	public final static String encodeBase64(final String str) {
		if(str == null)
			return null;
		//		byte data[] = new byte[str.length()];
		byte[] data = str.getBytes();

		//		str.getBytes(0, str.length(), data, 0);
		return new String(encodeBase64(data));
		//		return new String(base64Encode(data), 0);
	}

	/**
	 * This method encodes the given byte[] using the base64-encoding
	 * specified in RFC-2045 (Section 6.8).
	 *
	 * @param  data the data
	 * @return the base64-encoded <var>data</var>
	 */
	public final static byte[] encodeBase64(final byte[] data) {
		if(data == null)
			return null;
		int sidx, didx;
		byte dest[] = new byte[((data.length + 2) / 3) * 4];

		// 3-byte to 4-byte conversion + 0-63 to ascii printable conversion
		for(sidx = 0, didx = 0; sidx < data.length - 2; sidx += 3) {
			dest[didx++] = BASE64MAP[(data[sidx] >>> 2) & 077];
			dest[didx++] = BASE64MAP[(data[sidx + 1] >>> 4) & 017 | (data[sidx] << 4) & 077];
			dest[didx++] = BASE64MAP[(data[sidx + 2] >>> 6) & 003 | (data[sidx + 1] << 2) & 077];
			dest[didx++] = BASE64MAP[data[sidx + 2] & 077];
		}
		if(sidx < data.length) {
			dest[didx++] = BASE64MAP[(data[sidx] >>> 2) & 077];
			if(sidx < data.length - 1) {
				dest[didx++] = BASE64MAP[(data[sidx + 1] >>> 4) & 017 | (data[sidx] << 4) & 077];
				dest[didx++] = BASE64MAP[(data[sidx + 1] << 2) & 077];
			} else
				dest[didx++] = BASE64MAP[(data[sidx] << 4) & 077];
		}
		// add padding
		for(; didx < dest.length; didx++)
			dest[didx] = (byte) '=';
		return dest;
	}


	/**
	 * This method decodes the given string using the base64-encoding
	 * specified in RFC-2045 (Section 6.8).
	 *
	 * @param  str the base64-encoded string.
	 * @return the decoded <var>str</var>.
	 */
	public final static String decodeBase64ToString(final String str) {
		if(str == null)
			return null;
		//		byte data[] = new byte[str.length()];
		//		str.getBytes(0, str.length(), data, 0);
		byte[] data = str.getBytes();
		return new String(decodeBase64(data));
	}

	/**
	 * This method decodes the given string using the base64-encoding
	 * specified in RFC-2045 (Section 6.8).
	 *
	 * @param  str the base64-encoded string.
	 * @return the decoded <var>str</var>.
	 */
	public final static byte[] decodeBase64(final String str) {
		if(str == null)
			return null;
		//		byte data[] = new byte[str.length()];
		//		str.getBytes(0, str.length(), data, 0);
		byte[] data = str.getBytes();
		return decodeBase64(data);
	}

	/**
	 * This method decodes the given byte[] using the base64-encoding
	 * specified in RFC-2045 (Section 6.8).
	 *
	 * @param  data the base64-encoded data.
	 * @return the decoded <var>data</var>.
	 */
	public final static byte[] decodeBase64(final byte[] data) {
		if(data == null || data.length == 0)
			return null;
		int tail = data.length;
		while(data[tail - 1] == '=')
			tail--;
		byte dest[] = new byte[tail - data.length / 4];

		// ascii printable to 0-63 conversion
		for(int idx = 0; idx < data.length; idx++)
			data[idx] = BASE64DECMAP[data[idx]];

		// 4-byte to 3-byte conversion
		int sidx, didx;
		for(sidx = 0, didx = 0; didx < dest.length - 2; sidx += 4, didx += 3) {
			dest[didx] = (byte) (((data[sidx] << 2) & 255) | ((data[sidx + 1] >>> 4) & 003));
			dest[didx + 1] = (byte) (((data[sidx + 1] << 4) & 255) | ((data[sidx + 2] >>> 2) & 017));
			dest[didx + 2] = (byte) (((data[sidx + 2] << 6) & 255) | (data[sidx + 3] & 077));
		}
		if(didx < dest.length)
			dest[didx] = (byte) (((data[sidx] << 2) & 255) | ((data[sidx + 1] >>> 4) & 003));
		if(++didx < dest.length)
			dest[didx] = (byte) (((data[sidx + 1] << 4) & 255) | ((data[sidx + 2] >>> 2) & 017));
		return dest;
	}

	static public final String encodeBase64ToString(final byte[] data) {
		try {
			return new String(StringTool.encodeBase64(data), "utf8");
		} catch(Exception x) {
			//-- cannot happen.
			x.printStackTrace();
			throw new RuntimeException("bad encoding!?", x);
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Exception stuff..									*/
	/*--------------------------------------------------------------*/
	static public String strStacktrace(final Throwable t) {
		StringWriter sw = new StringWriter(1024);
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		pw.close();
		return sw.getBuffer().toString();
	}

	static public void strStacktrace(final Appendable sb, final Throwable t) {
		try {
			sb.append(strStacktrace(t));
		} catch(IOException x) // Sillyness of sillynesses
		{
			x.printStackTrace();
		}
	}

	static private boolean inSkipSet(final String[] set, final String name) {
		for(String s : set) {
			if(name.startsWith(s))
				return true;
		}
		return false;
	}

	/**
	 * Report a filtered location stack trace, where the start of the stack trace and the end can be removed.
	 * @param sb
	 * @param t
	 * @param skipbefore
	 * @param skipafter
	 */
	static public void strStacktraceFiltered(final Appendable sb, final Throwable t, String[] skipbefore, String[] skipafter, int linelimit) {
		StackTraceElement[] se = t.getStackTrace();

		//-- Find the first part to log,
		int len = se.length;
		int ix = 0;
		while(ix < len) {
			String m = se[ix].getClassName();
			if(!inSkipSet(skipbefore, m))
				break;
			ix++;
		}
		int sx = ix++; // First item not in head skipset; always logged.

		while(ix < len) {
			String m = se[ix].getClassName();
			if(inSkipSet(skipafter, m))
				break;
			ix++;
		}
		int ex = ix; // End bound, exclusive
		if(linelimit > 0) {
			if(ex - sx > linelimit)
				ex = sx + linelimit;
		}
		for(int i = sx; i < ex; i++) {
			try {
				sb.append("    " + se[i].toString() + "\n");
			} catch(IOException x) {
				throw new RuntimeException(x); // Sigh
			}
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	URL normalization and concatenation.				*/
	/*--------------------------------------------------------------*/
	/**
	 * <p>Called when generate() is called with a string. This must decode the
	 * string into a key object that can be used by the decodeInputURL key to
	 * determine a resource provider and a provider-relative key.</p>
	 * <p>This default implementation assumes that the key is to be a normal
	 * URL string, and uses path semantics to create the actual key from the
	 * string passed: if it contains a host name it is stripped; if it is
	 * relative then the complete path is appended.</p>
	 */
	static public final String normalizeURL(final String current, final String tpl) throws Exception {
		return normalizeUndot(normalizeConcat(current, tpl));
	}

	/**
	 * <p>Called when generate() is called with a string. This must decode the
	 * string into a key object that can be used by the decodeInputURL key to
	 * determine a resource provider and a provider-relative key.</p>
	 * <p>This default implementation assumes that the key is to be a normal
	 * URL string, and uses path semantics to create the actual key from the
	 * string passed: if it contains a host name it is stripped; if it is
	 * relative then the complete path is appended.</p>
	 */
	static public final String normalizeConcat(final String current, final String tpl) throws Exception {
		if(tpl == null || tpl.length() == 0)
			return tpl;

		if(tpl.charAt(0) == '/') // Is absolute site-relative?
			return tpl; // Yes-> use as-is
		if(tpl.length() > 5) {
			if(tpl.substring(0, 5).toLowerCase().equals("http:")) {
				int pos = tpl.indexOf('/', 7);
				if(pos == -1)
					throw new Exception("Cannot decode URL '" + tpl + "': missing / after host part");

				return tpl.substring(pos);
			}
		}

		//-- This path is relative to the "current" url... So - get that,
		int lix = current.lastIndexOf('/'); // Find last /
		if(lix == -1)
			return "/" + tpl; // Use root-based document,

		return current.substring(0, lix + 1) + tpl; // Make relative to old URL.
	}


	/**
	 * Takes an input URL and handles all '.' and '..' replacements. Any '.'
	 * sublevel is replaced by nothing (removed completely); any '..' is
	 * replaced by removing the 'upper' level and replacing that with the rest
	 * of the string.
	 * @param ins		the input URL
	 * @return			the output URL.
	 */
	static public final String normalizeUndot(final String ins) {
		//-- Get all chars in the source.
		int len = ins.length();
		char[] car = new char[len];
		ins.getChars(0, ins.length(), car, 0);

		//-- Traverse: find all /./, /../, or /..
		int six = 0;
		int dix = 0;

		while(six < len) {
			char c = car[six];
			if(c == '/') // Possible new level?
			{
				int dotlevel = 0;
				int tix = six + 1;
				if(tix < len) // Fits 1le dot?
				{
					if(car[tix] == '.') {
						dotlevel = 1; // Can be level 1
						tix++;
						if(tix < len) // There's more,
						{
							char tc = car[tix];
							if(tc == '/')
								; // Was /./ -> dotlevel 1
							else if(tc == '.') {
								dotlevel = 2;
								tix++;
								if(tix < len) {
									//-- To match we MUST have a / now!
									if(car[tix] != '/')
										dotlevel = 0;
								}
							} else {
								//-- It was .xxx
								dotlevel = 0;
							}
						}
					}
				}

				//-- handle depending on dotlevel,
				if(dotlevel == 0) {
					car[dix++] = car[six++]; // Copy /, rest follows,
				} else if(dotlevel == 1) // Was /. -> remove,
				{
					six += 2; // Get past /. without copy.
				} else if(dotlevel == 2) {
					//-- Move UPWARD by scanning for last /...
					while(dix > 0 && car[dix - 1] != '/')
						dix--; // Scan past last / or to start of $
					six += 3; // Scan to 1st
					if(dix > 0)
						dix--; // Copy over slash, if applicable
					else
						six++; // At start, and start didn't begin with /
				} else
					throw new IllegalStateException("?? Dotlevel bad!?");
			} else
				car[dix++] = car[six++]; // Just copy src to dest
		}

		if(dix == six)
			return ins;
		return new String(car, 0, dix);
	}

	static public int strToInt(final String v, final int defval) {
		try {
			return Integer.parseInt(v);
		} catch(Exception x) {}
		return defval;
	}

	static public long strToLong(final String v, final long defval) {
		try {
			return Long.parseLong(v);
		} catch(Exception x) {}
		return defval;
	}

	static public long strToLong(final String v, int six, final int eix, final int defval) {
		if(six >= eix)
			return defval;
		long val = 0;
		while(six < eix) {
			char c = v.charAt(six++);
			if(c < '0' || c > '9')
				return defval;
			val = val * 10 + (c - '0');
		}
		return val;
	}


	/**
	 * Find the 1st part of the path passed, i.e. the part before the first /.
	 * If the path contains no / it returns the full path.
	 *
	 * @param s
	 * @return
	 */
	static public String getNextPathComponent(final int ix, final String s, final boolean includeslash) {
		int pos = s.indexOf('/', ix);
		if(pos == -1)
			return s.substring(ix);
		if(includeslash)
			pos++;
		return s.substring(ix, pos);
	}

	static public String getNextPathComponent(final String s, final boolean includeslash) {
		return getNextPathComponent(0, s, includeslash);
	}

	static public boolean equalStringList(final List<String> inl, final List<String> al, final boolean caseindependent) {
		if(inl == al) // Same reference->equal
			return true;
		if((al == null && inl != null) || (inl == null && al != null))
			return false; // One is null the other isn't

		//-- Actual arrays exist..
		if(al.size() != inl.size()) // Size differs-> not equal
			return false;

		//-- Compare all strings, in order.
		for(int i = al.size(); --i >= 0;) {
			String a = al.get(i);
			String b = inl.get(i);
			if(caseindependent) {
				if(!a.equalsIgnoreCase(b))
					return false;
			} else {
				if(!a.equals(b))
					return false;
			}
		}
		return true;
	}

	/**
	 * Workaround for Java bug delivering file:// instead of file:/// for
	 * file.toURL().toString().
	 *
	 * @param f
	 * @return
	 */
	static public String makeURL(final File f) {
		String s = f.getAbsolutePath().toString();
		if(s.startsWith("/"))
			return "file://" + s;
		else
			return "file:///" + s;
	}

	static public String fixFileURL(final String fileurl) {
		if(fileurl.length() < 8)
			return fileurl;
		String s = fileurl.substring(0, 5);
		if(!s.equalsIgnoreCase("file:"))
			return fileurl;

		//-- File URL MUST start with file://, followed by c:/ or so for Windows or /xxx for linux
		char c1 = fileurl.charAt(5);
		char c2 = fileurl.charAt(6);
		char c3 = fileurl.charAt(7);
		if(c1 == '/' && c2 == '/' && c3 == '/')
			return fileurl;
		if(c1 == '/' && c2 == '/') {
			if(fileurl.length() > 8 && fileurl.charAt(8) == ':') // Is c: format
				return fileurl;
		}

		//-- Format is bad- fix.
		StringBuffer sb = new StringBuffer(fileurl.length() + 5);
		sb.append("file://");
		int ix = 5; // To possible 1st slash
		while(ix < fileurl.length() && fileurl.charAt(ix) == '/')
			ix++; // Skip all slashes

		//-- Drive letter or root slash?
		if(ix + 1 < fileurl.length()) {
			if(fileurl.charAt(ix + 1) == ':') // Drive letter?
			{
				sb.append(fileurl.substring(ix));
				return sb.toString();
			}
			sb.append('/');
			sb.append(fileurl.substring(ix));
			return sb.toString();
		}

		//-- There's 0 or 1  char past the slashes.
		if(ix >= fileurl.length())
			return fileurl;
		sb.append(fileurl.charAt(ix));
		return sb.toString();
	}

	/**
	 * Encode the string passed to URLEncoded format. See strDecodeURLEncoded
	 * for description of the format.
	 * @param sb
	 * @param data
	 */
	static public void encodeURLEncoded(final Appendable sb, final String str) {
		try {
			byte[] data = str.getBytes("utf-8");
			int len = data.length;
			for(int i = 0; i < len; i++) {
				byte da = data[i];
				if(isSpecialUrlChar(da)) {
					sb.append('%');
					sb.append(Character.forDigit(((da >> 4) & 0xf), 16));
					sb.append(Character.forDigit(da & 0xf, 16));
				} else {
					sb.append((char) da);
				}
			}
		} catch(Exception x) {
			throw new RuntimeException(x.toString(), x);
		}
	}

	/**
	 * ! 	* 	' 	( 	) 	; 	: 	@ 	& 	= 	+ 	$ 	, 	/ 	? 	% 	# 	[ 	]
	 * @param da
	 * @return
	 */
	static private boolean isSpecialUrlChar(byte da) {
		if(da <= 32 && da >= 0)
			return true;
		switch(da){
			case '!':
			case '*':
			case '\'':
			case '(':
			case ')':
			case ';':
			case ':':
			case '@':
			case '&':
			case '=':
			case '+':
				//			case '$':
				//			case ',':
			case '/':
			case '?':
			case '%':
			case '#':
			case '[':
			case ']':
				return true;
		}
		return false;
	}

	static public String encodeURLEncoded(final String str) {
		StringBuilder sb = new StringBuilder(str.length() + 30);
		encodeURLEncoded(sb, str);
		return sb.toString();
	}

	/**
	 * Decode the URLEncoded string passed to a real string. An URL encoded string
	 * is obtained as follows:
	 * <ul>
	 * 	<li>Convert the Unicode string to UTF-8 (bytes)</li>
	 * 	<li>Each byte that is not a letter or digit is replaced by %HH, where HH
	 * 		is the hex code for the byte.</li>
	 * </ul>
	 * This code undoes the encoding and delivers the original string. If the
	 * input is badly formed the result is undefined.
	 * @param encoded
	 * @return
	 */
	static public String decodeURLEncoded(final String encoded) {
		int len = encoded.length();
		int ix = 0;
		byte[] data = new byte[encoded.length()]; // Has enough space.
		int oix = 0;
		while(ix < len) {
			char c = encoded.charAt(ix++); // Get next char
			if(c != '%')
				data[oix++] = (byte) c;
			else {
				//-- Expecting HH
				if(ix + 2 > len) {
					data[oix++] = (byte) c; // Just copy as-is
				} else {
					int i1 = Character.getNumericValue(encoded.charAt(ix++));
					int i2 = Character.getNumericValue(encoded.charAt(ix++));
					if(i1 < 0 || i1 >= 16 || i2 < 0 || i2 >= 16) {
						//-- Invalid hex. Copy as-is.
						ix -= 2;
						data[oix++] = (byte) '%';
					} else {
						data[oix++] = (byte) ((i1 << 4) | i2);
					}
				}
			}
		}
		try {
			return new String(data, 0, oix, "utf-8");
		} catch(Exception x) {
			return encoded;
		}
	}

	static public final String getLocation() {
		StringBuffer sb = new StringBuffer(512);
		getLocation(sb);
		return sb.toString();
	}

	static public final void getLocation(final StringBuffer sb) {
		sb.append("At ");
		sb.append(new Date().toString());
		sb.append(" in thread ");
		sb.append(Thread.currentThread().getName());
		sb.append(" (");
		sb.append(Thread.currentThread().toString());
		sb.append("), stack:\n");

		try {
			throw new Exception("Trying to get source location");
		} catch(Exception z) {
			strStacktrace(sb, z);
		}
	}


	static public final void dumpLocation(final String msg) {
		try {
			throw new IllegalStateException("duh");
		} catch(IllegalStateException x) {
			System.out.println(msg);
			x.printStackTrace(System.out);
		}
	}

	static public String strUnquote(final String s) {
		if(s.length() < 2)
			return s;
		char c1 = s.charAt(0);
		char c2 = s.charAt(s.length() - 1);
		if(c1 != '\'' && c1 != '"')
			return s;
		if(c1 != c2)
			return s;
		return s.substring(1, s.length() - 1);
	}

	/**
	 * Removes all whitespace from a string.
	 * @param s
	 * @return
	 */
	static public String strUnspace(final String s) {
		if(s == null)
			return null;
		StringBuffer sb = new StringBuffer(s.length());
		int len = s.length();
		for(int i = 0; i < len; i++) {
			char c = s.charAt(i);
			if(!Character.isWhitespace(c))
				sb.append(c);
		}
		return sb.toString();
	}

	/**
	 * Handles Oracle truncation rules:
	 * <ul>
	 *	<li>If the string is > nchars truncate to nchars</li>
	 *	<li>Convert the string to bytes in UTF-8 encoding</li>
	 *	<li>If the string length, in bytes, is > 4000 bytes (the max stupid size of Oracle's stupid varchar2 column, stupid) remove characters until
	 *		the string fits the stupidly limited Oracle column</li>
	 * </ul>
	 * @param in
	 * @param nchars
	 * @return
	 */
	static public String strOracleTruncate(String in, final int nchars) {
		if(in == null)
			return null;
		int len = in.length();
		if(len > nchars)
			in = in.substring(0, nchars); // truncate to max size in characters.
		try {
			byte[] data = in.getBytes("UTF-8");
			if(data.length <= 4000)
				return in;

			//-- Sh*t, exceeded length. Slowly determine the max. size;
			len = nchars - (data.length - 4000);
			for(;;) {
				in = in.substring(0, len);
				data = in.getBytes("UTF-8");
				if(data.length <= 4000)
					return in;
				len--;
			}
		} catch(UnsupportedEncodingException x) {
			throw new RuntimeException(x); // Should not ever happen. Nice shiny checked exception crap.
		}
	}

	static private final long		MICROS		= 1000;

	static private final long		MILLIS		= 1000 * 1000;

	static private final long		SECONDS		= 1000 * 1000 * 1000;

	static private final long		MINUTES		= 60 * SECONDS;

	static private final long		NSHOURS		= 60 * MINUTES;

	static private final long[]		TIMESET		= {NSHOURS, MINUTES, SECONDS, MILLIS, MICROS, 1};

	static private final String[]	SUFFIXES	= {"H", "m", "s", "ms", "us", "ns"};

	/**
	 * Return a nanotime timestamp with 2 thousands of precision max.
	 * @param ns
	 * @return
	 */
	static public String strNanoTime(final long ns) {
		if(ns < 1000)
			return ns + " ns";

		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < TIMESET.length; i++) {
			if(ns >= TIMESET[i]) {
				long u = ns / TIMESET[i];
				sb.append(Long.toString(u));
				sb.append(SUFFIXES[i]);
				sb.append(' ');
				u = ns % TIMESET[i];
				i++;
				u = u / TIMESET[i];
				sb.append(Long.toString(u));
				sb.append(SUFFIXES[i]);
				return sb.toString();
			}
		}
		return ns + "ns";
	}

	/**
	 * Case-sensitive replace of all occurences of [old] with [new].
	 * @param src
	 * @param old
	 * @param nw
	 * @return
	 */
	static public String strReplace(final String src, final String old, final String nw) {
		if(src == null || old == null || nw == null || src.length() < old.length() || old.length() == 0)
			return src;
		int pos = src.indexOf(old); // Try to find quickly,
		if(pos == -1)
			return src; // Not found -> return original
		int len = src.length();
		StringBuilder sb = new StringBuilder(len + 20);
		int ix = 0;
		while(ix < len) {
			if(pos > ix) {
				sb.append(src, ix, pos); // Copy up to pos
				ix = pos;
			}
			sb.append(nw); // Replace occurence,
			ix += old.length(); // Past source occurence
			pos = src.indexOf(old, ix);
			if(pos == -1) {
				sb.append(src, ix, len);
				break;
			}
		}
		return sb.toString();
	}

	/**
	 * If the throwable passed as a message then return it verbatim, else
	 * return the exception's classname.
	 * @param t
	 * @return
	 */
	static public String getExceptionMessage(final Throwable t) {
		String s = t.getMessage();
		if(s == null || s.trim().length() == 0)
			return t.toString();
		return s;
	}

	static public int getJreVersion() {
		if(m_jre_checked)
			return m_jre_version;
		String jre = System.getProperty("java.version");
		//		System.out.println("Running on JDK="+jre);
		int ver = 0;
		StringTokenizer st = new StringTokenizer(jre, ".-/_");
		for(int i = 0; i < 4; i++) {
			int lev = 0;

			try {
				if(st.hasMoreTokens()) {
					String v = st.nextToken();
					lev = Integer.parseInt(v);
				}
			} catch(Exception x) {}
			ver = (ver << 8) + (lev & 0xff);
		}
		m_jre_checked = true;
		m_jre_version = ver;
		return ver;
	}

	/** JRE version as a packed integer: 1.4.2.1 */
	static private int		m_jre_version;

	static private boolean	m_jre_checked;

	/**
	 * Replaces long character sequences without space like ---- and ===== with
	 * a way shorter version.
	 *
	 * @param in
	 * @param maxlen
	 * @return
	 */
	static public String removeRepeatingCharacters(final String in) {
		if(null == in || in.length() < 20)
			return in;
		int len = in.length();
		StringBuilder sb = new StringBuilder(len);
		char lc = 0;
		int count = 0;
		for(int i = 0; i < len; i++) {
			char c = in.charAt(i);
			if(c == lc)
				count++;
			else {
				if(count < 3) {
					while(count > 0) {
						sb.append(lc);
						count--;
					}
				}
				lc = c;
				count = 1;
			}
		}
		if(count < 3) {
			while(count > 0) {
				sb.append(lc);
				count--;
			}
		}

		return sb.toString();
	}

	/**
	 * Tries to extract a single line of max. 80 chars from a memo field by
	 * scanning for a closing '.'
	 *
	 * @param in
	 * @return
	 */
	static public String extractSingleLine(final String in) {
		if(in == null || in.length() < 80)
			return in;
		int dotpos = in.indexOf('.');
		if(dotpos > 0)
			return in.substring(0, dotpos + 1);
		return in.substring(0, 75) + "...";
	}

	static private int			m_guidSeed;

	static private final char[]	GUIDBASE64MAP	= "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz$_".toCharArray();

	/**
	 * Generate an unique identifier with reasonable expectations that it will be globally unique. This
	 * does not use the known GUID format but shortens the string by encoding into base64-like encoding.
	 *
	 * @return
	 */
	static public String generateGUID() {
		byte[] bin = new byte[18];
		ByteArrayUtil.setInt(bin, 0, m_guidSeed); // Start with the seed
		ByteArrayUtil.setShort(bin, 4, (short) (Math.random() * 65536));
		long v = System.currentTimeMillis() / 1000 - (m_guidSeed * 60);
		ByteArrayUtil.setInt(bin, 6, (int) v);
		ByteArrayUtil.setLong(bin, 10, System.nanoTime());

		//          ByteArrayUtil.setLong(bin, 6, System.currentTimeMillis());
		//          System.out.print(StringTool.toHex(bin)+"   ");

		StringBuilder sb = new StringBuilder((bin.length + 2) / 3 * 4);

		//-- 3-byte to 4-byte conversion + 0-63 to ascii printable conversion
		int sidx;
		for(sidx = 0; sidx < bin.length - 2; sidx += 3) {
			sb.append(GUIDBASE64MAP[(bin[sidx] >>> 2) & 0x3f]);
			sb.append(GUIDBASE64MAP[(bin[sidx + 1] >>> 4) & 0xf | (bin[sidx] << 4) & 0x3f]);
			sb.append(GUIDBASE64MAP[(bin[sidx + 2] >>> 6) & 0x3 | (bin[sidx + 1] << 2) & 0x3f]);
			sb.append(GUIDBASE64MAP[bin[sidx + 2] & 0x3f]);
		}
		if(sidx < bin.length) {
			sb.append(GUIDBASE64MAP[(bin[sidx] >>> 2) & 077]);
			if(sidx < bin.length - 1) {
				sb.append(GUIDBASE64MAP[(bin[sidx + 1] >>> 4) & 017 | (bin[sidx] << 4) & 077]);
				sb.append(GUIDBASE64MAP[(bin[sidx + 1] << 2) & 077]);
			} else
				sb.append(GUIDBASE64MAP[(bin[sidx] << 4) & 077]);
		}
		return sb.toString();
	}

	static public void createInsertStatement(final StringBuilder sb, final String table, final String pkname, final String pkexpr, final String[] fields) {
		sb.append("insert into ");
		sb.append(table);
		sb.append('(');
		int fc = 0;
		for(String s : fields) {
			if(fc++ > 0)
				sb.append(',');
			sb.append(s);
		}
		sb.append(',');
		sb.append(pkname);
		sb.append(") values (");
		for(int i = 0; i < fields.length; i++) {
			if(i > 0)
				sb.append(',');
			sb.append('?');
		}
		sb.append(',');
		sb.append(pkexpr);
		sb.append(')');
	}

	static public void createUpdateStatement(final StringBuilder sb, final String table, final String pkname, final String[] fields) {
		sb.append("update ");
		sb.append(table);
		sb.append(" set ");
		for(int i = 0; i < fields.length; i++) {
			if(i > 0)
				sb.append(',');
			sb.append(fields[i]);
			sb.append("=?");
		}
		sb.append(" where ");
		sb.append(pkname);
		sb.append("=?");
	}

	static public String fill(final int count, final char character) {
		char[] fill = new char[count];
		Arrays.fill(fill, character);
		return new String(fill);
	}

	static {
		getJreVersion();
		long val = System.currentTimeMillis() / 1000 / 60;
		m_guidSeed = (int) val;
	}
}
