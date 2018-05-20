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
package to.etc.xml;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import to.etc.util.*;

/**
 * Small utility class to write formatted XML documents. This class writes it's
 * data to a printstream. The stream is never closed. The data is formatted
 * and nested appropriately by indenting tags. XML cdata is properly escaped
 * (replacing &lt; by &amp;lt; and the like.
 */
public class XmlWriter extends IndentWriter {
	/** The current tag level, */
	private int			m_tag_lvl	= 1;

	/** All open tags, indexed by tag level. */
	private String[]	m_tag_ar	= new String[15];

	public XmlWriter() {
	}

	public XmlWriter(Writer w) {
		init(w, 0);
	}

	@Override
	public void init(Writer w, int taglvl) {
		super.init(w, taglvl);
		m_tag_lvl = 0;
	}

	/**
	 * Writes data without replacing stuff; it does expand newlines to indent.
	 * @param s
	 */
	public void wraw(String s) throws IOException {
		super.write(s); // Ask indentwriter to write this
	}

	public void writeAttr(String name, String value) throws IOException {
		print(name);
		if(value == null)
			value = "";
		print("=\"");
		print(StringTool.xmlStringize(value));
		print("\"");
	}

	/**
	 *	Writes string data. This escapes XML control characters to their entity
	 *  equivalent. This does NOT indent data with newlines, because string data
	 *  in a content block may not change.
	 */
	public void cdata(String s) throws IOException {
		if(s == null) {
			writeRaw("(dbnull)");
			return;
		}

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
				case '\n': {
					println();
				}
			}
			ix++;
		}
	}

	public void unclosed() {
		dec();
		if(m_tag_lvl > 0)
			m_tag_lvl--;
	}

	/**
	 * Writes a new XML tag. The attrs field must at least contain a '>' to close
	 * the tag. If you want the tag to be the only one on the line then add a
	 * linefeed to the attrs field also.
	 * @param tn			The tag name without braces
	 * @param attrs			attributes.
	 */
	public void tag(String tn, String attrs) throws IOException {
		wraw("<" + tn);
		if(attrs.length() >= 1 && attrs.charAt(0) != '>')
			wraw(" ");
		wraw(attrs);
		m_tag_lvl++;
		inc();
		m_tag_ar[m_tag_lvl] = tn;
	}

	public void tagnl(String tn, String... attrvalueset) throws IOException {
		tag(tn, attrvalueset);
		wraw("\n");
	}

	public void tag(String tn, Object... ar) throws Exception {
		wraw("<" + tn);

		List<String> avs = new ArrayList<>();
		for(int i = 0; i < ar.length; i += 2) {
			String attr = (String) ar[i];
			Object val = ar[i + 1];
			if(val != null) {
				wraw(" ");
				wraw(attr);
				wraw("=\"");
				String s = renderValue(val);
				wraw(StringTool.xmlStringize(s));
				wraw("\"");
			}
		}
		wraw(">");
		m_tag_lvl++;
		inc();
		m_tag_ar[m_tag_lvl] = tn;
	}

	private String renderValue(Object val) throws Exception {
		if(null == val)
			return "";
		if(val instanceof String) {
			return (String) val;
		}
		if(val instanceof Date) {
			return W3CSchemaCoder.encodeDateTime((Date) val, null);
		}
		if(val instanceof Boolean) {
			return W3CSchemaCoder.encodeBoolean((Boolean) val);
		}
		return String.valueOf(val);
	}

	public void tag(String tn, String... attrvalueset) throws IOException {
		wraw("<" + tn);

		//-- Write all attribute/value pairs,
		for(int i = 0; i < attrvalueset.length; i += 2) {
			wraw(" ");
			wraw(attrvalueset[i]); // Name field,
			wraw("=\"");
			if(i + 1 < attrvalueset.length)
				wraw(StringTool.xmlStringize(attrvalueset[i + 1]));
			wraw("\"");
		}
		wraw(">");
		m_tag_lvl++;
		inc();
		m_tag_ar[m_tag_lvl] = tn;
	}

	public void startTag(String tn) throws IOException {
		wraw("<" + tn);
		m_tag_lvl++;
		inc();
		m_tag_ar[m_tag_lvl] = tn;
	}

	public void attr(String name, String value) throws IOException {
		wraw(" ");
		wraw(name);
		wraw("=\"");
		wraw(StringTool.xmlStringize(value));
		wraw("\"");
	}

	/**
	 *  Renders attribute with string value, complying to
	 *  DOM API 5.2 Character Escaping
	 *  http://www.w3.org/TR/2000/WD-xml-c14n-20000119.html#charescaping
	 *
	 * @param name
	 * @param value
	 * @throws IOException
	 */
	public void attrForDomApi(String name, String value) throws IOException {
		wraw(" ");
		wraw(name);
		wraw("=\"");
		wraw(StringTool.xmlStringizeForDomApi(value));
		wraw("\"");
	}

	public void attr(String name, Object value) throws Exception {
		if(null == value)
			return;
		wraw(" ");
		wraw(name);
		wraw("=\"");
		wraw(StringTool.xmlStringize(renderValue(value)));
		wraw("\"");
	}


	public void endTag(boolean complete) throws IOException {
		if(complete) {
			wraw("/>");
			m_tag_lvl--;
		} else {
			wraw(">");
		}
	}

	/**
	 * Outputs a tag AND parameters ONLY, i.e. it outputs the tag
	 * and completes it by adding /&gt;.
	 * @param tn
	 * @param attrvalueset
	 */
	public void tagonly(String tn, String... attrvalueset) throws IOException {
		wraw("<" + tn);

		//-- Write all attribute/value pairs,
		for(int i = 0; i < attrvalueset.length; i += 2) {
			if(attrvalueset[i + 1] != null) {
				wraw(" ");
				wraw(attrvalueset[i]); // Name field,
				wraw("=\"");
				wraw(StringTool.xmlStringize(attrvalueset[i + 1]));
				wraw("\"");
			}
		}
		wraw(" />\n");
	}

	public void tagonlynl(String tn, String... attrvalueset) throws IOException {
		tagonly(tn, attrvalueset);
		//		wraw("\n");
	}

	public void tag(String tn) throws IOException {
		tag(tn, ">\n");
	}

	/**
	 * Writes a complete tag (open AND close) without attributes.
	 * @param tag
	 */
	public void tagfull(String tn) throws IOException {
		wraw("<");
		wraw(tn);
		wraw("/>\n");
	}

	/**
	 * Writes a tag, a #text content and the end tag on one line.
	 * @param tag
	 * @param text
	 */
	public void tagfull(String tn, String text) throws IOException {
		wraw("<");
		wraw(tn);
		wraw(">");
		if(text == null)
			text = DomTools.DBNULL;
		cdata(text);
		wraw("</");
		wraw(tn);
		wraw(">\n");
	}

	/**
	 * Writes a tag, a #text content and the end tag on one line.
	 * @param tag
	 * @param text
	 */
	public void tagfull(String tn, Date dt) throws IOException {
		wraw("<");
		wraw(tn);
		wraw(">");
		if(dt == null)
			cdata(DomTools.DBNULL);
		else {
			cdata(DomTools.dateEncode(dt));
		}
		wraw("</");
		wraw(tn);
		wraw(">\n");
	}


	/**
	 * Writes a tag with the given attributes, a #text content and the end tag on one line.
	 * @param tag
	 * @param attrs
	 * @param text
	 */
	public void tagfull(String tn, String attrs, String text) throws IOException {
		wraw("<");
		wraw(tn);
		if(attrs.length() >= 1) {
			wraw(" ");
			wraw(attrs);
		}
		wraw(">");
		cdata(text);
		wraw("</");
		wraw(tn);
		wraw(">\n");
	}

	/**
	 * Writes a tag, a #text content and the end tag on one line.
	 * @param tag
	 * @param text
	 */
	public void tagfull(String tn, int val) throws IOException {
		tagfull(tn, Integer.toString(val));
	}

	/**
	 * Writes a tag, a #text content and the end tag on one line.
	 * @param tag
	 * @param text
	 */
	public void tagfull(String tn, long val) throws IOException {
		tagfull(tn, Long.toString(val));
	}

	public void tagfull(String tn, Number val) throws IOException {
		if(val == null)
			cdata(DomTools.DBNULL);
		else
			tagfull(tn, val.toString());
	}

	/**
	 * Writes a tag, a #text content and the end tag on one line.
	 * @param tag
	 * @param text
	 */
	public void tagfull(String tn, boolean onoff) throws IOException {
		tagfull(tn, onoff ? "true" : "false");
	}

	/**
	 * Writes a tag, a #text content and the end tag on one line.
	 * @param tag
	 * @param text
	 */
	public void tagfull(String tn, Boolean onoff) throws IOException {
		if(onoff != null)
			tagfull(tn, onoff.booleanValue());
	}


	public void tagendnl() throws IOException {
		m_tag_lvl--;
		dec();
		wraw("</" + m_tag_ar[m_tag_lvl + 1] + ">\n");
	}

	/**
	 * XMLWriters cannot be closed: ignore.
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException {
		while(m_tag_lvl > 0)
			tagendnl();
		super.close();
	}

	public void dumpBean(Object o) throws IOException {
		Class< ? > cl = o.getClass();
		Method[] mar = cl.getMethods();
		for(int i = mar.length; --i >= 0;) {
			handleMethod(o, mar[i]);
		}
	}

	private void handleMethod(Object o, Method m) throws IOException {
		String name = m.getName();
		if(!name.startsWith("get"))
			return;

		String res = null;
		try {
			Object val = m.invoke(o, (Object[]) null);
			if(val == null)
				res = "(null)";
			else
				res = val.toString();
		} catch(Exception x) {
			res = x.toString();
		}

		name = name.substring(3, 4).toLowerCase() + name.substring(4);
		tagfull(name, res);
	}
}
