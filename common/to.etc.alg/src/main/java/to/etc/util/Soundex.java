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
 * Encodes words using the SOUNDEX algorithm.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class Soundex {
	/** Length, */
	public static final transient int	NO_MAX		= -1;

	/** T when final 's' of word is dropped */
	private boolean						m_dropLastS	= false;

	/** Length of code to build. */
	private int							m_lengthInt	= 4;

	/** T to pad codes to length with zeroes */
	private boolean						m_pad		= true;

	/** Table */
	protected int[]						m_soundexInts;

	final private static String			LowerS		= "s";

	/**
	 * Creates the Soundex code table.  You can over-ride to build your
	 * own default table.
	 */
	public Soundex() {
		m_soundexInts = new int[]{-1, //a
			1, //b
			2, //c
			3, //d
			-1, //e
			1, //f
			2, //g
			-1, //h
			-1, //i
			2, //j
			2, //k
			4, //l
			5, //m
			5, //n
			-1, //o
			1, //p
			2, //q
			6, //r
			2, //s
			3, //t
			-1, //u
			1, //v
			-1, //w
			2, //x
			-1, //y
			2 //z
		};
	}

	/**
	* Returns the Soundex code for the specified character.  If the char
	* is not in the range a-z or A-Z then -1 is returned.
	*/
	public int getCode(char c) {
		int arrayidx = -1;
		if(('a' <= c) || (c <= 'z'))
			arrayidx = c - 'a';
		if((arrayidx >= 0) && (arrayidx < m_soundexInts.length))
			return m_soundexInts[arrayidx];
		else
			return -1;
	}


	/**
	* If true, the final 's' or 'S' of the word being encoded is dropped.
	* False by default.
	*/
	public boolean getDropLastS() {
		return m_dropLastS;
	}


	/**
	* Returns the length of code strings to build, 4 by default.
	* If negative, length is unlimited.
	* @see #NO_MAX
	*/
	public int getLength() {
		return (m_lengthInt + 1);
	}


	/**
	* If true and a word is coded to a shorter length than getLength(),
	* the code will be padded with zeros. True by default.
	*/
	public boolean getPad() {
		return m_pad;
	}


	/**
	* Returns the soundex code for the specified word.	Characters not in
	* the range a-z or A-Z are dropped.
	* @param string The word to code.
	*/
	public String soundex(String string) {
		string = string.toLowerCase();
		if(m_dropLastS) {
			if((string.length() > 1) && string.endsWith(LowerS))
				string = string.substring(0, (string.length() - 1));
		}
		string = reduce(string);
		int lengthInt = string.length(); //original string size
		int codesInt = 0; //how many codes have been created
		int maxInt = m_lengthInt - 1; //max number of codes, - 1, for first char
		if(m_lengthInt < 0) // in other words, NO_MAX
			maxInt = lengthInt; //lengthInt was the max possible size.
		int sndexInt = 0;
		char currentChar;
		StringBuilder buffer = new StringBuilder(maxInt);
		buffer.append(string.charAt(0));
		for(int i = 1; (i < lengthInt) && (codesInt < maxInt); i++) {
			currentChar = string.charAt(i);
			sndexInt = getCode(currentChar);
			if(sndexInt > 0) {
				buffer.append(sndexInt);
				codesInt++;
			}
		}
		if(m_pad && (m_lengthInt > 0)) {
			for(; codesInt < maxInt; codesInt++)
				buffer.append('0');
		}
		return buffer.toString();
	}


	/**
	* Displays the codes for the parameters.
	*/
	public static void main(String[] strings) {
		if((strings == null) || (strings.length == 0)) {
			throw new IllegalArgumentException("Specify some words and this will display a soundex code for each.");
		}
		Soundex s = new Soundex();
		for(int i = 0; i < strings.length; i++)
			System.out.println(s.soundex(strings[i]));
	}


	/**
	* Allows you to modify the default code table
	* @param c The character to specify the code for.
	* @param i The code to represent c with, must -1, or 0 thru 9
	*/
	public void setCode(char c, int i) {
		int arrayidx = -1;
		if(('a' <= c) || (c <= 'z'))
			arrayidx = c - 'a';
		if((0 <= arrayidx) && (arrayidx < m_soundexInts.length))
			m_soundexInts[arrayidx] = i;
	}


	/**
	* If true, the final 's' of the word being encoded is dropped.
	*/
	public void setDropLastS(boolean bool) {
		m_dropLastS = bool;
	}


	/**
	* Sets the length of code strings to build. 4 by default.
	* @param Length of code to produce, must be >= 1
	*/
	public void setLength(int lengthInt) {
		m_lengthInt = --lengthInt;
	}


	/**
	* If true, pads code to getLength() with zeros.  True, by default.
	*/
	public void setPad(boolean bool) {
		m_pad = bool;
	}

	/**
	* Removes adjacent sounds.
	*/
	protected String reduce(String string) {
		int lengthInt = string.length();
		StringBuilder buffer = new StringBuilder(lengthInt);
		char c = string.charAt(0);
		int currentCode = getCode(c);
		buffer.append(c);
		int lastCode = currentCode;
		for(int i = 1; i < lengthInt; i++) {
			c = string.charAt(i);
			currentCode = getCode(c);
			if((currentCode != lastCode) && (currentCode >= 0)) {
				buffer.append(c);
				lastCode = currentCode;
			}
		}
		return buffer.toString();
	}
}
