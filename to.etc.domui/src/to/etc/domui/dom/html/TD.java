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

import javax.annotation.*;

import to.etc.domui.dom.*;
import to.etc.domui.util.*;

public class TD extends NodeContainer implements IRenderNBSPIfEmpty {
	private TableVAlign m_valign;

	private int m_colspan = -1;

	private int m_rowspan = -1;

	private boolean m_nowrap;

	private String m_cellHeight;

	private String m_cellWidth;

	private TDAlignType m_align;

	public TD() {
		super("td");
	}

	TD(String tag) {
		super(tag);
	}

	@Override
	public void visit(INodeVisitor v) throws Exception {
		v.visitTD(this);
	}

	@Nonnull
	public Table getTable() {
		return getParent(Table.class);
	}

	public TableVAlign getValign() {
		return m_valign;
	}

	public void setValign(TableVAlign valign) {
		if(m_valign == valign)
			return;
		m_valign = valign;
		changed();
	}

	public int getColspan() {
		return m_colspan;
	}

	public void setColspan(int colspan) {
		if(m_colspan == colspan)
			return;
		m_colspan = colspan;
		changed();
	}

	public int getRowspan() {
		return m_rowspan;
	}

	public void setRowspan(int rowspan) {
		if(m_rowspan == rowspan)
			return;
		m_rowspan = rowspan;
		changed();
	}

	public boolean isNowrap() {
		return m_nowrap;
	}

	public void setNowrap(boolean nowrap) {
		if(m_nowrap == nowrap)
			return;
		m_nowrap = nowrap;
		changed();
	}

	public String getCellHeight() {
		return m_cellHeight;
	}

	public void setCellHeight(String cellHeight) {
		if(DomUtil.isEqual(m_cellHeight, cellHeight))
			return;
		m_cellHeight = cellHeight;
		changed();
	}

	public String getCellWidth() {
		return m_cellWidth;
	}

	public void setCellWidth(String cellWidth) {
		if(DomUtil.isEqual(m_cellWidth, cellWidth))
			return;
		m_cellWidth = cellWidth;
		changed();
	}

	public TDAlignType getAlign() {
		return m_align;
	}

	public void setAlign(TDAlignType align) {
		if(m_align == align)
			return;
		m_align = align;
		changed();
	}
}
