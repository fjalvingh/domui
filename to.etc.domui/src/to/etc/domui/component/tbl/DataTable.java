package to.etc.domui.component.tbl;

import java.util.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;

/**
 * POC for a datatable based on the live dom code.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 1, 2008
 */
public class DataTable<T> extends TabularComponentBase<T> {
	private Table m_table = new Table();

	protected IRowRenderer m_rowRenderer;

	/** The size of the page */
	private int m_pageSize;

	private TBody m_dataBody;

	public DataTable(IRowRenderer r) {
		m_rowRenderer = r;
	}

	public DataTable(ITableModel<T> m, IRowRenderer r) {
		super(m);
		m_rowRenderer = r;
	}

	public DataTable(Class<T> actualClass, ITableModel<T> model, IRowRenderer r) {
		super(actualClass, model);
		m_rowRenderer = r;
	}

	public DataTable(Class<T> actualClass, IRowRenderer r) {
		super(actualClass);
		m_rowRenderer = r;
	}

	/**
	 * Return the backing table for this data browser. For component extension only - DO NOT MAKE PUBLIC.
	 * @return
	 */
	protected Table getTable() {
		return m_table;
	}

	/**
	 * UNSTABLE INTERFACE - UNDER CONSIDERATION.
	 * @param dataBody
	 */
	protected void setDataBody(TBody dataBody) {
		m_dataBody = dataBody;
	}

	protected TBody getDataBody() {
		return m_dataBody;
	}

	@Override
	public void createContent() throws Exception {
		setCssClass("ui-dt");

		//-- Ask the renderer for a sort order, if applicable
		m_rowRenderer.beforeQuery(this); // ORDER!! BEFORE CALCINDICES or any other call that materializes the result.

		calcIndices(); // Calculate rows to show.

		List< ? > list = getPageItems(); // Data to show
		if(list.size() == 0) {
			Div error = new Div();
			error.setCssClass("ui-dt-nores");
			error.setText(NlsContext.getGlobalMessage(Msgs.UI_DATATABLE_EMPTY));
			add(error);
			return;
		}
		m_table.removeAllChildren();
		add(m_table);

		//-- Render the header.
		THead hd = new THead();
		m_table.add(hd);
		HeaderContainer hc = new HeaderContainer(this);
		TR tr = new TR();
		tr.setCssClass("ui-dt-hdr");
		hd.add(tr);
		hc.setParent(tr);
		m_rowRenderer.renderHeader(this, hc);

		//-- Render loop: add rows && ask the renderer to add columns.
		m_dataBody = new TBody();
		m_table.add(m_dataBody);
		//		b.setOverflow(Overflow.SCROLL);
		//		b.setHeight("400px");
		//		b.setWidth("100%");

		ColumnContainer<T> cc = new ColumnContainer<T>(this);
		int ix = m_six;
		for(Object o : list) {
			tr = new TR();
			m_dataBody.add(tr);
			cc.setParent(tr);
			m_rowRenderer.renderRow(this, cc, ix, o);
			ix++;
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Dumbass setters and getters.						*/
	/*--------------------------------------------------------------*/
	@Override
	public int getPageSize() {
		return m_pageSize;
	}

	public void setPageSize(int pageSize) {
		if(m_pageSize == pageSize)
			return;
		m_pageSize = pageSize;
		forceRebuild();
		firePageChanged();
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	TableModelListener implementation					*/
	/*--------------------------------------------------------------*/
	/**
	 * Called when there are sweeping changes to the model. It forces a complete re-render of the table.
	 */
	public void modelChanged(ITableModel<T> model) {
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
	public void rowAdded(ITableModel<T> model, int index, T value) throws Exception {
		if(!isBuilt())
			return;
		calcIndices(); // Calculate visible nodes
		if(index < m_six || index >= m_eix) // Outside visible bounds
			return;

		//-- What relative row?
		int rrow = index - m_six; // This is the location within the child array
		ColumnContainer<T> cc = new ColumnContainer<T>(this);
		TR tr = new TR();
		cc.setParent(tr);
		m_rowRenderer.renderRow(this, cc, index, value);
		m_dataBody.add(rrow, tr);

		//-- Is the size not > the page size?
		if(m_pageSize > 0 && m_dataBody.getChildCount() > m_pageSize) {
			//-- Delete the last row.
			m_dataBody.removeChild(m_dataBody.getChildCount() - 1); // Delete last element
		}
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
		if(index < m_six || index >= m_eix) // Outside visible bounds
			return;
		int rrow = index - m_six; // This is the location within the child array
		m_dataBody.removeChild(rrow); // Discard this one;

		//-- One row gone; must we add one at the end?
		int peix = m_six + m_pageSize - 1; // Index of last element on "page"
		if(m_pageSize > 0 && peix < m_eix) {
			ColumnContainer<T> cc = new ColumnContainer<T>(this);
			TR tr = new TR();
			cc.setParent(tr);
			m_rowRenderer.renderRow(this, cc, peix, getModelItem(peix));
			m_dataBody.add(m_pageSize - 1, tr);
		}
	}

	/**
	 * Merely force a full redraw of the appropriate row.
	 *
	 * @see to.etc.domui.component.tbl.ITableModelListener#rowModified(to.etc.domui.component.tbl.ITableModel, int, java.lang.Object)
	 */
	public void rowModified(ITableModel<T> model, int index, T value) throws Exception {
		if(!isBuilt())
			return;
		if(index < m_six || index >= m_eix) // Outside visible bounds
			return;
		int rrow = index - m_six; // This is the location within the child array
		TR tr = (TR) m_dataBody.getChild(rrow); // The visible row there
		tr.removeAllChildren(); // Discard current contents.

		ColumnContainer<T> cc = new ColumnContainer<T>(this);
		m_rowRenderer.renderRow(this, cc, index, value);
	}

	public void setTableWidth(String w) {
		m_table.setTableWidth(w);
	}
}
