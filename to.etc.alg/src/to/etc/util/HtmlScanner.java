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
 * This class helps one to scan HTML documents. It contains stuff to scan
 * for tags, to decode attributes and the like.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class HtmlScanner {
	/** The document we're currently scanning */
	private String	m_doc;

	/** The length of the document we're scanning */
	private int		m_len;

	/** The current location we're at in the document. */
	private int		m_ix;

	/** The start index of the last token returned. */
	private int		m_start_ix;

	/** The last tag returned by nextTag() */
	private String	m_lasttag;

	private boolean	m_unquote;


	public HtmlScanner() {
	}

	public HtmlScanner(boolean unquote) {
		m_unquote = unquote;
	}

	static public String unquote(String s) {
		int l = s.length();
		if(l < 2)
			return s;

		char c1 = s.charAt(0);
		char c2 = s.charAt(l - 1);
		if(c1 != c2)
			return s;
		if(c1 == '\'' || c1 == '"')
			return s.substring(1, l - 1);
		return s;
	}

	public HtmlScanner duplicate() {
		HtmlScanner hs = new HtmlScanner();
		assignTo(this);
		return hs;
	}

	public void assignTo(HtmlScanner hs) {
		hs.m_doc = m_doc;
		hs.m_len = m_len;
		hs.m_ix = m_ix;
		hs.m_lasttag = m_lasttag;
		hs.m_start_ix = m_start_ix;
	}

	public void setDocument(String s) {
		m_doc = s;
		m_ix = 0;
		m_len = s.length();
		m_start_ix = 0;
	}

	public String getDocument() {
		return m_doc;
	}

	public void moveTo(int ix) {
		m_ix = ix;
	}

	public void reset() {
		m_ix = 0;
		m_start_ix = 0;
	}

	public int getPos() {
		return m_ix;
	}

	public int getStartPos() {
		return m_start_ix;
	}

	public int inc() {
		if(m_ix < m_len)
			m_ix++;
		return m_ix;
	}

	public String getCurrentTag() {
		return m_lasttag;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Scan for tags.										*/
	/*--------------------------------------------------------------*/
	/**
	 * This tries to find the tag specified starting from the current position
	 * in the document.
	 *
	 * @param name
	 * @return
	 */
	public boolean findTag(String name) {
		m_lasttag = null;
		int nl = name.length();

		int ix = m_ix;
		while(ix < m_len) {
			int tp = m_doc.indexOf('<', ix);
			if(tp == -1)
				return false; // Nothing found;

			//-- Starting thing found at tp. Is there a text immediately after?
			m_start_ix = tp;
			tp++; // Past <
			int te = scanTagName(tp); // Try to get a tag name,
			if(nl == (te - tp)) // Something was found with same len as input?
			{
				if(m_doc.regionMatches(true, tp, name, 0, nl)) {
					m_ix = tp - 1; // To <
					return true;
				}
			}
			ix = tp;
		}
		return false;
	}

	/**
	 * This scans for the next tag starting at the current position. If a next
	 * tag is found this returns the tag's name without the braces. The "current
	 * position" is left at the tag start character &lt;.
	 * @return	the tag name, or null if nothing was found.
	 */
	public String nextTag() {
		m_lasttag = null;
		int ix = m_ix;
		while(ix < m_len) {
			int tp = m_doc.indexOf('<', ix); // Find next tag start
			if(tp == -1)
				return null; // End of segment reached.
			tp++; // Past < to name,
			int te = scanTagName(tp); // Try to scan the name
			if(te > tp) // A name was indeed found?
			{
				String s = m_doc.substring(tp, te); // Make a name from the $
				m_ix = tp - 1;
				return (m_lasttag = s);
			}

			//-- Was not a tag. Move to next <
			ix = tp;
		}
		return null;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Parsing tag attributes.								*/
	/*--------------------------------------------------------------*/
	/**
	 * This must be called with the current position on a tag. It initializes
	 * for parsing the tag's attribute contents. It moves the current position
	 * past the tag, at the location where the 1st attribute would be if the
	 * tag had them.
	 * @return	the name of the tag.
	 */
	public String tagParseInit() {
		int ix = m_ix;
		if(ix >= m_len)
			throw new IllegalStateException("Current pos is eof.");
		if(m_doc.charAt(ix) != '<')
			throw new IllegalStateException("Current pos is not at tag start.");
		ix++;
		int ep = scanTagName(ix);
		if(ep <= ix)
			throw new IllegalStateException("Current pos is not at tag start.");
		String name = m_doc.substring(ix, ep);
		m_ix = ep;
		return name;
	}

	/**
	 * This tries to parse a parameter name from the current pos. The name must
	 * be alphanumeric and ends in either spaces or an equals sign. If the current
	 * pos does not represent a parameter then this returns null.
	 * @return	the parameter name or null if no parameter here.
	 */
	public String tagParseParamName() {
		int ix = skipWS(m_ix);

		//-- Must be valid attribute name or..
		int ep = scanAttributeName(ix);
		if(ep <= ix)
			return null; // No name here, dude.
		m_start_ix = m_ix;
		m_ix = ep;
		return m_doc.substring(ix, ep);
	}

	/**
	 * Can be called after tagParseParamname() returned a name. This returns
	 * the parameter value, or null if no value is present. The value includes
	 * any quotes if present. If the value is unquoted then it is delimited by
	 * the 1st space or &gt;.
	 * @return	a value string, or null if no value is present.
	 */
	public String tagParseValue() {
		int ix = skipWS(m_ix);
		if(ix >= m_len)
			return null; // At end -> no value
		if(m_doc.charAt(ix) != '=')
			return null; // No equals -> no value
		ix = skipWS(ix + 1); // Get to 1st meaningful thing after =
		if(ix >= m_len)
			return ""; // Empty string

		int vsix = ix;
		char qc = m_doc.charAt(ix); // Get evt. quote
		if(qc != '\'' && qc != '"') {
			//-- Not a quote - scan till 1st whitespace or >
			while(ix < m_len) {
				qc = m_doc.charAt(ix);
				if(!isAttrValueChar(qc))
					break;
				ix++;
			}

			//-- Set current pos and return a value
			m_ix = ix;
			String v = m_doc.substring(vsix, ix);
			return m_unquote ? unquote(v) : v;
		}

		//-- Quoted thing-
		ix++; // Past quote
		while(ix < m_len) {
			char c = m_doc.charAt(ix);
			if(c == qc) {
				ix++;
				break; // Found end quote
			}
			ix++;
		}

		m_ix = ix;
		String v = m_doc.substring(vsix, ix);
		return m_unquote ? unquote(v) : v;
	}

	/**
	 * Returns T if the current location contains the tag end &gt; character.
	 * @return
	 */
	public boolean tagIsTagEnd() {
		int ix = skipWS(m_ix);
		if(ix >= m_len)
			return true; // Prevent loop..
		if(m_doc.charAt(ix) == '>') {
			m_ix = ix + 1;
			return true;
		}
		if(m_doc.charAt(ix) == '/' && ix + 1 < m_len && m_doc.charAt(ix + 1) == '>') {
			m_ix = ix + 2;
			return true;
		}
		return false;
	}

	public boolean atEof() {
		return skipWS(m_ix) >= m_len;
	}

	public boolean atEof(int pos) {
		return pos >= m_len;
	}

	private int scanEndString(int six, char qc) {
		int ix = six;
		while(ix < m_len) {
			char c = m_doc.charAt(ix);
			if(c == qc)
				return ix + 1;
			ix++;
		}
		return six;
	}

	/**
	 * When called this parses the tag until the end of the tag is reached.
	 * @return T if it worked.
	 */
	public boolean tagToEnd() {
		int ix = m_ix;
		while(ix < m_len) {
			char c = m_doc.charAt(ix);
			if(c == '\'' || c == '"') {
				//-- Move to end of string;
				ix = scanEndString(ix + 1, c);
			} else if(c == '>') {
				m_ix = ix + 1;
				return true;
			} else
				ix++;
		}
		return false;
	}

	/**
	 * Finds the first matching end tag at the CURRENT level. Nested tags are
	 * counted.
	 * @param tag	The name of the tag, like "A".
	 * @return	T if a match was found. If so the current position will be at
	 * 			the start of the end tag.
	 */
	public boolean findMatchingEndTag(String tag) {
		int stix = m_ix; // Safeguard current index.
		int nl = 0; // Current nesting level.
		String etag = "/" + tag;

		//-- Loop: keep finding tags,
		for(;;) {
			String ts = nextTag();
			if(ts == null)
				break;

			if(ts.equalsIgnoreCase(etag)) {
				if(nl == 0)
					return true; // Found!!
				nl--;
			} else if(ts.equalsIgnoreCase(tag)) {
				nl++; // Increment nesting level
			}

			//-- Skip this tag dude
			tagParseInit();
			tagToEnd();
		}

		//-- No end tag found..
		m_ix = stix;
		return false;
	}


	public void skipTag() {
		if(m_ix >= m_len)
			return;
		if(m_doc.charAt(m_ix) != '<')
			return; // Not at a tag

		tagParseInit();
		tagToEnd();
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Small helpers.										*/
	/*--------------------------------------------------------------*/
	/**
	 * Scans from the pos at ix for valid tag names.
	 * @param six
	 * @return	the end index (exclusive) of the matched name, or same as input
	 * 			if nothing matched.
	 */
	private int scanTagName(int six) {
		int ix = six;

		//-- Move to the 1st non-tag character here,
		while(ix < m_len) {
			char c = m_doc.charAt(ix);
			if(!isTagChar(c))
				break;
			ix++;
		}
		return ix;
	}

	private int scanAttributeName(int six) {
		int ix = six;

		//-- Move to the 1st non-tag character here,
		while(ix < m_len) {
			char c = m_doc.charAt(ix);
			if(!isAttrNameChar(c))
				break;
			ix++;
		}
		return ix;
	}

	static private boolean isTagChar(char c) {
		return Character.isLetterOrDigit(c) || c == ':' || c == '.' || c == '_' || c == '/' || c == '!';
	}

	static private boolean isAttrValueChar(char c) {
		return Character.isLetterOrDigit(c) || c == ':' || c == '.' || c == '_';
	}

	static private boolean isAttrNameChar(char c) {
		return Character.isLetterOrDigit(c) || c == ':' || c == '.' || c == '_';
	}

	private int skipWS(int ix) {
		while(ix < m_len) {
			char c = m_doc.charAt(ix);
			if(!Character.isWhitespace(c))
				return ix;
			ix++;
		}
		return ix;
	}
}
