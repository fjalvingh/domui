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
	 * Sets a new table. This resets the current body and stuff.
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

	public Table getTable() {
		return m_parentTable;
	}

	/**
	 * Sets the TBody to use. This resets all layout state.
	 * @param b
	 */
	public void setTBody(final TBody b) {
		finish(); // Make sure old dude is finished
		m_tbody = b;
		m_parentTable = b.getParent(Table.class);
	}

	protected TBody tbody() {
		if(m_tbody == null) {
			if(m_parentTable == null)
				m_parentTable = new Table();
			m_tbody = m_parentTable.getBody(); // Force a new body.
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
		TBody b = new TBody();
		m_parentTable.add(b);
		m_tbody = b;
		internalClearLocation();
		m_lastUsedRow = null;
		m_lastUsedCell = null;
		return b;
	}

	/**
	 * This finishes off the current table by calculating colspans for all skewed rows. This discards the
	 * current table!
	 *
	 * @return
	 */
	@Override
	public Table finish() {
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
