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

public class TR extends NodeContainer implements IDraggable {
	private Object m_rowData;

	private IDragHandler m_dragHandler;

	public TR() {
		super("tr");
	}

	@Override
	public void visit(INodeVisitor v) throws Exception {
		v.visitTR(this);
	}

	public Object getRowData() {
		return m_rowData;
	}

	public void setRowData(Object rowData) {
		m_rowData = rowData;
	}

	public TD addCell() {
		TD td = new TD();
		add(td);
		return td;
	}

	public TD addCell(String cssclass) {
		TD td = new TD();
		add(td);
		td.setCssClass(cssclass);
		return td;
	}


	@Override
	public IDragHandler getDragHandler() {
		return m_dragHandler;
	}

	@Override
	public void setDragHandler(IDragHandler dh) {
		m_dragHandler = dh;
	}
}
