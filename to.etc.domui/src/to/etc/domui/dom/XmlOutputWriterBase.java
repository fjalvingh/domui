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
package to.etc.domui.dom;

import java.io.*;

public class XmlOutputWriterBase {
	private Writer m_w;

	protected boolean m_intag;

	public XmlOutputWriterBase(Writer w) {
		m_w = w;
	}

	protected Writer getWriter() {
		return m_w;
	}

	/**
	 * Writes string data. This escapes XML control characters to their entity
	 * equivalent. This does NOT indent data with newlines, because string data
	 * in a content block may not change.
	 */
	public void text(String s) throws IOException {
		if(s == null)
			throw new IllegalStateException("Attempt to write null cdata.");
		closePrevious(); // If a tag was unclosed close it now before writing it's body

		//-- Start writing strings...
		int ix = 0;
		int sl = s.length();
		while(ix < sl) {
			//-- Collect a run of chars that don't need to be escaped
			int runstart = ix;
			char c = 0;
			while(ix < sl) {
				c = s.charAt(ix);
				if(c == '<' || c == '>' || c == '&' || c == '\n')
					break;
				ix++;
			}

			//-- If we have a run output it
			if(ix > runstart) {
				writeRaw(s, runstart, ix - runstart);
				if(ix >= sl)
					return;
			}

			//-- Now handle the character we've found
			switch(c){
				case '>':
					writeRaw("&gt;");
					break;
				case '<':
					writeRaw("&lt;");
					break;
				case '&':
					writeRaw("&amp;");
					break;
				case '\n':
					println();
					break;
			}
			ix++;
		}
	}

	protected void println() throws IOException {
		m_w.write("\n");
	}

	public void nl() throws IOException {}

	public void inc() {}

	public void dec() {}

	public boolean isIndentEnabled() {
		return false;
	}

	/**
	 * Writes a tag start. It can be followed by attr() calls. If the namespace is in the current
	 * namespace the tag will not have prefixes.
	 *
	 * @param namespace
	 * @param tagname
	 */
	public void tag(final String tagname) throws IOException {
		closePrevious(); // If an earlier tag is open close it,
		if(isIndentEnabled())
			nl();
		writeRaw("<");
		writeRaw(tagname);
		m_intag = true;
		inc();
	}

	/**
	 * If we're in an open tag this closes that tag. The tag gets closed using a >, so the next thing will
	 * be contained in the tag.
	 */
	private void closePrevious() throws IOException {
		if(!m_intag)
			return;
		m_intag = false;
		writeRaw(">");
	}

	/**
	 * Ends a tag by adding a > only.
	 */
	public void endtag() throws IOException {
		if(!m_intag)
			throw new IllegalStateException("Ending tag but not in a tag?");
		m_intag = false;
		writeRaw(">");
	}

	/**
	 * Ends a tag by adding />.
	 * @throws IOException
	 */
	public void endAndCloseXmltag() throws IOException {
		if(!m_intag)
			throw new IllegalStateException("Ending tag but not in a tag?");
		m_intag = false;
		writeRaw("/>");
		dec();
	}

	public void closetag(String name) throws IOException {
		closePrevious();
		dec();
		writeRaw("</");
		writeRaw(name);
		writeRaw(">");
		if(isIndentEnabled())
			nl();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Writing attributes.									*/
	/*--------------------------------------------------------------*/
	/**
	 * Appends an attribute to the last tag. The value's characters that are invalid are quoted into
	 * entities.
	 *
	 * @param namespace
	 * @param name
	 * @param value
	 * @throws IOException
	 */
	public void attr(String name, String value) throws IOException {
		if(!m_intag)
			throw new IllegalStateException("No tag is currently 'active'");
		writeRaw(" ");
		writeRaw(name);
		writeRaw("=\"");
		writeAttrValue(value);
		writeRaw("\"");
	}

	public void rawAttr(String name, String value) throws IOException {
		if(!m_intag)
			throw new IllegalStateException("No tag is currently 'active'");
		writeRaw(" ");
		writeRaw(name);
		writeRaw("=\"");
		writeRaw(value);
		writeRaw("\"");
	}

	private void writeAttrValue(String value) throws IOException {
		//-- Write the quoted string, quickly, by using runs.
		int len = value.length();
		int pos = 0;
		String entity = null;
		while(pos < len) {
			int spos = pos;

			//-- Find 1st character we have trouble with
			while(pos < len) {
				entity = null;
				char c = value.charAt(pos);
				switch(c){
					default:
						pos++;
						break;
					case '&':
						entity = "&amp;";
						break;
					case '<':
						entity = "&lt;";
						break;
					case '>':
						entity = "&gt;";
						break;
					case '\n':
						entity = "&#0010;";
						break;
					case '\r':
						entity = "&#0013;";
						break;
					case '\"':
						entity = "&quot;";
						break;
				}
				if(entity != null)
					break;
			}

			//-- First handle the run upto the failed char
			if(pos > spos) {
				writeRaw(value, spos, pos - spos); // Write the fragment upto the char,
			}
			if(entity != null) {
				writeRaw(entity);
				pos++;
			}
		}
	}

	/**
	 * Write a simple numeric attribute thingy.
	 *
	 * @param namespace
	 * @param name
	 * @param value
	 * @throws IOException
	 */
	public void attr(String name, long value) throws IOException {
		attr(name, Long.toString(value));
	}

	public void attr(String name, int value) throws IOException {
		attr(name, Integer.toString(value));
	}

	public void attr(String name, boolean value) throws IOException {
		attr(name, Boolean.toString(value));
	}


	public void writeRaw(CharSequence s) throws IOException {
		m_w.append(s);
	}

	protected void writeRaw(String s, int off, int len) throws IOException {
		m_w.write(s, off, len);
	}
}
