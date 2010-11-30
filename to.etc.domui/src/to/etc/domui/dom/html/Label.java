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

public class Label extends NodeContainer {
	private NodeBase m_forNode;

	private String m_for;

	public Label() {
		super("label");
	}

	public Label(String text) {
		super("label");
		setText(text);
	}

	public Label(NodeBase fr, String text) {
		super("label");
		setText(text);
		setForNode(fr);
	}

	public Label(String text, String cssClass) {
		this();
		setText(text);
		setCssClass(cssClass);
	}

	public Label(NodeBase fr, String text, String cssClass) {
		super("label");
		setText(text);
		setForNode(fr);
		setCssClass(cssClass);
	}

	@Override
	public void visit(INodeVisitor v) throws Exception {
		v.visitLabel(this);
	}

	public NodeBase getForNode() {
		return m_forNode;
	}

	public void setForNode(NodeBase forNode) {
		m_forNode = forNode;
	}

	public String getFor() {
		if(m_forNode != null)
			return m_forNode.getActualID();
		return m_for;
	}

	public void setFor(String for1) {
		m_for = for1;
	}
}
