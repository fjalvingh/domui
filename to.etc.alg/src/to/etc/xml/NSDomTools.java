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

import java.util.*;

import org.w3c.dom.*;

/**
 * DomTools version that is namespace-aware.
 * Created on Oct 12, 2005
 * @author jal
 */
public class NSDomTools {
	/*--------------------------------------------------------------*/
	/*	CODING:	DOM helper stuff									*/
	/*--------------------------------------------------------------*/
	/**
	 * Finds a single element with the name spec'd in the node. If more than
	 * one node with the same name exists this throws an exception.
	 */
	static public Node nodeFind(Node rn, String ns, String name) throws Exception {
		NodeList nl = rn.getChildNodes();
		Node fn = null;
		for(int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if(name.equals(n.getLocalName()) && ns.equals(n.getNamespaceURI()) && n.getNodeType() == Node.ELEMENT_NODE) {
				if(fn != null)
					throw new Exception(name + ": duplicate instance within " + rn.getNodeName());
				fn = nl.item(i);
			}
		}
		return fn;
	}

	/**
	 * Gets the text part contained in a node... All text parts are obtained and
	 * concatenated with a single space.
	 */
	static public String textFrom_untrimmed(Node n) {
		return DomTools.textFrom_untrimmed(n);
	}

	/**
	 * Gets the text part contained in a node... All text parts are obtained and
	 * concatenated with a single space.
	 */
	static public String textFrom(Node n) {
		return DomTools.textFrom(n);
	}

	/**
	 * Finds the child node with the name specified, and returns it's text
	 * value. If the node is not found it returns null.
	 */
	static public String stringNode(Node rootnode, String ns, String name) throws Exception {
		Node n = nodeFind(rootnode, ns, name);
		if(n == null)
			return null;
		return textFrom(n);
	}

	/**
	 * Finds the child node with the name specified, and returns it's text
	 * value. If the node is not found it returns null.
	 */
	static public String stringNode(Node rootnode, String ns, String name, int trunclen) throws Exception {
		String s = stringNode(rootnode, ns, name);
		if(s != null) {
			if(s.length() > trunclen)
				s = s.substring(0, trunclen);
		}
		return s;
	}

	/**
	 * Finds the child node with the name specified, and returns it's text
	 * value. If the node is not found it returns the default string.
	 */
	static public String stringNode(Node rootnode, String ns, String name, String deflt) throws Exception {
		Node n = nodeFind(rootnode, ns, name);
		if(n == null)
			return deflt;
		return textFrom(n);
	}

	/**
	 * Finds the child node with the name specified, and returns it's text
	 * value. If the node is not found it returns null.
	 */
	static public String stringNode_untrimmed(Node rootnode, String ns, String name) throws Exception {
		Node n = nodeFind(rootnode, ns, name);
		if(n == null)
			return null;
		return textFrom_untrimmed(n);
	}

	/**
	 * Finds the child node with the name specified, and returns it's text
	 * value. If the node is not found it returns the default string.
	 */
	static public String stringNode_untrimmed(Node rootnode, String ns, String name, String deflt) throws Exception {
		Node n = nodeFind(rootnode, ns, name);
		if(n == null)
			return deflt;
		return textFrom_untrimmed(n);
	}

	static public Date dateNode(Node rn, String ns, String name) throws Exception {
		String s = stringNode(rn, ns, name);
		if(s == null)
			return null;
		return DomTools.dateDecode(s);
	}

	static public Date dateNode(Node rn, String ns, String name, Date dflt) throws Exception {
		String s = stringNode(rn, ns, name);
		if(s == null)
			return dflt;
		if(DomTools.DBNULL.equals(s))
			return dflt;
		return DomTools.dateDecode(s);
	}

	static private final void timeError(String v) throws Exception {
		throw new Exception(v + ": must be hh:mm, hh:mm:ss, hhmm or hhmmss.");
	}

