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
package to.etc.lexer;

import java.util.*;

import to.etc.util.*;

/**
 * Helper class to scan HTML and remove invalid constructs.
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 22, 2010
 */
public class HtmlTextScanner extends TextScanner {
	private List<String>	m_tagStack = new ArrayList<String>();

	static private final Map<String, TagInfo> DEFAULT_MAP = new HashMap<String, TagInfo>();

	private Map<String, TagInfo> m_acceptMap = DEFAULT_MAP;

	private StringBuilder m_sb;

	private String m_lastSingleTag;

	public HtmlTextScanner() {
	}

	static private class TagInfo {
		public boolean ends;

		//		public boolean args;

		public TagInfo(boolean ends, boolean args) {
			this.ends = ends;
			//			this.args = args;
		}
	}

	static private void p(String node, boolean ends, boolean args) {
		DEFAULT_MAP.put(node, new TagInfo(ends, args));
	}

	static private void p(String node, boolean ends) {
		p(node, ends, false);
	}

	static {
		p("b", false);
		p("i", false);
		p("u", false);
		p("em", false);
		p("strong", false);
		p("ul", false);
		p("li", false);
		p("br", true);
	}

	public Map<String, TagInfo> getMap() {
		return m_acceptMap;
	}

	/**
	 * Scan HTML and remove unsafe tags and attributes. The result is garantueed to be safe and well-formed.
	 * @param sb
	 * @param html
	 */
	public void scan(StringBuilder sb, String html) {
		setString(html);
		m_sb = sb;
		m_lastSingleTag = null;
		m_tagStack.clear();

		while(!eof()) {
			scanText(sb);
			if(eof())
				break;

			scanTag();
		}

		//-- Just flush the open tag stack
		while(m_tagStack.size() > 0) {
			sb.append("</");
			sb.append(m_tagStack.remove(m_tagStack.size() - 1));
			sb.append(">");
		}
	}

	/**
	 * Remove all HTML tags and collapse whitespace.
	 * @param sb
	 * @param html
	 * @param includelf
	 */
	public void scanAndRemove(StringBuilder sb, String html, boolean includelf) {
		setString(html);
		m_sb = sb;
		m_lastSingleTag = null;
		m_tagStack.clear();

		while(!eof()) {
			//-- 1. Scan text segment. Do not add >1 space between nonspace characters.
			boolean ws = false;
			while(!eof()) {
				int c = LA();
				accept();
				if(Character.isWhitespace(c))
					ws = true;
				else if(c == '<')
					break;
				else {
					if(ws) {
						sb.append(' ');
						ws = false;
					}
					sb.append((char) c);
				}
			}
			if(eof())
				break;

			//-- We must be @ a tag. Get it's name
			if(includelf) {
				//-- We add a LF for /p, br.
				boolean end = false;
				if(LA() == '/') {
					end = true;
					accept();
				}
				String name = scanWord();
				if(name != null) {
					if(end)
						name = "/" + name;
					if("/p".equals(name) || "br".equals(name))
						sb.append('\n');
				}
			}
			skipTag();
		}
	}

	/**
	 * Scan a tag. We are at the opening <.
	 */
	private void scanTag() {
		accept();
		skipWS();
		boolean endtag = false;
		if(LA() == '/') {
			endtag = true;
			accept();
			skipWS();
		}

		String s = scanWord();
		if(s == null) {
			skipTag(); // Skip everything until >
			return;
		}
		s = s.toLowerCase();
		TagInfo ti = getMap().get(s); // Is this a supported tag?
		if(ti == null) {
			skipTag();
			return;
		}

		//-- Tag is supported.
		if(endtag) {
			//-- If this is the "end" tag of the previous contentless tag- just skip it.
			if(s.equalsIgnoreCase(m_lastSingleTag)) {
				skipTag();
				m_lastSingleTag = null;
				return;
			}

			//-- Is this end tag on top of the stack? Then just pop it.
			if(popStackIf(s)) {
				//-- Valid end tag- add it.
				m_sb.append("</").append(s).append(">");
				skipTag();
				return;
			}

			//-- No match for end tag 8-/ Just skip it.
			skipTag();
			return;
		}

		//-- Ok: this is a normal tag. For now skip all tag parameters
		skipTag();
		m_sb.append("<");
		m_sb.append(s);

		//-- We have a start tag. If it must be balanced just exit.
		if(!ti.ends) {
			m_sb.append('>');
			m_tagStack.add(s);
			m_lastSingleTag = null;
			return;
		}

		//-- This is a single tag like <br> or <img>. These must be output as <br/>, and if closed with </br> that one must be skipped.
		m_sb.append("/>");
		m_lastSingleTag = s;
	}

	private boolean popStackIf(String s) {
		if(m_tagStack.size() == 0)
			return false;
		if(m_tagStack.get(m_tagStack.size() - 1).equalsIgnoreCase(s)) {
			m_tagStack.remove(m_tagStack.size() - 1);
			return true;
		}
		return false;
	}

	/**
	 * Scans a tag's attribute area for the terminating >. This escapes strings.
	 */
	private void skipTag() {
		int sc = 0;
		while(!eof()) {
			int c = LA();
			accept();
			if(sc != 0) {
				if(sc == c) {
					sc = 0;
				}
			} else {
				if(c == '\'' || c == '"') {
					sc = c;
				} else if(c == '>') {
					return;
				}
			}
		}
	}

	/**
	 * Scan until start-of-tag or eof.
	 * @param sb
	 */
	private void scanText(StringBuilder sb) {
		while(! eof()) {
			if(LA() == '<')
				return;
			copy(sb);
		}
	}


	/**
	 * This scans the input, and only copies "safe" html, which is HTML with only
	 * simple constructs. It checks to make sure the resulting document is xml-safe (well-formed),
	 * if the input is not well-formed it will add or remove tags until the result is valid.
	 *
	 * @param sb
	 * @param html
	 */
	static public void htmlRemoveUnsafe(StringBuilder outsb, String text) {
		if(text == null || text.length() == 0)
			return;
		new HtmlTextScanner().scan(outsb, text);
	}

	static public String htmlRemoveUnsafe(String html) {
		if(html == null || html.length() == 0)
			return "";
		StringBuilder sb = new StringBuilder(html.length() + 20);
		htmlRemoveUnsafe(sb, html);
		return sb.toString();
	}

	static public void htmlRemoveAll(StringBuilder outsb, String text, boolean lf) {
		if(text == null || text.length() == 0)
			return;
		new HtmlTextScanner().scanAndRemove(outsb, text, lf);
	}

	static public String htmlRemoveAll(String html, boolean lf) {
		if(html == null || html.length() == 0)
			return "";
		StringBuilder sb = new StringBuilder(html.length() + 20);
		htmlRemoveAll(sb, html, lf);
		return sb.toString();
	}
}
