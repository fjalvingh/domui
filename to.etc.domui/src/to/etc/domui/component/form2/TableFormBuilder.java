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
package to.etc.domui.component.form2;

import javax.annotation.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

abstract public class TableFormBuilder extends AbstractFormBuilder {
	@Nullable
	private Table m_parentTable;

	/** The current body we're filling in */
	@Nullable
	private TBody m_tbody;

	@Nullable
	private TR m_lastUsedRow;

	protected TableFormBuilder(@Nonnull IAppender a) {
		super(a);
	}

	protected TableFormBuilder(@Nonnull NodeContainer target) {
		super(target);
	}

	/**
	 * This finishes off the current table by calculating colspans for all skewed rows. This discards the
	 * current table!
	 *
	 * @return
	 */
	public void finish() {
		if(m_parentTable == null)
			return;
		reset();							// Finish current table, and clear for a new one.
	}

	/**
	 * Called when a new table, body or whatever is made current; it should reset all known positioning information.
	 */
	protected void internalClearLocation() {
	}

	/**
	 * Clears the current generated layout and starts a new table.
	 */
	public void reset() {
		if(m_parentTable != null) {
			DomUtil.adjustTableColspans(m_parentTable);
		}
		m_tbody = null;
		m_parentTable = null;
		m_lastUsedRow = null;
		//		m_lastUsedCell = null;
		internalClearLocation();
	}

	/**
	 * Called when a new table is added.
	 * @param t
	 */
	protected void onTableAdded(@Nonnull Table t) {}

	protected void onBodyAdded(@Nonnull TBody b) {}

	protected void onRowAdded(@Nonnull TR row) {}

	/**
	 * Return the current table, or null if nothing is current.
	 * @return
	 */
	@Nullable
	public Table getTable() {
		return m_parentTable;
	}

	/**
	 * Return the current tbody, or null if nothing is current.
	 * @return
	 */
	@Nullable
	public TBody getTBody() {
		return m_tbody;
	}

	/**
	 * Gets the current table, or creates a new one if none is set. If a new one
	 * is created this fires the {@link #onTableAdded(Table)} event.
	 * @return
	 */
	@Nonnull
	protected Table table() {
		Table pt = m_parentTable;
		if(pt == null) {
			pt = m_parentTable = new Table();
			appendFormNode(pt);
			internalClearLocation();
			m_lastUsedRow = null;
			//			m_lastUsedCell = null;
			onTableAdded(pt);
		}
		return pt;
	}

	/**
	 * Gets the current tbody, or creates a new one if none is set. If a new one
	 * is created this fires the {@link #onBodyAdded(TBody)} event.
	 * @return
	 */
	@Nonnull
	protected TBody tbody() {
		TBody b = m_tbody;
		if(b == null) {
			b = m_tbody = table().getBody();
			m_lastUsedRow = null;
			//			m_lastUsedCell = null;
			internalClearLocation();
			onBodyAdded(b);
		}
		return b;
	}

	/**
	 * Creates a new TBody and adds it to the table. This can be used to create multiple re-generatable
	 * layouts within a single layout table. The body inherits the table's core layout.
	 *
	 * @return
	 */
	@Nonnull
	public TBody newBody() {
		TBody b = m_tbody = new TBody();
		table().add(b);
		m_lastUsedRow = null;
		//		m_lastUsedCell = null;
		internalClearLocation();
		onBodyAdded(b);
		return b;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Simple table manipulation.							*/
	/*--------------------------------------------------------------*/
	/**
	 * Add a new row to the current body; create a body (and a table) if necessary. The row becomes the "last row".
	 * @return
	 */
	@Nonnull
	public TR addRow() {
		TR lr = m_lastUsedRow = tbody().addRow();
		onRowAdded(lr);
		return lr;
	}

	/**
	 * Gets the last-used row. If it is unset it gets created and added to the current tbody. This also creates tbody and
	 * table if needed.
	 * @return
	 */
	@Nonnull
	public TR row() {
		TR r = m_lastUsedRow;
		if(r == null)
			r = addRow();
		return r;
	}

	/**
	 * Get the last-used row. This can return null!!
	 * @return
	 */
	@Nullable
	public TR getLastUsedRow() {
		return m_lastUsedRow;
	}

	/**
	 * This makes the row with the specified index in the current body the "current" row. If it does
	 * not already exist it gets created!
	 * @param ix
	 * @return
	 */
	@Nonnull
	public TR selectRow(int ix) {
		while(tbody().getChildCount() <= ix)
			addRow();
		m_lastUsedRow = (TR) tbody().getChild(ix);
		return m_lastUsedRow;
	}

	/**
	 * Add a new cell to the last-used row.
	 * @return
	 */
	@Nonnull
	public TD addCell() {
		return /* m_lastUsedCell = */row().addCell();
	}

	@Nonnull
	public TD addCell(String css) {
		return /* m_lastUsedCell = */row().addCell(css);
	}

	@Nonnull
	public TD addRowAndCell() {
		addRow();
		return addCell();
	}

	@Nonnull
	public TD addRowAndCell(String tdcss) {
		addRow();
		return addCell(tdcss);
	}

	protected void setLastUsedRow(@Nullable TR row) {
		m_lastUsedRow = row;
	}

	protected void setLastUsedCell(@Nullable TD cell) {
		//		m_lastUsedCell = cell;
	}
}
