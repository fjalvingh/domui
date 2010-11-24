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
package to.etc.domui.util.upload;

import java.util.*;


/**
 * This decodes the headers. Each header is on a separate line (where a line ends in CRLF). An empty
 * line denotes the end of the header area. Each header has the format name: value CRLF
 * <p>If a header name occurs twice the map will contain a List of the values.
 */
public class HeaderParser {
	private String m_str;

	private int m_ix;

	private int m_len;

	private String m_property;

	private String m_value;

	public HeaderParser() {}


	public final String getProperty() {
		return m_property;
	}

	public final void setProperty(final String property) {
		m_property = property;
	}

	public final String getValue() {
		return m_value;
	}

	public final void setValue(final String value) {
		m_value = value;
	}

	public void init(final String in) {
		m_str = in;
		m_ix = 0;
		m_len = in.length();
	}

	/**
	 * Parses the next header from the area. Returns false if the line end has been reached.
	 * @return
	 */
	public boolean parseNext() {
		int nch = 0;
		char c = 0;
		char lc = 0;

		m_property = null;
		m_value = null;

		//-- 1. Skip ws in the name.
		int sp = m_ix;
		int ep = sp;
		while(m_ix < m_len) {
			c = m_str.charAt(m_ix++);
			if(c == 10 && lc == 13) // eoln?
			{
				if(nch == 0) {
					m_ix -= 2; // Back to crlf
					return false; // we're done!
				}
				ep = m_ix - 2; // Endpos minus crlf
				break;
			}
			if(c == ':') {
				ep = m_ix - 1;
				break;
			}
			lc = c;
		}
		while(sp < ep && Character.isWhitespace(m_str.charAt(sp)))
			sp++;
		while(ep > sp && Character.isWhitespace(m_str.charAt(ep - 1)))
			ep--;
		if(sp >= ep)
			return false;
		m_property = m_str.substring(sp, ep);
		if(lc == 13 && c == 10)
			return true; // Have a name but no value -> keep

		//-- We have a ':': parse the value.
		sp = m_ix;
		ep = sp;
		lc = 0;
		while(m_ix < m_len) {
			c = m_str.charAt(m_ix++);
			if(c == 10 && lc == 13) // eoln?
			{
				ep = m_ix - 2; // Endpos minus crlf
				break;
			}
			lc = c;
		}
		while(sp < ep && Character.isWhitespace(m_str.charAt(sp)))
			sp++;
		while(ep > sp && Character.isWhitespace(m_str.charAt(ep - 1)))
			ep--;
		m_value = m_str.substring(sp, ep);
		return true;
	}

	public void parse(final Map<String, Object> m, final String hdr, final boolean lcnames) {
		m.clear();
		init(hdr);
		while(parseNext()) {
			String n = getProperty();
			String v = getValue();
			if(v == null)
				continue; // Skip malformed headers
			if(lcnames)
				n = n.toLowerCase();
			Object o = m.get(n);
			if(o == null)
				m.put(n, v);
			else if(o instanceof List< ? >) {
				((List<Object>) o).add(v);
			} else {
				List<String> l = new ArrayList<String>(3);
				l.add((String) o);
				l.add(v);
				m.put(n, l);
			}
		}
	}

}
