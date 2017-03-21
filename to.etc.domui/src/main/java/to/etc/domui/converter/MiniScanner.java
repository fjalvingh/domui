/*
 * DomUI Java User Interface library
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
package to.etc.domui.converter;

import to.etc.domui.trouble.*;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;

/**
 * A helper class which handles string scanning for converters.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 29, 2009
 */
public class MiniScanner {
	/** Cached copy of the instance */
	static private ThreadLocal<MiniScanner> m_current = new ThreadLocal<MiniScanner>();

	private String m_in;

	private int m_len;

	private int m_ix;

	private int m_val;

	private StringBuilder m_buffer = new StringBuilder(128);

	/**
	 * Get a miniscanner instance. This is the preferred method to allocate one if you need it a lot (it caches
	 * an instance in a threadlocal).
	 * @return
	 */
	static public MiniScanner getInstance() {
		MiniScanner ms = m_current.get();
		if(ms == null) {
			ms = new MiniScanner();
			m_current.set(ms);
		}
		return ms;
	}

	/**
	 * Reset the scanner for a next string to scan.
	 * @param in
	 */
	public void init(String in) {
		m_in = in.trim();
		m_ix = 0;
		m_len = m_in.length();
		if(m_buffer.length() > 256)
			m_buffer = new StringBuilder(128);
		else
			m_buffer.setLength(0);
	}

	/**
	 * Return the character at the current location, or -1 if at end of the string.
	 * @return
	 */
	public int LA() {
		if(m_ix >= m_len)
			return -1;
		return (0xffff & m_in.charAt(m_ix));
	}

	/**
	 * Return the nth char after the current location, or -1 if that is past the end of the string.
	 * @param ix
	 * @return
	 */
	public int LA(int ix) {
		if(m_ix + ix >= m_len)
			return -1;
		return (0xffff & m_in.charAt(m_ix + ix));
	}

	/**
	 * Move past the "current" character to the next one; return false if eof.
	 */
	public boolean accept() {
		if(m_ix < m_len)
			m_ix++;
		return m_ix < m_len;
	}

	/**
	 * Accept the current character and copy it to the buffer; return eof if we are at eof after the accept.
	 * @return
	 */
	public boolean copy() {
		if(m_ix >= m_len)
			return false;
		m_buffer.append(m_in.charAt(m_ix++)); // Copy to buffer
		return m_ix < m_len;
	}

	public void copy(int i) {
		while(i-- > 0)
			copy();
	}

	/**
	 * If the current character equals the specified one skip it by accept() and return true, else
	 * do nothing and return false.
	 * @param c
	 * @return
	 */
	public boolean skip(char c) {
		if(LA() == (c & 0xffff)) {
			accept();
			return true;
		}
		return false;
	}

	/**
	 * Return T if at eof.
	 * @return
	 */
	public boolean eof() {
		return m_ix >= m_len;
	}

	/**
	 * Returns the string built in the buffer and clears the buffer in the process.
	 * @return
	 */
	public String	getStringResult() {
		String s = m_buffer.toString();
		if(m_buffer.length() > 256)
			m_buffer = new StringBuilder(128);
		else
			m_buffer.setLength(0);
		return s;
	}

