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
			throw new IllegalArgumentException(s + ": expecting an integer value.");
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
			throw new IllegalArgumentException(s + ": expecting an integer value.");
		}
	}


}
