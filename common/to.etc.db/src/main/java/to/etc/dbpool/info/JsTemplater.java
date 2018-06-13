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
package to.etc.dbpool.info;

/**
 * This implements a small templating engine.
 * Helper class handling javascript-like JSP templating.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 11, 2010
 */
public class JsTemplater {
	private String m_outmethod = "self.write";

	private String m_exprmethod = "self.writeExpr";

	/**
	 * Replace all <% .. %> and related occurences inside the file with a full Javascript
	 * command set. This scans verbatim texts and replaces that with a "self.write" call
	 * using the text as a Javascript string.
	 * @param in
	 * @return
	 */
	public String replaceTemplate(String resource) {
		StringBuilder sb = new StringBuilder();
		boolean hascode = false;
		int ix = 0;
		int len = resource.length();
		while(ix < len) {
			int mark = resource.indexOf("<%", ix);
			if(mark == -1) {
				//-- Just append the last segment, then be done
				appendStringWrite(sb, resource.substring(ix, len));
				break;
			}

			//-- New replacement. Copy current part;
			if(mark > ix)
				appendStringWrite(sb, resource.substring(ix, mark));
			mark += 2;

			int emark = resource.indexOf("%>", mark);
			if(emark == -1)
				throw new RuntimeException("Missing %> terminator");
			ix = emark + 2;
			boolean asexpr = false;
			if(resource.charAt(mark) == '=') {
				asexpr = true;
				mark++;
			}

			String code = resource.substring(mark, emark);
			if(asexpr) {
				sb.append("__tv = ").append(code).append(";");
				sb.append(m_exprmethod).append("(__tv);\n");
			} else {
				sb.append(code);
			}
		}
		return sb.toString();
	}

	static public void strToJavascriptString(final StringBuilder w, final String cs, final boolean dblquote) {
		int len = cs.length();
		//		if(len == 0)					jal 20090225 WTF!?!! Empty strings MUST be ""!!!!!
		//			return;
		int ix = 0;
		char quotechar;
		quotechar = dblquote ? '\"' : '\'';
		w.append(quotechar);

		while(ix < len) {
			//-- Collect a run
			int runstart = ix;
			char c = 0;
			while(ix < len) {
				c = cs.charAt(ix);
				if(c < 32 || c == '\'' || c == '\\' || c == quotechar)
					break;
				ix++;
			}
			if(ix > runstart) {
				w.append(cs, runstart, ix);
				if(ix >= len)
					break;
			}
			ix++;
			switch(c){
				default:
					w.append("\\u"); // Unicode escape
					w.append(intToStr(c & 0xffff, 16, 4));
					break;
				case '\n':
					w.append("\\n");
					break;
				case '\b':
					w.append("\\b");
					break;
				case '\f':
					w.append("\\f");
					break;
				case '\r':
					w.append("\\r");
					break;
				case '\t':
					w.append("\\t");
					break;
				case '\'':
					w.append("\\'");
					break;
				case '\"':
					w.append("\\\"");
					break;
				case '\\':
					w.append("\\\\");
					break;
			}
		}
		w.append(quotechar);
	}

	static public String intToStr(final int val, final int radix, final int npos) {
		String v = "000000000000" + Integer.toString(val, radix);
		return v.substring(v.length() - npos, v.length());
	}

	/**
	 * Convert the input to a Javascript string, then write a call.
	 * @param sb
	 * @param substring
	 */
	private void appendStringWrite(StringBuilder sb, String substring) {
		sb.append(m_outmethod);
		sb.append("(");
		strToJavascriptString(sb, substring, true);
		sb.append(");\n");
	}
}