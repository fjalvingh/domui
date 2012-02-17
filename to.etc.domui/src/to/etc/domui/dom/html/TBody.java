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

public class TBody extends NodeContainer implements IDropBody {
	//	private IDropHandler			m_dropHandler;
	public TBody() {
		super("tbody");
	}

	@Override
	public void visit(INodeVisitor v) throws Exception {
		v.visitTBody(this);
	}

	/**
	 * Return the table that contains this body.
	 * @return
	 */
	public Table getTable() {
		return getParent(Table.class);
	}

	/**
	 * Utility to return the nth child as a row.
	 * @param ix
	 * @return
	 */
	public TR getRow(int ix) {
		return (TR) getChild(ix);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Utility functions.									*/
	/*--------------------------------------------------------------*/
	//	private int		m_columnCount;
	private TR m_currentRow;

	private TD m_currentCell;

	/**
	 * Add a new row to the table.
	 * @return
	 */
	public TR addRow() {
		m_currentRow = new TR();
		add(m_currentRow);
		return m_currentRow;
	}

	public TD addCell() {
		m_currentCell = new TD();
		if(m_currentRow == null)
			addRow();
		m_currentRow.add(m_currentCell);
		return m_currentCell;
	}

	public TD addCell(String css) {
		addCell();
		m_currentCell.setCssClass(css);
		return m_currentCell;
	}

	public TD addRowAndCell() {
		addRow();
		return addCell();
	}

	public TD cell() {
		return m_currentCell;
	}

	public TR row() {
		return m_currentRow;
	}

	public TD nextRowCell() {
		addRow();
		return addCell();
	}
	//	/**
	//	 * {@inheritDoc}
	//	 * @see to.etc.domui.util.IDropTargetable#getDropHandler()
	//	 */
	//	public IDropHandler getDropHandler() {
	//		return m_dropHandler;
	//	}
	//	/**
	//	 * {@inheritDoc}
	//	 * @see to.etc.domui.util.IDropTargetable#setDropHandler(to.etc.domui.util.IDropHandler)
	//	 */
	//	public void setDropHandler(IDropHandler dropHandler) {
	//		m_dropHandler = dropHandler;
	//	}
}
