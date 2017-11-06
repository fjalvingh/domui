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

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import to.etc.util.FileTool;
import to.etc.util.StringTool;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

/**
 * Static utility class having stuff to easily obtain data from a DOM parsed
 * document.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class DomTools {
	static public final Date				OBLIVIAN;

	static public final Date				BIGBANG;

	static public final String				DBNULL			= "(dbnull)";

	static private final SimpleDateFormat	m_dateFormat	= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS z");

	static {
		GregorianCalendar cal = new GregorianCalendar();
		cal.set(Calendar.YEAR, 2);
		cal.set(Calendar.MONTH, 0);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		BIGBANG = cal.getTime();

		cal.set(Calendar.YEAR, 3000);
		cal.set(Calendar.MONTH, 0);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		OBLIVIAN = cal.getTime();
	}

	private DomTools() {
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Reading XML.										*/
	/*--------------------------------------------------------------*/
	/**
	 * Creates a DOM parser, parses the document, and returns the DOM
	 * associated with the thing. If errors occur they are logged into an error
	 * message string; these are thrown as an exception when complete.
	 */
	static public Document getDocument(final InputStream is, final String ident, final ErrorHandler eh, final boolean nsaware) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(nsaware);
		DocumentBuilder db = dbf.newDocumentBuilder();
		try {
			//-- Assign myself as the error handler for parsing,
			db.setErrorHandler(eh);
			InputSource ins = new InputSource(is);
			if(ident != null)
				ins.setPublicId(ident);
			return db.parse(ins);
		} catch(IOException x) {
			throw new IOException("XML Parser IO error on " + ident + ": " + x.toString());
		}
	}

	static public Node getDocumentRoot(final String txt, final String ident, final ErrorHandler eh, final boolean nsaware) throws Exception {
		Document doc = getDocument(new StringReader(txt), ident, eh, nsaware);
		if(doc != null)
			return getRootElement(doc);
		return null;
	}

	static public Node getDocumentRoot(final String txt, final String ident, final boolean nsaware) throws Exception {
		DefaultErrorHandler deh = new DefaultErrorHandler();
		Node n = getDocumentRoot(txt, ident, deh, nsaware);
		if(deh.hasErrors())
			throw new Exception(ident + ": xml parse errors: " + deh.getErrors());
		return n;
	}

	/**
	 * Creates a DOM parser, parses the document, and returns the DOM
	 * associated with the thing. If errors occur they are logged into an error
	 * message string; these are thrown as an exception when complete.
	 */
	static public Document getDocument(final Reader is, final String ident, final ErrorHandler eh, final boolean nsaware) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(nsaware);
		DocumentBuilder db = dbf.newDocumentBuilder();
		try {
			//-- Assign myself as the error handler for parsing,
			db.setErrorHandler(eh);
			InputSource ins = new InputSource(is);
			if(ident != null)
				ins.setPublicId(ident);
			return db.parse(ins);
		} catch(IOException x) {
			throw new IOException("XML Parser IO error on " + ident + ": " + x.toString());
		}
	}

	/**
	 * Creates a DOM parser, parses the document, and returns the DOM
	 * associated with the thing. If errors occur they are logged into an error
	 * message string; these are thrown as an exception when complete.
	 */
	static public Document getDocument(File inf, final ErrorHandler eh, final boolean nsaware) throws Exception {
		//-- Create a DOM parser.
		if(!inf.exists() || !inf.isFile())
			throw new IOException(inf + ": file not found.");
		inf = inf.getAbsoluteFile();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(nsaware);
		dbf.setValidating(false);
		dbf.setFeature("http://xml.org/sax/features/namespaces", false);
		dbf.setFeature("http://xml.org/sax/features/validation", false);
		dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
		dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

		DocumentBuilder db = dbf.newDocumentBuilder();
		try {
			//-- Assign myself as the error handler for parsing,
			db.setErrorHandler(eh);
			return db.parse(inf);
		} catch(IOException x) {
			throw new IOException("XML Parser IO error on " + inf + ": " + x.toString());
		}
	}

	/**
	 * Creates a DOM parser, parses the document, and returns the DOM
	 * associated with the thing. If errors occur they are logged into an error
	 * message string; these are thrown as an exception when complete.
	 */
	@Nonnull
	static public Document getDocument(@Nonnull final File inf, final boolean nsaware) throws Exception {
		DefaultErrorHandler deh = new DefaultErrorHandler();
		Document doc = getDocument(inf, deh, nsaware);
		if(deh.hasErrors())
			throw new Exception(inf + ": xml parse errors: " + deh.getErrors());
		return doc;
	}

	/**
	 * Creates a DOM parser, parses the document, and returns the DOM
	 * associated with the thing. If errors occur they are logged into an error
	 * message string; these are thrown as an exception when complete.
	 */
	static public Document getDocument(final InputStream is, final String ident, final boolean nsaware) throws Exception {
		DefaultErrorHandler deh = new DefaultErrorHandler();
		Document doc = getDocument(is, ident, deh, nsaware);
		if(deh.hasErrors())
			throw new Exception(ident + ": xml parse errors: " + deh.getErrors());
		return doc;
	}

	/**
	 * Creates a DOM parser, parses the document, and returns the DOM
	 * associated with the thing. If errors occur they are logged into an error
	 * message string; these are thrown as an exception when complete.
	 */
	static public Document getDocument(final Reader is, final String ident, final boolean nsaware) throws Exception {
		DefaultErrorHandler deh = new DefaultErrorHandler();
		Document doc = getDocument(is, ident, deh, nsaware);
		if(deh.hasErrors())
			throw new Exception(ident + ": xml parse errors: " + deh.getErrors());
		return doc;
	}

	static public Document getDocumentFromZIP(final File zipfile, final String name, final ErrorHandler eh, final boolean nsaware) throws Exception {
		InputStream is = null;
		try {
			is = FileTool.getZipContent(zipfile, name);
			if(is == null)
				return null;
			return getDocument(is, zipfile.toString() + "/" + name, eh, nsaware);
		} finally {
			try {
				if(is != null)
					is.close();
			} catch(Exception x) {}
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	DOM helper stuff									*/
	/*--------------------------------------------------------------*/
	/**
	 *	Finds a single element with the name spec'd in the node. If more than
	 *  one node with the same name exists this throws an exception.
	 */
	static public Node nodeFind(final Node rn, final String name) throws Exception {
		NodeList nl = rn.getChildNodes();
		Node fn = null;
		for(int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if((n.getNodeName().equalsIgnoreCase(name) || name.equalsIgnoreCase(n.getLocalName())) && n.getNodeType() == Node.ELEMENT_NODE) {
				if(fn != null)
					throw new Exception(name + ": duplicate instance within " + rn.getNodeName());
				fn = nl.item(i);
			}
		}
		return fn;
	}

	/**
	 * Searches for child Nodes in the specified Node which have the specified
	 * name and returns the result as a Set. If null is specified for the
	 * parentNode an empty Set is returned.
	 * @param rn, the Node which is queried.
	 * @param name, the name of the childNodes we are searching.
	 * @return
	 * 		a Set<Node> containing the childnodes with the specified name.
	 */
	static public List<Node> nodesFind(final Node rn, final String name) {
		List<Node> nnl = new ArrayList<Node>();
		if(rn == null)
			return nnl;
		NodeList nl = rn.getChildNodes();
		for(int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if((n.getNodeName().equalsIgnoreCase(name) || name.equalsIgnoreCase(n.getLocalName())) && n.getNodeType() == Node.ELEMENT_NODE) {
				nnl.add(nl.item(i));
			}
		}
		return nnl;
	}

	/**
	 *	Gets the text part contained in a node... All text parts are obtained and
	 *  concatenated with a single space.
	 */
	static public String textFrom_untrimmed(final Node n) {
		StringBuffer sb = new StringBuffer();
		NodeList nl = n.getChildNodes();
		for(int i = 0; i < nl.getLength(); i++) {
			Node cn = nl.item(i);
			String txt = cn.getNodeValue();
			if(txt != null) {
				sb.append(txt);
			}
		}
		String s = sb.toString();
		if(DBNULL.equals(s))
			return null;

		return s;
	}

	/**
	 *	Gets the text part contained in a node... All text parts are obtained and
	 *  concatenated with a single space.
	 */
	@Nullable
	static public String textFrom(@Nonnull final Node n) {
		StringBuffer sb = new StringBuffer();
		NodeList nl = n.getChildNodes();
		for(int i = 0; i < nl.getLength(); i++) {
			Node cn = nl.item(i);
			String txt = cn.getNodeValue();
			if(txt != null) {
				sb.append(txt.trim());
			}
		}
		String s = sb.toString();
		if(DBNULL.equals(s))
			return null;

		return s;
	}


	/**
	 *	Finds the child node with the name specified, and returns it's text
	 *  value. If the node is not found it returns null.
	 *  @deprecated
	 *  @see #stringNode(Node, String)
	 */
	@Deprecated
	static public String findChildNodeValue(final Node rootnode, final String name) throws Exception {
		Node n = nodeFind(rootnode, name);
		if(n == null)
			return null;
		return textFrom(n);
	}


	/**
	 *	Finds the child node with the name specified, and returns it's text
	 *  value. If the node is not found it returns null.
	 */
	static public String stringNode(final Node rootnode, final String name) throws Exception {
		Node n = nodeFind(rootnode, name);
		if(n == null)
			return null;
		return textFrom(n);
	}

	/**
	 *	Finds the child node with the name specified, and returns it's text
	 *  value. If the node is not found it returns null.
	 */
	static public String stringNode(final Node rootnode, final String name, final int trunclen) throws Exception {
		String s = stringNode(rootnode, name);
		if(s != null) {
			if(s.length() > trunclen)
				s = s.substring(0, trunclen);
		}
		return s;
	}

	/**
	 *	Finds the child node with the name specified, and returns it's text
	 *  value. If the node is not found it returns the default string.
	 */
	static public String stringNode(final Node rootnode, final String name, final String deflt) throws Exception {
		Node n = nodeFind(rootnode, name);
		if(n == null)
			return deflt;
		return textFrom(n);
	}

	/**
	 *	Finds the child node with the name specified, and returns it's text
	 *  value. If the node is not found it returns null.
	 */
	static public String stringNode_untrimmed(final Node rootnode, final String name) throws Exception {
		Node n = nodeFind(rootnode, name);
		if(n == null)
			return null;
		return textFrom_untrimmed(n);
	}

	/**
	 *	Finds the child node with the name specified, and returns it's text
	 *  value. If the node is not found it returns the default string.
	 */
	static public String stringNode_untrimmed(final Node rootnode, final String name, final String deflt) throws Exception {
		Node n = nodeFind(rootnode, name);
		if(n == null)
			return deflt;
		return textFrom_untrimmed(n);
	}


	static public Date dateNode(final Node rn, final String name) throws Exception {
		String s = stringNode(rn, name);
		if(s == null)
			return null;
		return dateDecode(s);
	}

	static public Date dateNode(final Node rn, final String name, final Date dflt) throws Exception {
		String s = stringNode(rn, name);
		if(s == null)
			return dflt;
		if(DBNULL.equals(s))
			return dflt;
		return dateDecode(s);
	}

	static private final void timeError(final String v) throws Exception {
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
	static public int timeNode(final Node rn, final String name, final int dflt) throws Exception {
		String v = stringNode(rn, name, null);
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

	static public int intNode(final Node rootnode, final String name) throws Exception {
		String s = stringNode(rootnode, name);
		if(s != null) {
			s = s.trim();
			if(s.length() >= 0 && !DBNULL.equals(s)) {
				try {
					return Integer.parseInt(s);
				} catch(Exception ex) {
					throw new Exception(name + ": integer value expected, got '" + s + "'");
				}
			}
		}

		throw new Exception("Missing '" + name + "' node [integer]");
	}

	static public int intNode(final Node rootnode, final String name, final int val) throws Exception {
		String s = stringNode(rootnode, name);
		if(s == null)
			return val;
		s = s.trim();
		if(s.length() == 0 || DBNULL.equals(s))
			return val;

		try {
			return Integer.parseInt(s);
		} catch(Exception ex) {
			throw new Exception(name + ": integer value expected, got '" + s + "'");
		}
	}

	static public Long longWrapperNode(final Node rootnode, final String name) throws Exception {
		String s = stringNode(rootnode, name);
		if(s == null)
			return null;
		s = s.trim();
		if(s.length() == 0 || DBNULL.equals(s))
			return null;
		try {
			return Long.decode(s);
		} catch(Exception ex) {
			throw new Exception(name + ": long value expected, got '" + s + "'");
		}
	}

	static public long longNode(final Node rootnode, final String name) throws Exception {
		String s = stringNode(rootnode, name);
		if(s != null) {
			s = s.trim();
			if(s.length() >= 0 && !DBNULL.equals(s)) {
				try {
					return Long.parseLong(s);
				} catch(Exception ex) {
					throw new Exception(name + ": long value expected, got '" + s + "'");
				}
			}
		}

		throw new Exception("Missing '" + name + "' node [long]");
	}

	static public long longNode(final Node rootnode, final String name, final long val) throws Exception {
		String s = stringNode(rootnode, name);
		if(s == null)
			return val;
		s = s.trim();
		if(s.length() == 0 || DBNULL.equals(s))
			return val;

		try {
			return Long.parseLong(s);
		} catch(Exception ex) {
			throw new Exception(name + ": long value expected, got '" + s + "'");
		}
	}

	static public double doubleNode(final Node rootnode, final String name, final double val) throws Exception {
		String s = stringNode(rootnode, name);
		if(s == null)
			return val;
		s = s.trim();
		if(s.length() == 0 || DBNULL.equals(s))
			return val;

		try {
			return Double.parseDouble(s);
		} catch(Exception ex) {
			throw new Exception(name + ": double value expected, got '" + s + "'");
		}
	}

	/**
	 * Checks if there's a file node with the spec'd name in the node. If so
	 * this returns the node's filename... If the node doesn't exist this
	 * returns null..
	 * @param rootnode
	 * @param name
	 * @return
	 * @throws Exception
	 */
	static public String fileNameNode(final Node rootnode, final String name) throws Exception {
		Node fn = nodeFind(rootnode, name);
		if(fn == null)
			return null; // No node.

		//-- Named node present. Is it a file node?
		Node fnn = DomTools.nodeFind(fn, "file"); // Has a file node?
		if(fnn == null)
			return null; // No -> no file.

		//-- has a <file> node. Get filename...
		String rname = DomTools.textFrom(fnn);
		if(rname == null || rname.length() == 0) {
			System.out.println("DomTools: no name in file node!?");
			return null;
		}
		rname = rname.trim();
		return rname;
	}


	/**
	 *	Finds the child node with the name specified, and returns it as a
	 *  boolean value. If the node has NO text associated then this returns TRUE,
	 *  if the node is not present then this returns false; if the node is present
	 *  and has text the text field is interpreted: if numeric we return T if
	 *  the number is not null; if text the value must start with T for true and
	 *  F for false. All other values throw an exception.
	 */
	static public boolean boolNode(final Node rootnode, final String name) throws Exception {
		Node n = nodeFind(rootnode, name);
		if(n == null)
			return false; // Not present.
		return decodeBoolStr(textFrom(n), name);
	}

	/**
	 *	Finds the child node with the name specified, and returns it as a
	 *  boolean value. If the node has NO text associated then this returns TRUE,
	 *  if the node is not present then this returns false; if the node is present
	 *  and has text the text field is interpreted: if numeric we return T if
	 *  the number is not null; if text the value must start with T for true and
	 *  F for false. All other values throw an exception.
	 */
	static public Boolean booleanNode(final Node rootnode, final String name) throws Exception {
		Node n = nodeFind(rootnode, name);
		if(n == null)
			return null; // Not present.
		return Boolean.valueOf(decodeBoolStr(textFrom(n), name));
	}

	static public boolean decodeBoolStr(String nt, final String name) {
		if(nt == null)
			return false; // was (dbnull).
		nt = nt.trim();
		if(nt.length() == 0)
			return true; // No text,
		char c = nt.charAt(0);
		if(c == '0')
			return false;
		if(Character.isDigit(c))
			return true;
		if(c == 't' || c == 'T')
			return true;
		if(c == 'f' || c == 'F')
			return false;
		throw new IllegalArgumentException(name + ": node must contain a boolean value");
	}

	/**
	 * Get the named attribute from a node. If the attribute is not present
	 * return the default value in defval
	 * @param n			the node to containing the attribute.
	 * @param aname		the name of the attribute.
	 * @param defval	the value to return if the attribute is not present,
	 * @return			a string containing the attribute's value or the default.
	 */
	static public String getNodeAttribute(@Nonnull final Node n, @Nonnull final String aname, @Nullable final String defval) {
		if(n.hasAttributes()) {
			Node idn = n.getAttributes().getNamedItem(aname);
			if(idn != null)
				return idn.getNodeValue();
		}
		return defval;
	}

	static public String strAttr(final Node n, final String aname, final String def) {
		return getNodeAttribute(n, aname, def);
	}

	@Nonnull
	static public String strAttr(@Nonnull final Node n, @Nonnull final String aname) {
		String s = getNodeAttribute(n, aname, null);
		if(s == null)
			throw new IllegalStateException("Missing attribute '" + aname + "' on node '" + n.getNodeName() +"'");
		return s;
	}

	static public int intAttr(final Node n, final String aname, final int defval) throws Exception {
		return getNodeAttribute(n, aname, defval);
	}

	static public int intAttr(final Node n, final String aname) throws Exception {
		if(n.hasAttributes()) {
			Node idn = n.getAttributes().getNamedItem(aname);
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
		throw new Exception("Missing mandatory int attribute '" + aname + "' on tag '" + n.getNodeName() + "'");
	}

	static public long longAttr(final Node n, final String aname) throws Exception {
		if(n.hasAttributes()) {
			Node idn = n.getAttributes().getNamedItem(aname);
			if(idn != null) {
				String v = idn.getNodeValue();
				if(v != null && v.length() > 0) {
					try {
						return Long.parseLong(v.trim());
					} catch(Exception ex) {
						throw new Exception("expected long value for attribute " + aname + ", but got '" + v + "'");
					}
				}
			}
		}
		throw new Exception("Missing mandatory long attribute '" + aname + "' on tag '" + n.getNodeName() + "'");
	}

	static public Long longAttrWrapped(final Node n, final String aname) throws Exception {
		return Long.valueOf(longAttr(n, aname));
	}

	static public boolean boolAttr(final Node n, final String aname) throws Exception {
		if(n.hasAttributes()) {
			Node idn = n.getAttributes().getNamedItem(aname);
			if(idn != null) {
				String v = idn.getNodeValue();
				return decodeBoolStr(v, aname);
			}
		}
		throw new Exception("Missing mandatory boolean attribute '" + aname + "' on tag '" + n.getNodeName() + "'");
	}

	static public boolean boolAttr(final Node n, final String aname, final boolean defval) throws Exception {
		if(n.hasAttributes()) {
			Node idn = n.getAttributes().getNamedItem(aname);
			if(idn != null) {
				String v = idn.getNodeValue();
				return decodeBoolStr(v, aname);
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
	static public int getNodeAttribute(final Node n, final String aname, final int defval) throws Exception {
		if(n.hasAttributes()) {
			Node idn = n.getAttributes().getNamedItem(aname);
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

	/**
	 * Encodes a date-only field to some readable form. Can be decoded by
	 * decodeDate or getNodeDate(). The date contains local timezone info.
	 * @param dt	the date to encode.
	 * @return		a string
	 */
	static public String dateEncode(final Date dt) {
		if(dt == null)
			return "(dbnull)";
		//		System.out.println("dateEncode: input is "+dt+", obl="+OBLIVIAN+", t="+dt.getClass().getName());
		if(dt.equals(OBLIVIAN) || dt.after(OBLIVIAN)) {
			//			System.out.println("dateEncode: out oblivian");
			return "oblivian";
		}
		if(dt.equals(BIGBANG) || dt.before(BIGBANG)) {
			//			System.out.println("dateEncode: out big-bang");
			return "big-bang";
		}

		GregorianCalendar c = new GregorianCalendar();
		c.setTime(dt);
		TimeZone tz = TimeZone.getDefault();

		StringBuffer sb = new StringBuffer(30);
		sb.append(StringTool.intToStr(c.get(Calendar.YEAR), 10, 4));
		sb.append('-');
		sb.append(StringTool.intToStr(c.get(Calendar.MONTH) + 1, 10, 2));
		sb.append('-');
		sb.append(StringTool.intToStr(c.get(Calendar.DAY_OF_MONTH), 10, 2));
		sb.append(' ');
		sb.append(StringTool.intToStr(c.get(Calendar.HOUR_OF_DAY), 10, 2));
		sb.append(':');
		sb.append(StringTool.intToStr(c.get(Calendar.MINUTE), 10, 2));
		sb.append(':');
		sb.append(StringTool.intToStr(c.get(Calendar.SECOND), 10, 2));
		sb.append('.');
		sb.append(StringTool.intToStr(c.get(Calendar.MILLISECOND), 10, 4));

		sb.append(' ');
		sb.append(tz.getID());
		//		System.out.println("dateEncode: out "+sb.toString());
		return sb.toString();
	}


	/**
	 * Decodes a date and converts it to the local time. The format MUST match
	 * the format generated by encode or we fail. Dates can contain three special
	 * values:
	 * <dl>
	 * 	<dt>(dbnull)<dd>The date field is null
	 * 	<dt>big-bang<dd>The date field represents a date before any other date,
	 *	<dt>oblivian<dd>The dtae field represents a date after any other date.
	 * <pre>
	 * Format:
	 * 0123456789012345678901234567
	 * 2001-12-24 18:10:52.0012 (timezone)
	 *
	 * @param s
	 * @return
	 */
	static public Date dateDecode(String s) {
		s = s.trim();
		//		System.out.print("dateDecode: input="+s);
		if(s.equals("(dbnull)")) {
			//			System.out.println(", return null");
			return null;
		}
		if(s.equals("oblivian")) {
			//			System.out.println(", return oblivian");
			return OBLIVIAN;
		}
		if(s.equals("big-bang")) {
			//			System.out.println(", return big-bang");
			return BIGBANG;
		}

		try {
			return m_dateFormat.parse(s);
		} catch(Exception x) {}

		try {
			int year = Integer.parseInt(s.substring(0, 4));
			int month = Integer.parseInt(s.substring(5, 7));
			int day = Integer.parseInt(s.substring(8, 10));
			int hour = Integer.parseInt(s.substring(11, 13));
			int min = Integer.parseInt(s.substring(14, 16));
			int sec = Integer.parseInt(s.substring(17, 19));
			int msec = Integer.parseInt(s.substring(20, 24));
			//			String	tzs	= s.substring(24).trim();

			//-- Ok- convert,
			//			TimeZone	mytz= TimeZone.getDefault();
			//			TimeZone	tz	= TimeZone.getTimeZone(tzs);
			//			GregorianCalendar	c	= new GregorianCalendar(tz);
			GregorianCalendar c = new GregorianCalendar();
			c.set(Calendar.YEAR, year);
			c.set(Calendar.MONTH, month - 1);
			c.set(Calendar.DAY_OF_MONTH, day);
			c.set(Calendar.HOUR_OF_DAY, hour);
			c.set(Calendar.MINUTE, min);
			c.set(Calendar.SECOND, sec);
			c.set(Calendar.MILLISECOND, msec);
			Date dt = c.getTime();
			//			System.out.println(", result="+dt);
			return dt;
		} catch(Exception ex) {
			return null;
		}
	}

	static public final boolean isOblivian(final Date dt) {
		if(dt == null)
			return false;
		if(dt instanceof java.sql.Date) {
			return dt.getTime() >= OBLIVIAN.getTime();
		}
		return dt.equals(OBLIVIAN) || dt.after(OBLIVIAN);
	}

	static public final boolean isBigBang(final Date dt) {
		if(dt == null)
			return false;
		if(dt instanceof java.sql.Date) {
			return dt.getTime() <= BIGBANG.getTime();
		}
		return dt.equals(BIGBANG) || dt.before(BIGBANG);
	}

	static public final String[] getStringList(final Node inn, final String itemname) throws Exception {
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

	static public final List<String> getStringList(final Node inn, final String listname, final String itemname) throws Exception {
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

	static public Node getRootElement(final Document doc) throws Exception {
		NodeList nl = doc.getChildNodes();
		Node root = null;
		for(int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if(n.getNodeType() == Node.ELEMENT_NODE) {
				if(root != null)
					throw new IllegalStateException("Multiple root nodes in document: " + root.getNodeName() + " and " + n.getNodeName());
				root = n;
			}
		}
		if(root == null)
			throw new IllegalStateException("No root node in XML document.");
		return root;
	}

	static public boolean isTextOnly(final Node inn) {
		NodeList nl = inn.getChildNodes();
		for(int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			short nt = n.getNodeType();
			if(nt != Node.TEXT_NODE && nt != Node.PROCESSING_INSTRUCTION_NODE && nt != Node.COMMENT_NODE)
				return false;
		}
		return true;
	}

	static public void saveDocument(final File of, final Document doc) throws Exception {
		Source s = new DOMSource(doc);
		Result r = new StreamResult(of);
		Transformer t = TransformerFactory.newInstance().newTransformer();
		t.transform(s, r);
	}

	static public void saveDocument(final Writer of, final Document doc) throws Exception {
		Source s = new DOMSource(doc);
		Result r = new StreamResult(of);
		Transformer t = TransformerFactory.newInstance().newTransformer();
		t.transform(s, r);
	}


	static public void setAttr(final Node elem, final String name, final String value) {
		Node n = elem.getOwnerDocument().createAttribute(name);
		n.setNodeValue(value);
		elem.getAttributes().setNamedItem(n);
	}

	/**
	 * Finds a child node, via the xpath query route. Thus the name of all the nodes, separated by a "/".
	 * The path can only use unique names. If a child node with the same name exists an exception will be
	 * thrown.
	 *
	 * @param node the node to search
	 * @param xpathQuery path to the child node
	 * @return the child node, or null if it cannot be found.
	 * @throws Exception
	 */
	static public Node nodeFindXpath(final Node node, final String xpathQuery) throws Exception {
		String[] nodes = xpathQuery.split("/");
		if(nodes.length == 0)
			return null;
		Node returnNode = nodeFind(node, nodes[0]);
		if(returnNode == null)
			return null;
		for(int i = 1; i < nodes.length; i++) {
			returnNode = nodeFind(returnNode, nodes[i]);
			if(null == returnNode)
				return null;
		}
		return returnNode;
	}

	/**
	 * Get a stream reader that does not ^&*^(^$ connect to the Internet while fscking reading xml 8-(
	 * @return
	 */
	@Nonnull
	static public XMLInputFactory getStreamFactory() {
		XMLInputFactory xmlif = XMLInputFactory.newInstance();
		//		xmlif.setProperty("http://apache.org/xml/features/nonvalidating/load-external-dtd", Boolean.FALSE);
		xmlif.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
		xmlif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
		xmlif.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
		//		xmlif.setProperty(XMLInputFactory., Boolean.FALSE);
		//		xmlif.setProperty(XMLInputFactory., Boolean.FALSE);
		//		xmlif.setProperty(XMLInputFactory., Boolean.FALSE);
		//		xmlif.setProperty(XMLInputFactory., Boolean.FALSE);
		return xmlif;
	}


}
