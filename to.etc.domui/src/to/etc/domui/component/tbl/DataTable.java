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
package to.etc.domui.component.tbl;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * POC for a datatable based on the live dom code.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 1, 2008
 */
public class DataTable<T> extends TabularComponentBase<T> implements ISelectionListener<T> {
	private Table m_table = new Table();

	private IRowRenderer<T> m_rowRenderer;

	/** The size of the page */
	private int m_pageSize;

	/** If a result is visible this is the data table */
	private TBody m_dataBody;

	/** When the query has 0 results this is set to the div displaying that message. */
	private Div m_errorDiv;

	/** The items that are currently on-screen, to prevent a reload from the model when reused. */
	final private List<T> m_visibleItemList = new ArrayList<T>();

	/** When set, the table is in "multiselect" mode and shows checkboxes before all rows. */
	private boolean m_multiSelectMode;

	/** When T and the table has a multiselection model the checkboxes indicating selection will be rendered always, even when no selection has been made. */
	private boolean m_showSelectionAlways;

	public DataTable(@Nonnull ITableModel<T> m, @Nonnull IRowRenderer<T> r) {
		super(m);
		m_rowRenderer = r;
	}

	public DataTable(@Nonnull ITableModel<T> m) {
		super(m);
	}

	/**
	 * Return the backing table for this data browser. For component extension only - DO NOT MAKE PUBLIC.
	 * @return
	 */
	@Nullable
	protected Table getTable() {
		return m_table;
	}

	/**
	 * UNSTABLE INTERFACE - UNDER CONSIDERATION.
	 * @param dataBody
	 */
	protected void setDataBody(@Nullable TBody dataBody) {
		m_dataBody = dataBody;
	}

	@Nullable
	protected TBody getDataBody() {
		return m_dataBody;
	}

	@Override
	public void createContent() throws Exception {
		m_dataBody = null;
		m_errorDiv = null;
		setCssClass("ui-dt");

		//-- Do we need to render multiselect checkboxes?
		if(isShowSelectionAlways() || (getSelectionModel() != null && getSelectionModel().getSelectionCount() > 0)) {
			m_multiSelectMode = getSelectionModel().isMultiSelect();
		} else {
			m_multiSelectMode = false;
		}

		//-- Ask the renderer for a sort order, if applicable
		m_rowRenderer.beforeQuery(this); // ORDER!! BEFORE CALCINDICES or any other call that materializes the result.

		calcIndices(); // Calculate rows to show.

		List<T> list = getPageItems(); // Data to show
		if(list.size() == 0) {
			setNoResults();
			return;
		}

		setResults();

		//-- Render the rows.
		ColumnContainer<T> cc = new ColumnContainer<T>(this);
		m_visibleItemList.clear();
		int ix = m_six;
		for(T o : list) {
			m_visibleItemList.add(o);
			TR tr = new TR();
			m_dataBody.add(tr);
			cc.setParent(tr);
			renderRow(tr, cc, ix, o);
			ix++;
		}
		appendCreateJS(JavascriptUtil.disableSelection(this)); // Needed to prevent ctrl+click in IE doing clipboard-select, because preventDefault does not work there of course.
	}

	@SuppressWarnings("deprecation")
	private void setResults() throws Exception {
		if(m_errorDiv != null) {
			m_errorDiv.remove();
			m_errorDiv = null;
		}
		if(m_dataBody != null)
			return;

		m_table.removeAllChildren();
		add(m_table);

		//-- Render the header.
		THead hd = new THead();
		HeaderContainer<T> hc = new HeaderContainer<T>(this);
		TR tr = new TR();
		tr.setCssClass("ui-dt-hdr");
		hd.add(tr);
		hc.setParent(tr);

		renderHeader(hc);
		if(hc.hasContent()) {
			m_table.add(hd);
		} else {
			hc = null;
			hd = null;
			tr = null;
		}

		m_dataBody = new TBody();
		m_table.add(m_dataBody);
	}

	/**
	 * DO NOT OVERRIDE - INTERNAL ONLY - DEPRECATED FOR EXTERNAL USE!!
	 *
	 * Renders the table header. If we're in multiselect mode the first column will be
	 * added as a checkbox column. The rest of the columns is delegated to the row
	 * renderer in use.
	 *
	 * @param hc specified header container
	 * @throws Exception
	 */
	@Deprecated
	void renderHeader(@Nonnull HeaderContainer<T> hc) throws Exception {
		//-- Are we rendering a multi-selection?
		if(m_multiSelectMode)
			hc.add(new Img("THEME/dspcb-on.png"));
		m_rowRenderer.renderHeader(this, hc);
	}

