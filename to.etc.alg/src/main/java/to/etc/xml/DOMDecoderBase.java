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

public class DOMDecoderBase {
	/** The namespace that was used to encode the types herein. */
	private String	m_encodingNamespace;

	/** When not null, this is the namespace to look for values. If the empty string it requires a namespaceless lookup. */
	private String	m_defaultNamespace;

	private Node	m_currentRoot;

	public DOMDecoderBase() {
		m_encodingNamespace = XMLNameSpaces.SOAP_ENCODING;
	}

	public DOMDecoderBase(Node root) {
		this(root, null, XMLNameSpaces.SOAP_ENCODING);
	}

	public DOMDecoderBase(String encodingNamespace) {
		m_encodingNamespace = encodingNamespace;
	}

	public DOMDecoderBase(Node currentRoot, String defaultNamespace, String encodingNamespace) {
		m_currentRoot = currentRoot;
		m_defaultNamespace = defaultNamespace;
		m_encodingNamespace = encodingNamespace;
	}

	//	/**
	//	 * Finds the 0..1 string in the specified root.
	//	 * @param rootnode
	//	 * @param ns
	//	 * @param name
	//	 * @return
	//	 */
	//	private String	findStringNode(Node rootnode, String ns, String name) {
	//		Node	n	= nodeFind(rootnode, ns, name);
	//		if(n == null) return null;
	//		return textFrom(n);
	//	}
	private String findStringNode(String name) {
		Node n = nodeFind(getCurrentRoot(), currentNS(), name);
		if(n == null)
			return null;
		return textFrom(n);
	}

	private void appendParent(StringBuilder sb, Node nd) {
		if(nd == null)
			return;
		if(nd.getParentNode() != null && nd.getParentNode().getNodeType() != Node.DOCUMENT_NODE) {
			appendParent(sb, nd.getParentNode());
			sb.append('.');
		}
		sb.append(nd.getNodeName());
	}

	private String createLocation(String name) {
		StringBuilder sb = new StringBuilder();
		appendParent(sb, getCurrentRoot());
		if(sb.length() > 0)
			sb.append('.');
		sb.append(name);
		return sb.toString();
	}

	private void missing(String name) {
		StringBuilder sb = new StringBuilder();
		appendParent(sb, getCurrentRoot());
		throw new W3CEncodingException("Missing element '" + name + "@" + currentNS() + "'").setLocation(sb.toString());
	}