	/**
	 * Scans a node as a hh:mm:ss time (or hh:mm). The time is returned
	 * as a #seconds in the day.
	 * @param rn
	 * @param name
	 * @param dflt
	 * @return
	 * @throws Exception
	 */
	static public int timeNode(Node rn, String ns, String name, int dflt) throws Exception {
		String v = stringNode(rn, ns, name, null);
		if(v == null || v.length() == 0)
			return dflt;

		int ix = 0;
		int len = v.length();
		int hh, mm, ss = 0;

		//-- Collect max. 2 digits for hh
		int nr = 0;
		char c = v.charAt(ix++);
		if(!Character.isDigit(c))
			timeError(v);
		nr = c - '0'; // Make numeric
		if(ix >= len)
			timeError(v);
		c = v.charAt(ix++);
		if(Character.isDigit(c)) {
			nr = nr * 10 + c - '0'; // Has a 2digit time,
			if(ix >= len)
				timeError(v);
			c = v.charAt(ix); // Get next char: ':' or digit
			if(c == ':') // Get past evt colon.
				ix++;
		} else if(c != ':') // No 2nd digit means it must be ':'
			timeError(v);
		hh = nr;

		//-- Collect 2 digits (mand) minutes.
		if(ix + 2 > len)
			timeError(v);
		c = v.charAt(ix++);
		if(!Character.isDigit(c))
			timeError(v);
		nr = c - '0'; // Make numeric
		c = v.charAt(ix++);
		if(!Character.isDigit(c))
			timeError(v);
		nr = nr * 10 + c - '0'; // Make numeric
		mm = nr;

		if(ix < len) // room for ss?
		{
			c = v.charAt(ix++);
			if(c == ':') {
				if(ix >= len)
					timeError(v);
				c = v.charAt(ix++); // 1st digit of secs
			}
			if(ix >= len)
				timeError(v);
			if(!Character.isDigit(c))
				timeError(v);
			nr = c - '0'; // Make numeric
			c = v.charAt(ix++);
			if(!Character.isDigit(c))
				timeError(v);
			nr = nr * 10 + c - '0'; // Make numeric
			ss = nr;
		}

		if(hh < 0 || hh > 23 || mm < 0 || mm >= 60 || ss < 0 || ss >= 60)
			throw new Exception(v + ": invalid time (hours or minutes bad)");
		return hh * 60 * 60 + mm * 60 + ss;
	}

	static public int intNode(Node rootnode, String ns, String name) throws Exception {
		String s = stringNode(rootnode, ns, name);
		if(s != null) {
			s = s.trim();
			if(s.length() >= 0 && !DomTools.DBNULL.equals(s)) {
				try {
					return Integer.parseInt(s);
				} catch(Exception ex) {
					throw new Exception(name + ": integer value expected, got '" + s + "'");
				}
			}
		}

		throw new Exception("Missing '" + name + "' node [integer]");
	}

	static public int intNode(Node rootnode, String ns, String name, int val) throws Exception {
		String s = stringNode(rootnode, ns, name);
		if(s == null)
			return val;
		s = s.trim();
		if(s.length() == 0 || DomTools.DBNULL.equals(s))
			return val;

		try {
			return Integer.parseInt(s);
		} catch(Exception ex) {
			throw new Exception(name + ": integer value expected, got '" + s + "'");
		}
	}

	static public long longNode(Node rootnode, String ns, String name) throws Exception {
		String s = stringNode(rootnode, ns, name);
		if(s != null) {
			s = s.trim();
			if(s.length() >= 0 && !DomTools.DBNULL.equals(s)) {
				try {
					return Long.parseLong(s);
				} catch(Exception ex) {
					throw new Exception(name + ": long value expected, got '" + s + "'");
				}
			}
		}

		throw new Exception("Missing '" + name + "' node [long]");
	}

	static public long longNode(Node rootnode, String ns, String name, long val) throws Exception {
		String s = stringNode(rootnode, ns, name);
		if(s == null)
			return val;
		s = s.trim();
		if(s.length() == 0 || DomTools.DBNULL.equals(s))
			return val;

		try {
			return Long.parseLong(s);
		} catch(Exception ex) {
			throw new Exception(name + ": long value expected, got '" + s + "'");
		}
	}

	static public double doubleNode(Node rootnode, String ns, String name, double val) throws Exception {
		String s = stringNode(rootnode, ns, name);
		if(s == null)
			return val;
		s = s.trim();
		if(s.length() == 0 || DomTools.DBNULL.equals(s))
			return val;

		try {
			return Double.parseDouble(s);
		} catch(Exception ex) {
			throw new Exception(name + ": double value expected, got '" + s + "'");
		}
	}

	/**
	 * Finds the child node with the name specified, and returns it as a
	 * boolean value. If the node has NO text associated then this returns TRUE,
	 * if the node is not present then this returns false; if the node is present
	 * and has text the text field is interpreted: if numeric we return T if
	 * the number is not null; if text the value must start with T for true and
	 * F for false. All other values throw an exception.
	 */
	static public boolean boolNode(Node rootnode, String ns, String name) throws Exception {
		Node n = nodeFind(rootnode, ns, name);
		if(n == null)
			return false; // Not present.
		return DomTools.decodeBoolStr(textFrom(n), name);
	}

