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
package to.etc.csv;

import to.etc.util.StringTool;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Reads CSV files record by record, and implements the iLoadInputProvider interface
 * to access the fields.
 * Created on Oct 13, 2003
 *
 * @author jal
 */
public class CSVRecordReader {
	/**
	 * The name for the input, for reporting pps.
	 */
	private String m_name;

	/**
	 * The source thing to read the data from
	 */
	//private Reader m_r;

	private LineNumberReader m_lineReader;

	/**
	 * Current line #
	 */
	int m_lnr;

	/**
	 * When T, whitespace between fields is skipped
	 */
	private boolean m_skipWs = true;

	/**
	 * The list of fields for the CURRENT record.
	 */
	private List<Field> m_fldList = new ArrayList<>();

	private List<String> m_fldSepList = new ArrayList<>();

	/**
	 * All characters that are allowed as quote characters
	 */
	private StringBuilder m_quoteSb = new StringBuilder();

	/**
	 * Ignore all quotes.
	 */
	private boolean m_ignoreQuotes = false;

	/**
	 * When set, any quote is escaped by the backslash character (C mode).
	 */
	private boolean m_escapeBackslash = true;

	/**
	 * When set quotes are escaped by repeating them (BASIC mode)
	 */
	private boolean m_escapeDupQuote = false;

	/**
	 * If T, the first line is read as a set of field names.
	 */
	private boolean m_startWithFieldNames;

	/**
	 * We can use a whitespace as separator. Signal this, otherwise it will be skipped as a whitespace.
	 */
	private boolean m_whitespaceSeparator;

	/**
	 * When set this allows shitty escaping: when quotes are not followed by a field separator they
	 * are assumed to be within the field data.
	 */
	private boolean m_escapeBadly = false;

	private class Field implements IInputField {
		//		int		m_lpos;

		int m_index;

		String m_fldName;

		String m_value;

		int m_fieldLnr;

		public Field() {
		}

		public void setName(String name) {
			m_fldName = name;
		}

		/**
		 * Return the real name of the field, or the numeric name.
		 *
		 * @see IInputField#getName()
		 */
		public String getName() {
			if(m_fldName == null)
				return "#" + m_index;
			return m_fldName;
		}

		/**
		 * @see IInputField#getValue()
		 */
		public String getValue() {
			return m_value;
		}

		/**
		 * @see IInputField#isEmpty()
		 */
		public boolean isEmpty() {
			return m_value == null || m_lnr != m_fieldLnr;
		}

		public void setValue(String s) {
			m_fieldLnr = m_lnr;
			m_value = s;
		}
	}

	public void open(Reader r, String name) throws Exception {
		m_name = name;
		m_lnr = 0;
		//m_r = r;
		m_lineReader = new LineNumberReader(r);
	}

	public void close() throws Exception {
		m_lineReader.close();
	}

	private void error(String s) throws IOException {
		throw new IOException(m_name + "(" + m_lnr + ":" + m_ix + "): " + s);
	}

	public int getCurrentRecNr() {
		return m_lnr;
	}

	public void setFieldSeparator(String sep) {
		m_fldSepList.clear();
		m_fldSepList.add(sep);
	}

	public void addFieldSeparator(String sep) {

		// Check if we are dealing with a whitespace separator.
		if(sep.length() == 1 && Character.isWhitespace(sep.charAt(0)))
			setWhitespaceSeparator(true);
		m_fldSepList.add(sep);
	}

	/**
	 * When T, whitespace between fields is skipped
	 */
	public void setSkipWhitespace(boolean skipWs) {
		m_skipWs = skipWs;
	}

	public void setIgnoreQuotes(boolean ignoreQuotes) {
		m_ignoreQuotes = ignoreQuotes;
	}

	public void setEscapeBackslash(boolean escapeBackslash) {
		m_escapeBackslash = escapeBackslash;
	}

	public void setEscapeDupQuote(boolean escapeDupQuote) {
		m_escapeDupQuote = escapeDupQuote;
	}

	public void setStartWithFieldNames(boolean startWithFieldNames) {
		m_startWithFieldNames = startWithFieldNames;
	}

	public void setEscapeBadly(boolean escapeBadly) {
		m_escapeBadly = escapeBadly;
	}

	private boolean internalNextRecord() throws IOException {
		String line = m_lineReader.readLine();
		if(line == null)
			return false;
		m_lnr++;
		decode(line);
		return true;
	}

	/**
	 * Read the next (or first) record from the input and prepare it for
	 * processing.
	 */
	public boolean nextRecord() throws IOException {
		// Reading line 1?
		if(m_startWithFieldNames && m_lnr == 0) {
			if(!internalNextRecord()) // Try to read,
				return false;
			//-- Move the values to the field names.
			for(int i = size(); --i >= 0; ) {
				Field field = m_fldList.get(i);
				field.m_fldName = field.m_value;
			}
		}
		return internalNextRecord();
	}