	private void invalid(String name, String value) {
		throw new W3CEncodingException("Invalid value", value).setLocation(createLocation(name));
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	String retrieval.									*/
	/*--------------------------------------------------------------*/
	/**
	 * Mandatory string node.
	 * @param rootnode
	 * @param ns
	 * @param name
	 * @return
	 */
	public String string(String name) {
		Node n = nodeFind(getCurrentRoot(), currentNS(), name);
		if(n == null)
			missing(name);
		return textFrom(n);
	}
	public String	string(final String name, final int maxlen) {
		Node	n	= nodeFind(getCurrentRoot(), currentNS(), name);
		if(n == null)
			missing(name);
		String val = textFrom(n);
		if(val.length() > maxlen)
			val = val.substring(0, maxlen);
		return val;
	}

	/**
	 * Finds the 0..1 string in the specified root, returns the default value if the string is not found.
	 * @param rootnode
	 * @param ns
	 * @param name
	 * @param dflt
	 * @return
	 */
	public String string(String name, String dflt) {
		String res = findStringNode(name);
		return res==null ? dflt : res;
	}
	public String	string(final String name, final String dflt, final int maxlen) {
		String res = findStringNode(name);
		if(res == null)
			res = dflt;
		if(res != null && res.length() > maxlen)
			res = res.substring(0, maxlen);
		return res;
	}

	public String	oneOf(final String name, final String... list) {
		String	value = string(name, null);					// Locate the string variant or null
		for(String pv : list) {
			if(pv == null) {
				if(value == null)
					return null;
			} else if(pv.equals(value))
				return value;
		}
		invalid(name, value); // Throws Exception.
		return null;
	}

	/**
	 * Returns an encoded DATE string, represented by xsd:date.
	 * @param name
	 * @return
	 */
	public Date dateOnly(String name) {
		try {
			return W3CSchemaCoder.decodeDate(string(name)).getTime();
		} catch(W3CEncodingException x) {
			x.setLocation(createLocation(name));
			throw x;
		}
	}

	/**
	 * Returns an encoded DATE string, represented by xsd:date.
	 * @param name
	 * @return
	 */
	public Date dateOnly(String name, Date deflt) {
		String s = string(name, null);
		if(s == null || s.length() == 0)
			return deflt;
		try {
			return W3CSchemaCoder.decodeDate(s).getTime();
		} catch(W3CEncodingException x) {
			x.setLocation(createLocation(name));
			throw x;
		}
	}

	/**
	 * Returns an encoded DATE string, encoded by xsd:dateTime.
	 * @param name
	 * @return
	 */
	public Date dateTime(String name) {
		try {
			return W3CSchemaCoder.decodeDateTime(string(name)).getTime();
		} catch(W3CEncodingException x) {
			x.setLocation(createLocation(name));
			throw x;
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Current node navigation.							*/
	/*--------------------------------------------------------------*/
	/**
	 * Return the current root for node lookups.
	 * @return
	 */
	public Node getCurrentRoot() {
		if(m_currentRoot == null)
			throw new IllegalStateException("The current root node is undefined.");
		return m_currentRoot;
	}

	private String currentNS() {
		if(m_defaultNamespace == null)
			throw new IllegalStateException("No namespace to use for nodes known");
		return m_defaultNamespace;
	}

	/**
	 * Checks if the specified node in the current namespace is present in the currentRoot. If the node is not
	 * present it returns false, leaving the currentRoot unaltered. If the node is present it gets selected as
	 * the new currentRoot and the routine returns true.
	 * @param name
	 * @return
	 */
	public boolean selectNode(String name) {
		Node nd = nodeFind(getCurrentRoot(), currentNS(), name);
		if(nd == null)
			return false;
		m_currentRoot = nd;
		return true;
	}

	/**
	 * Like select this locates the named element and makes it current, but this one aborts if the element is not present.
	 * @param name
	 */
	public void into(String name) {
		if(!selectNode(name))
			missing(name);
	}

	/**
	 * Move n nodes UP.
	 * @param count
	 */
	public void up(int count) {
		while(count-- > 0) {
			m_currentRoot = getCurrentRoot().getParentNode();
		}
	}

	public void up() {
		up(1);
	}

	//	/**
	//	 * Select a node in ANOTHER namespace.
	//	 * @param ns
	//	 * @param name
	//	 * @return
	//	 */
	//	public boolean		nsSelectNode(String ns, String name) {
	//		Node	nd	= nodeFind(getCurrentRoot(), currentNS(), name);
	//		if(nd == null)
	//			return false;
	//		m_currentRoot = nd;
	//		return true;
	//	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Node list walking.									*/
	/*--------------------------------------------------------------*/
	static private final DOMNodeIterator	EMPTY_ITERATOR	= new DOMNodeIterator();

	/**
	 * When present this selects the node with the specified name, and initializes a list-walk at this level. This
	 * returns an iterator for the nodes below the list. If the node specified does not exist it returns the empty
	 * iterator.
	 * this call returns true (indicating the node exists) you can
	 */
	public Iterable<DOMDecoder> list(String name) {
		Node nd = nodeFind(getCurrentRoot(), currentNS(), name);
		if(nd == null)
			return EMPTY_ITERATOR;
		return new DOMNodeIterator(this, nd);
	}

	public Iterable<DOMDecoder> list(String name, String childNodeName) {
		Node nd = nodeFind(getCurrentRoot(), currentNS(), name);
		if(nd == null)
			return EMPTY_ITERATOR;
		return new DOMNodeIterator(this, nd, childNodeName, null, true);
	}

	/**
	 * Creates an iterator for ALL nodes below the currently selected node.
	 * @return
	 */
	public Iterable<DOMDecoder> getChildIterator() {
		return new DOMNodeIterator(this, m_currentRoot);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Helper code.										*/
	/*--------------------------------------------------------------*/
	/**
	 * Finds a single element with the name spec'd in the node. If more than
	 * one node with the same name exists this throws an exception.
	 */
	public Node nodeFind(Node rn, String ns, String name) {
		NodeList nl = rn.getChildNodes();
		Node fn = null;
		for(int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if(name.equals(n.getLocalName()) && ns.equals(n.getNamespaceURI()) && n.getNodeType() == Node.ELEMENT_NODE) {
				if(fn != null)
					throw new W3CEncodingException(name + ": duplicate instance within " + rn.getNodeName());
				fn = nl.item(i);
			}
		}
		return fn;
	}

	public Node nodeGet(Node rn, String ns, String name) {
		Node n = nodeFind(rn, ns, name);
		if(n == null)
			throw new W3CEncodingException(name + "@" + ns + ": node not found.");
		return n;
	}

	/**
	 * Gets the text part contained in a node... All text parts are obtained and
	 * concatenated with a single space.
	 */
	public String textFrom_untrimmed(Node n) {
		StringBuilder sb = null;
		String res = null;
		NodeList nl = n.getChildNodes();
		for(int i = 0; i < nl.getLength(); i++) {
			Node cn = nl.item(i);
			String txt = cn.getNodeValue();
			if(txt != null) {
				if(res == null)
					res = txt;
				else {
					if(sb == null) {
						sb = new StringBuilder(txt.length() + res.length() + 100);
						sb.append(res);
					}
					sb.append(txt);
				}
			}
		}
		return sb != null ? sb.toString() : res;
	}

	/**
	 *	Gets the text part contained in a node... All text parts are obtained and
	 *  concatenated with a single space.
	 */
	public String textFrom(Node n) {
		String v = textFrom_untrimmed(n);
		return v == null ? null : v.trim();
	}

	public String getEncodingNamespace() {
		return m_encodingNamespace;
	}

	public void setEncodingNamespace(String encodingNamespace) {
		m_encodingNamespace = encodingNamespace;
	}

	public String getDefaultNamespace() {
		return m_defaultNamespace;
	}

	public void setDefaultNamespace(String defaultNamespace) {
		m_defaultNamespace = defaultNamespace;
	}

	public void setCurrentRoot(Node currentRoot) {
		m_currentRoot = currentRoot;
	}

}
