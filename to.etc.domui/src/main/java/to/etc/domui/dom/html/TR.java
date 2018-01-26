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

import to.etc.domui.util.IDragHandler;
import to.etc.domui.util.IDraggable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TR extends NodeContainer implements IDraggable {
	private Object m_rowData;

	private IDragHandler m_dragHandler;

	public TR() {
		super("tr");
	}

	public TR(String rowCss) {
		super("tr");
		setCssClass(rowCss);
	}

	@Override
	public void visit(INodeVisitor v) throws Exception {
		v.visitTR(this);
	}

	@Nullable
	public Object getRowData() {
		return m_rowData;
	}

	public void setRowData(@Nullable Object rowData) {
		m_rowData = rowData;
	}

	@Nonnull
	public TD addCell() {
		TD td = new TD();
		add(td);
		return td;
	}

	@Nonnull
	public TD addCell(@Nullable String cssclass) {
		TD td = new TD();
		add(td);
		td.setCssClass(cssclass);
		return td;
	}

	@Nullable
	@Override
	public IDragHandler getDragHandler() {
		return m_dragHandler;
	}

	@Override
	public void setDragHandler(@Nullable IDragHandler dh) {
		m_dragHandler = dh;
	}
}
