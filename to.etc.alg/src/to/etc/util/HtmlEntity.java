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

import java.io.*;
import java.util.*;


/**
 * This class generates the class HtmlEntityTables from the DTD's for
 * HTML entities as published by the w3c. It generates the tables that quickly
 * convert entity names to their Unicode code and vice versa.
 *
 * This class generates a binary search <i>function</i> which is faster than
 * using a binary search on a table.
 *
 * You only need this class to generate new versions of the tables if new
 * DTD's become available. The DTD are part of the source distribution as .ent
 * files at the ./src/ level.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class HtmlEntity {
	public HtmlEntity() {
	}


	/**
	 *	Finds an entity by code.
	 */


	/*--------------------------------------------------------------*/
	/*	CODING:	Read .ent files and build tables...					*/
	/*--------------------------------------------------------------*/
	static private TreeMap<String, Integer>	m_name_mp;

	static private TreeMap<Integer, String>	m_val_mp;

	static private TreeMap<Integer, String>	m_desc_mp;

	/**
	 *	Reads an entire token in a string.
	 */
	static private String _getToken(Reader pr) throws Exception {
		int c;

		//-- Skip whitespace,
		for(;;) {
			c = pr.read();
			if(c == -1)
				return null;
			if(!Character.isWhitespace((char) c))
				break;
		}

		//-- Collect till whitespace..
		StringBuffer sb = new StringBuffer();
		for(;;) {
			sb.append((char) c);
			c = pr.read();
			if(c == -1)
				break;
			if(Character.isWhitespace((char) c))
				break;
		}
		return sb.toString();
	}

	static private String getToken(Reader pr) throws Exception {
		for(;;) {
			String t = _getToken(pr);
			if(t == null)
				return t;
			if(!t.equalsIgnoreCase("<!--"))
				return t;

			for(;;) {
				t = _getToken(pr);
				if(t == null)
					throw new Exception("EOF in comment.");
				if(t.equalsIgnoreCase("-->"))
					break;
			}
		}
	}

	static private String getComment(Reader pr) throws Exception {
		String s = getToken(pr);
		if(!s.equalsIgnoreCase("--"))
			throw new Exception("Missing comment start for entity: found" + s);

		//-- Now start reading data till --> is found....
		StringBuffer sb = new StringBuffer();
		int c;
		int spaces = 0;
		for(;;) {
			c = pr.read();
			if(c == '>') {
				int sl = sb.length();
				if(sl > 2) {
					if(sb.charAt(sl - 1) == '-' && sb.charAt(sl - 2) == '-') {
						sb.setLength(sl - 2); // Remove --
						break; // END!
					}
				}
			} else if(c == -1)
				throw new IOException("Unexpected EOF.");

			if(Character.isWhitespace((char) c))
				spaces++;
			else {
				if(spaces > 0) {
					sb.append(' ');
					spaces = 0;
				}
				sb.append((char) c);
			}
		}

		//-- Comment found. Remove all sheet,
		String cmt = sb.toString().trim();
		int pos = cmt.indexOf("U+");
		if(pos >= 0)
			cmt = cmt.substring(0, pos);
		pos = cmt.indexOf("u+");
		if(pos >= 0)
			cmt = cmt.substring(0, pos);

		//-- Now remove backwards spaces and comma's
		pos = cmt.length();
		while(pos > 0) {
			pos--;
			c = cmt.charAt(pos);
			if(c != ' ' && c != '\t' && c != ',' && c != ';') {
				pos++;
				break;
			}
		}

		cmt = cmt.substring(0, pos);
		return cmt;
	}


	static private void decodeEntity(Reader pr) throws Exception {
		//-- Next is name;
		String name = getToken(pr);
		if(name == null)
			return;

		String cn = getToken(pr); // Must be CNAME
		if(!cn.equalsIgnoreCase("CDATA"))
			throw new Exception("expected token CDATA after " + name);

		String str = getToken(pr); // quoted string "&#D" or "&#xH"
		if(str.length() <= 4)
			throw new Exception(name + ": data string too short " + str);
		if(!str.startsWith("\"&#"))
			throw new Exception(name + ": data string invalid start " + str);
		if(!str.endsWith("\""))
			throw new Exception(name + ": data string invalid end quote " + str);

		//-- Now collect subcomment area...
		String cmt = getComment(pr);

		//-- decode all we've gotten.
		int ep = str.length();
		while(ep > 0) {
			ep--;
			if(str.charAt(ep) != '"' && str.charAt(ep) != ';') {
				ep++;
				break;
			}
		}

		String dv = str.substring(3, ep);
		int val;

		try {
			if(dv.charAt(0) == 'X' || dv.charAt(0) == 'x')
				val = Integer.parseInt(dv.substring(1), 16);
			else
				val = Integer.parseInt(dv);
		} catch(Exception x) {
			throw new Exception(name + ": bad number " + dv);
		}

		//-- Ok: code point found. Add to tables.
		Integer iv = new Integer(val);
		m_name_mp.put(name, iv);
		m_val_mp.put(iv, name);
		m_desc_mp.put(iv, cmt);
	}


	/**
	 *	Generate the output file.
	 */
	@SuppressWarnings("rawtypes")
	static private void write(File f) throws Exception {
		PrintWriter pw = new PrintWriter(new FileWriter(f));
		try {
			pw.println("/** Auto-generated by HtmlEntity on " + (new Date()) + " */");
			pw.println("package to.etc.util;\n");
			pw.println("public class HtmlEntityTables {");

			//-- Create all strings.
			Iterator it = m_name_mp.keySet().iterator();
			//			while(it.hasNext())
			//			{
			//				String	val	= (String) it.next();
			//				pw.println("    static private final String s_"+val+" = \""+val+"\";");
			//			}

			//-- nameStrTbl: sorted by name. Assoc with NameCode table to get code.
			pw.print("\n    static public final String[] m_nameStrTbl = {");
			it = m_name_mp.keySet().iterator();
			int cc = 999;
			int ct = 0;
			while(it.hasNext()) {
				String val = (String) it.next();
				if(cc > 8) {
					pw.print("\n        ");
					cc = 0;
				}
				if(ct > 0)
					pw.print(',');
				pw.print("\"" + val + "\"");
				ct++;
				cc++;
			}
			pw.println("\n    };\n");


			//-- nameCodeTbl: sorted by name. Assoc with NameCode table to get code.
			Vector namev = new Vector();
			pw.print("\n    static public final int[] m_nameCodeTbl = {");
			it = m_name_mp.entrySet().iterator();
			cc = 999;
			ct = 0;
			while(it.hasNext()) {
				Map.Entry me = (Map.Entry) it.next();
				namev.add(me);

				Integer val = (Integer) me.getValue();
				if(cc > 8) {
					pw.print("\n        ");
					cc = 0;
				}
				if(ct > 0)
					pw.print(',');
				pw.print(val.toString());
				ct++;
				cc++;
			}
			pw.println("\n    };\n");

			//-- codeStrTbl sorted by code,
			pw.print("\n    static public final String[] m_codeStrTbl = {");
			it = m_val_mp.entrySet().iterator();
			cc = 999;
			ct = 0;
			while(it.hasNext()) {
				Map.Entry me = (Map.Entry) it.next();

				String val = (String) me.getValue();
				if(cc > 8) {
					pw.print("\n        ");
					cc = 0;
				}
				if(ct > 0)
					pw.print(',');
				pw.print("\"" + val + "\"");
				ct++;
				cc++;
			}
			pw.println("\n    };\n");

			//-- codeDescTbl sorted by code,
			pw.print("\n    static public final String[] m_codeDescTbl = {");
			it = m_desc_mp.entrySet().iterator();
			cc = 999;
			ct = 0;
			while(it.hasNext()) {
				Map.Entry me = (Map.Entry) it.next();

				String val = (String) me.getValue();
				if(cc > 8) {
					pw.print("\n        ");
					cc = 0;
				}
				if(ct > 0)
					pw.print(',');
				pw.print("\"" + val + "\"");
				ct++;
				cc++;
			}
			pw.println("\n    };\n");


			//-- And the codeCodeTbl sorted by code.
			Vector valv = new Vector();
			pw.print("\n    static public final int[] m_codeCodeTbl = {");
			it = m_val_mp.entrySet().iterator();
			cc = 999;
			ct = 0;
			while(it.hasNext()) {
				Map.Entry me = (Map.Entry) it.next();
				valv.add(me);

				Integer val = (Integer) me.getKey();

				if(cc > 8) {
					pw.print("\n        ");
					cc = 0;
				}
				if(ct > 0)
					pw.print(',');
				pw.print(val.toString());
				ct++;
				cc++;
			}
			pw.println("\n    };\n");


			//-- Generate a find function by NAME
			pw.println("    static public int findCode(String name) {");
			pw.println("        int rv;");
			mkNameFind(pw, namev, 4, 0, namev.size());
			pw.println("    }\n\n");


			//-- Generate a find function by VALUE
			pw.println("    static public String findName(int code) {");
			mkValFind(pw, valv, 4, 0, valv.size());
			pw.println("    }\n\n");


			pw.println("\n}\n");
		} finally {
			try {
				pw.close();
			} catch(Exception x) {}

		}
	}

	static private void w(PrintWriter pw, int ind, String s) {
		ind *= 2;
		while(ind-- > 0)
			pw.print(' ');
		pw.println(s);
	}

	static private void genNameUnknown(PrintWriter pw, int lvl) {
		w(pw, lvl, "return -1;");
	}

	@SuppressWarnings("rawtypes")
	static private void mkNameFind(PrintWriter pw, Vector v, int lvl, int low, int high) {
		if(low >= high) {
			// Nothing left to compare!!!
			genNameUnknown(pw, lvl);
			return;
		}

		//-- Only one thing to compare?
		if(high - low == 1) {
			Map.Entry me = (Map.Entry) v.elementAt(low);
			String name = (String) me.getKey();
			int val = ((Integer) me.getValue()).intValue();

			w(pw, lvl, "if(name.equals(\"" + name + "\"))");
			w(pw, lvl + 1, "return " + val + ";");
			w(pw, lvl, "else");
			genNameUnknown(pw, lvl + 1);
			return;
		}

		//-- Two things can be more efficient too.
		if(high - low == 2) {
			Map.Entry me = (Map.Entry) v.elementAt(low);
			String name = (String) me.getKey();
			int val = ((Integer) me.getValue()).intValue();

			w(pw, lvl, "if(name.equals(\"" + name + "\"))");
			w(pw, lvl + 1, "return " + val + ";");

			me = (Map.Entry) v.elementAt(low + 1);
			name = (String) me.getKey();
			val = ((Integer) me.getValue()).intValue();
			w(pw, lvl, "if(name.equals(\"" + name + "\"))");
			w(pw, lvl + 1, "return " + val + ";");
			genNameUnknown(pw, lvl);
			return;
		}

		//-- We need to compare at least 2 items,
		int half = (low + high) / 2;
		Map.Entry me = (Map.Entry) v.elementAt(half);
		String name = (String) me.getKey();
		int val = ((Integer) me.getValue()).intValue();

		w(pw, lvl, "rv = name.compareTo(\"" + name + "\");");
		w(pw, lvl, "if(rv == 0)");
		w(pw, lvl + 1, "return " + val + ";");
		w(pw, lvl, "else if(rv < 0)");
		w(pw, lvl, "{");
		mkNameFind(pw, v, lvl + 1, low, half);
		w(pw, lvl, "}");
		w(pw, lvl, "else");
		w(pw, lvl, "{");
		mkNameFind(pw, v, lvl + 1, half + 1, high);
		w(pw, lvl, "}");


	}


	static private void genValUnknown(PrintWriter pw, int lvl) {
		w(pw, lvl, "return null;");
	}

	@SuppressWarnings("rawtypes")
	static private void mkValFind(PrintWriter pw, Vector v, int lvl, int low, int high) {
		if(low >= high) {
			// Nothing left to compare!!!
			genValUnknown(pw, lvl);
			return;
		}

		//-- Only one thing to compare?
		if(high - low == 1) {
			Map.Entry me = (Map.Entry) v.elementAt(low);
			String name = (String) me.getValue();
			int val = ((Integer) me.getKey()).intValue();

			w(pw, lvl, "if(code == " + val + ")");
			w(pw, lvl + 1, "return \"" + name + "\";");
			w(pw, lvl, "else");
			genValUnknown(pw, lvl + 1);
			return;
		}

		//-- Two things can be more efficient too.
		if(high - low == 2) {
			Map.Entry me = (Map.Entry) v.elementAt(low);
			String name = (String) me.getValue();
			int val = ((Integer) me.getKey()).intValue();

			w(pw, lvl, "if(code == " + val + ")");
			w(pw, lvl + 1, "return \"" + name + "\";");

			me = (Map.Entry) v.elementAt(low + 1);
			name = (String) me.getValue();
			val = ((Integer) me.getKey()).intValue();
			w(pw, lvl, "if(code == " + val + ")");
			w(pw, lvl + 1, "return \"" + name + "\";");
			genValUnknown(pw, lvl);
			return;
		}

		//-- We need to compare at least 2 items,
		int half = (low + high) / 2;
		Map.Entry me = (Map.Entry) v.elementAt(half);
		String name = (String) me.getValue();
		int val = ((Integer) me.getKey()).intValue();

		w(pw, lvl, "if(code == " + val + ")");
		w(pw, lvl + 1, "return \"" + name + "\";");
		w(pw, lvl, "else if(code < " + val + ")");
		w(pw, lvl, "{");
		mkValFind(pw, v, lvl + 1, low, half);
		w(pw, lvl, "}");
		w(pw, lvl, "else");
		w(pw, lvl, "{");
		mkValFind(pw, v, lvl + 1, half + 1, high);
		w(pw, lvl, "}");
	}


	/**
	 *
	 */
	static private void scanFile(File f) throws Exception {
		Reader lr = new BufferedReader(new FileReader(f), 8192);
		try {
			String t;
			int cc = m_name_mp.size();

			while(null != (t = getToken(lr))) {
				if(t.equalsIgnoreCase("<!ENTITY")) {
					decodeEntity(lr);
				}
			}


			System.out.println(f + ": " + (m_name_mp.size() - cc) + " entities.");
		} finally {
			try {
				lr.close();
			} catch(Exception x) {}
		}
	}

	/**
	 *	Main.
	 */
	@SuppressWarnings("rawtypes")
	static public void main(String args[]) {
		try {
			//-- 1. Scan all .ent files.
			if(args.length != 2)
				throw new Exception("Usage: HtmlEntity <directory> outputfile");
			File f = new File(args[0]);
			File[] far = f.listFiles();

			m_name_mp = new TreeMap();
			m_val_mp = new TreeMap();
			m_desc_mp = new TreeMap();

			int count = 0;
			for(int i = 0; i < far.length; i++) {
				String fn = far[i].getName().toLowerCase();
				if(fn.endsWith(".ent")) {
					scanFile(far[i]);
					count++;
				}
			}
			if(count == 0)
				throw new Exception("No files there.");

			File of = new File(args[1]);
			write(of);
		} catch(Exception ex) {
			System.out.println("Exception: " + ex.toString());
		}
	}

}
