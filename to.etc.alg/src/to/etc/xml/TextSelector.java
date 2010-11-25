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

import org.w3c.dom.*;

/**
 * Matches a single #text node. The node cannot contain anything else but #text nodes.
 * <p>Created on Jun 9, 2005
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class TextSelector implements PathSelector {
	public TextSelector() {
	}

	public Node select(Node root, Node parent) throws Exception {
		NodeList nl = parent.getChildNodes();
		if(nl.getLength() == 0)
			return null;
		parent.normalize();
		if(nl.getLength() != 1)
			return null;

		//-- Only text nodes and PI nodes?
		for(int i = nl.getLength(); --i >= 0;) {
			Node n = nl.item(i);
			if(n.getNodeType() != Node.TEXT_NODE) {
				if(n.getNodeType() == Node.ELEMENT_NODE)
					return null;
			}
		}
		return nl.item(0);
	}

	@Override
	public String toString() {
		return "#text";
	}
}
