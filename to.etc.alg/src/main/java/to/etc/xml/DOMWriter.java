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
/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "SOAP" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 2000, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package to.etc.xml;

import java.io.*;

import org.w3c.dom.*;

/**
 * REPLACE WITH PROPER ContentHandler and SAX pusher version.
 *
 * This class is a utility to serialize a DOM node as XML. This code is
 * derived from the sample of the same name distributed with XML4J_2_0_15.
 * The following significant changes were made:
 * comments and top-level PIs, now uses short-hand element syntax when an
 * element is childless, now attempts to use the correct line-termination
 * sequence. Also: removed the code related to canonical ordering, alternate
 * encodings, and use as a stand-alone utility.
 *
 * @author Matthew J. Duftler (duftler@us.ibm.com)
 */
@Deprecated
public class DOMWriter {
	/**
	 * Return a string containing this node serialized as XML.
	 */
	public static String nodeToString(Node node) {
		StringWriter sw = new StringWriter();

		serializeAsXML(node, sw);

		return sw.toString();
	}

	/**
	* Serialize this node into the writer as XML.
	*/
	public static void serializeAsXML(Node node, Writer writer) {
		print(node, new PrintWriter(writer));
	}

	private static void print(Node node, PrintWriter out) {
		if(node == null) {
			return;
		}

		boolean hasChildren = false;
		int type = node.getNodeType();

		switch(type){
			case Node.DOCUMENT_NODE: {
				out.println("<?xml version=\"1.0\"?>");

				NodeList children = node.getChildNodes();

				if(children != null) {
					int numChildren = children.getLength();

					for(int i = 0; i < numChildren; i++) {
						print(children.item(i), out);
					}
				}
				break;
			}

			case Node.ELEMENT_NODE: {
				out.print('<' + node.getNodeName());

				NamedNodeMap attrs = node.getAttributes();
				int len = (attrs != null) ? attrs.getLength() : 0;

				if(attrs != null) {
					for(int i = 0; i < len; i++) {
						Attr attr = (Attr) attrs.item(i);

						out.print(' ' + attr.getNodeName() + "=\"" + normalize(attr.getValue()) + '\"');
					}
				}

				NodeList children = node.getChildNodes();

				if(children != null) {
					int numChildren = children.getLength();

					hasChildren = (numChildren > 0);

					if(hasChildren) {
						out.print('>');
					}

					for(int i = 0; i < numChildren; i++) {
						print(children.item(i), out);
					}
				} else {
					hasChildren = false;
				}

				if(!hasChildren) {
					out.print("/>");
				}
				break;
			}

			case Node.ENTITY_REFERENCE_NODE: {
				out.print('&');
				out.print(node.getNodeName());
				out.print(';');
				break;
			}

			case Node.CDATA_SECTION_NODE: {
				out.print("<![CDATA[");
				out.print(node.getNodeValue());
				out.print("]]>");
				break;
			}

			case Node.TEXT_NODE: {
				out.print(normalize(node.getNodeValue()));
				break;
			}

			case Node.COMMENT_NODE: {
				out.print("<!--");
				out.print(node.getNodeValue());
				out.print("-->");
				break;
			}

			case Node.PROCESSING_INSTRUCTION_NODE: {
				out.print("<?");
				out.print(node.getNodeName());

				String data = node.getNodeValue();

				if(data != null && data.length() > 0) {
					out.print(' ');
					out.print(data);
				}

				out.println("?>");
				break;
			}
		}

		if(type == Node.ELEMENT_NODE && hasChildren == true) {
			out.print("</");
			out.print(node.getNodeName());
			out.print('>');
			hasChildren = false;
		}
	}

	private static String normalize(String s) {
		StringBuffer str = new StringBuffer();
		if(s == null) {
			return str.toString();
		}

		int len = (s != null) ? s.length() : 0;

		for(int i = 0; i < len; i++) {
			char ch = s.charAt(i);

			switch(ch){
				case '<': {
					str.append("&lt;");
					break;
				}
				case '>': {
					str.append("&gt;");
					break;
				}
				case '&': {
					str.append("&amp;");
					break;
				}
				case '"': {
					str.append("&quot;");
					break;
				}
				case '\n': {
					if(i > 0) {
						char lastChar = str.charAt(str.length() - 1);

						if(lastChar != '\r') {
							str.append(lineSeparator);
						} else {
							str.append('\n');
						}
					} else {
						str.append(lineSeparator);
					}
					break;
				}
				default: {
					str.append(ch);
				}
			}
		}

		return (str.toString());
	}

	static private String	lineSeparator	= System.getProperty("line.separator");
}
