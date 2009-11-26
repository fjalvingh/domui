package to.etc.domui.component.form;

import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * This explicitly makes forms that are represented by some kind of table. It contains
 * basic table manipulation code and stuff.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 13, 2009
 */
abstract public class GenericTableFormBuilder extends GenericFormBuilder {
	protected Table m_parentTable;

	/** The current body we're filling in */
	protected TBody m_tbody;

	protected TR m_lastUsedRow;

	protected TD m_lastUsedCell;

	public GenericTableFormBuilder() {}

	/**
	 * {@inheritDoc}
	 */
	public <T> GenericTableFormBuilder(Class<T> clz, IReadOnlyModel<T> mdl) {
		super(clz, mdl);
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
		m_lastUsedCell = null;
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
		m_lastUsedCell = null;
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
			m_lastUsedCell = null;
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
			m_lastUsedCell = null;
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
		m_lastUsedCell = null;
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
	 * Add a new row.
	 * @return
	 */
	public TR addRow() {
		return m_lastUsedRow = tbody().addRow();
	}

	/**
	 * Add a new cell.
	 * @return
	 */
	public TD addCell() {
		return m_lastUsedCell = tbody().addCell();
	}

	public TD addCell(String css) {
		return m_lastUsedCell = tbody().addCell(css);
	}

	public TD addRowAndCell() {
		addRow();
		return addCell();
	}

	public TD addRowAndCell(String tdcss) {
		addRow();
		return addCell(tdcss);
	}
}
