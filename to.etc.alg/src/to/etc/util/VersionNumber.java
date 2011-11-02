package to.etc.util;

public class VersionNumber {
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
