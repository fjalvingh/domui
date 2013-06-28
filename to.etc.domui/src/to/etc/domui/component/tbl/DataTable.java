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
public class DataTable<T> extends TabularComponentBase<T> implements ISelectionListener<T>, ISelectableTableComponent<T> {
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

	/** When selecting, this is the last index that was used in a select click.. */
	private int m_lastSelectionLocation = -1;

	private boolean m_disableClipboardSelection;

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
	@Nonnull
	protected Table getTable() {
		if(null == m_table)
			throw new IllegalStateException("Backing table is still null");
		return m_table;
	}

	/**
	 * UNSTABLE INTERFACE - UNDER CONSIDERATION.
	 * @param dataBody
	 */
	protected void setDataBody(@Nonnull TBody dataBody) {
		m_dataBody = dataBody;
	}

	@Nonnull
	protected TBody getDataBody() {
		if(null == m_dataBody)
			throw new IllegalStateException("dataBody is still null");
		return m_dataBody;
	}

	@Override
	public void createContent() throws Exception {
		m_dataBody = null;
		m_errorDiv = null;
		setCssClass("ui-dt");

		//-- Do we need to render multiselect checkboxes?
		ISelectionModel<T> sm = getSelectionModel();
		if(sm != null) {
			if(isShowSelectionAlways() || sm.getSelectionCount() > 0) {
				m_multiSelectMode = sm.isMultiSelect();
			} else {
				m_multiSelectMode = false;
			}
		}

		//-- Ask the renderer for a sort order, if applicable
		if(m_rowRenderer == null)
			throw new IllegalStateException("There is no row renderer assigned to the table");
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
		if(isDisableClipboardSelection())
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
	@SuppressWarnings("deprecation")
	private void renderRow(@Nonnull final TR tr, @Nonnull ColumnContainer<T> cc, int index, @Nonnull final T value) throws Exception {
		//-- Is a rowclick handler needed?
		ISelectionModel<T> sm = getSelectionModel();
		if(m_rowRenderer.getRowClicked() != null || null != sm) {
			//-- Add a click handler to select or pass the rowclicked event.
			cc.getTR().setClicked(new IClicked2<TR>() {
				@Override
				@SuppressWarnings({"synthetic-access"})
				public void clicked(@Nonnull TR b, @Nonnull ClickInfo clinfo) throws Exception {
					handleRowClick(b, value, clinfo);
				}
			});
			cc.getTR().addCssClass("ui-rowsel");
		}

		//-- If we're in multiselect mode show the select boxes
		if(m_multiSelectMode && sm != null) {
			Checkbox cb = new Checkbox();
			cc.add(cb);
			cb.setClicked(new IClicked2<Checkbox>() {
				@Override
				public void clicked(@Nonnull Checkbox clickednode, @Nonnull ClickInfo info) throws Exception {
					selectionCheckboxClicked(value, clickednode.isChecked(), info);
				}
			});

			boolean issel = sm.isSelected(value);
			cb.setChecked(issel);
			if(issel)
				tr.addCssClass("mselected");
			else
				tr.removeCssClass("mselected");
		}
		internalRenderRow(tr, cc, index, value);
	}

	/**
	 * Must exist for CheckBoxDataTable; remove asap AND DO NOT USE AGAIN - internal interfaces should remain hidden.
	 * @param tr
	 * @param cc
	 * @param index
	 * @param value
	 * @throws Exception
	 */
	@Deprecated
	void internalRenderRow(@Nonnull final TR tr, @Nonnull ColumnContainer<T> cc, int index, @Nonnull final T value) throws Exception {
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
				handleSelectClicky(instance, clinfo, null);
				return; // Do NOT fire on selection clickies.
			}
		}

