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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import static java.lang.Character.isJavaIdentifierPart;

/**
 * This static utility class contains a load of string functions. And some other
 * stuff I could not quickly find a place for ;-)
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * @version 1.0
 */
public class StringTool {
	static private final Pattern NORMALIZE_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

	/**
	 * PLEASE DO NOT USE ANYMORE - this limit will change in Oracle 12. To get the
	 * correct limit please call DomApplication.getPlatformVarcharByteLimit() which will
	 * return either 0 or the correct byte limit for the oracle version
	 * used.
	 */
	@Deprecated
	public static final int MAX_SIZE_IN_BYTES_FOR_ORACLE_VARCHAR2 = 4000;

	/**
	 * According to RFC 3696
	 */
	public static final int MAX_EMAIL_LENGTH = 255;

	private final static String m_charString = "bcdfghjklmnpqrstvwxyz";

	private final static char[] m_characters = m_charString.toCharArray();

	static private final long DAYS = 24L * 60 * 60;

	static private final long HOURS = 60L * 60;

	/** Returns "u" for Dutch (uur), "h" for other locales (hour). */
	private static String hourSuffix(Locale locale) {
		return "nl".equals(locale.getLanguage()) ? "u" : "h";
	}

	/**
	 * JRE version as a packed integer: 1.4.2.1
	 */
	static private int m_jreVersion;

	static private boolean m_jreChecked;

	static private int m_guidSeed;

	static private final char[] GUIDBASE64MAP = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz$_".toCharArray();

