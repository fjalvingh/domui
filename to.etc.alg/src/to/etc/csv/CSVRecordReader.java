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

import java.io.*;
import java.util.*;

import to.etc.util.*;

/**
 * Reads CSV files record by record, and implements the iLoadInputProvider interface
 * to access the fields.
 * Created on Oct 13, 2003
 * @author jal
 */
public class CSVRecordReader implements iRecordReader {
	/** The name for the input, for reporting pps. */
	private String				m_name;

	/** The source thing to read the data from */
	private Reader				m_r;

	private LineNumberReader	m_line_r;

	/** Current line # */
	int							m_lnr;

	/** When T, whitespace between fields is skipped */
	private boolean				m_skip_ws			= true;

	/** The list of fields for the CURRENT record. */
	private List<Field>			m_fld_al			= new ArrayList<Field>();

	private List<String>		m_fldsep_al			= new ArrayList<String>();

	/** All characters that are allowed as quote characters */
	private StringBuffer		m_quote_sb			= new StringBuffer();

	/** Ignore all quotes. */
	private boolean				m_ignoreQuotes		= false;

	/** When set, any quote is escaped by the backslash character (C mode). */
	private boolean				m_escapeBackslash	= true;

	/** When set quotes are escaped by repeating them (BASIC mode) */
	private boolean				m_escapeDupQuote	= false;

	/** If T, the first line is read as a set of field names. */
	private boolean				m_startWithFieldNames;

	/** We can use a whitespace as separator. Signal this, otherwise it will be skipped as a whitespace. */
	private boolean				m_whitespaceSeparator;

	/**
	 * When set this allows shitty escaping: when quotes are not followed by a field separator they
	 * are assumed to be within the field data.
	 */
	private boolean				m_escapeBadly		= false;

	private class Field implements iInputField {
		//		int		m_lpos;

		int		m_index;

		String	m_fldname;

		String	m_value;

		int		m_field_lnr;

		public Field() {
		}

		public void setName(String name) {
			m_fldname = name;
		}


		/**
		 * Return the real name of the field, or the numeric name.
		 * @see to.etc.csv.iInputField#getName()
		 */
		public String getName() {
			if(m_fldname == null)
				return "#" + m_index;
			return m_fldname;
		}

		/**
		 *
		 * @see to.etc.csv.iInputField#getValue()
		 */
		public String getValue() {
			return m_value;
		}

		/**
		 *
		 * @see to.etc.csv.iInputField#isEmpty()
		 */
		public boolean isEmpty() {
			return m_value == null || m_lnr != m_field_lnr;
		}

		public void setValue(String s) {
			m_field_lnr = m_lnr;
			m_value = s;
		}
	}

	public void open(Reader r, String name) throws Exception {
		m_name = name;
		m_lnr = 0;
		m_r = r;
		m_line_r = new LineNumberReader(m_r);
	}

	public void close() throws Exception {
		m_line_r.close();
	}

	private void error(String s) throws IOException {
		throw new IOException(m_name + "(" + m_lnr + ":" + m_ix + "): " + s);
	}

	public int getCurrentRecNr() {
		return m_lnr;
	}

	public void setFieldSeparator(String sep) {
		m_fldsep_al.clear();
		m_fldsep_al.add(sep);
	}

	public void addFieldSeparator(String sep) {

		// Check if we are dealing with a whitespace separator.
		if(sep.length() == 1 && Character.isWhitespace(sep.charAt(0)))
			setWhitespaceSeparator(true);
		m_fldsep_al.add(sep);
	}