		//-- If this has a click handler- fire it.
		ICellClicked< ? > rowClicked = m_rowRenderer.getRowClicked();
		if(null != rowClicked)
			((ICellClicked<T>) rowClicked).cellClicked(b, instance);
	}

	/**
	 * When checkbox itself is clicked, this handles shift stuff.
	 * @param instance
	 * @param checked
	 * @param info
	 * @throws Exception
	 */
	private void selectionCheckboxClicked(T instance, boolean checked, ClickInfo info) throws Exception {
		handleSelectClicky(instance, info, Boolean.valueOf(checked));
	}

	/**
	 * Handle a click that is meant to select/deselect the item(s). It handles ctrl+click as "toggle selection",
	 * and shift+click as "toggle everything between this and the last one".
	 *
	 * @param instance
	 * @param clinfo
	 * @param setTo		When null toggle, else set to specific.
	 */
	private void handleSelectClicky(@Nonnull T instance, @Nonnull ClickInfo clinfo, @Nullable Boolean setTo) throws Exception {
		ISelectionModel<T> sm = getSelectionModel();
		if(null == sm)
			throw new IllegalStateException("SelectionModel is null??");
		boolean nvalue = setTo != null ? setTo.booleanValue() : !sm.isSelected(instance);

		if(!clinfo.isShift()) {
			sm.setInstanceSelected(instance, nvalue);
			m_lastSelectionLocation = -1;
			return;
		}

		//-- Toggle region. Get the current item's index.
		int itemindex = -1, index = 0;
		for(T item : m_visibleItemList) {
			if(MetaManager.areObjectsEqual(item, instance)) {
				itemindex = index;
				break;
			}
			index++;
		}
		if(itemindex == -1) // Ignore when thingy not found
			return;
		itemindex += m_six;

		//-- Is a previous location set? If not: just toggle the current and retain the location.
		if(m_lastSelectionLocation == -1) {
			//-- Start of region....
			m_lastSelectionLocation = itemindex;
			sm.setInstanceSelected(instance, !sm.isSelected(instance));
			return;
		}

		//-- We have a previous location- we need to toggle all instances;
		int sl, el;
		if(m_lastSelectionLocation < itemindex) {
			sl = m_lastSelectionLocation + 1; // Exclusive
			el = itemindex + 1;
		} else {
			sl = itemindex;
			el = m_lastSelectionLocation; // Exclusive
		}

		//-- Now toggle all instances, in batches, to prevent loading 1000+ records that cannot be gc'd.
		index = sl;

		for(int i = sl; i < el;) {
			int ex = i + 50;
			if(ex > el)
				ex = el;

			List<T> sub = getModel().getItems(i, ex);
			i += ex;

			for(T item : sub) {
				if(item == null)
					throw new IllegalStateException("null item in list");
				sm.setInstanceSelected(item, !sm.isSelected(item));
			}
		}
		m_lastSelectionLocation = -1;
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
		ISelectionModel<T> sm = getSelectionModel();
		if(sm == null)
			throw new IllegalStateException("No selection model!?");
		TR row = (TR) m_dataBody.getChild(lrow);
		THead head = m_table.getHead();
		if(null == head)
			throw new IllegalStateException("I've lost my head!?");

		TR headerrow = (TR) head.getChild(0);
		if(!sm.isMultiSelect()) {
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
			cb.setClicked(new IClicked2<Checkbox>() {
				@Override
				public void clicked(@Nonnull Checkbox clickednode, @Nonnull ClickInfo clinfo) throws Exception {
					selectionCheckboxClicked(instance, clickednode.isChecked(), clinfo);
				}
			});
			cb.setChecked(false);
		}

		fireSelectionUIChanged();
	}

	/**
	 * When T and a selection model in multiselect mode is present, this causes the
	 * checkboxes to be rendered initially even when no selection is made.
	 * @return
	 */
	public boolean isShowSelectionAlways() {
		return m_showSelectionAlways;
	}

	@Override
	public boolean isMultiSelectionVisible() {
		return m_multiSelectMode;
	}

	/**
	 * When T and a selection model in multiselect mode is present, this causes the
	 * checkboxes to be rendered initially even when no selection is made.
	 * @param showSelectionAlways
	 * @throws Exception
	 */
	@Override
	public void setShowSelection(boolean showSelectionAlways) throws Exception {
		if(m_showSelectionAlways == showSelectionAlways || getModel() == null || getModel().getRows() == 0)
			return;
		m_showSelectionAlways = showSelectionAlways;
		ISelectionModel<T> sm = getSelectionModel();
		if(sm == null)
			throw new IllegalStateException("Selection model is empty?");
		if(!isBuilt() || m_multiSelectMode || getSelectionModel() == null || !sm.isMultiSelect())
			return;

		THead head = m_table.getHead();
		if(null == head)
			throw new IllegalStateException("I've lost my head!?");

		TR headerrow = (TR) head.getChild(0);
		createMultiselectUI(headerrow);
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
		fireModelChanged(null, model);
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
	public void rowAdded(@Nonnull ITableModel<T> model, int index, @Nonnull T value) throws Exception {
		if(!isBuilt())
			return;
		calcIndices(); // Calculate visible nodes
		if(index < m_six || index >= m_eix) { // Outside visible bounds
			firePageChanged();
			return;
		}

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
		if(m_pageSize > 0) {
			while(m_visibleItemList.size() > m_pageSize)
				m_visibleItemList.remove(m_visibleItemList.size() - 1);
		}
		handleOddEven(rrow);
		firePageChanged();
	}

	/**
	 * Delete the row specified. If it is not visible we do nothing. If it is visible we
	 * delete the row. This causes one less row to be shown, so we check if we have a pagesize
	 * set; if so we add a new row at the end IF it is available.
	 *
	 * @see to.etc.domui.component.tbl.ITableModelListener#rowDeleted(to.etc.domui.component.tbl.ITableModel, int, java.lang.Object)
	 */
	@Override
	public void rowDeleted(@Nonnull ITableModel<T> model, int index, @Nonnull T value) throws Exception {
		if(!isBuilt())
			return;

		//-- We need the indices of the OLD data, so DO NOT RECALCULATE - the model size has changed.
		if(index < m_six || index >= m_eix) { // Outside visible bounds
			calcIndices(); // Calculate visible nodes
			firePageChanged();
			return;
		}
		int rrow = index - m_six; // This is the location within the child array
		m_dataBody.removeChild(rrow); // Discard this one;
		m_visibleItemList.remove(rrow);
		if(m_dataBody.getChildCount() == 0) {
			calcIndices(); // Calculate visible nodes
			setNoResults();
			firePageChanged();
			return;
		}

		//-- One row gone; must we add one at the end?
		int peix = m_six + m_pageSize - 1; // Index of last element on "page"
		if(m_pageSize > 0 && peix < m_eix && peix < getModel().getRows()) {
			ColumnContainer<T> cc = new ColumnContainer<T>(this);
			TR tr = new TR();
			cc.setParent(tr);

			T mi = getModelItem(peix);
			renderRow(tr, cc, peix, mi);
			m_dataBody.add(m_pageSize - 1, tr);
			m_visibleItemList.add(m_pageSize - 1, mi);
		}
		calcIndices(); // Calculate visible nodes
		handleOddEven(rrow);
		firePageChanged();
	}

	private void handleOddEven(int index) {
		for(int ix = index; ix < m_dataBody.getChildCount(); ix++) {
			TR tr = (TR) m_dataBody.getChild(ix);
			if((ix & 0x1) == 0) {
				//-- Even
				tr.removeCssClass("ui-odd");
				tr.addCssClass("ui-even");
			} else {
				tr.addCssClass("ui-odd");
				tr.removeCssClass("ui-even");
			}
		}
	}

	/**
	 * Merely force a full redraw of the appropriate row.
	 *
	 * @see to.etc.domui.component.tbl.ITableModelListener#rowModified(to.etc.domui.component.tbl.ITableModel, int, java.lang.Object)
	 */
	@Override
	public void rowModified(@Nonnull ITableModel<T> model, int index, @Nonnull T value) throws Exception {
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
		m_lastSelectionLocation = -1;
		super.onForceRebuild();
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
	@Override
	public void selectionChanged(@Nonnull T row, boolean on) throws Exception {
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

	@Nullable
	private ISelectionAllHandler m_selectionAllHandler;

	@Override
	@Nullable
	public ISelectionAllHandler getSelectionAllHandler() {
		return m_selectionAllHandler;
	}

	public void setSelectionAllHandler(@Nullable ISelectionAllHandler selectionAllHandler) {
		if(m_selectionAllHandler == selectionAllHandler)
			return;
		m_selectionAllHandler = selectionAllHandler;
		fireSelectionUIChanged();
	}

	/**
	 * Return the model used for table selections, if applicable.
	 * @return
	 */
	@Override
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
		ISelectionModel<T> oldsm = m_selectionModel;
		if(DomUtil.isEqual(oldsm, selectionModel))
			return;
		if(oldsm != null) {
			oldsm.removeListener(this);
			setDisableClipboardSelection(true);
		}
		m_selectionModel = selectionModel;
		if(null != selectionModel) {
			selectionModel.addListener(this);
		}
		m_lastSelectionLocation = -1;
		forceRebuild();
	}

	/**
	 * Called when a selection cleared event fires. The underlying model has already been changed. It
	 * tries to see if the row is currently paged in, and if so asks the row renderer to update
	 * it's selection presentation.
	 *
	 * @see to.etc.domui.component.tbl.ISelectionListener#selectionCleared(java.lang.Object, boolean)
	 */
	@Override
	public void selectionAllChanged() throws Exception {
		ISelectionModel<T> sm = getSelectionModel();
		if(sm == null)
			throw new IllegalStateException("Got selection changed event but selection model is empty?");
		//-- Is this a visible row?
		for(int i = 0; i < m_visibleItemList.size(); i++) {
			T item = m_visibleItemList.get(i);
			updateSelectionChanged(item, i, sm.isSelected(item));
		}
	}

	public boolean isDisableClipboardSelection() {
		return m_disableClipboardSelection;
	}

	public void setDisableClipboardSelection(boolean disableClipboardSelection) {
		if(m_disableClipboardSelection == disableClipboardSelection)
			return;
		m_disableClipboardSelection = disableClipboardSelection;
		if(isBuilt() && disableClipboardSelection) {
			appendJavascript(JavascriptUtil.disableSelection(this)); // Needed to prevent ctrl+click in IE doing clipboard-select, because preventDefault does not work there of course.
		}
	}
}
