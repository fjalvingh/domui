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

public class IndexedSelector implements PathSelector {
	/** The element name of the repeating thingy */
	private String	m_elem;

	/** The index of the repeat, starting at 0 */
	private int		m_index;

	public IndexedSelector(String elem, int index) {
		m_elem = elem;
		m_index = index;
	}


	public Node select(Node root, Node parent) throws Exception {
		//-- 1. Move to the nearest node
		NodeList nl = parent.getChildNodes();
		int len = nl.getLength();
		if(len < m_index) // Too small already-> exit,
			return null;
		int ct = 0;
		for(int i = 0; i < len; i++) // For all nodes,
		{
			Node n = nl.item(i); // Get item
			if(m_elem.equals(n.getNodeName())) // Is the name we need?
			{
				if(ct == m_index)
					return n; // Return if we're at the appropriate index
				ct++;
			}
		}
		return null; // Not enough occurences.
	}

	@Override
	public String toString() {
		return m_elem + "[" + m_index + "]";
	}
}
