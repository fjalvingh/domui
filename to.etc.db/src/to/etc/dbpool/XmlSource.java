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
package to.etc.dbpool;

import java.io.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.*;

public class XmlSource extends PoolConfigSource {
	static class DefaultErrorHandler implements ErrorHandler {
		/** This string buffer receives error messages while the document gets parsed. */
		private StringBuilder m_xmlerr_sb = new StringBuilder();

		private boolean m_errors;

		private void genErr(SAXParseException exception, String type) {
			//          exception.printStackTrace();
			if(m_xmlerr_sb.length() > 0)
				m_xmlerr_sb.append("\n");
			String id = exception.getPublicId();
			if(id == null || id.length() == 0)
				id = exception.getPublicId();
			if(id == null || id.length() == 0)
				id = "unknown-source";
			m_xmlerr_sb.append(id);
			m_xmlerr_sb.append('(');
			m_xmlerr_sb.append(Integer.toString(exception.getLineNumber()));
			m_xmlerr_sb.append(':');
			m_xmlerr_sb.append(Integer.toString(exception.getColumnNumber()));
			m_xmlerr_sb.append(") ");
			m_xmlerr_sb.append(type);
			m_xmlerr_sb.append(":");
			m_xmlerr_sb.append(exception.getMessage());
		}

		public final void warning(SAXParseException exception) throws SAXException {
			genErr(exception, "warning");
		}

		public final void error(SAXParseException exception) throws SAXException {
			m_errors = true;
			genErr(exception, "error");
		}

		public final void fatalError(SAXParseException exception) throws SAXException {
			m_errors = true;
			genErr(exception, "fatal");
		}

		public String getErrors() {
			return m_xmlerr_sb.toString();
		}

		public boolean hasErrors() {
			return m_errors;
		}
	}

	private Node m_src;

	private Node m_backup;

	public XmlSource(File src, File back) {
		super(src, back);
	}

	private synchronized void init() throws Exception {
		if(m_src != null)
			return;
		if(!getSrc().exists())
			throw new IllegalArgumentException("The pool manager config file " + getSrc() + " does not exist.");
		m_src = getDocument(getSrc(), false);
		if(getBackupSrc() != null && getBackupSrc().exists()) {
			m_backup = getDocument(getBackupSrc(), false);
		}
	}

	/**
	 * Creates a DOM parser, parses the document, and returns the DOM
	 * associated with the thing. If errors occur they are logged into an error
	 * message string; these are thrown as an exception when complete.
	 */
	private Node getDocument(File f, boolean nsaware) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(nsaware);
		DocumentBuilder db = dbf.newDocumentBuilder();
		InputStream is = null;
		DefaultErrorHandler deh = new DefaultErrorHandler();
		try {
			//-- Assign myself as the error handler for parsing,
			is = new FileInputStream(f);
			db.setErrorHandler(deh);
			InputSource ins = new InputSource(is);
			ins.setPublicId(f.toString());
			Document doc = db.parse(ins);
			if(deh.hasErrors())
				throw new Exception(f + ": xml parse errors: " + deh.getErrors());
			return getRootElement(doc);
		} catch(IOException x) {
			throw new IOException("XML Parser IO error on " + f + ": " + x.toString());
		} finally {
			try {
				if(is != null)
					is.close();
			} catch(Exception x) {}
		}
	}

	static private Node getRootElement(Document doc) throws Exception {
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

	private Node findPoolNode(Node root, String sec) throws Exception {
		NodeList nl = root.getChildNodes();
		Node found = null;
		for(int i = 0; i < nl.getLength(); i++) {
			//-- find 'pool' node with 'name=' attribute equals to section.
			Node n = nl.item(i);
			if(n.getNodeName().equals("pool")) {
				NamedNodeMap m = n.getAttributes();
				if(m != null) {
					Node nn = m.getNamedItem("name");
					if(nn != null) {
						String s = nn.getNodeValue();
						if(s != null) {
							if(sec.equals(s.trim())) {
								//-- Found named node!!
								if(found != null)
									throw new Exception("Duplicate pool ID=" + sec + " in xml file");
								found = n;
							}
						}
					}
				}
			}
		}
		return found;
	}

	private String getValue(Node pool, String name) throws Exception {
		//-- Try attributes
		NamedNodeMap m = pool.getAttributes();
		if(m == null)
			return null;
		Node n = m.getNamedItem(name);
		if(n == null)
			return null;
		if(n.getNodeValue() == null)
			return "";
		return n.getNodeValue().trim();
	}

	private String getValue(Node root, String section, String name) throws Exception {
		Node pool = findPoolNode(root, section);
		if(pool == null)
			return null;
		return getValue(pool, name);
	}

	@Override
	public String getProperty(String section, String name) throws Exception {
		init();
		String v = null;
		if(null != m_backup) {
			v = getValue(m_backup, section, name);
		}
		if(v == null)
			v = getValue(m_src, section, name);
		return v;
	}

}