	/**
	 * Method to see if it is an empty line. This is to make
	 * config files ending with an extra newline not cause unnecessary errors
	 */
	public boolean isEmptyLine() {
		return m_len == 0;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Parser.												*/
	/*--------------------------------------------------------------*/
	private int m_len;

	private int m_ix;

	private int m_fldIx;

	private String m_line;

	/**
	 * Parses a single line into fields. This fills the field set with
	 * data from the record.
	 */
	private void decode(String line) throws IOException {
		//		System.out.println("Decode line "+m_lnr+": "+line);
		m_line = line;
		m_len = line.length();
		m_ix = 0;
		m_fldIx = 0;
		for(Field field : m_fldList) {
			field.setValue(null);
		}

		//-- Start the parse.
		while(m_ix < m_len) {
			if(m_skipWs)
				m_ix = checkForWS(m_line, m_ix); // Get past whitespace if needed,
			if(m_ix >= m_len)
				break;
			parseField();
			if(m_skipWs)
				m_ix = checkForWS(m_line, m_ix); // Get past whitespace if needed,
			if(m_ix >= m_len)
				break;
			parseSeparator();
		}
	}

	public final String getLine() {
		return m_line;
	}

	/**
	 * Defines fieldnames using a comma or semicolon separated field name string.
	 */
	public void defineFields(String fields) {
		StringTokenizer st = new StringTokenizer(fields, ";,");
		int ix = 0;
		while(st.hasMoreTokens()) {
			String name = st.nextToken().trim();
			if(name.length() > 2) {
				char c = name.charAt(0);
				if(c == '"' || c == '\'' || c == '`') {
					char ec = name.charAt(name.length() - 1);
					if(ec == c) {
						name = name.substring(1, name.length() - 1);
					}
				}
			}

			IInputField f = getField(ix);
			f.setName(name);
			ix++;
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Field parser.										*/
	/*--------------------------------------------------------------*/
	private void parseField() throws IOException {
		char c = m_line.charAt(m_ix);
		if(isQuote(c)) {
			parseQuoted();
			return;
		}

		//-- Unquoted field. Stop as soon as a separator is found.
		int six = m_ix; // Save start position,
		// While there's data
		while(m_ix < m_len) {
			int sl = checkForSeparator(m_line, m_ix);
			if(sl != 0) // Separator at current location?
				break;
			m_ix++; // To next char
		}
		if(m_ix == six) // No spaces between separators?
			addField(null); // Treat as NULL value
		else
			addLitField(six, m_line, m_ix - six); // add the field.
	}

	private void parseQuoted() throws IOException {
		char qc = m_line.charAt(m_ix++);
		StringBuilder sb = new StringBuilder();
		for(; ; ) {
			if(m_ix >= m_len)
				error("Missing end quote in field " + m_fldIx);
			int ql = checkEscapeQuote(m_line, m_ix, qc);
			if(ql > 0) {
				//-- Escaped quote found: add it,
				sb.append(qc); // Add quote char
				m_ix += ql; // And past it,
			} else {
				char c = m_line.charAt(m_ix++);
				if(c == qc)
					break;
				sb.append(c); // Add the char literally,
			}
		}

		//-- Field completed,
		addField(sb.toString());
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Field access.										*/
	/*--------------------------------------------------------------*/
	private Field elementAt(int i) {
		if(i > m_fldList.size())
			return null;
		return m_fldList.get(i);
	}

	public IInputField getField(int ix) {
		while(m_fldList.size() <= ix) {
			Field f = new Field();
			m_fldList.add(f);
			f.m_index = m_fldList.size() - 1;
		}
		return elementAt(ix);
	}

	public int size() {
		return m_fldIx;
	}

	private void addLitField(int spos, String line, int len) {
		//-- 1. Find/add a Field structure
		if(m_fldIx >= m_fldList.size())
			m_fldList.add(new Field());
		Field f = m_fldList.get(m_fldIx);
		f.m_index = m_fldIx++;
		//		f.m_lpos = spos;
		f.setValue(line.substring(spos, spos + len));
		//		System.out.println(">> addLitField "+f.m_value);
	}

	private void addField(String val) {
		//-- 1. Find/add a Field structure
		if(m_fldIx >= m_fldList.size())
			m_fldList.add(new Field());
		Field f = m_fldList.get(m_fldIx);
		f.m_index = m_fldIx++;
		//		f.m_lpos = spos;
		f.setValue(val);
		//		System.out.println(">> addField "+f.m_value);
	}

	public IInputField find(String name) {
		// Numeric reference?
		if(name.startsWith("#")) {
			int ix = StringTool.strToInt(name.substring(1), -1);
			if(ix < 0 || ix >= m_fldList.size())
				return null;
			return m_fldList.get(ix);
		}

		for(int i = m_fldList.size(); --i >= 0; ) {
			Field f = elementAt(i);
			if(f != null && f.getName().equalsIgnoreCase(name))
				return f;
		}
		return null;
	}

	public String getValue(int ix) {
		IInputField f = getField(ix);
		if(f == null)
			return null;
		return f.getValue();
	}

	public String getValue(String name) {
		IInputField f = find(name);
		if(f == null)
			return null;
		return f.getValue();
	}

	public int getIntValue(String name) throws IOException {
		IInputField f = find(name);
		if(f == null || f.isEmpty()) {
			error("Expecting an integer value in '" + name + "'");
			return 0; //This will never be returned but it fools the compiler into accepting that the null check was done.
		}

		return convertToInt(f.getValue(), name);
	}

	public int getIntValue(String name, int def) throws IOException {
		IInputField f = find(name);
		if(f == null || f.isEmpty())
			return def;
		return convertToInt(f.getValue(), name);
	}

	private int convertToInt(String val, String field) throws IOException {
		try {
			return Integer.parseInt(val.trim());
		} catch(Exception x) {
			error("Expecting integer value in '" + field + "', got '" + val + "'");
			return -1;
		}
	}

	public long getLongValue(String name) throws IOException {
		IInputField f = find(name);
		if(f == null || f.isEmpty()) {
			error("Expecting an long value in '" + name + "'");
			return 0; //This will never be returned but it fools the compiler into accepting that the null check was done.
		}
		return convertToLong(f.getValue(), name);
	}

	public long getLongValue(String name, long def) throws IOException {
		IInputField f = find(name);
		if(f == null || f.isEmpty())
			return def;
		return convertToLong(f.getValue(), name);
	}

	private long convertToLong(String val, String field) throws IOException {
		try {
			return Long.parseLong(val.trim());
		} catch(Exception x) {
			error("Expecting long value in '" + field + "', got '" + val + "'");
			return -1;
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Field separators.									*/
	/*--------------------------------------------------------------*/

	/**
	 * Checks if the current position contains a separator. If so, this
	 * skips the separator and exits; else it throws an error.
	 */
	private void parseSeparator() throws IOException {
		int sl = checkForSeparator(m_line, m_ix);
		if(sl == 0)
			error("Missing field separator in input file " + m_name);
		m_ix += sl;

		if(m_fldIx >= m_fldList.size())
			m_fldList.add(new Field());
		Field f = m_fldList.get(m_fldIx);
		f.m_index = m_fldIx;
		//		f.m_lpos = m_ix;
		f.setValue(null);
	}

	private int checkForSeparator(String line, int ix) {
		if(m_fldSepList.isEmpty()) // Make sure that at least 1 separator (comma) is registered
			m_fldSepList.add(",");
		for(int i = m_fldSepList.size(); --i >= 0; ) {
			int sc = checkForSeparator(m_fldSepList.get(i), line, ix);
			if(sc > 0) // This IS a separator
				return sc;
		}
		return 0;
	}

	/**
	 * Checks if the separator specified is at the current location, and
	 * if so returns the #chars to skip past it.
	 */
	private int checkForSeparator(String sep, String line, int ix) {
		int len = line.length();
		int six = ix;
		if(m_skipWs)
			ix = checkForWS(line, ix);
		if(ix + sep.length() > len)
			return 0;
		if(line.substring(ix, ix + sep.length()).equalsIgnoreCase(sep))
			return ix + sep.length() - six;
		return 0;
	}

	/**
	 * Returns the first non-whitespace character on the line (can be eoln)
	 */
	private int checkForWS(String line, int ix) {
		int len = line.length();
		while(ix < len && Character.isWhitespace(line.charAt(ix))) {
			// It's possible we have a Whitespace character as separator.
			// If so, return
			if(hasWhitespaceSeparator()) {
				for(int i = m_fldSepList.size(); --i >= 0; ) {
					if((m_fldSepList.get(i)).length() == 1 && (m_fldSepList.get(i)).charAt(0) == line.charAt(ix))
						return ix;
				}
			}

			ix++;
		}
		return ix;
	}

	private int checkEscapeQuote(String line, int ix, char qc) {
		int len = line.length();
		if(m_escapeBackslash && ix + 2 <= len && line.charAt(ix) == '\\' && line.charAt(ix + 1) == qc)
			return 2;
		if(m_escapeDupQuote && ix + 2 <= len && line.charAt(ix) == qc && line.charAt(ix + 1) == qc)
			return 2;

		// Badly quoted: recognised if followed by non-separator.
		// Has at least 2 chars,
		if(m_escapeBadly && ix + 2 <= len && line.charAt(ix) == qc) {
			//-- If the thing after the quote is NOT a separator then assume this quote fits,
			int tix = ix + 1; // Past leading quote,
			if(m_skipWs)
				tix = checkForWS(line, tix);
			int sl = checkForSeparator(line, tix);
			if(sl == 0)
				return 1; // Not a separator-> assume this is an embedded quote.
		}
		return 0;
	}

	private boolean isQuote(char c) {
		if(!m_ignoreQuotes && m_quoteSb.isEmpty())
			m_quoteSb.append('\"');
		for(int i = m_quoteSb.length(); --i >= 0; ) {
			if(m_quoteSb.charAt(i) == c)
				return true;
		}
		return false;
	}

	private boolean hasWhitespaceSeparator() {
		return m_whitespaceSeparator;
	}

	private void setWhitespaceSeparator(boolean separator) {
		m_whitespaceSeparator = separator;
	}
}