	/**
	 * Returns T if all that is left is whitespace before eof.
	 * @return
	 */
	public boolean eofSkipWS() {
		if(eof())
			return true;
		for(int ix = m_ix; ix < m_len; ix++) {
			char c = m_in.charAt(ix);
			if(!DomUtil.isWhitespace(c))
				return false;
		}
		return true;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Lax scanner for euro amounts.						*/
	/*--------------------------------------------------------------*/
	/**
	 * Scans the input as a lax euro string and leave the buffer to hold
	 * a parseable numeric string for one of the to-java-type converters.
	 */
	public boolean scanLaxWithCurrencySign(String in, int scale, boolean useStrictScale) throws ValidationException {
		return scanLax(in, true, scale, useStrictScale);
	}

	/**
	 * Does LAX Scanning on input using numeric (non monetary) values parser.
	 * @param input
	 * @param scale
	 * @param useStrictScale
	 * @return
	 * @throws ValidationException
	 */
	public boolean scanLaxNumber(String input, int scale, boolean useStrictScale) throws ValidationException {
		return scanLax(input, false, scale, useStrictScale);
	}

	private boolean scanCurrencySign() {
		if(skip('\u20ac'))
			return true;
		String cs = NlsContext.getCurrencySymbol();
		for(int i = 0; i < cs.length(); i++) {
			if(!skip(cs.charAt(i)))
				return false;
		}
		return true;
	}

	/**
	 * EXPERIMENTAL: Tries to fix problems with custom scale.
	 * Scans the input as a lax euro string and leave the buffer to hold
	 * a parseable numeric string for one of the to-java-type converters.
	 * TESTING: method is tested via verbose testing on NumberConverter that uses internaly this method
	 *
	 * @param in
	 * @param monetary
	 * @param scale	What scale do we use?
	 * @param useStrictScale If set to T, we expect that in inputs need to be formatted to comply with specified scale.
	 * @return
	 * @throws ValidationException
	 */
	private boolean scanLax(String in, boolean monetary, int scale, boolean useStrictScale) throws ValidationException {
		init(in.trim()); // Remove leading and trailing spaces
		boolean haseur = scanCurrencySign();
		skipWs(); // And any ws after
		if(eof()) {
			if(haseur) // Euro sign without anything around it is error
				badamount(monetary);
			return false; // But accept whitespace only (empty input, null value)
		}
		if(haseur && !monetary)
			badnumber();

		if(skip('-')) { // Leading minus?
			m_buffer.append('-');
			skipWs();
		} else if(m_in.endsWith("-")) { // trailing minus?
			m_buffer.append('-');
			m_len = m_len - 1;
		}

		/*
		 * There's more: we should only have digits, dots and comma's here forming some valid numeric
		 * representation. We will determine which one is decimal point and which one is thousands
		 * separator while parsing. The output (in the buffer) will consist of only digits and an optional
		 * dot (always used as the decimal point). This leaves the buffer suitable for parsing by one
		 * of the Java standard numeric type wrappers.
		 *
		 * Example parseable number:
		 * 0123456789012345678
		 * 1,000,000.55
		 */
		int cix = 0; // Relative index in the string, to mark . and , locations.
		int dotct = 0; // #of . encountered so far.
		int commact = 0; // #of , encountered so far
		int lastdotix = -1; // Last location of an encountered .
		int lastcommaix = -1; // Last location of an encountered ,
		int ndigits = 0;

		while(!eof()) {
			char c = (char) LA(); // Cannot have eof here, so there.
			if(c == ',') {
				commact++;
				if(commact > 1) {
					//-- COMMA Encountered at least 2ce, this means this MUST be the thousands separator. Issue checks for that, then;
					if(dotct > 1) // Also has more dots - cannot determine thousands separator.
						badamount(monetary);

					//-- Since this is thousands separator the last encounter must be 3 pos ago
					if(lastcommaix + 4 != cix) // Bad location of thousands separator (4 = 3 digits + separator).
						badamount(monetary);
				}
				lastcommaix = cix;
				accept();
			} else if(c == '.') {
				dotct++;
				if(dotct > 1) {
					//-- DOT Encountered at least 2ce, this means this MUST be the thousands separator. Issue checks for that, then;
					if(commact > 1) // Also has more commas - cannot determine thousands separator.
						badamount(monetary);

					//-- Since this is thousands separator the last encounter must be 3 pos ago
					if(lastdotix + 4 != cix) // Bad location of thousands separator (4 = 3 digits + separator).
						badamount(monetary);
				}
				lastdotix = cix;
				accept();
			} else if(Character.isDigit(c)) {
				copy(); // copy to output buffer,
				ndigits++;
			} else
				badamount(monetary);
			cix++;
		}

		if(ndigits == 0) // Got some commas and stuff but no digits?
			badamount(monetary);

		/*
		 * Basic parsing is over and real silly errors have been fixed. We now need to formally decide
		 * on a thousands separator and a decimal point and check the final format of the number; then
		 * we need to insert the decimal point at the correct location in the output buffer (because it
		 * only contains digits up till now).
		 */
		//-- Have we had either comma's or dots? If not we're done and the number is valid in the buffert.
		if(dotct == 0 && commact == 0) {
			if(scale > 0 && useStrictScale) {
				badamount(monetary);
			}
			return true;
		}
		if(dotct > 1 && commact > 1) // Cannot happen
			throw new IllegalStateException("Programmer's blunder: dotct=" + dotct + ", commact=" + commact);

		//-- Easy cases in determining decimal point and comma
		int decimalpos = -1; // Location of the decimal point, or -1 if not there
		int lastthoupos = -1; // Location of the last thousands separator, or -1 if not there,

		if(dotct > 1) {
			//-- Dot is thousands, the , is decimal point.
			lastthoupos = lastdotix;
			if(commact > 0)
				decimalpos = lastcommaix;
		} else if(commact > 1) {
			//-- Comma is thousands, the . is decimal point
			lastthoupos = lastcommaix;
			if(dotct > 0)
				decimalpos = lastdotix;
		} else if((dotct == 1 && commact == 0) || (dotct == 0 && commact == 1)) {

			//-- Only a single dot somewhere. Check it's location to see if it is decimal point or comma,
			int delta = (dotct == 1 ? cix - lastdotix : cix - lastcommaix) - 1; // #of chars after dot or comma
			int lastDotOrComma = (dotct == 1 ? lastdotix : lastcommaix);
			if(delta <= 2) {
				if(useStrictScale && scale != delta) {
					badamount(monetary);
				}
				//-- 0=ending in ., 1=.5, 2=.50 which are valid for decimal point.
				decimalpos = lastDotOrComma;
			} else if(delta == 3) {
				if(scale == 0) {
					//-- 3= .000 which is proper for thousands separator. Since that is checked by this we leave the lastthoupos unaltered
					if(ndigits <= 3) // Do not allow .000 but require 1.000
						badamount(monetary);
					lastthoupos = lastDotOrComma;
				} else if(scale == 3 && useStrictScale) {
					if(delta < 3) // Do not allow .000 but require 1.000
						badamount(monetary);
					decimalpos = lastDotOrComma; //this is actually decimal separator
				} else if(useStrictScale) {
					badamount(monetary);
				} else if(scale >= 3) {
					decimalpos = lastDotOrComma; //this is actually decimal separator
				}
			} else if(delta > scale) {
				badamount(monetary);
			} else if(delta == scale) {
				decimalpos = lastDotOrComma;
			} else if(useStrictScale) {
				badamount(monetary);
			} else {
				decimalpos = lastDotOrComma;
			}
		} else {
			//-- We have one dot and one comma. Which one is which?
			int cdelta = cix - lastcommaix - 1;
			int ddelta = cix - lastdotix - 1;

			if(lastcommaix - lastdotix == 4) { // comma is decimal point and delta is PROPER thousands separator
				if(cdelta == scale || (cdelta < scale && !useStrictScale)) {
					decimalpos = lastcommaix;
				} else {
					badamount(monetary);
				}
			} else if(lastdotix - lastcommaix == 4) { // Dot is decimal point
				if(ddelta == scale || (ddelta < scale && !useStrictScale)) {
					decimalpos = lastdotix;
				} else {
					badamount(monetary);
				}
			} else
				badamount(monetary);
		}

		//-- Do the final checks if still needed,
		if(decimalpos == -1) {
			if(lastthoupos != -1 && (cix - lastthoupos) != 4)
				badamount(monetary);
			if(scale > 0 && useStrictScale) {
				badamount(monetary);
			}
		} else {
			//-- We have a decimal point; check it's location;
			int ddelta = cix - decimalpos - 1;
			if(ddelta > scale)
				badamount(monetary);
			if(ddelta < scale && useStrictScale)
				badamount(monetary);
			if(lastthoupos != -1 && decimalpos - lastthoupos != 4)
				badamount(monetary);

			//-- Add decimal point in output buffer @ proper location
			if(ddelta > 0) {
				m_buffer.insert(m_buffer.length() - ddelta, '.');
			}
		}

		return true;
	}

	private void badnumber() throws ValidationException {
		throw new ValidationException(Msgs.V_INVALID, m_in);
	}

	private void badamount(boolean monetary) {
		throw new ValidationException(monetary ? Msgs.V_BAD_AMOUNT : Msgs.V_INVALID, m_in);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Code to scan for a "duration".						*/
	/*--------------------------------------------------------------*/
	/**
	 * Scan a number-delimiter pair.
	 * @return
	 */
	private char nextNumberDelimiter() {
		skipWs();
		if(m_ix >= m_len)
			return 0;

		//-- Scan the number
		m_val = 0;
		char c = ' ';
		while(m_ix < m_len) {
			c = m_in.charAt(m_ix);
			if(!Character.isDigit(c))
				break;
			m_val = m_val * 10 + (c - '0'); // Implement in #
			c = ' ';
			m_ix++;
		}
		skipWs();
		if(m_ix < m_len) {
			c = m_in.charAt(m_ix++);
			if(Character.isDigit(c))
				throw new IllegalStateException("invalid: # without separators.");
		} else
			c = 1;
		return c;
	}

	/**
	 * Returns the last integer "value" scanned.
	 * @return
	 */
	public int val() {
		return m_val;
	}

	public long scanDuration(String in) {
		init(in);

		long res = 0;


		for(;;) {

			char c = nextNumberDelimiter();
			if(c == 1) {
				//-- Lone #: is time in minutes,
				return val() * 60;
			}
			if(c == 'D' || c == 'd') {
				res += (long) val() * 24 * 60 * 60l;
				c = nextNumberDelimiter();
			} else if(c == 'H' || c == 'h' || c == 'U' || c == 'u') {
				res += (long) val() * 60 * 60l;
				c = nextNumberDelimiter();
			} else if(c == 's' || c == 'S') {
				res += val();
				c = nextNumberDelimiter();
			} else if(c == 0) {
				return res;
			} else if(c == ':') {
				res += val() * 60*60l;
				c = nextNumberDelimiter();
				c = 'S';
			} else {
				throw new ValidationException(Msgs.V_INVALID_DATE, "(Voorbeeld: 5d 8h)");
			}
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Globally useful thingies.							*/
	/*--------------------------------------------------------------*/
	/**
	 * Skip till the 1st non-ws character.
	 */
	public void skipWs() {
		while(m_ix < m_len) {
			char c = m_in.charAt(m_ix);
			if(!DomUtil.isWhitespace(c))
				return;
			m_ix++;
		}
	}

	public boolean match(String s) {
		if(s.length() > m_len - m_ix)
			return false;
		for(int i = 0; i < s.length(); i++) {
			if((s.charAt(i) & 0xffff) != LA(i))
				return false;
		}
		copy(s.length());
		return true;
	}


}
