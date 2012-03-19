/*
 * DomUI Java User Interface library
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
package to.etc.domui.dom.html;

import to.etc.domui.util.*;

public class TextNode extends NodeBase {
	private String m_text;

	/**
	 * Empty textnode constructor.
	 */
	public TextNode() {
		super("#text");
	}

	/**
	 * Create a TextNode for the given text, using tilde replacement. If the text starts with a tilde it
	 * is assumed to be a key in the page's resource bundle.
	 * @param text
	 */
	public TextNode(String text) {
		super("#text");
		m_text = text;
	}

	@Override
	public void visit(INodeVisitor v) throws Exception {
		v.visitTextNode(this);
	}

	/**
	 * Returns the text as set by setText(), it does not do tilde replacement.
	 * @return
	 */
	public String getText() {
		return m_text;
	}

	public void setText(String text) {
		if(DomUtil.isEqual(text, m_text))
			changed();
		m_text = text;
		if(hasParent()) {
			getParent().childChanged();
			getParent().treeChanging();
			getParent().setMustRenderChildrenFully();
		}
	}
}