	/**
	 * Removes any data table, and presents the "no results found" div.
	 */
	private void setNoResults() {
		m_visibleItemList.clear();
		if(m_errorDiv != null)
			return;

		if(m_table != null) {
			m_table.removeAllChildren();
			m_table.remove();
			m_dataBody = null;
		}

		m_errorDiv = new Div();
		m_errorDiv.setCssClass("ui-dt-nores");
		m_errorDiv.setText(Msgs.BUNDLE.getString(Msgs.UI_DATATABLE_EMPTY));
		add(m_errorDiv);
		return;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Row rendering & select click handling.				*/
	/*--------------------------------------------------------------*/
	/**
	 * DO NOT OVERRIDE - DEPRECATED FOR EXTERNAL USE!!
	 * Renders row content into specified row.
	 *
	 * @param cc
	 * @param index
	 * @param value
	 * @throws Exception
	 */
	@Deprecated
	void renderRow(@Nonnull final TR tr, @Nonnull ColumnContainer<T> cc, int index, @Nullable final T value) throws Exception {
		//-- Is a rowclick handler needed?
		if(m_rowRenderer.getRowClicked() != null || null != getSelectionModel()) {
			//-- Add a click handler to select or pass the rowclicked event.
			cc.getTR().setClicked(new IClicked2<TR>() {
				@Override
				@SuppressWarnings({"synthetic-access"})
				public void clicked(TR b, ClickInfo clinfo) throws Exception {
					handleRowClick(b, value, clinfo);
				}
			});
			cc.getTR().addCssClass("ui-rowsel");
		}

		//-- If we're in multiselect mode show the select boxes
		if(m_multiSelectMode) {
			Checkbox cb = new Checkbox();
			cc.add(cb);
			cb.setClicked(new IClicked<Checkbox>() {
				@Override
				public void clicked(Checkbox clickednode) throws Exception {
					getSelectionModel().setInstanceSelected(value, clickednode.isChecked());
				}
			});
			cb.setChecked(getSelectionModel().isSelected(value));
		}

		m_rowRenderer.renderRow(this, cc, index, value);
	}

	/**
	 * Click handler for rows. This handles both row clicked handling and row selection handling.
	 *
	 * @param tbl
	 * @param b
	 * @param instance
	 * @param clinfo
	 * @throws Exception
	 */
	private void handleRowClick(final TR b, final T instance, final ClickInfo clinfo) throws Exception {
		//-- If we have a selection model: check if this is some selecting clicky.
		if(getSelectionModel() != null) {
			//-- Treat clicks with ctrl or shift as selection clickies
			if(clinfo.isControl() || clinfo.isShift()) {
				handleSelectClicky(b, instance, clinfo);
				return; // Do NOT fire on selection clickies.
			}
		}

		//-- If this has a click handler- fire it.
		if(null != m_rowRenderer.getRowClicked())
			((ICellClicked<T>) m_rowRenderer.getRowClicked()).cellClicked(b, instance);
	}

	/**
	 * Handle a click that is meant to select/deselect the item(s).
	 * @param tbl
	 * @param b
	 * @param instance
	 * @param clinfo
	 */
	private void handleSelectClicky(TR b, T instance, ClickInfo clinfo) throws Exception {
		getSelectionModel().setInstanceSelected(instance, !getSelectionModel().isSelected(instance));
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Selection UI update handling.						*/
	/*--------------------------------------------------------------*/
	/**
	 * Updates the "selection" state of the specified local row#.
	 * @param instance
	 * @param i
	 * @param on
	 */
	private void updateSelectionChanged(T instance, int lrow, boolean on) throws Exception {
		if(getSelectionModel() == null)
			throw new IllegalStateException("No selection model!?");
		TR row = (TR) m_dataBody.getChild(lrow);
		THead head = m_table.getHead();
		if(null == head)
			throw new IllegalStateException("I've lost my head!?");

		TR headerrow = (TR) head.getChild(0);
		if(!getSelectionModel().isMultiSelect()) {
			//-- Single selection model. Just add/remove the "selected" class from the row.
			if(on)
				row.addCssClass("selected");
			else
				row.removeCssClass("selected");
			return;
		}

		/*
		 * In multiselect. If the multiselect UI is not visible we check if we are switching
		 * ON, else there we can exit. If we switch ON we add the multiselect UI if not
		 * yet present. Then we set/reset the checkbox for the row.
		 */
		if(!m_multiSelectMode) {
			if(!on) // No UI yet, but this is a deselect so let it be
				return;

			//-- Render the multiselect UI: add the header cell and row cells.
			createMultiselectUI(headerrow);
		}

		//-- The checkbox is in cell0; get it and change it's value if (still) needed
		TD td = (TD) row.getChild(0);
		Checkbox cb = (Checkbox) td.getChild(0);
		if(cb.isChecked() != on) // Only change if not already correct
			cb.setChecked(on);
		if(on)
			row.addCssClass("mselected");
		else
			row.removeCssClass("mselected");
	}

	/**
	 * Make the multiselect UI for all visible rows and the header.
	 */
	private void createMultiselectUI(TR headerrow) {
		if(m_multiSelectMode)
			return;
		m_multiSelectMode = true;

		//-- 1. Add the select TH.
		TD th = new TH();
		th.add(new Img("THEME/dspcb-on.png"));
		headerrow.add(0, th);

		//-- 2. Insert a checkbox in all rows.
		for(int i = 0; i < m_dataBody.getChildCount(); i++) {
			final T instance = m_visibleItemList.get(i);
			TR tr = (TR) m_dataBody.getChild(i);
			TD td = new TD();
			tr.add(0, td);

			Checkbox cb = new Checkbox();
			td.add(cb);
			cb.setClicked(new IClicked<Checkbox>() {
				@Override
				public void clicked(Checkbox clickednode) throws Exception {
					getSelectionModel().setInstanceSelected(instance, clickednode.isChecked());
				}
			});
			cb.setChecked(false);
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Dumbass setters and getters.						*/
	/*--------------------------------------------------------------*/
	/**
	 * Return the page size: the #of records to show. If &lt;= 0 all records are shown.
	 */
	@Override
	public int getPageSize() {
		return m_pageSize;
	}

	/**
	 * Set the page size: the #of records to show. If &lt;= 0 all records are shown.
	 *
	 * @param pageSize
	 */
	public void setPageSize(int pageSize) {
		if(m_pageSize == pageSize)
			return;
		m_pageSize = pageSize;
		forceRebuild();
		firePageChanged();
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	ITableModelListener implementation					*/
	/*--------------------------------------------------------------*/
	/**
	 * Called when there are sweeping changes to the model. It forces a complete re-render of the table.
	 */
	@Override
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
	@Override
	public void rowAdded(@Nonnull ITableModel<T> model, int index, @Nullable T value) throws Exception {
		if(!isBuilt())
			return;
		calcIndices(); // Calculate visible nodes
		if(index < m_six || index >= m_eix) // Outside visible bounds
			return;

		//-- What relative row?
		setResults();
		int rrow = index - m_six; // This is the location within the child array
		ColumnContainer<T> cc = new ColumnContainer<T>(this);
		TR tr = new TR();
		cc.setParent(tr);
		renderRow(tr, cc, index, value);
		m_dataBody.add(rrow, tr);
		m_visibleItemList.add(rrow, value);

		//-- Is the size not > the page size?
		if(m_pageSize > 0 && m_dataBody.getChildCount() > m_pageSize) {
			//-- Delete the last row.
			m_dataBody.removeChild(m_dataBody.getChildCount() - 1); // Delete last element
		}
		while(m_visibleItemList.size() > m_pageSize)
			m_visibleItemList.remove(m_visibleItemList.size() - 1);
	}

	/**
	 * Delete the row specified. If it is not visible we do nothing. If it is visible we
	 * delete the row. This causes one less row to be shown, so we check if we have a pagesize
	 * set; if so we add a new row at the end IF it is available.
	 *
	 * @see to.etc.domui.component.tbl.ITableModelListener#rowDeleted(to.etc.domui.component.tbl.ITableModel, int, java.lang.Object)
	 */
	@Override
	public void rowDeleted(@Nonnull ITableModel<T> model, int index, @Nullable T value) throws Exception {
		if(!isBuilt())
			return;
		if(index < m_six || index >= m_eix) // Outside visible bounds
			return;
		int rrow = index - m_six; // This is the location within the child array
		m_dataBody.removeChild(rrow); // Discard this one;
		m_visibleItemList.remove(rrow);
		if(m_dataBody.getChildCount() == 0) {
			setNoResults();
			return;
		}

		//-- One row gone; must we add one at the end?
		int peix = m_six + m_pageSize - 1; // Index of last element on "page"
		if(m_pageSize > 0 && peix < m_eix) {
			ColumnContainer<T> cc = new ColumnContainer<T>(this);
			TR tr = new TR();
			cc.setParent(tr);
			renderRow(tr, cc, peix, getModelItem(peix));
			m_dataBody.add(m_pageSize - 1, tr);
			m_visibleItemList.add(m_pageSize - 1, value);
		}
	}

	/**
	 * Merely force a full redraw of the appropriate row.
	 *
	 * @see to.etc.domui.component.tbl.ITableModelListener#rowModified(to.etc.domui.component.tbl.ITableModel, int, java.lang.Object)
	 */
	@Override
	public void rowModified(@Nonnull ITableModel<T> model, int index, @Nullable T value) throws Exception {
		if(!isBuilt())
			return;
		if(index < m_six || index >= m_eix) // Outside visible bounds
			return;
		int rrow = index - m_six; // This is the location within the child array
		TR tr = (TR) m_dataBody.getChild(rrow); // The visible row there
		tr.removeAllChildren(); // Discard current contents.
		m_visibleItemList.set(rrow, value);

		ColumnContainer<T> cc = new ColumnContainer<T>(this);
		cc.setParent(tr);
		renderRow(tr, cc, index, value);
	}

	public void setTableWidth(@Nullable String w) {
		m_table.setTableWidth(w);
	}

	@Nonnull
	public IRowRenderer<T> getRowRenderer() {
		return m_rowRenderer;
	}

	public void setRowRenderer(@Nonnull IRowRenderer<T> rowRenderer) {
		if(DomUtil.isEqual(m_rowRenderer, rowRenderer))
			return;
		m_rowRenderer = rowRenderer;
		forceRebuild();
	}

	@Override
	protected void onForceRebuild() {
		m_visibleItemList.clear();
		super.onForceRebuild();
	}

	/**
	 * When T and a selection model in multiselect mode is present, this causes the
	 * checkboxes to be rendered initially even when no selection is made.
	 * @return
	 */
	public boolean isShowSelectionAlways() {
		return m_showSelectionAlways;
	}

	/**
	 * When T and a selection model in multiselect mode is present, this causes the
	 * checkboxes to be rendered initially even when no selection is made.
	 * @param showSelectionAlways
	 */
	public void setShowSelectionAlways(boolean showSelectionAlways) {
		m_showSelectionAlways = showSelectionAlways;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	ISelectionListener.									*/
	/*--------------------------------------------------------------*/
	/**
	 * Called when a selection event fires. The underlying model has already been changed. It
	 * tries to see if the row is currently paged in, and if so asks the row renderer to update
	 * it's selection presentation.
	 *
	 * @see to.etc.domui.component.tbl.ISelectionListener#selectionChanged(java.lang.Object, boolean)
	 */
	public void selectionChanged(T row, boolean on) throws Exception {
		//-- Is this a visible row?
		for(int i = 0; i < m_visibleItemList.size(); i++) {
			if(MetaManager.areObjectsEqual(row, m_visibleItemList.get(i))) {
				updateSelectionChanged(row, i, on);
				return;
			}
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Handling selections.								*/
	/*--------------------------------------------------------------*/
	/** If this table allows selection of rows, this model maintains the selections. */
	@Nullable
	private ISelectionModel<T> m_selectionModel;

	/**
	 * Return the model used for table selections, if applicable.
	 * @return
	 */
	@Nullable
	public ISelectionModel<T> getSelectionModel() {
		return m_selectionModel;
	}

	/**
	 * Set the model to maintain selections, if this table allows selections.
	 *
	 * @param selectionModel
	 */
	public void setSelectionModel(@Nullable ISelectionModel<T> selectionModel) {
		if(DomUtil.isEqual(m_selectionModel, selectionModel))
			return;
		if(m_selectionModel != null)
			m_selectionModel.removeListener(this);
		m_selectionModel = selectionModel;
		if(null != selectionModel) {
			selectionModel.addListener(this);
		}
		forceRebuild();
	}
}
