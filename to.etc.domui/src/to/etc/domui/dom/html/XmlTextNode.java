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

/**
 * This special TextNode is treated by DomUI as a normal TextNode, but with one exception: it's content
 * is not normal text but XML. When rendered this node does not escape anything, so tags in here are
 * rendered as tags that fall "outside" the DomUI DOM tree. This can be used when strict formatting
 * is needed, or when the overhead for a DOM tree for a part is too big. In addition, this will also
 * prevent any indenting from taking place inside this node, so space-perfect rendering can be done,
 * for instance inside pre-like blocks.
 * Disadvantage is that when the content changes it is replaced in it's entirery.
 *
 * <p>This mostly replaces the LiteralXhtml tag because it is more useful: it does not need any
 * enclosing tag but can be used everywhere.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 15, 2010
 */
public class XmlTextNode extends TextNode {
	public XmlTextNode() {}

	public XmlTextNode(String txt) {
		setText(txt);
	}

	@Override
	public void visit(INodeVisitor v) throws Exception {
		v.visitXmlNode(this);
	}
}
