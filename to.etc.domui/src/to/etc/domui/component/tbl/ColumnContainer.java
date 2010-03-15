package to.etc.domui.component.tbl;

import to.etc.domui.dom.html.*;

/**
 * Helper class which maintains data for a set of columns (i.e. a row).
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 1, 2008
 */
public class ColumnContainer<T> {
	private TableModelTableBase<T> m_table;

	private RowButtonContainer m_rowButtonContainer;

	private TR m_tr;

	public ColumnContainer(TableModelTableBase<T> table) {
		m_table = table;
	}

	public void setParent(TR p) {
		m_tr = p;
	}

	public TableModelTableBase<T> getTable() {
		return m_table;
	}

	/**
	 * Adds a column to the table.
	 * @param columnContent
	 */
	public TD add(NodeBase columnContent) {
		TD td = new TD();
		m_tr.add(td);
		if(columnContent != null)
			td.add(columnContent);
		return td;
	}

	public TD add(String txt) {
		return add(new TextNode(txt));
	}

	public TR getTR() {
		return m_tr;
	}

	public RowButtonContainer getRowButtonContainer() {
		if(m_rowButtonContainer == null)
			m_rowButtonContainer = new RowButtonContainer();
		return m_rowButtonContainer;
	}
}
