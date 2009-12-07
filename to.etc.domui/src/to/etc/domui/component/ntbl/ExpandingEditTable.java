package to.etc.domui.component.ntbl;

import javax.annotation.*;

import to.etc.domui.component.tbl.*;
import to.etc.domui.dom.html.*;

/**
 * This component is a table, using a TableModel, which can also edit it's rows
 * and expand them. It does not support pagination and is used for relatively small
 * datasets.
 * <p>The basic display is that of a normal table, with each row occupying a single
 * table row, controlled by a RowRenderer as with the DataTable. This row usually
 * displays a "summary" of the available data.</p>
 * <p>To edit a row the row is <i>expanded</i> by selecting it's index column; this
 * makes the row enter EDIT mode. In edit mode the entire contents of the row is
 * replaced with an edit form containing all of the data to enter on the row. When
 * editing is complete the row can be collapsed again at which point the data will
 * be moved to the underlying model and the result will be redisplayed in the
 * "summary". If the data is in some way invalid the row cannot collapse until the
 * data is valid.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 7, 2009
 */
public class ExpandingEditTable<T> extends TableModelTableBase<T> {
	private Table m_table = new Table();

	protected IRowRenderer<T> m_rowRenderer;

	//	private TBody m_dataBody;

	public ExpandingEditTable(@Nonnull Class<T> actualClass, @Nullable IRowRenderer<T> r) {
		super(actualClass);
		m_rowRenderer = r;
	}

	public ExpandingEditTable(@Nonnull Class<T> actualClass, @Nullable ITableModel<T> m, @Nullable IRowRenderer<T> r) {
		super(actualClass, m);
		m_rowRenderer = r;
	}

	/**
	 * Return the backing table for this data browser. For component extension only - DO NOT MAKE PUBLIC.
	 * @return
	 */
	@Nonnull
	protected Table getTable() {
		return m_table;
	}

	public void setTableWidth(@Nullable String w) {
		m_table.setTableWidth(w);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	TableModelListener implementation					*/
	/*--------------------------------------------------------------*/
	/**
	 * Called when there are sweeping changes to the model. It forces a complete re-render of the table.
	 */
	public void modelChanged(@Nullable ITableModel<T> model) {
		forceRebuild();
	}

	/**
	 * Row add. Determine if the row is within the paged-in indexes. If not we ignore the
	 * request. If it IS within the paged content we insert the new TR. Since this adds a
	 * new row to the visible set we check if the resulting rowset is not bigger than the
	 * page size; if it is we delete the last node. After all this the renderer will render
	 * the correct result.
	 * When called the actual insert has already taken place in the model.
	 *
	 * @see to.etc.domui.component.tbl.ITableModelListener#rowAdded(to.etc.domui.component.tbl.ITableModel, int, java.lang.Object)
	 */
	public void rowAdded(@Nonnull ITableModel<T> model, int index, @Nullable T value) throws Exception {
		if(!isBuilt())
			return;

		//		//-- Create an insert row && show as collapsed item
		//		ColumnContainer cc = new ColumnContainer(this);
		//		TR tr = new TR();
		//		cc.setParent(tr);
		//		m_rowRenderer.renderRow(this, cc, index, value);
		//		m_dataBody.add(rrow, tr);
		//
		//		//-- What relative row?
		//		int rrow = index - m_six; // This is the location within the child array
		//
		//		//-- Is the size not > the page size?
		//		if(m_pageSize > 0 && m_dataBody.getChildCount() > m_pageSize) {
		//			//-- Delete the last row.
		//			m_dataBody.removeChild(m_dataBody.getChildCount() - 1); // Delete last element
		//		}
	}

	/**
	 * Delete the row specified. If it is not visible we do nothing. If it is visible we
	 * delete the row. This causes one less row to be shown, so we check if we have a pagesize
	 * set; if so we add a new row at the end IF it is available.
	 *
	 * @see to.etc.domui.component.tbl.ITableModelListener#rowDeleted(to.etc.domui.component.tbl.ITableModel, int, java.lang.Object)
	 */
	public void rowDeleted(ITableModel<T> model, int index, T value) throws Exception {
		if(!isBuilt())
			return;
		//		if(index < m_six || index >= m_eix) // Outside visible bounds
		//			return;
		//		int rrow = index - m_six; // This is the location within the child array
		//		m_dataBody.removeChild(rrow); // Discard this one;
		//
		//		//-- One row gone; must we add one at the end?
		//		int peix = m_six + m_pageSize - 1; // Index of last element on "page"
		//		if(m_pageSize > 0 && peix < m_eix) {
		//			ColumnContainer cc = new ColumnContainer(this);
		//			TR tr = new TR();
		//			cc.setParent(tr);
		//			m_rowRenderer.renderRow(this, cc, peix, getModelItem(peix));
		//			m_dataBody.add(m_pageSize - 1, tr);
		//		}
	}

	/**
	 * Merely force a full redraw of the appropriate row.
	 *
	 * @see to.etc.domui.component.tbl.ITableModelListener#rowModified(to.etc.domui.component.tbl.ITableModel, int, java.lang.Object)
	 */
	public void rowModified(ITableModel<T> model, int index, T value) throws Exception {
		if(!isBuilt())
			return;
		//		if(index < m_six || index >= m_eix) // Outside visible bounds
		//			return;
		//		int rrow = index - m_six; // This is the location within the child array
		//		TR tr = (TR) m_dataBody.getChild(rrow); // The visible row there
		//		tr.removeAllChildren(); // Discard current contents.
		//
		//		ColumnContainer cc = new ColumnContainer(this);
		//		m_rowRenderer.renderRow(this, cc, index, value);
	}
}