	/**
	 * Get the named attribute from a node. If the attribute is not present
	 * return the default value in defval
	 * @param n			the node to containing the attribute.
	 * @param aname		the name of the attribute.
	 * @param defval	the value to return if the attribute is not present,
	 * @return			a string containing the attribute's value or the default.
	 */
	static public String getNodeAttribute(Node n, String ns, String aname, String defval) {
		if(n.hasAttributes()) {
			Node idn = n.getAttributes().getNamedItemNS(ns, aname);
			if(idn != null)
				return idn.getNodeValue();
		}
		return defval;
	}

	static public String strAttr(Node n, String ns, String aname, String def) {
		return getNodeAttribute(n, ns, aname, def);
	}

	static public int intAttr(Node n, String ns, String aname, int defval) throws Exception {
		return getNodeAttribute(n, ns, aname, defval);
	}

	static public int intAttr(Node n, String ns, String aname) throws Exception {
		if(n.hasAttributes()) {
			Node idn = n.getAttributes().getNamedItemNS(ns, aname);
			if(idn != null) {
				String v = idn.getNodeValue();
				if(v != null && v.length() > 0) {
					try {
						return Integer.parseInt(v.trim());
					} catch(Exception ex) {
						throw new Exception("expected integer value for attribute " + aname + " in namespace " + ns + ", but got '" + v + "'");
					}
				}
			}
		}
		throw new Exception("Missing mandatory int attribute '" + aname + "' on tag '" + n.getNodeName() + "'");
	}

	static public boolean boolAttr(Node n, String ns, String aname) throws Exception {
		if(n.hasAttributes()) {
			Node idn = n.getAttributes().getNamedItemNS(ns, aname);
			if(idn != null) {
				String v = idn.getNodeValue();
				return DomTools.decodeBoolStr(v, aname);
			}
		}
		throw new Exception("Missing mandatory boolean attribute '" + aname + "' on tag '" + n.getNodeName() + "'");
	}

	static public boolean boolAttr(Node n, String ns, String aname, boolean defval) throws Exception {
		if(n.hasAttributes()) {
			Node idn = n.getAttributes().getNamedItemNS(ns, aname);
			if(idn != null) {
				String v = idn.getNodeValue();
				return DomTools.decodeBoolStr(v, aname);
			}
		}
		return defval;
	}

	/**
	 * Get the named attribute from a node. If the attribute is not present
	 * return the default value in defval
	 * @param n			the node to containing the attribute.
	 * @param aname		the name of the attribute.
	 * @param defval	the value to return if the attribute is not present,
	 * @return			a string containing the attribute's value or the default.
	 */
	static public int getNodeAttribute(Node n, String ns, String aname, int defval) throws Exception {
		if(n.hasAttributes()) {
			Node idn = n.getAttributes().getNamedItemNS(ns, aname);
			if(idn != null) {
				String v = idn.getNodeValue();
				if(v != null && v.length() > 0) {
					try {
						return Integer.parseInt(v.trim());
					} catch(Exception ex) {
						throw new Exception("expected integer value for attribute " + aname + ", but got '" + v + "'");
					}
				}
			}
		}
		return defval;
	}

	static public final String[] getStringList(Node inn, String itemname) throws Exception {
		List<String> al = new ArrayList<String>();
		NodeList nl = inn.getChildNodes();
		for(int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if(n.getNodeName().equals("#text"))
				;
			else if(n.getNodeName().equalsIgnoreCase(itemname))
				al.add(DomTools.textFrom(n));
			else
				throw new Exception("xml: unexpected node " + n.getNodeName());
		}
		return al.toArray(new String[al.size()]);
	}

	static public final List<String> getStringList(Node inn, String listname, String itemname) throws Exception {
		Node ln = DomTools.nodeFind(inn, listname);
		if(ln == null)
			return null;
		List<String> al = new ArrayList<String>();
		NodeList nl = ln.getChildNodes();
		for(int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if(n.getNodeName().equals("#text"))
				;
			else if(n.getNodeName().equalsIgnoreCase(itemname))
				al.add(DomTools.textFrom(n));
			else
				throw new Exception("xml: unexpected node " + n.getNodeName());
		}
		return al;
	}

	static public Node getRootElement(Document doc) throws Exception {
		return DomTools.getRootElement(doc);
	}

}
