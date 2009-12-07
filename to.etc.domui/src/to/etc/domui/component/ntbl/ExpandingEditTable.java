package to.etc.domui.component.ntbl;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.tbl.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;

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

	private TBody m_dataBody;

	private boolean m_hideHeader;

	private boolean m_hideIndex;

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

	/**
	 *
	 * @see to.etc.domui.dom.html.NodeBase#createContent()
	 */
	@Override
	public void createContent() throws Exception {
		setCssClass("ui-xdt");

		//-- Ask the renderer for a sort order, if applicable
		m_rowRenderer.beforeQuery(this); // ORDER!! BEFORE CALCINDICES or any other call that materializes the result.

		List<T> list = getPageItems(); // Data to show
		if(list.size() == 0) {
			Div error = new Div();
			error.setCssClass("ui-xdt-nores");
			error.setText(NlsContext.getGlobalMessage(Msgs.UI_DATATABLE_EMPTY));
			add(error);
			return;
		}
		m_table.removeAllChildren();
		add(m_table);

		//-- Render the header, if applicable
		if(!isHideHeader()) {
			THead hd = new THead();
			m_table.add(hd);
			HeaderContainer<T> hc = new HeaderContainer<T>(this);
			TR tr = new TR();
			tr.setCssClass("ui-xdt-hdr");
			hd.add(tr);
			hc.setParent(tr);
			if(!isHideIndex()) {
				hc.add((NodeBase) null);
			}

			m_rowRenderer.renderHeader(this, hc);
		}

		//-- Render loop: add rows && ask the renderer to add columns.
		m_dataBody = new TBody();
		m_table.add(m_dataBody);

		ColumnContainer<T> cc = new ColumnContainer<T>(this);
		int ix = 0;
		for(T o : list) {
			TR tr = new TR();
			m_dataBody.add(tr);
			renderCollapsedRow(cc, tr, ix, o);
			ix++;
		}
	}

	/**
	 * Returns all items in the list.
	 * @return
	 * @throws Exception
	 */
	protected List<T> getPageItems() throws Exception {
		return getModel() == null ? Collections.EMPTY_LIST : getModel().getItems(0, getModel().getRows());
	}

	/**
	 * Renders a row with all embellishments.
	 * @param index
	 * @param value
	 * @throws Exception
	 */
	private void renderCollapsedRow(int index, T value) throws Exception {
		ColumnContainer<T> cc = new ColumnContainer<T>(this);
		TR tr = (TR) m_dataBody.getChild(index);
		tr.removeAllChildren(); // Discard current contents.
		renderCollapsedRow(cc, tr, index, value);
	}

	private void renderCollapsedRow(ColumnContainer<T> cc, TR tr, int index, T value) throws Exception {
		cc.setParent(tr);

		if(! isHideIndex()) {
			TD td = cc.add((NodeBase) null);
			createIndexNode(td, index, true);
		}
		m_rowRenderer.renderRow(this, cc, index, value);
	}

	private void createIndexNode(TD td, int index, boolean collapsed) {
		Div d = new Div(Integer.toString(index + 1));
		td.add(d);
		d.setCssClass(collapsed ? "ui-xdt-ix ui-xdt-clp" : "ui-xdt-ix ui-xdt-exp");
	}

	/**
	 * When a row is added or deleted all indexes of existing rows after the changed one must change.
	 * @param start
	 */
	private void updateIndexes(int start) {
		if(isHideIndex())
			return;
		for(int ix = start; ix < m_dataBody.getChildCount(); ix++) {
			TR tr = (TR) m_dataBody.getChild(ix);
			TD td = (TD) tr.getChild(0);
			td.removeAllChildren();
			createIndexNode(td, ix, isExpanded(ix));
		}
	}


	private boolean isExpanded(int ix) {
		// TODO Auto-generated method stub
		return false;
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
	 * A record is added. Add a collapsed row at the required position.
	 *
	 * @see to.etc.domui.component.tbl.ITableModelListener#rowAdded(to.etc.domui.component.tbl.ITableModel, int, java.lang.Object)
	 */
	public void rowAdded(@Nonnull ITableModel<T> model, int index, @Nullable T value) throws Exception {
		if(!isBuilt())
			return;

		//-- Sanity
		if(index < 0 || index > m_dataBody.getChildCount())
			throw new IllegalStateException("Insane index: " + index);

		//-- Create an insert row && show as collapsed item
		TR tr = new TR();
		m_dataBody.add(index, tr);
		renderCollapsedRow(index, value);
		updateIndexes(index + 1);
	}

	/**
	 * Delete the row specified. If it is not visible we do nothing. If it is visible we
	 * delete the row. This causes one less row to be shown, so we check if we have a pagesize
	 * set; if so we add a new row at the end IF it is available.
	 *
	 * @see to.etc.domui.component.tbl.ITableModelListener#rowDeleted(to.etc.domui.component.tbl.ITableModel, int, java.lang.Object)
	 */
	public void rowDeleted(@Nonnull ITableModel<T> model, int index, @Nullable T value) throws Exception {
		if(!isBuilt())
			return;

		//-- Sanity
		if(index < 0 || index >= m_dataBody.getChildCount())
			throw new IllegalStateException("Insane index: " + index);

		//-- Remove, and discard any open edit box
		TR row = (TR) m_dataBody.removeChild(index); // Discard this one;
		updateIndexes(index);

		// TODO close editor

	}

	/**
	 * When a row is modified we redraw the row in collapsed mode; if it was in edit mode before bad luck.
	 *
	 * @see to.etc.domui.component.tbl.ITableModelListener#rowModified(to.etc.domui.component.tbl.ITableModel, int, java.lang.Object)
	 */
	public void rowModified(ITableModel<T> model, int index, T value) throws Exception {
		if(!isBuilt())
			return;
		//-- Sanity
		if(index < 0 || index >= m_dataBody.getChildCount())
			throw new IllegalStateException("Insane index: " + index);

		// TODO Close any open editor
		renderCollapsedRow(index, value);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Sillyness.											*/
	/*--------------------------------------------------------------*/
	/**
	 * When set the table will not render a THead.
	 * @return
	 */
	public boolean isHideHeader() {
		return m_hideHeader;
	}

	public void setHideHeader(boolean hideHeader) {
		if(m_hideHeader == hideHeader)
			return;
		m_hideHeader = hideHeader;
		forceRebuild();
	}

	/**
	 * When set the index number before the row is not shown.
	 * @return
	 */
	public boolean isHideIndex() {
		return m_hideIndex;
	}

	public void setHideIndex(boolean hideIndex) {
		if(m_hideIndex == hideIndex)
			return;
		m_hideIndex = hideIndex;
		forceRebuild();
	}
}