	/** When T, whitespace between fields is skipped */
	public void setSkipWhitespace(boolean skip_ws) {
		m_skip_ws = skip_ws;
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

	private boolean _nextRecord() throws IOException {
		String line = m_line_r.readLine();
		if(line == null)
			return false;
		m_lnr++;
		decode(line);
		return true;
	}

	/**
	 * Read the next (or first) record from the input and prepare it for
	 * processing.
	 * @return
	 */
	public boolean nextRecord() throws IOException {
		if(m_startWithFieldNames && m_lnr == 0) // Reading line 1?
		{
			if(!_nextRecord()) // Try to read,
				return false;
			//-- Move the values to the field names.
			for(int i = size(); --i >= 0;)
				elementAt(i).m_fldname = elementAt(i).m_value;
		}
		return _nextRecord();
	}

	/**
	 * @author mbp, added method to see if it is an empty line. This is to make
	 * config files ending with an extra newline not cause unnecessary errors
	 */
	public boolean isEmptyLine() {
		return m_len == 0;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Parser.												*/
	/*--------------------------------------------------------------*/
	private int		m_len;

	private int		m_ix;

	private int		m_fld_ix;

	private String	m_line;

	/**
	 * Parses a single line into fields. This fills the field set with
	 * data from the record.
	 * @param line
	 */
	private void decode(String line) throws IOException {
		//		System.out.println("Decode line "+m_lnr+": "+line);
		m_line = line;
		m_len = line.length();
		m_ix = 0;
		m_fld_ix = 0;

		//-- Start the parse.
		while(m_ix < m_len) {
			if(m_skip_ws)
				m_ix = checkForWS(m_line, m_ix); // Get past whitespace if needed,
			if(m_ix >= m_len)
				break;
			parseField();
			if(m_skip_ws)
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
	 * @param fields
	 */
	public void defineFields(String fields) {
		StringTokenizer st = new StringTokenizer(fields, ";,");
		int ix = 0;
		while(st.hasMoreTokens()) {
			String name = st.nextToken().trim();
			if(name.length() > 2) {
				char c = name.charAt(0);
				if(c == '"' || c == '\"' || c == '`') {
					char ec = name.charAt(name.length() - 1);
					if(ec == c) {
						name = name.substring(1, name.length() - 1);
					}
				}
			}

			iInputField f = getField(ix);
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
		while(m_ix < m_len) // While there's data
		{
			int sl = checkForSeparator(m_line, m_ix);
			if(sl != 0) // Separator at current location?
				break;
			m_ix++; // To next char
		}
		if(m_ix == six) // No spaces between separators?
			addField(m_ix, null); // Treat as NULL value
		else
			addLitField(six, m_line, m_ix - six); // add the field.
	}

	private void parseQuoted() throws IOException {
		int spos = m_ix;
		char qc = m_line.charAt(m_ix++);
		StringBuffer sb = new StringBuffer();
		for(;;) {
			if(m_ix >= m_len)
				error("Missing end quote in field " + m_fld_ix);
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
		addField(spos, sb.toString());
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Field access.										*/
	/*--------------------------------------------------------------*/
	private Field elementAt(int i) {
		if(i > m_fld_al.size())
			return null;
		return m_fld_al.get(i);
	}

	public iInputField getField(int ix) {
		while(m_fld_al.size() <= ix) {
			Field f = new Field();
			m_fld_al.add(f);
			f.m_index = m_fld_al.size() - 1;
		}
		return elementAt(ix);
	}

	public int size() {
		return m_fld_ix;
	}

	private void addLitField(int spos, String line, int len) {
		//-- 1. Find/add a Field structure
		if(m_fld_ix >= m_fld_al.size())
			m_fld_al.add(new Field());
		Field f = m_fld_al.get(m_fld_ix);
		f.m_index = m_fld_ix++;
		//		f.m_lpos = spos;
		f.setValue(line.substring(spos, spos + len));
		//		System.out.println(">> addLitField "+f.m_value);
	}

	private void addField(int spos, String val) {
		//-- 1. Find/add a Field structure
		if(m_fld_ix >= m_fld_al.size())
			m_fld_al.add(new Field());
		Field f = m_fld_al.get(m_fld_ix);
		f.m_index = m_fld_ix++;
		//		f.m_lpos = spos;
		f.setValue(val);
		//		System.out.println(">> addField "+f.m_value);
	}

	public iInputField find(String name) {
		if(name.startsWith("#")) // Numeric reference?
		{
			int ix = StringTool.strToInt(name.substring(1), -1);
			if(ix < 0 || ix >= m_fld_al.size())
				return null;
			return elementAt(ix);
		}

		for(int i = m_fld_al.size(); --i >= 0;) {
			Field f = elementAt(i);
			if(f.getName().equalsIgnoreCase(name))
				return f;
		}
		return null;
	}

	public String getValue(int ix) {
		iInputField f = getField(ix);
		if(f == null)
			return null;
		return f.getValue();
	}

	public String getValue(String name) {
		iInputField f = find(name);
		if(f == null)
			return null;
		return f.getValue();
	}

	public int getIntValue(String name) throws IOException {
		iInputField f = find(name);
		if(f == null || f.isEmpty()) {
			error("Expecting an integer value in '" + name + "'");
			return 0; //This will never be returned but it fools the compiler into accepting that the null check was done.
		}

		return convertToInt(f.getValue(), name);
	}

	public int getIntValue(String name, int def) throws IOException {
		iInputField f = find(name);
		if(f == null || f.isEmpty())
			return def;
		return convertToInt(f.getValue(), name);
	}

	private int convertToInt(String val, String field) throws IOException {
		try {
			return Integer.parseInt(val.trim());
		} catch(Exception x) {}
		error("Expecting integer value in '" + field + "', got '" + val + "'");
		return -1;
	}

	public long getLongValue(String name) throws IOException {
		iInputField f = find(name);
		if(f == null || f.isEmpty()) {
			error("Expecting an long value in '" + name + "'");
			return 0; //This will never be returned but it fools the compiler into accepting that the null check was done.
		}
		return convertToLong(f.getValue(), name);
	}

	public long getLongValue(String name, long def) throws IOException {
		iInputField f = find(name);
		if(f == null || f.isEmpty())
			return def;
		return convertToLong(f.getValue(), name);
	}

	private long convertToLong(String val, String field) throws IOException {
		try {
			return Long.parseLong(val.trim());
		} catch(Exception x) {}
		error("Expecting long value in '" + field + "', got '" + val + "'");
		return -1;
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

		if(m_fld_ix >= m_fld_al.size())
			m_fld_al.add(new Field());
		Field f = m_fld_al.get(m_fld_ix);
		f.m_index = m_fld_ix;
		//		f.m_lpos = m_ix;
		f.setValue(null);
	}

	private int checkForSeparator(String line, int ix) {
		if(m_fldsep_al.size() == 0) // Make sure that at least 1 separator (comma) is registered
			m_fldsep_al.add(",");
		for(int i = m_fldsep_al.size(); --i >= 0;) {
			int sc = checkForSeparator(m_fldsep_al.get(i), line, ix);
			if(sc > 0) // This IS a separator
				return sc;
		}
		return 0;
	}


	/**
	 * Checks if the separator specified is at the current location, and
	 * if so returns the #chars to skip past it.
	 * @param sep
	 * @param line
	 * @param ix
	 * @return
	 */
	private int checkForSeparator(String sep, String line, int ix) {
		int len = line.length();
		int six = ix;
		if(m_skip_ws)
			ix = checkForWS(line, ix);
		if(ix + sep.length() > len)
			return 0;
		if(line.substring(ix, ix + sep.length()).equalsIgnoreCase(sep))
			return ix + sep.length() - six;
		return 0;
	}

	/**
	 * Returns the first non-whitespace character on the line (can be eoln)
	 * @param line
	 * @param ix
	 * @return
	 */
	private int checkForWS(String line, int ix) {
		int len = line.length();
		while(ix < len && Character.isWhitespace(line.charAt(ix))) {
			// It's possible we have a Whitespace character as separator.
			// If so, return
			if(hasWhitespaceSeparator()) {
				for(int i = m_fldsep_al.size(); --i >= 0;) {
					if((m_fldsep_al.get(i)).length() == 1 && (m_fldsep_al.get(i)).charAt(0) == line.charAt(ix))
						return ix;
				}
			}

			ix++;
		}
		return ix;
	}

	private int checkEscapeQuote(String line, int ix, char qc) {
		int len = line.length();
		if(m_escapeBackslash) // Escape using \"
		{
			if(ix + 2 <= len) {
				if(line.charAt(ix) == '\\' && line.charAt(ix + 1) == qc)
					return 2;
			}
		}
		if(m_escapeDupQuote) // Escape using the quote char 2ce
		{
			if(ix + 2 <= len) {
				if(line.charAt(ix) == qc && line.charAt(ix + 1) == qc)
					return 2;
			}
		}

		if(m_escapeBadly) // Badly quoted: recognised if followed by non-separator.
		{
			if(ix + 2 <= len) // Has at least 2 chars,
			{
				if(line.charAt(ix) == qc) // Starts with quote,
				{
					//-- If the thing after the quote is NOT a separator then assume this quote fits,
					int tix = ix + 1; // Past leading quote,
					if(m_skip_ws)
						tix = checkForWS(line, tix);
					int sl = checkForSeparator(line, tix);
					if(sl == 0)
						return 1; // Not a separator-> assume this is an embedded quote.
				}
			}
		}
		return 0;
	}

	private boolean isQuote(char c) {
		if(!m_ignoreQuotes) {
			if(m_quote_sb.length() == 0)
				m_quote_sb.append('\"');
		}
		for(int i = m_quote_sb.length(); --i >= 0;) {
			if(m_quote_sb.charAt(i) == c)
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
