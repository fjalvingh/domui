package to.etc.util;

import java.util.*;

/**
 * A version number represented as a string like "pppx.x.xsss". The version consists of
 * an optional prefix, a list of dotted numbers of any length, and an optional suffix. Both
 * prefix and suffix can only contain latin letters, and there is no punctuation between them
 * and the dotted number.
 * <p>The dotted number will always be "normalized", meaning that all trailing zeroes will be
 * removed all of the time. In comparisons missing trailing numbers will behave as zeroes.</p>
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 2, 2011
 */
final public class VersionNumber implements Comparable<VersionNumber> {
	/** Any textual string before the x.x.x version #, or empty string if no such string is present. */
	final private String	m_prefix;

	/** Any textual string after the x.x.x version #, or empty string if no such string is present. */
	final private String	m_suffix;

	final private int[]		m_version;

	public VersionNumber(String prefix, int[] version, String suffix) {
		if(version == null || version.length == 0)
			throw new IllegalArgumentException("The version array cannot be null or empty");
		if(version[version.length - 1] <= 0)
			throw new IllegalArgumentException("The version array cannot end in a 0: normalize the version # by removing trailing zeroes.");
		m_prefix = prefix == null ? "" : prefix;
		m_version = version;
		m_suffix = suffix == null ? "" : suffix;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(m_prefix);
		for(int i = 0; i < m_version.length; i++) {
			if(i != 0)
				sb.append('.');
			sb.append(m_version[i]);
		}
		sb.append(m_suffix);
		return sb.toString();
	}