	// rfc-2045: Base64 Alphabet
	static private final byte[] BASE64MAP = {(byte) 'A', (byte) 'B', (byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F', (byte) 'G', (byte) 'H', (byte) 'I', (byte) 'J', (byte) 'K', (byte) 'L',
		(byte) 'M', (byte) 'N', (byte) 'O', (byte) 'P', (byte) 'Q', (byte) 'R', (byte) 'S', (byte) 'T', (byte) 'U', (byte) 'V', (byte) 'W', (byte) 'X', (byte) 'Y', (byte) 'Z', (byte) 'a', (byte) 'b',
		(byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f', (byte) 'g', (byte) 'h', (byte) 'i', (byte) 'j', (byte) 'k', (byte) 'l', (byte) 'm', (byte) 'n', (byte) 'o', (byte) 'p', (byte) 'q', (byte) 'r',
		(byte) 's', (byte) 't', (byte) 'u', (byte) 'v', (byte) 'w', (byte) 'x', (byte) 'y', (byte) 'z', (byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4', (byte) '5', (byte) '6', (byte) '7',
		(byte) '8', (byte) '9', (byte) '+', (byte) '/'};

	static private final byte[] BASE64DECMAP;

	private static final Random m_random = new Random();

	static {
		BASE64DECMAP = new byte[128];
		for(int ix = 0; ix < BASE64MAP.length; ix++)
			BASE64DECMAP[BASE64MAP[ix]] = (byte) ix;
	}

	static public boolean isValidJavaIdentifier(@NonNull final String s) {
		int len = s.length();
		if(len == 0)
			return false;
		if(!Character.isJavaIdentifierStart(s.charAt(0)))
			return false;
		for(int i = 1; i < len; i++) {
			if(!isJavaIdentifierPart(s.charAt(i)))
				return false;
		}
		return true;
	}

	/**
	 * This methods creates a random String with the specified prefix and length given.
	 * <p>
	 * The generated string does not contain vowels (a, e, i, o, u)
	 */
	@NonNull
	public static String getRandomStringWithPrefix(int length, @NonNull String prefix) {

		if(length <= prefix.length())
			throw new IllegalArgumentException("Prefix is too long");

		StringBuilder randomString = new StringBuilder(length);
		randomString.append(prefix);

		for(int i = prefix.length(); i < length; i++) {
			int position = m_random.nextInt(m_charString.length());
			randomString.append(m_characters[position]);
		}
		return randomString.toString();
	}

	/**
	 * Checks if the name is a valid domain name. These can contain only
	 * letters (a..z), digits (0..9), the dash and dots. Dots cannot start or
	 * end a name, nor can two dots occurs immediately next to another.
	 */
	static public boolean isValidDomainName(@NonNull final String s) {
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
		if(lastdot == -1 && !"LOCALHOST".equalsIgnoreCase(s)) {
			return false; // There must be at least one dot.
		}
		return lastdot + 1 != len;
	}

	static public final boolean isValidIpAddress(@NonNull String ip) {
		if(ip.isEmpty())
			return false;
		String[] split = ip.split("\\.");
		if(split.length != 4)
			return false;
		for(String s : split) {
			try {
				int val = Integer.parseInt(s);
				if(val < 0 || val > 255)
					return false;
			} catch(Exception x) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks that the path is a valid relative path, not starting with a / and not
	 * containing the '..' pattern.
	 */
	static public boolean isValidRelativePath(@NonNull String path) {
		path = path.replace("\\", "/");                            // Accept both / and \ as dir separators
		if(path.startsWith("/"))
			return false;
		return !path.contains("/..") && !path.contains("../");
	}

	/**
	 * Checks that the path is a valid path (security wise), not containing the '..' pattern.
	 */
	static public boolean isValidPath(@NonNull String path) {
		path = path.replace("\\", "/");                            // Accept both / and \ as dir separators
		return !path.contains("/..") && !path.contains("../");
	}

	/**
	 * Returns TRUE if the string is a number, possibly containing a '.'.
	 */
	static public boolean isNumber(@NonNull final String s) {
		int dots = 0;
		int digits = 0;
		for(int i = s.length(); --i >= 0; ) {
			char c = s.charAt(i);
			if(c == '.')
				dots++;
			else if(c < '0' || c > '9')
				return false;
			else
				digits++;
		}
		return dots < 2 && digits >= 1;
	}

	static public boolean isDigitsOnly(@NonNull String s) {
		for(int i = s.length(); --i >= 0; ) {
			char c = s.charAt(i);
			if(!Character.isDigit(c))
				return false;
		}
		return true;
	}

	static public boolean isDomainChar(final char c) {
		return c == '-' || c == '.' || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9');
	}

	static public boolean isWhiteSpaceOrNbsp(final char c) {
		return c == 0x00a0 || Character.isWhitespace(c);
	}

	static public boolean isAllSpaces(@NonNull final String s) {
		for(int i = s.length(); --i >= 0; ) {
			if(!isWhiteSpaceOrNbsp(s.charAt(i)))
				return false;
		}
		return true;
	}

	static public boolean isValidEmail(@NonNull final String em) {
		if(em.length() > MAX_EMAIL_LENGTH) {
			return false;
		}
		int ix = em.indexOf('@');
		if(ix <= 0)
			return false;
		String pre = em.substring(0, ix);
		if(pre.startsWith(".") || pre.endsWith(".") || pre.contains(" ")) {
			return false;
		}
		String dom = em.substring(ix + 1);
		return isValidDomainName(dom);
	}

	/**
	 * Field name must start with ascii letter, then letters, digits or _.
	 */
	static public boolean isValidDbFieldName(@NonNull String s) {
		if(s.isEmpty())
			return false;
		if(s.length() > 30)
			return false;
		s = s.toLowerCase();
		char c = s.charAt(0);
		if(c < 'a' || c > 'z')
			return false;

		for(int i = s.length(); --i > 0; ) {
			c = s.charAt(i);
			if((c < 'a' || c > 'z') && (c < '0' || c > '9') && c != '_') {
				return false;
			}
		}
		return true;
	}

	//	@Contract(value = "null -> null; !null -> !null")
	static public boolean isValidDottedName(String s) {
		if(s == null)
			return false;
		s = s.trim();
		if(s.isEmpty())
			return false;

		//-- Now: only allow names containing ascii chars starting with a nondigit.
		for(int i = s.length(); --i >= 0; ) {
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
		return c == '.' || c == '_';
	}

	static public boolean isEqual(@Nullable final Object a, @Nullable final Object b) {
		if(a == b)
			return true;
		if(a == null || b == null)
			return false;
		return a.equals(b);
	}

	@SuppressWarnings("squid:S4973")
	static public final boolean isEqualIgnoreCase(@Nullable String a, @Nullable String b) {
		if(a == b)
			return true;
		if(a == null || b == null)
			return false;
		return a.equalsIgnoreCase(b);
	}

	public static void stringize(@NonNull final StringBuilder sb, @NonNull final String s) {
		for(int i = 0; i < s.length(); i++) {
			char c = s.charAt(i); // Get the char to put,
			switch(c) {
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
	 * Converts a string into a java-compilable version of a string, i.e.
	 * surrounded by quotes, and with escape sequences escaped..
	 */
	public static StringBuilder stringize(@NonNull final String s) {
		StringBuilder sb = new StringBuilder(s.length() + 20);

		sb.append("\""); // Write a quote,
		stringize(sb, s);
		sb.append("\"");
		return sb;
	}

	public static StringBuilder stringizeNQ(@NonNull final String s) {
		StringBuilder sb = new StringBuilder(s.length() + 20);
		stringize(sb, s);
		return sb;
	}

	/**
	 * Takes an input string and replaces all occurences of the backslash with
	 * a forward slash.
	 */
	public static String strBackslashToSlash(@NonNull final String s) {
		StringBuilder sb = new StringBuilder(s.length());

		int six = 0;
		int ix;
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
	 * Returns a string representing some size, in bytes. Depending on the size
	 * it will be represented as KB, MB, GB or TB.
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
		StringBuilder sb = new StringBuilder(15);

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
	 * If the input string is too long, returns a substring containing at most
	 * maxlen characters.
	 */
//	@Contract("null -> null; !null -> !null")
	public static String truncLength(@Nullable final String s, final int maxlen) {
		if(s == null || s.length() < maxlen)
			return s;
		return s.substring(0, maxlen);
	}

	/**
	 * Get position in a string of a given zero-based line and column. Returns -1 if not located.
	 */
	static public int getPositionIn(String text, int line, int column) {
		int i = 0;
		while(i < text.length() && line > 0) {
			char c = text.charAt(i++);
			if(c == '\n') {
				line--;
			}
		}

		i += column;
		return i < 0 || i > text.length() ? -1 : i;
	}

	/**
	 * Returns a string with the specified length. If the string is too long
	 * it is truncated; if it is too short it is filled with spaces.
	 */
	public static String strToFixedLength(String s, final int l) {
		if(s == null)
			s = "null";
		StringBuilder sb = new StringBuilder(s);
		if(s.length() > l) {
			sb.setLength(l);
		} else {
			while(sb.length() < l) {
				sb.append(' ');
			}
		}
		return sb.toString();
	}

	/**
	 * Returns a string with the specified length. If the string is too long
	 * it is truncated; if it is too short it is filled with c.
	 */
	public static String strToFixedLength(@NonNull final String s, final char c, final int l) {
		if(s.length() == l)
			return s; // Length already OK,
		if(s.length() > l)
			return s.substring(0, l); // Truncate,

		//** Need to add spaces.. Can we do that quickly?
		int dl = l - s.length(); // Get difference in lengths,
		StringBuilder sb = new StringBuilder(l);
		sb.append(s);
		while(dl-- > 0)
			sb.append(c);
		return sb.toString();
	}

	/**
	 * Returns a coordinate pair as a string.
	 */
	@NonNull
	static public String toXY(final int x, final int y) {
		return "(" + x + "," + y + ")";
	}

	/**
	 * This returns the Levenshtein distance between two strings, which is the number of <i>changes</i> (adds, removes)
	 * that are needed to convert source into target. The number of changes is an indication of the difference between
	 * those strings.
	 */
	public static int getLevenshteinDistance(@NonNull String s, @NonNull String t, boolean ignorecase) {
		return getLevenshteinDistance(s, t, ignorecase, 1);
	}

	public static int getLevenshteinDistance(@NonNull String s, @NonNull String t, boolean ignorecase, int substitutionCost) {
		if(ignorecase) {
			s = s.trim().toLowerCase();
			t = t.trim().toLowerCase();
		}
		int n = s.length();
		int m = t.length();

		if(n == 0) {
			return m;
		} else // Optimization for when strings are equal.
			if(m == 0) {
				return n;
			} else if(m == n && s.equals(t))
				return 0;

		int[] p = new int[n + 1]; // 'previous' cost array, horizontally
		int[] d = new int[n + 1]; // cost array, horizontally
		int[] altd; //placeholder to assist in swapping p and d

		// indexes into strings s and t
		int i; // iterates through s
		int j; // iterates through t

		char tjth; // jth character of t

		int cost; // cost

		for(i = 0; i <= n; i++) {
			p[i] = i;
		}

		for(j = 1; j <= m; j++) {
			tjth = t.charAt(j - 1);
			d[0] = j;

			for(i = 1; i <= n; i++) {
				cost = s.charAt(i - 1) == tjth ? 0 : substitutionCost;
				//-- minimum of cell to the left+1, to the top+1, diagonally left and up +cost
				d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
			}

			//-- copy current distance counts to 'previous row' distance counts
			altd = p;
			p = d;
			d = altd;
		}

		//-- our last action in the above loop was to switch d and p, so p now
		//-- actually has the most recent cost counts
		return p[n];
	}

	/**
	 * Returns T if the string starts with the specified string, while ignoring
	 * case.
	 *
	 * @param st   the string whose start is to be checked
	 * @param with the start string
	 */
	static public boolean strStartsWithIgnoreCase(@NonNull final String st, @NonNull final String with) {
		if(st.length() < with.length())
			return false;

		String p = st.substring(0, with.length());
		return with.equalsIgnoreCase(p);
	}

	/**
	 * Returns T if the string ends with the specified string, while ignoring
	 * case.
	 *
	 * @param st   the string whose end is to be checked
	 * @param with the end string
	 */
	static public boolean strEndsWithIgnoreCase(@NonNull final String st, @NonNull final String with) {
		if(st.length() < with.length())
			return false;

		int l = st.length();
		String p = st.substring(l - with.length(), l);
		return with.equalsIgnoreCase(p);
	}

	/**
	 * Tries to locate a substring in a string while ignoring case.
	 */
	static public int strIndexOfIgnoreCase(@NonNull final String txt, @NonNull final String match) {
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
				for(; ; ) {
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
	 * Returns a number in the specified base, and with the specified #of
	 * positions. If the number is too large for the #positions then the
	 * high values are cut off.
	 */
	@NonNull
	static public String intToStr(final int val, final int radix, final int npos) {
		String v = "000000000000" + Integer.toString(val, radix);

		return v.substring(v.length() - npos, v.length());
	}

	/**
	 * Converts the integer to a string with a fixed length, adding leading zeroes
	 * if needed.
	 */
	static public void strAddIntFixed(@NonNull final Appendable sb, final int val, final int radix, final int len) {
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
	 */
	@NonNull
	static public String strCommad(final long val) {
		String v = Long.toString(val);
		StringBuilder sb = new StringBuilder(30);
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

	@NonNull
	static public String strDuration(long dlt) {
		return strDuration(dlt, Locale.getDefault());
	}

	@NonNull
	static public String strDuration(long dlt, Locale locale) {
		StringBuilder sb = new StringBuilder();

		if(dlt >= DAYS) {
			sb.append(Long.toString(dlt / DAYS));
			sb.append("D ");
			dlt %= DAYS;
		}
		if(dlt >= HOURS) {
			sb.append(Long.toString(dlt / HOURS));
			sb.append(hourSuffix(locale)).append(" ");
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

	@NonNull
	static public String strDurationMillis(long dlt) {
		return strDurationMillis(dlt, Locale.getDefault());
	}

	@NonNull
	static public String strDurationMillis(long dlt, Locale locale) {
		StringBuilder sb = new StringBuilder();

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
				sb.append(hourSuffix(locale));
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
		} else if(millis == 0) {
			sb.append("0s");
		}
		if(millis != 0) {
			if(sp)
				sb.append(' ');
			sb.append(millis);
			sb.append("ms");
		}
		return sb.toString();
	}

	static public String strTrunc(@Nullable final String s, final int len) {
		if(s == null)
			return null;
		if(s.length() <= len)
			return s;
		return s.substring(0, len);
	}

	static public String strTruncWithEclipses(@Nullable final String s, final int len) {
		if(s == null)
			return null;
		if(s.length() <= len)
			return s;
		if(len < 5) {
			throw new IllegalArgumentException("len needs to be at least 5!");
		}
		return s.substring(0, len - 3) + "...";
	}

	/**
	 * Returns a string of hex bytes for a given thing.
	 */
	static public void arrayToHexStr(@NonNull Appendable sb, @NonNull byte[] ar, int bufferIndex, int bytesPerLine, int bufferSize, boolean fillout) throws IOException {
		int ei = bytesPerLine + bufferIndex;
		for(int i = bufferIndex; i < ei; i++) {
			if(i >= ar.length || i >= bufferSize) {                // Past end of array?
				if(!fillout)                                    // No need to add spaces?
					return;                                        // Then return the result
				sb.append("   "); // Add 3 spaces.
			} else {
				sb.append(intToStr((ar[i] & 0xff), 16, 2));
				sb.append(' ');
			}
		}
	}

	/**
	 * Returns a string containing only printable chars for the given bytes.
	 */
	static public void arrayToAsciiStr(@NonNull final Appendable sb, @NonNull final byte[] ar, final int bi, final int nc) throws IOException {
		int ei = nc + bi;
		for(int i = bi; i < ei && i < ar.length; i++) {
			byte c = ar[i];
			if(c >= 32 && c < 255)
				sb.append((char) c);
			else
				sb.append('.');
		}
	}

	/**
	 * Returns a dumpstring containing the offset, the hex bytes, and the ascii
	 * representation of a given dump buffer.
	 */
	static public void arrayToDumpLine(@NonNull Appendable sb, @NonNull byte[] ar, int bi, int bytesPerLine, int bufferSize) throws IOException {
		sb.append(intToStr(bi, 16, 4));                // Buffer offset
		sb.append(": ");
		arrayToHexStr(sb, ar, bi, bytesPerLine, bufferSize, true);    // Get filled-out string of nc bytes in HEX
		sb.append("  ");
		arrayToAsciiStr(sb, ar, bi, bytesPerLine);
	}

	/**
	 * Dump the data as a formatted multiline buffer: like
	 * <pre>
	 * 	0000 ff ef aa bb cc dd 99 88  ff ef aa bb cc dd 99 88 sgdfkajse
	 * </pre>
	 */
	static public void dumpData(@NonNull Appendable sb, @NonNull byte[] ar, int off, int len) throws IOException {
		int ix = off;
		int left = len;
		while(left > 0) {
			StringTool.arrayToDumpLine(sb, ar, ix, 16, len);
			sb.append("\n");
			left -= 16;
			ix += 16;
		}
	}

	static public void dumpData(@NonNull Appendable sb, @NonNull byte[] ar, int off, int len, @NonNull String prefix) throws IOException {
		int ix = off;
		int left = len;
		while(left > 0) {
			sb.append(prefix);
			StringTool.arrayToDumpLine(sb, ar, ix, 16, len);
			sb.append("\n");
			left -= 16;
			ix += 16;
		}
	}

	static public void printHex(@NonNull final PrintWriter pw, @NonNull final byte[] arr) {
		printHex(pw, arr, 0, arr.length);
	}

	static public void printHex(@NonNull final PrintWriter pw, @NonNull final byte[] arr, final int start, final int end) {
		//-- Dump the data as a hex string, completely.
		for(int i = start; i < end; i++) {
			pw.print(intToStr((arr[i]) & 0xff, 16, 2));
		}
		pw.println("");
	}

	static public void printHex(@NonNull final PrintStream pw, @NonNull final byte[] arr) {
		printHex(pw, arr, 0, arr.length);
	}

	static public void printHex(@NonNull final PrintStream pw, @NonNull final byte[] arr, final int start, final int end) {
		//-- Dump the data as a hex string, completely.
		for(int i = start; i < end; i++) {
			pw.print(intToStr((arr[i]) & 0xff, 16, 2));
		}
		pw.println("");
	}

	/**
	 * Converts the byte array passed to a hex string. This converts the
	 * region [start..end&gt;.
	 *
	 * @param arr   the array containing the data to convert
	 * @param start the first byte in the array to convert
	 * @param end   the exclusive end of the region to convert
	 */
	static public String toHex(final byte[] arr, final int start, final int end) {
		StringBuilder sb = new StringBuilder(arr.length * 2);

		for(int i = start; i < end; i++) {
			int v = arr[i];
			int c = (v >> 4) & 0xf;
			sb.append(c <= 9 ? (char) (c + '0') : (char) (c + 'a' - 10));
			c = v & 0xf;
			sb.append(c <= 9 ? (char) (c + '0') : (char) (c + 'a' - 10));
		}
		return sb.toString();
	}

	/**
	 * Converts the byte array to a hex string.
	 */
	static public String toHex(final byte[] arr) {
		return toHex(arr, 0, arr.length);
	}

	/**
	 * Converts the byte array passed to a hex string. This converts the
	 * region [start..end&gt;.
	 *
	 * @param arr   the array containing the data to convert
	 * @param start the first byte in the array to convert
	 * @param end   the exclusive end of the region to convert
	 */
	static public String toHexSp(final byte[] arr, final int start, final int end) {
		StringBuilder sb = new StringBuilder(arr.length * 2);

		for(int i = start; i < end; i++) {
			sb.append(intToStr((arr[i]) & 0xff, 16, 2));
			sb.append(' ');
		}
		return sb.toString();
	}

	/**
	 * Converts the byte array to a hex string.
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
	 *
	 * @param s the string
	 * @return the decoded array
	 * @throws Exception if the array is malformed.
	 */
	@NonNull
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

	/**
	 * Deprecated: use {@link FileTool#getFileExtension(String)}.
	 * <p>
	 * Returns the extension of a file. The extension includes the . If no
	 * extension is present then the empty string is returned ("").
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

	/*--------------------------------------------------------------*/
	/*	CODING:	Unhtmlize											*/
	/*--------------------------------------------------------------*/

	/**
	 * Enter with a string; it returns the same string but replaces HTML
	 * recognized characters with their &..; equivalent. This allows parts of
	 * HTML to be rendered neatly.
	 */
	static public String htmlStringize(final String is) {
		StringBuilder sb = new StringBuilder(is.length() + 20);
		htmlStringize(sb, is);
		return sb.toString();
	}

	/**
	 * Enter with a string; it returns the same string but replaces HTML
	 * recognized characters with their &..; equivalent. This allows parts of
	 * HTML to be rendered neatly.
	 */
	static public void htmlStringize(final StringBuilder sb, final String is) {
		htmlStringizeLinefeeds(sb, is, false);
	}

	/**
	 * Enter with a string; it returns the same string but replaces HTML
	 * recognized characters with their &..; equivalent. This allows parts of
	 * HTML to be rendered neatly.
	 * Linefeeds are not removed.
	 */
	static public void htmlStringizewithLF(final StringBuilder sb, final String is) {
		htmlStringizeLinefeeds(sb, is, true);
	}

	/**
	 * Enter with a string; it returns the same string but replaces HTML
	 * recognized characters with their &..; equivalent. This allows parts of
	 * HTML to be rendered neatly.
	 * when linefeeds leave linefeeds in body
	 */
	static private void htmlStringizeLinefeeds(final StringBuilder sb, final String is, final boolean linefeeds) {
		int len = is.length();
		for(int i = 0; i < len; i++) {
			char c = is.charAt(i);
			switch(c) {
				default:
					sb.append(c);
					break;
				case '\n':
					sb.append("<br>");
					if(linefeeds)
						sb.append(c);
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
	 * Enter with a string; it returns the same string but replaces HTML
	 * recognized characters with their &..; equivalent. This allows parts of
	 * HTML to be rendered neatly.
	 */
	static public void htmlStringize(final Appendable o, final String is) throws Exception {
		StringBuilder sb = new StringBuilder(256);
		int len = is.length();
		for(int i = 0; i < len; i++) {
			char c = is.charAt(i);
			switch(c) {
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
	 * Enter with a string; it returns the same string but replaces HTML
	 * recognized characters with their &..; equivalent. This allows parts of
	 * HTML to be rendered neatly.
	 */
	static public String xmlStringize(final String is) {
		if(is == null)
			return "null";
		StringBuilder sb = new StringBuilder(is.length() + 20);
		xmlStringize(sb, is);
		return sb.toString();
	}

	/**
	 * Converts input string to xml representation that complies to
	 * DOM API 5.2 Character Escaping
	 * http://www.w3.org/TR/2000/WD-xml-c14n-20000119.html#charescaping
	 */
	static public String xmlStringizeForDomApi(final String is) {
		if(is == null)
			return "null";
		StringBuilder sb = new StringBuilder(is.length() + 20);
		xmlStringizeForDomApi(sb, is);
		return sb.toString();
	}

	/**
	 * Enter with a string; it returns the same string but replaces HTML
	 * recognized characters with their &..; equivalent. This allows parts of
	 * HTML to be rendered neatly.
	 */
	static public void xmlStringize(final StringBuilder sb, final String is) {
		if(is == null) {
			sb.append("null");
			return;
		}
		for(int i = 0; i < is.length(); i++) {
			char c = is.charAt(i);
			switch(c) {
				case '>':
					sb.append("&gt;");
					break;
				case '<':
					sb.append("&lt;");
					break;
				case '&':
					sb.append("&amp;");
					break;
				case '"':
					sb.append("&quot;");
					break;
				case '\'':
					sb.append("&apos;");
					break;
				default:
					sb.append(c);
					break;
			}
		}
	}

	@Nullable
	static public String xmlDeStringize(final String is) {
		if(is == null || "null".equals(is))
			return null;
		return is
			.replace("&gt;", ">")
			.replace("&lt;", "<")
			.replace("&amp;", "&")
			.replace("&quot;", "\"")
			.replace("&apos;", "'");
	}

	/**
	 * Converts input string to xml representation that complies to
	 * DOM API 5.2 Character Escaping
	 * http://www.w3.org/TR/2000/WD-xml-c14n-20000119.html#charescaping
	 */
	static public void xmlStringizeForDomApi(final StringBuilder sb, final String is) {
		if(is == null) {
			sb.append("null");
			return;
		}
		for(int i = 0; i < is.length(); i++) {
			char c = is.charAt(i);
			switch(c) {
				case '\n':
					sb.append("&#xA;");
					break;
				case '\t':
					sb.append("&#x9;");
					break;
				case '\r':
					sb.append("&#xD;");
					break;
				case '"':
					sb.append("&quot;");
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
	 *
	 * @param sb  the buffer to append the string to
	 * @param str the string to copy while replacing entities.
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
			if(epos == -1) { // Missing ; means 'no entity'
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
	 */
	static public void unicodeToEntities(final StringBuilder sb, final String str) {
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
	 */
	static public int entityToUnicode(final String ename) {
		if(ename.startsWith("#")) { // Decimal code?
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
		strToJavascriptString(sb, cs, dblquote);
		return sb.toString();
	}

	static public void strToJavascriptString(final Appendable w, final String cs, final boolean dblquote) {
		try {
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
					if(c < 32 || c == '\'' || c == '\\' || c == quotechar || c == '\u2028' || c == '\u2029')
						break;
					ix++;
				}
				if(ix > runstart) {
					w.append(cs, runstart, ix);
					if(ix >= len)
						break;
				}
				ix++;
				switch(c) {
					default:
						w.append("\\u"); // Unicode escape
						w.append(StringTool.intToStr(c & 0xffff, 16, 4));
						break;
					case '\n':
					case '\u2028'://Unicode linefeeds
					case '\u2029':
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
						if(dblquote)
							w.append("'");
						else
							w.append("\\'");
						break;
					case '\"':
						if(dblquote)
							w.append("\\\"");
						else
							w.append('"');
						break;
					case '\\':
						w.append("\\\\");
						break;
				}
			}
			w.append(quotechar);
		} catch(IOException x) {
			throw WrappedException.wrap(x);
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Base 64 encoding/decoding (rfc2045)					*/
	/*--------------------------------------------------------------*/

	static public byte[] getBase64Map() {
		return BASE64MAP;
	}

	/**
	 * This method encodes the given string using the base64-encoding
	 * specified in RFC-2045 (Section 6.8). It's used for example in the
	 * "Basic" authorization scheme.
	 *
	 * @param str the string
	 * @return the base64-encoded <var>str</var>
	 */
	public static String encodeBase64(final String str) {
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
	 * @param data the data
	 * @return the base64-encoded <var>data</var>
	 */
	@NonNull
	public static byte[] encodeBase64(@NonNull final byte[] data) {
		int sidx;
		int didx;
		byte[] dest = new byte[((data.length + 2) / 3) * 4];

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
	 * @param str the base64-encoded string.
	 * @return the decoded <var>str</var>.
	 */
	public static String decodeBase64ToString(final String str) {
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
	 * @param str the base64-encoded string.
	 * @return the decoded <var>str</var>.
	 */
	@Nullable
	public static byte[] decodeBase64(final String str) {
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
	 * @param data the base64-encoded data.
	 * @return the decoded <var>data</var>.
	 */
	@NonNull
	public static byte[] decodeBase64(final byte[] data) {
		if(data.length == 0)
			return new byte[0];
		int tail = data.length;
		while(data[tail - 1] == '=')
			tail--;
		byte[] dest = new byte[tail - data.length / 4];

		// ascii printable to 0-63 conversion
		for(int idx = 0; idx < data.length; idx++)
			data[idx] = BASE64DECMAP[data[idx]];

		// 4-byte to 3-byte conversion
		int sidx;
		int didx;
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

	static public String encodeBase64ToString(final byte[] data) {
		return new String(StringTool.encodeBase64(data), StandardCharsets.UTF_8);
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
		} catch(IOException x) {
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
		if(linelimit > 0 && ex - sx > linelimit)
			ex = sx + linelimit;
		for(int i = sx; i < ex; i++) {
			try {
				sb.append("    " + se[i].toString() + "\n");
			} catch(IOException x) {
				throw new RuntimeException(x); // Sigh
			}
		}
	}

	/**
	 * Do a case-insensitive replace.
	 */
	static public String strReplaceCI(String input, String old, String nw) {
		return input.replaceAll("(?i)" + Pattern.quote(old), nw);
	}

	static public int strToInt(final String v, final int defval) {
		try {
			return Integer.parseInt(v);
		} catch(Exception x) {
			//Ignore
		}
		return defval;
	}

	static public long strToLong(final String v, final long defval) {
		try {
			return Long.parseLong(v);
		} catch(Exception x) {
			//Ignore
		}
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
	 * Strip characters with a code < 32 (except newline) from
	 * the string.
	 */
	static public String stripInvalidCharacters(String in) {
		StringBuilder sb = new StringBuilder(in.length());
		for(int i = 0; i < in.length(); i++) {
			char c = in.charAt(i);
			if(c >= 32 || c == '\n') {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static boolean isAsciiLetter(char c) {
		return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
	}

	public static boolean isAsciiDigit(char c) {
		return c >= '0' && c <= '9';
	}

	public static String trimSource(String viewSource) {
		StringBuilder sb = new StringBuilder();
		boolean atstart = true;
		for(String s : new LineIterator(viewSource)) {
			//-- brute-force strip ws
			int len = s.length();
			while(len > 0 && Character.isWhitespace(s.charAt(len - 1)))
				len--;
			if(len != 0 || !atstart) {
				atstart = false;
				sb.append(s, 0, len).append("\n");
			}
		}

		//-- Strip trailing
		while(sb.length() > 2 && sb.charAt(sb.length() - 1) == '\n' && sb.charAt(sb.length() - 2) == '\n') {
			sb.setLength(sb.length() - 1);
		}
		return sb.toString();
	}

	private final static class ExceptionDup {
		private final String m_message;

		private int m_count = 1;

		public ExceptionDup(String message) {
			m_message = message;
		}
	}

	static public String getAllExceptionTexts(Exception x) {
		if(x instanceof SQLException sx) {
			StringBuilder sb = new StringBuilder();
			sb.append(sx);

			List<ExceptionDup> dups = new ArrayList<>();

			for(; ; ) {
				SQLException nx = sx.getNextException();
				if(nx == null || nx == sx)
					break;
				addExceptionDup(dups, nx);
				sx = nx;
			}

			for(ExceptionDup dup : dups) {
				sb.append("\n- ").append(dup.m_message);
				if(dup.m_count > 1) {
					sb.append(" (repeated ").append(dup.m_count).append("x)");
				}
			}

			return sb.toString();
		}
		return x.toString();
	}

	private static void addExceptionDup(List<ExceptionDup> dups, SQLException sx) {
		String msg = sx.toString();
		for(ExceptionDup dup : dups) {
			if(dup.m_message.equals(msg)) {
				dup.m_count++;
				return;
			}
		}
		dups.add(new ExceptionDup(msg));
	}

	/**
	 * Encode the string passed to URLEncoded format. See strDecodeURLEncoded
	 * for description of the format.
	 */
	static public void encodeURLEncoded(final Appendable sb, final String str) {
		try {
			byte[] data = str.getBytes(StandardCharsets.UTF_8);
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
	 * ! 	* 	' 	( 	) 	; 	: 	@ 	& 	= 	+ 	$ 	, 	/ 	? 	% 	# 	[ 	] { }
	 */
	static private boolean isSpecialUrlChar(byte da) {
		if(da <= 32) // Everything including -1..-128 (0x80..0xff) is special
			return true;
		return switch(da) {
			default -> false;
			case '!', '*', '\'', '(', ')', ';', ':', '@', '&', '=', '+', '/', '?', '%', '#', '[', ']', '<', '>', '{', '}' -> true;
		};
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
		return new String(data, 0, oix, StandardCharsets.UTF_8);
	}

	static public String getLocation() {
		StringBuilder sb = new StringBuilder(512);
		getLocation(sb);
		return sb.toString();
	}

	static public void getLocation(final StringBuilder sb) {
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

	/**
	 * Kept in api since it was useful for some debugs while coding, use just for debug purposes only, do not use in produciton code.
	 * In case that dumping stack is required for production code for remote sessions on client side, please use sumbLEVELLocation methods that are using regular logger.
	 */
	static public void dumpLocation(final String msg) {
		try {
			throw new IllegalStateException("duh");
		} catch(IllegalStateException x) {
			System.out.println(msg);
			x.printStackTrace(System.out);
		}
	}

	static public void dumpDebugLocation(@NonNull Logger log, final @NonNull String msg) {
		try {
			throw new IllegalStateException("Dump at debug level for source location...");
		} catch(IllegalStateException x) {
			log.debug(msg, x);
		}
	}

	static public void dumpTraceLocation(@NonNull Logger log, final @NonNull String msg) {
		try {
			throw new IllegalStateException("Dump at trace level for source location...");
		} catch(IllegalStateException x) {
			log.trace(msg, x);
		}
	}

	static public void dumpInfoLocation(@NonNull Logger log, final @NonNull String msg) {
		try {
			throw new IllegalStateException("Dump at info level for source location...");
		} catch(IllegalStateException x) {
			log.info(msg, x);
		}
	}

	static public void dumpWarnLocation(@NonNull Logger log, final @NonNull String msg) {
		try {
			throw new IllegalStateException("Dump at warn level for source location...");
		} catch(IllegalStateException x) {
			log.warn(msg, x);
		}
	}

	static public void dumpErrorLocation(@NonNull Logger log, final @NonNull String msg) {
		try {
			throw new IllegalStateException("Dump at error level for source location...");
		} catch(IllegalStateException x) {
			log.error(msg, x);
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
	 */
	static public String strUnspace(final String s) {
		if(s == null)
			return null;
		StringBuilder sb = new StringBuilder(s.length());
		int len = s.length();
		for(int i = 0; i < len; i++) {
			char c = s.charAt(i);
			if(!Character.isWhitespace(c))
				sb.append(c);
		}
		return sb.toString();
	}

	@Nullable
	static public String strOracleTruncate(@Nullable String in, int nchars) {
		return strTruncateUtf8Bytes(in, nchars, MAX_SIZE_IN_BYTES_FOR_ORACLE_VARCHAR2);
	}

	@Nullable
	static public String strTruncateUtf8Bytes(@Nullable String in, int nchars, int nbytes) {
		if(null == in)
			return null;
		int length = in.length();

		//-- Can we do a quick exit?
		if(length <= (nbytes / 3)) {                    // Max UTF-8 size for 16-byte chars is 3 bytes
			if(length > nchars) {
				in = in.substring(0, nchars);
			}
			return in;
		}

		int maxlength = utf8Truncated(in, nchars, nbytes);        // Oracle <= 11g allows maximal 4000 bytes in any varchar2 column
		if(maxlength == length)
			return in;
		return in.substring(0, maxlength);
	}

	static public int utf8Truncated(String in, int length, int maxbytes) {
		if(length > in.length())
			length = in.length();

		//-- Loop characters and calculate running length
		int bytes = 0;
		for(int ix = 0; ix < length; ix++) {
			char c = in.charAt(ix);
			if(c < 0x80)
				bytes++;
			else if(c < 0x800)
				bytes += 2;
			else
				bytes += 3;
			if(bytes > maxbytes)
				return ix;
		}
		return length;
	}

	static public int utf8Length(String in) {
		//-- Loop characters and calculate running length
		int length = in.length();
		int bytes = 0;
		for(int ix = 0; ix < length; ix++) {
			char c = in.charAt(ix);
			if(c < 0x80)
				bytes++;
			else if(c < 0x800)
				bytes += 2;
			else
				bytes += 3;
		}
		return bytes;
	}

	static public String strOracleTruncate(String in, int nchars, String suffix) {
		return strTruncateUtf8Bytes(in, nchars, MAX_SIZE_IN_BYTES_FOR_ORACLE_VARCHAR2, suffix);
	}

	static public String strTruncateUtf8Bytes(String in, int nchars, int nbytes, String suffix) {
		if(null == in)
			return null;
		int length = in.length();
		int suffixLength = suffix.length();

		//-- Can we do a quick exit?
		if(length <= (nbytes / 3)) {                                                // Max UTF-8 size for 16-byte chars is 3 bytes
			if(length > nchars) {
				if(nchars <= suffixLength)
					return in.substring(0, nchars);                                // Don't add a suffix if input is silly.

				StringBuilder sb = new StringBuilder(nchars);
				sb.append(in, 0, nchars - suffixLength);
				sb.append(suffix);
				return sb.toString();
			}
			return in;
		}

		int maxlength = utf8Truncated(in, nchars, nbytes);                        // Oracle <= 11g allows maximal 4000 bytes in any varchar2 column
		if(maxlength == length)
			return in;

		//-- We need to truncate..
		int suffixBytes = utf8Length(suffix);
		maxlength -= suffixBytes;                        // Remove this many chars as there are bytes
		StringBuilder sb = new StringBuilder(nbytes);
		sb.append(in, 0, maxlength);
		sb.append(suffix);
		return sb.toString();
	}

	static private final long MICROS = 1000L;

	static private final long MILLIS = 1000L * 1000;

	static private final long SECONDS = 1000L * 1000 * 1000;

	static private final long MINUTES = 60 * SECONDS;

	static private final long NSHOURS = 60 * MINUTES;

	static private final long[] TIMESET = {NSHOURS, MINUTES, SECONDS, MILLIS, MICROS, 1};

	static private final String[] SUFFIXES = {"H", "m", "s", "ms", "us", "ns"};

	/**
	 * Return a nanotime timestamp with 2 thousands of precision max.
	 */
	static public String strNanoTime(final long ns) {
		if(ns < 1000)
			return ns + " ns";

		StringBuilder sb = new StringBuilder();
		int i = 0;
		while(i < TIMESET.length) {
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
			i++;
		}
		return ns + "ns";
	}

	/**
	 * If the throwable passed as a message then return it verbatim, else
	 * return the exception's classname.
	 */
	static public String getExceptionMessage(final Throwable t) {
		String s = t.getMessage();
		if(s == null || s.trim().isEmpty())
			return t.toString();
		return s;
	}

	static public int getJreVersion() {
		if(m_jreChecked)
			return m_jreVersion;
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
			} catch(Exception x) {
				//Ignore
			}
			ver = (ver << 8) + (lev & 0xff);
		}
		m_jreChecked = true;
		m_jreVersion = ver;
		return ver;
	}

	/**
	 * Replaces long character sequences without space like ---- and ===== with
	 * a way shorter version (single character).
	 * If the chars in sequence are digits, they won't be shortened.
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
			if(Character.isDigit(c) || c != lc) {
				addRepeatingCharacterOnce(sb, lc, count);
				lc = c;
				count = 1;
			} else {
				count++;
			}
		}
		addRepeatingCharacterOnce(sb, lc, count);

		return sb.toString();
	}

	/**
	 * @param lc    last character
	 * @param count repeat count of last character
	 */
	private static void addRepeatingCharacterOnce(StringBuilder sb, char lc, int count) {
		if(count < 3) {  //repeated less then 3, eg ee oo like in eet ook, just add unchanged
			while(count > 0) {
				sb.append(lc);
				count--;
			}
		} else {      //just 1 entry if repeated more than 2.
			sb.append(lc);
		}
	}

	/**
	 * Tries to extract a single line of max. 80 chars from a memo field by
	 * scanning for a closing '.'
	 */
	static public String extractSingleLine(final String in) {
		if(in == null || in.length() < 80)
			return in;
		int dotpos = in.indexOf('.');
		if(dotpos > 0)
			return in.substring(0, dotpos + 1);
		return in.substring(0, 75) + "...";
	}

	/**
	 * Generate an unique identifier with reasonable expectations that it will be globally unique. This
	 * does not use the known GUID format but shortens the string by encoding into base64-like encoding.
	 */
	@NonNull
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

	/**
	 * Checks if string is blank.
	 *
	 * @return true if the string is null, empty or only spaces; false otherwise.
	 */
	static public boolean isBlank(String in) {
		if(null == in)
			return true;
		for(int i = in.length(); --i >= 0; ) {
			if(!Character.isWhitespace(in.charAt(i)))
				return false;
		}
		return true;
	}

	static public String fill(final int count, final char character) {
		char[] fill = new char[count];
		Arrays.fill(fill, character);
		return new String(fill);
	}

	/**
	 * Replaces all accented letters with their non-accented equivalents - FOR WESTERN LANGUAGES ONLY!
	 */
	@NonNull
	static public String removeAccents(@NonNull String str) {
		String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
		return NORMALIZE_PATTERN.matcher(nfdNormalizedString).replaceAll("");
	}

	static {
		getJreVersion();
		long val = System.currentTimeMillis() / 1000 / 60;
		m_guidSeed = (int) val;
	}

	/**
	 * <pre>
	 * This method removes the leading characters from the string, if it exceeds the column size
	 * or needs more then 4000 bytes to store in the database.
	 *
	 * There's a limit on the size in bytes for an Oracle varchar2. This limit is
	 * 4000 bytes (MAX_SIZE_IN_BYTES_FOR_ORACLE_VARCHAR2). So if you declare a varchar2(4000 char),
	 * it's possible that for example 3970 characters won't fit, because there are charaters in the string
	 * that needs more then one byte in UTF8.
	 * </pre>
	 */
	@Nullable
	public static String truncLeadingOracleColumn(@Nullable String text, int columnSize) {

		if(text == null)
			return null;

		if(text.length() > columnSize)
			text = text.substring(text.length() - columnSize);

		int lengthInBytes = getUtf8LengthInBytes(text);

		if(lengthInBytes <= MAX_SIZE_IN_BYTES_FOR_ORACLE_VARCHAR2) {
			return text;
		}

		while((lengthInBytes = getUtf8LengthInBytes(text)) > MAX_SIZE_IN_BYTES_FOR_ORACLE_VARCHAR2) {
			int tooMuchBytes = lengthInBytes - MAX_SIZE_IN_BYTES_FOR_ORACLE_VARCHAR2;
			int startPosition = tooMuchBytes / 2 + 1;
			text = text.substring(startPosition);
		}
		return text;
	}

	private static int getUtf8LengthInBytes(@NonNull String text) {
		return text.getBytes(StandardCharsets.UTF_8).length;
	}

	@NonNull
	public static String strCapitalized(@NonNull String name) {
		if(name.isEmpty())
			return name;
		char c = name.charAt(0);
		return Character.toUpperCase(c) + name.substring(1).toLowerCase();
	}

	/**
	 * Just capitalizes first letter and leaves the rest of the string intact
	 * Example StringTool.strCapitalizedIntact("executeNow") -> "ExecuteNow"
	 */
	@NonNull
	public static String strCapitalizedIntact(@NonNull String name) {
		if(name.isEmpty())
			return name;
		char c = name.charAt(0);
		return Character.toUpperCase(c) + name.substring(1);
	}

	/**
	 * Decapitalizes a string and lowercases everything else
	 * Example: StringTool.strDecapitalized("ExecuteNow") -> "executenow"
	 * Added for consistency in the API.
	 */
	@NonNull
	public static String strDecapitalized(@NonNull String name) {
		return name.toLowerCase();
	}

	/**
	 * Decapitalizes a string and lowercases everything else
	 * Example: StringTool.strDecapitalized("ExecuteNow") -> "executeNow"
	 */
	@NonNull
	public static String strDecapitalizedIntact(@NonNull String name) {
		if(name.isEmpty())
			return name;
		char c = name.charAt(0);
		return Character.toLowerCase(c) + name.substring(1);
	}

	/**
	 * Checks whether a given text is too big for the maximum varchar2 database field
	 */
	public static boolean isInvalidOracleLength(@NonNull String text) {
		return getUtf8LengthInBytes(text) >= MAX_SIZE_IN_BYTES_FOR_ORACLE_VARCHAR2;
	}

	/**
	 * Checks if forwarded string can be parsed into an Integer.
	 */
	public static boolean isInteger(@Nullable String string) {
		if(null == string)
			return false;
		try {
			Integer.parseInt(string);
			return true;
		} catch(Exception e) {
			return false;
		}
	}

	/**
	 * Replaces all end of line characters with space so that content is represented in one line.
	 *
	 * @param content     that should be without new line characters
	 * @param replacement for new line characters
	 * @return given string without new line characters
	 */
	@NonNull
	public static String replaceNewLineChars(@NonNull String content, @NonNull String replacement) {
		return content.replace("\r\n", replacement).replace("\r", replacement).replace("\n", replacement);
	}

	/**
	 * Does some transformation of custom string to html output -> taking into account several customizations.
	 * If input uses HTML for new line (<br> or <br/>) then it ignores rest or \n characters, otherwise converts \n to <br/>.
	 * If removeEndingNewLines then it removes all the ending new lines too.
	 */
	public static String renderAsRawHtml(@NonNull String input, boolean removeEndingNewLines) {
		input = input.replace("<br>", "<br/>");
		if(input.contains("<br/>")) {
			input = input.replace("\n", "");
		} else {
			input = input.replace("\n", "<br/>");
		}
		if(removeEndingNewLines) {
			while(StringTool.strEndsWithIgnoreCase(input, "<br/>")) {
				input = input.substring(0, input.length() - 5).trim();
			}
		}
		return input;
	}

	@Nullable
	static public String nullIfEmpty(@Nullable String in) {
		if(null == in)
			return null;
		for(int i = in.length(); --i >= 0; ) {
			if(!Character.isWhitespace(in.charAt(i)))
				return in;
		}
		return null;
	}

	static public boolean isWindows() {
		String os = System.getProperty("os.name").toLowerCase();
		return os.contains("win");
	}

	static public boolean isLinux() {
		return "linux".equalsIgnoreCase(System.getProperty("os.name"));
	}

	static public String stripInvalidUnicode(String in) {
		StringBuilder sb = new StringBuilder(in.length());
		stripInvalidUnicode(sb, in);
		return sb.toString();
	}

	public static void stripInvalidUnicode(StringBuilder sb, CharSequence in) {
		int len = in.length();
		for(int i = 0; i < len; i++) {
			char c = in.charAt(i);
			if(c == 0x0a || c == 0x0d) {
				sb.append(c);
			} else if(c < 32) {
				// -- ignore
			} else if(Character.isDefined(c)) {
				sb.append(c);
			}
		}
	}

	public static String stripAccents(String s) {
		s = Normalizer.normalize(s, Normalizer.Form.NFD);
		s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
		return s;
	}

	/**
	 * Util that locates given qualified name in expression (with ignored casing), and replaces it with new qualified name.
	 * It ignores other cases when old name is part of naming of other variables in expression.
	 * It requires that old qualified name prefix and name are named by java identifier convention.
	 *
	 * @param expression  expression where we replace variables with qualified names
	 * @param prefixQName prefix in qualified name to replace.
	 * @param oldName     name in qualified name to replace.
	 * @param newQName    new qualified name that replaced old one.
	 * @return replaced expression.
	 */
	@Nullable
	public static String replaceQualifiedNameInExpression(@Nullable String expression, String prefixQName, String oldName, String newQName) {
		if(null == expression) {
			return null;
		}
		String lEntityName = prefixQName.toLowerCase();
		String lOldName = oldName.toLowerCase();
		String literalToFind = lEntityName + "." + lOldName;
		return replaceVariableNameInExpression(expression, literalToFind, newQName);
	}

	/**
	 * Util that locates given variable name in expression (with ignored casing), and replaces it with new name.
	 * It ignores other cases when old name is part of naming of other variables in expression.
	 * It requires that old and new name are named by java identifier convention.
	 *
	 * @param expression expression where we replace variable name
	 * @param oldName    name to replace.
	 * @return replaced expression.
	 */
	@Nullable
	public static String replaceVariableNameInExpression(@Nullable String expression, String oldName, String newName) {
		if(null == expression) {
			return null;
		}
		String lowerCaseExpression = expression.toLowerCase();
		String literalToFind = oldName.toLowerCase();
		int lastReplacedIndex = 0;
		int pos = -1;
		StringBuilder replaceSb = new StringBuilder();
		do {
			pos = lowerCaseExpression.indexOf(literalToFind, pos + 1);
			if(pos > -1) {
				Character nextChar = null;
				if(lowerCaseExpression.length() > pos + literalToFind.length()) {
					nextChar = lowerCaseExpression.charAt(pos + literalToFind.length());
				}
				Character prevChar = null;
				if(pos > 0) {
					prevChar = expression.charAt(pos - 1);
				}
				if((nextChar == null || !isJavaIdentifierPart(nextChar)) && (prevChar == null || !isJavaIdentifierPart(prevChar))) {
					replaceSb
						.append(expression.substring(lastReplacedIndex, pos))
						.append(newName);
					lastReplacedIndex = pos + literalToFind.length();
				}
			} else if(lastReplacedIndex < expression.length()) {
				replaceSb.append(expression.substring(lastReplacedIndex));
			}
		} while(pos >= 0);
		return replaceSb.toString();
	}

	/**
	 * This method checks that the name passed only contains a name,
	 * and nothing that looks like a SQL injection.
	 */
	static public void sqlCheckNameOnly(@Nullable String name) {
		if(null == name)
			return;
		for(int i = 0, len = name.length(); i < len; i++) {
			char c = name.charAt(i);
			if(!isValidSqlNameChar(c))
				throw new IllegalArgumentException("Invalid characters in SQL name <<" + name + ">>: " + c);
		}
	}

	private static boolean isValidSqlNameChar(char c) {
		/*
		 * Since we need to support Microsoft's crap we need to accept any kind
		 * of garbage characters in database names. These incompetent idiots must
		 * really like SQL injection attacks and other security issues.
		 */
		return c >= ' ' && c < 128;
		//return Character.isLetterOrDigit(c)
		//	|| c == '_'
		//	|| c == '.'
		//	|| c == '['
		//	|| c == ']'
		//	|| c == '"'
		//	|| c == ' '
		//	|| c == '-'
		//	|| c == '/'					// Oh brother.
		//	;
	}

	static public void sqlCheckNoQuotes(@Nullable String password) {
		if(null != password && password.contains("'"))
			throw new IllegalArgumentException("Invalid characters in SQL");
	}

	static private final char[] PUNCT = "!#_^&*.;".toCharArray();

	static private final char[] DIGITS = "023456789".toCharArray();

	static private final char[] LETTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

	/**
	 * Generate a reasonably secure password.
	 */
	static public String generatePassword(int nchar) {
		return generatePassword(nchar, 2, 2);
	}

	/*
	 * Postgres' passwords should not include dollar signs nor percentage signs.
	 * jal 20200608 actually postgres is OK with it, it's Azure Tabular that dies with it.
	 */
	static public String generatePassword(int nchar, int punctuation, int digits) {
		if(nchar < 6)
			throw new IllegalStateException("Don't be silly.");

		char[] buf = new char[nchar];                            // Password buffer

		//-- Randomly assign the #of punctuation chars
		while(punctuation > 0) {
			char c = PUNCT[m_random.nextInt(PUNCT.length)];        // Random punctuation

			for(; ; ) {
				int pos = m_random.nextInt(nchar);                // Get a position
				if(buf[pos] == 0) {
					buf[pos] = c;
					break;
				}
			}
			punctuation--;
		}

		//-- Randomly assign digits
		while(digits > 0) {
			char c = DIGITS[m_random.nextInt(DIGITS.length)];        // Random punctuation

			for(; ; ) {
				int pos = m_random.nextInt(nchar);                    // Get a position
				if(buf[pos] == 0) {
					buf[pos] = c;
					break;
				}
			}
			digits--;
		}

		//-- And finally: fill the rest with random letters.
		for(int i = 0; i < nchar; i++) {
			if(buf[i] == 0) {
				buf[i] = LETTERS[m_random.nextInt(LETTERS.length)];
			}
		}
		return new String(buf);
	}

	/**
	 * This does its best to create a Dutch plural form for the word
	 * specified. It obeys the rules <a href="https://www.braint.nl/taalgids/spelling/meervoud.html">described here</a>.
	 * This will use 's where needed, so if you expect an identifier
	 * out of this replace all ' with something else!!
	 * This is not perfect; it does not handle exceptions like "kind" and "blad".
	 */
	@NonNull
	static public String dutchPluralOf(@NonNull String word) {
		word = word.trim();
		if(word.length() == 0)
			return word;
		String lc = word.toLowerCase();
		boolean isUC = Character.isUpperCase(word.charAt(word.length() - 1));
		String enSuffix = isUC ? "EN" : "en";
		String sSuffix = isUC ? "S" : "s";
		String sApoSuffix = isUC ? "'S" : "'s";

		if(lc.endsWith("e") || lc.endsWith("eau") || lc.endsWith("ail"))
			return word + sSuffix;
		if(lc.endsWith("eid")) // eenheid -> eenheden
			return word.substring(0, word.length() - 3) + "eden";
		if(lc.endsWith("eel")) // bouwdeel -> bouwdelen
			return word.substring(0, word.length() - 3) + "elen";
		if(lc.endsWith("slag"))
			return word + enSuffix;
		if(lc.endsWith("iel"))
			return word + enSuffix;

		//}
		if(word.length() > 3 && (lc.endsWith("el") || lc.endsWith("en") || lc.endsWith("er") || lc.endsWith("em") || lc.endsWith("ie")))
			return word + sSuffix;

		if(lc.endsWith("i") || lc.endsWith("a") || lc.endsWith("o") || lc.endsWith("u"))
			return word + (isUC ? "'S" : "'s");

		//-- If we end in "y" it depends on whether the letter before the end is a vowel
		if(lc.endsWith("y")) {
			char before = lc.charAt(lc.length() - 2);
			if(isVowel(before)) {
				return word + sSuffix;
			} else {
				return word + sApoSuffix;
			}
		}

		//-- We will want to use "en"...
		//-- Ends in 2 same vowels and consonant -> remove one of the vowels (afspraak -> afspraken)
		if(word.length() >= 3 && lc.charAt(word.length() - 2) == lc.charAt(word.length() - 3) && isVowel(word.charAt(word.length() - 2)) && !isVowel(word.charAt(word.length() - 1))) {
			word = word.substring(0, word.length() - 3) + word.substring(word.length() - 2) + enSuffix;
			return word;
		}

		//-- Does the word end in a single vowel + consonant? Then repeat the final consonant (adres -> adressen).
		if(word.length() >= 2 && isVowel(word.charAt(word.length() - 2)) && !isVowel(word.charAt(word.length() - 1)) && !isVowel(word, -3)) {
			return word + word.charAt(word.length() - 1) + enSuffix;
		}

		//-- If the word ends in "f" we need to change it to a "v"
		if(lc.endsWith("f"))
			return word.substring(0, word.length() - 1) + (isUC ? "VEN" : "ven");

		return word + enSuffix;
	}

	private static boolean isVowel(char c) {
		return c == 'a' || c == 'A'
			|| c == 'i' || c == 'I'
			|| c == 'u' || c == 'U'
			|| c == 'o' || c == 'O'
			|| c == 'e' || c == 'E'
			;
	}

	private static boolean isVowel(String word, int index) {
		if(index >= 0)
			return false;
		int pos = word.length() + index;
		if(pos < 0)
			return false;
		return isVowel(word.charAt(pos));
	}

	/**
	 * Calculates a records per second count from a #records and a
	 * time it took to load them in millis.
	 */
	static public String rps(long records, long millis) {
		double r = records / ((double) millis / 1000);
		return NumberFormat.getNumberInstance().format((long) r);
	}

	/**
	 * Formats a number in user readable form, with thousands separators.
	 */
	static public String nr(long records) {
		return NumberFormat.getNumberInstance().format(records);
	}

	static public String bps(double bytesPerSecond) {
		if(bytesPerSecond < 1024 * 10) {
			return String.format("%.0f bytes/s", bytesPerSecond);
		}
		if(bytesPerSecond < 1024 * 1024) {
			return String.format("%.1f KB/s", bytesPerSecond / 1024.0D);
		}
		return String.format("%.1f MB/s", bytesPerSecond / (1024.0D * 1024.0D));
	}

	static public void main(final String[] args) throws Exception {
		System.out.println(dutchPluralOf("huurtoeslag"));
	}

	public static class Chunk {
		private final int m_nextStartIndex;

		private final String m_content;

		public Chunk(int startIndex, String content) {
			this.m_nextStartIndex = startIndex;
			this.m_content = content;
		}

		public int getNextStartIndex() {
			return m_nextStartIndex;
		}

		public String getContent() {
			return m_content;
		}
	}

	/**
	 * Calculates next chunk of split message,
	 * with decorating ... at the beginning of continued text
	 * and with decorating ... at the end of the text that has more chinks to follow.
	 */
	@Nullable
	static public Chunk nextChunk(String content, int start, int chunkSize) {
		if(isBlank(content)) {
			return null;
		}
		if(start >= content.length()) {
			return null;
		}
		int offset = start > 0 ? 3 : 0;
		int end = start + chunkSize - offset - 3;
		if(end > content.length()) {
			end = content.length();
		}
		String chunk = content.substring(start, end);
		if(start > 0) {
			chunk = "..." + chunk;
		}
		if(end < content.length()) {
			chunk += "...";
		}
		return new Chunk(end, chunk);
	}

	/**
	 * Yes, this is a waste of 64K, but it IS fast.
	 */
	private static final boolean[] WSARRAY = new boolean[65536];

	static public boolean isUnicodeWhitespace(char ch) {
		return WSARRAY[ch & 0xffff];
	}

	static private void registerWhiteSpace(int... chars) {
		for(int chr : chars) {
			WSARRAY[chr] = true;
		}
	}

	static {
		// See https://en.wikipedia.org/wiki/Whitespace_character
		registerWhiteSpace(0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x20, 0x85, 0xa0, 0x1680, 0x2000, 0x2001, 0x2002);
		registerWhiteSpace(0x2003, 0x2004, 0x2005, 0x2006, 0x2007, 0x2008, 0x2009, 0x200a);
		registerWhiteSpace(0x2028, 0x2029, 0x202f, 0x205f, 0x3000);
	}

	public static String trimAllWS(String v) {
		int len = v.length();
		StringBuilder sb = new StringBuilder(len);
		int ix = 0;

		//-- Trim leading whitespace, all types
		while(ix < len) {
			char c = v.charAt(ix);
			if(!isUnicodeWhitespace(c))
				break;
			ix++;
		}

		//-- Copy chars, keep track of the first whitespace in a set
		int lastws = -1;
		while(ix < len) {
			char c = v.charAt(ix);
			if(isUnicodeWhitespace(c)) {
				if(lastws == -1)
					lastws = sb.length();
			} else {
				lastws = -1;
			}
			sb.append(c);
			ix++;
		}
		if(lastws != -1) {
			sb.setLength(lastws);
		}
		return sb.toString();
	}

}
