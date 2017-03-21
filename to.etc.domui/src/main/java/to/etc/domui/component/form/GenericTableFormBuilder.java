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
package to.etc.domui.component.form;

import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.TBody;
import to.etc.domui.dom.html.TD;
import to.etc.domui.dom.html.TR;
import to.etc.domui.dom.html.Table;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.IReadOnlyModel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Deprecated: use {@link to.etc.domui.component2.form4.FormBuilder}.
 * This explicitly makes forms that are represented by some kind of table. It contains
 * basic table manipulation code and stuff.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 13, 2009
 */
@Deprecated
abstract public class GenericTableFormBuilder extends GenericFormBuilder {
	private Table m_parentTable;

	/** The current body we're filling in */
	private TBody m_tbody;

	private TR m_lastUsedRow;

	//	@SuppressWarnings("unused")
	//	private TD m_lastUsedCell;

	public GenericTableFormBuilder() {}

	/**
	 * {@inheritDoc}
	 */
	public <T> GenericTableFormBuilder(Class<T> clz, IReadOnlyModel<T> mdl) {
		super(clz, mdl);
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> GenericTableFormBuilder(T instance) {
		super(instance);
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
		m_tbody = null;
		m_parentTable = null;
		m_lastUsedRow = null;
		//		m_lastUsedCell = null;
		internalClearLocation();
	}

	/**
	 * Sets a new table. This resets the current body and stuff. Since the table was not created here the
	 * onBodyAdded local event is not fired.
	 * @param b
	 */
	public void setTable(final Table b) {
		finish(); // Make sure old dude is finished
		m_parentTable = b;
		m_tbody = null;
		m_lastUsedRow = null;
		//		m_lastUsedCell = null;
		internalClearLocation();
	}

	/**
	 * Sets the TBody to use. This resets all layout state. Since the table and the body was not created here the
	 * onBodyAdded local event is not fired.
	 * @param b
	 */
	public void setTBody(final TBody b) {
		finish(); // Make sure old dude is finished
		m_tbody = b;
		m_parentTable = b.getParent(Table.class);
	}

	/**
	 * Called when a new table is added.
	 * @param t
	 */
	protected void onTableAdded(Table t) {}

	protected void onBodyAdded(TBody b) {}

	protected void onRowAdded(TR row) {}

	/**
	 * Return the current table, or null if nothing is current.
	 * @return
	 */
	public Table getTable() {
		return m_parentTable;
	}

	/**
	 * Return the current tbody, or null if nothing is current.
	 * @return
	 */
	public TBody getTBody() {
		return m_tbody;
	}

	/**
	 * Gets the current table, or creates a new one if none is set. If a new one
	 * is created this fires the {@link #onTableAdded(Table)} event.
	 * @return
	 */
	protected Table table() {
		if(m_parentTable == null) {
			m_parentTable = new Table();
			internalClearLocation();
			m_lastUsedRow = null;
			//			m_lastUsedCell = null;
			onTableAdded(m_parentTable);
		}
		return m_parentTable;
	}

	/**
	 * Gets the current tbody, or creates a new one if none is set. If a new one
	 * is created this fires the {@link #onBodyAdded(TBody)} event.
	 * @return
	 */
	protected TBody tbody() {
		if(m_tbody == null) {
			m_tbody = table().getBody();
			m_lastUsedRow = null;
			//			m_lastUsedCell = null;
			internalClearLocation();
			onBodyAdded(m_tbody);
		}
		return m_tbody;
	}

	/**
	 * Creates a new TBody and adds it to the table. This can be used to create multiple re-generatable
	 * layouts within a single layout table. The body inherits the table's core layout.
	 *
	 * @return
	 */
	public TBody newBody() {
		m_tbody = new TBody();
		table().add(m_tbody);
		m_lastUsedRow = null;
		//		m_lastUsedCell = null;
		internalClearLocation();
		onBodyAdded(m_tbody);
		return m_tbody;
	}

	/**
	 * This finishes off the current table by calculating colspans for all skewed rows. This discards the
	 * current table!
	 *
	 * @return
	 */
	@Override
	public NodeContainer finish() {
		if(m_parentTable == null)
			return null;

		//-- jal 20090508 MUST clear the table, because when the builder is used for the NEXT tab it must return a new table!
		Table tbl = m_parentTable;
		DomUtil.adjustTableColspans(tbl);
		reset();
		return tbl;
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
		m_lastUsedRow = tbody().addRow();
		onRowAdded(m_lastUsedRow);
		return m_lastUsedRow;
	}

	/**
	 * Gets the last-used row. If it is unset it gets created and added to the current tbody. This also creates tbody and
	 * table if needed.
	 * @return
	 */
	@Nonnull
	public TR row() {
		if(m_lastUsedRow == null)
			addRow();
		return m_lastUsedRow;
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

	protected void setLastUsedRow(TR row) {
		m_lastUsedRow = row;
	}

	protected void setLastUsedCell(TD cell) {
		//		m_lastUsedCell = cell;
	}
}