	public String toString(int mindigits) {
		StringBuilder sb = new StringBuilder();
		sb.append(m_prefix);
		if(mindigits < m_version.length)
			mindigits = m_version.length;

		for(int i = 0; i < mindigits; i++) {
			if(i != 0)
				sb.append('.');
			if(i >= m_version.length)
				sb.append('0');
			else
				sb.append(m_version[i]);
		}
		sb.append(m_suffix);
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_prefix == null) ? 0 : m_prefix.hashCode());
		result = prime * result + ((m_suffix == null) ? 0 : m_suffix.hashCode());
		result = prime * result + Arrays.hashCode(m_version);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		VersionNumber other = (VersionNumber) obj;
		if(m_prefix == null) {
			if(other.m_prefix != null)
				return false;
		} else if(!m_prefix.equals(other.m_prefix))
			return false;
		if(m_suffix == null) {
			if(other.m_suffix != null)
				return false;
		} else if(!m_suffix.equals(other.m_suffix))
			return false;
		return Arrays.equals(m_version, other.m_version);
	}

	/**
	 * Parses a version number in the format: pppx.x.xsss, where ppp is any non-numeric string, x.x.x is a repeated set of numbers separated by dots, and
	 * sss is another non-numeric string. In the version number part (x.x.x) the code will always remove all trailing zeroes to normalize the version
	 * number.
	 * @param input
	 * @return
	 * @throws InvalidVersionNumberException
	 */
	static public VersionNumber parse(String input) throws InvalidVersionNumberException {
		if(input.trim().equalsIgnoreCase("trunk"))
			return new VersionNumber("", new int[]{999, 999}, "");

		int len = input.length();
		int ix = 0;
		while(ix < len) {
			char c = input.charAt(ix);
			if(Character.isDigit(c))
				break;
			if(!Character.isLetter(c))
				throw new InvalidVersionNumberException("A version number must start with either letters or digits, the version '" + input + "' is invalid");
			ix++;
		}
		String prefix = input.substring(0, ix);

		//-- Get any suffix, if applicable
		while(len > ix) {
			char c = input.charAt(len - 1); // Character before
			if(Character.isDigit(c))
				break;
			if(!Character.isLetter(c))
				throw new InvalidVersionNumberException("A version number must end with either letters or digits, the version '" + input + "' is invalid");
			len--;
		}
		if(ix >= len)
			throw new InvalidVersionNumberException("The version number '" + input + "' is invalid (no dotted number present)");
		String suffix = input.substring(len);

		//-- Handle the dotted #
		String vns = input.substring(ix, len);
		String[] ar = vns.split("\\.");
		int[] res = new int[ar.length];
		ix = 0;
		for(String v : ar) {
			try {
				res[ix] = Integer.parseInt(v);
				if(res[ix] < 0)
					throw new RuntimeException();
				ix++;
			} catch(Exception x) {
				throw new InvalidVersionNumberException("The version number '" + input + "' is invalid (invalid number '" + v + "' in dotted number)");
			}
		}

		//-- Normalize the version# by removing all trailing 0
		ix = res.length;
		while(ix > 0 && res[ix - 1] == 0)
			ix--;
		if(ix != res.length) { // Has the array been normalized?
			int[] nr = new int[ix];
			System.arraycopy(res, 0, nr, 0, ix);
			res = nr;
		}
		if(res.length == 0)
			throw new InvalidVersionNumberException("The version number '" + input + "' is invalid (invalid dotted number present)");
		return new VersionNumber(prefix, res, suffix);
	}

	@Override
	public int compareTo(VersionNumber o) {
		int r = m_prefix.compareTo(o.m_prefix);
		if(r != 0)
			return r;
		r = compareVersion(m_version, o.m_version);
		if(r != 0)
			return r;
		return m_suffix.compareTo(o.m_suffix);
	}

	static private int compareVersion(int[] a, int[] b) {
		if(a == null && b == null)
			return 0;
		else if(a != null && b == null)
			return -1;
		else if(a == null && b != null)
			return 1;

		if(a == null) {
			throw new IllegalStateException("This is not possible because of the previous checks.");
		}

		if(a.length == 0 && b.length == 0)
			return 0;

		int clen = a.length; // Find common length
		if(clen > b.length)
			clen = b.length;

		//-- Common length comparison
		for(int i = 0; i < clen; i++) {
			int res = a[i] - b[i];
			if(res != 0)
				return res < 0 ? -1 : 1;
		}

		//-- Common lengths are same.
		if(a.length == b.length)
			return 0;

		//-- Treat 3.0 and 3.0.0 the same (equal versions)
		if(a.length > b.length) {
			if(isRestZero(a, clen))
				return 0;
			return 1; // a is bigger version  (3.0.0.0.1 vs 3.0)
		} else {
			if(isRestZero(b, clen))
				return 0;
			return -1; // a is bigger version  (3.0.0.0.1 vs 3.0)
		}
	}

	static private boolean isRestZero(int[] a, int ix) {
		while(ix < a.length) {
			if(a[ix++] != 0)
				return false;
		}
		return true;
	}

	/**
	 * Returns T if both numbers have the same prefix and suffix. Meaningful
	 * version# comparison can be done only if this condition holds really.
	 * @param o
	 * @return
	 */
	public boolean isSameBase(VersionNumber o) {
		if(null == o)
			return false;
		return m_prefix.equals(o.m_prefix) && m_suffix.equals(o.m_suffix);
	}

	/**
	 * Return T if the version number starts with 99 or higher, as an indication this is a development thing.
	 * @return
	 */
	public boolean isDevelopment() {
		return m_version[0] >= 99;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Test code.											*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @param in
	 * @param out
	 * @throws Exception
	 */
	static private void check(String in, String out) throws Exception {
		VersionNumber vn = parse(in);
		String vns = vn.toString();
		if(vns.equals(out))
			return;
		System.out.println("ERROR: input '" + in + "' -> " + vns + " instead of '" + out + "'");

	}

	public static void main(String[] args) {
		try {
			VersionNumber vn = parse("vp4.0kv");
			System.out.println("vn = " + vn);
			check("vp4.0kv", "vp4kv");
			check("4.10", "4.10");
			check("4.1.1kv", "4.1.1kv");

		} catch(Exception x) {
			x.printStackTrace();
		}
	}

}
