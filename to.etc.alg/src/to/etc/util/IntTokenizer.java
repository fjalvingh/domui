package to.etc.util;

import java.util.*;

/**
 * Tokenizer which can detokenize integers.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class IntTokenizer extends StringTokenizer {
	public IntTokenizer(String str) {
		super(str);
	}

	public IntTokenizer(String str, String delim) {
		super(str, delim);
	}

	public IntTokenizer(String str, String delim, boolean returndelim) {
		super(str, delim, returndelim);
	}

	/**
	 * Takes the next token and decodes it as an integer value.
	 * @return				the value
	 * @throws Exception	if the string was not a valid number.
	 */
	public int nextInt() throws Exception {
		String s = nextToken();
		try {
			return Integer.parseInt(s);
		} catch(Exception ex) {
			throw new Exception(s + ": expecting an integer value.");
		}
	}


	/**
	 * Takes the next token and decodes it as a long value.
	 * @return				the value
	 * @throws Exception	if the string was not a valid number.
	 */
	public long nextLong() throws Exception {
		String s = nextToken();
		try {
			return Long.parseLong(s);
		} catch(Exception ex) {
			throw new Exception(s + ": expecting an integer value.");
		}
	}


}
