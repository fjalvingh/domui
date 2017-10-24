package to.etc.domui.component.tbl;

import javax.annotation.DefaultNonNull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This maintains the list-of-rows associated with a single data entity. A RowSet
 * always has one "primary" row which is always there. Renderers and clients can
 * add extra rows by calling methods here or on the DataTableRow.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 7/29/16.
 */
@DefaultNonNull
final public class TableRowSet<T> implements Iterable<DataTableRow<T>> {
	final private DataTable<T> m_dataTable;

	final private T m_instance;

	final private DataTableRow<T> m_primaryRow;

	final private List<DataTableRow<T>> m_rowsList = new ArrayList<>();

	private boolean m_even;

	public TableRowSet(DataTable<T> dataTable, T instance) {
		m_dataTable = dataTable;
		m_instance = instance;
		m_primaryRow = new DataTableRow<>(this);
		m_rowsList.add(m_primaryRow);
	}

	@Override
	public Iterator<DataTableRow<T>> iterator() {
		return m_rowsList.iterator();
	}

	public DataTableRow<T> getPrimaryRow() {
		return m_primaryRow;
	}

	public T getInstance() {
		return m_instance;
	}

	public DataTableRow<T> addRowAfter(DataTableRow<T> row) {
		DataTableRow<T> newRow = new DataTableRow<>(this);
		int index = m_rowsList.indexOf(row);
		if(index < 0)
			throw new IllegalStateException("Invalid reference row");
		m_rowsList.add(index+1, newRow);
		m_dataTable.appendExtraRowAfter(this, newRow, row);
		return newRow;
	}

	public DataTableRow<T> addRowBefore(DataTableRow<T> row) {
		DataTableRow<T> newRow = new DataTableRow<>(this);
		int index = m_rowsList.indexOf(row);
		if(index < 0)
			throw new IllegalStateException("Invalid reference row");
		m_rowsList.add(index, newRow);
		m_dataTable.appendExtraRowBefore(this, newRow, row);
		return newRow;
	}


	public int rowCount() {
		return m_rowsList.size();
	}

	public void markEven(boolean even) {
		m_even = even;
		for(DataTableRow<T> tr : m_rowsList) {
			tr.markEven(even);
		}
	}

	public boolean isVisible() {
		return m_dataTable.isVisible(this);
	}
}
