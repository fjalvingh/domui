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

import javax.annotation.*;

public class Table extends NodeContainer {
	private String m_cellPadding;

	private String m_cellSpacing;

	private String m_tableWidth;

	private String m_tableHeight;

	private int m_tableBorder = -1;

	private TableAlignType m_align;

	//	private THead		m_head;
	//	private TBody		m_body;

	public Table() {
		super("table");
	}

	public Table(@Nonnull String cssClass) {
		this();
		setCssClass(cssClass);
	}

	@Override
	public void visit(final INodeVisitor v) throws Exception {
		v.visitTable(this);
	}

	public String getCellPadding() {
		return m_cellPadding;
	}

	public void setCellPadding(final String cellPadding) {
		if(DomUtil.isEqual(cellPadding, m_cellPadding))
			return;
		changed();
		m_cellPadding = cellPadding;
	}

	public String getCellSpacing() {
		return m_cellSpacing;
	}

	public void setCellSpacing(final String cellSpacing) {
		if(DomUtil.isEqual(cellSpacing, m_cellSpacing))
			return;
		changed();
		m_cellSpacing = cellSpacing;
	}

	public String getTableWidth() {
		return m_tableWidth;
	}

	public void setTableWidth(final String tableWidth) {
		if(!DomUtil.isEqual(tableWidth, m_tableWidth))
			changed();
		m_tableWidth = tableWidth;
	}

	public int getTableBorder() {
		return m_tableBorder;
	}

	public void setTableBorder(final int tableBorder) {
		if(tableBorder != m_tableBorder)
			changed();
		m_tableBorder = tableBorder;
	}

	/**
	 * Quicky thingy to set a table header.
	 * @param labels
	 */
	public void setTableHead(final String... labels) {
		THead h = getHead();
		h.forceRebuild();
		TR row = new TR();
		h.add(row);
		for(String s : labels) {
			TH th = new TH();
			row.add(th);
			th.setText(s);
		}
	}

	public TH addHeader(final NodeBase header) {
		THead h = getHead();
		return h.addHeader(header);
	}

	public TH addHeader(String text) {
		THead h = getHead();
		return h.addHeader(text);
	}

	/**
	 * Forbid some often made node errors in table's content model.
	 * @see to.etc.domui.dom.html.NodeContainer#canContain(to.etc.domui.dom.html.NodeBase)
	 */
	@Override
	protected void canContain(@Nonnull NodeBase node) {
		if(node instanceof XmlTextNode)
			return;
		if(node instanceof TextNode)
			throw new IllegalStateException("Dont be silly- cannot add text to a table");
		if(node instanceof TR)
			throw new IllegalStateException("Add TR's to the TBody, not the Table");
		if(!(node instanceof TBody) && !(node instanceof THead))
			throw new IllegalStateException("Dont be silly- should not add a " + node.getTag() + " to a table");
		super.canContain(node);
	}

	public TBody getBody() {
		for(int i = getChildCount(); --i >= 0;) {
			NodeBase n = getChild(i);
			if(n instanceof TBody)
				return (TBody) n;
		}
		TBody b = new TBody();
		super.add(b);
		return b;
	}

	public THead getHead() {
		for(int i = getChildCount(); --i >= 0;) {
			NodeBase n = getChild(i);
			if(n instanceof THead)
				return (THead) n;
		}
		THead b = new THead();
		super.add(b);
		return b;
	}

	public TBody addBody() {
		TBody b = new TBody();
		add(b);
		return b;
	}

	public String getTableHeight() {
		return m_tableHeight;
	}

	public void setTableHeight(final String tableHeight) {
		if(DomUtil.isEqual(tableHeight, m_tableHeight))
			return;
		m_tableHeight = tableHeight;
		changed();
	}

	public TableAlignType getAlign() {
		return m_align;
	}

	public void setAlign(final TableAlignType align) {
		if(m_align == align)
			return;
		m_align = align;
		changed();
	}
}
