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

import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.misc.MiniLogger;
import to.etc.domui.dom.html.Checkbox;
import to.etc.domui.dom.html.ClickInfo;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClickBase;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.IClicked2;
import to.etc.domui.dom.html.Img;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.TBody;
import to.etc.domui.dom.html.TD;
import to.etc.domui.dom.html.TH;
import to.etc.domui.dom.html.THead;
import to.etc.domui.dom.html.TR;
import to.etc.domui.dom.html.Table;
import to.etc.domui.dom.html.TextNode;
import to.etc.domui.server.RequestContextImpl;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.JavascriptUtil;
import to.etc.domui.util.Msgs;
import to.etc.util.DeveloperOptions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * DataTable which allows rendering of multiple rows per data element. Originally created
 * from OldDataTable (in legacy) which it later replaced.
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 7/29/16.
 */
final public class DataTable<T> extends PageableTabularComponentBase<T> implements ISelectionListener<T>, ISelectableTableComponent<T> {
	private MiniLogger m_ml = new MiniLogger(40);

	private Table m_table = new Table();

	private IRowRenderer<T> m_rowRenderer;

	/** The size of the page */
	private int m_pageSize;

	/** If a result is visible this is the data table */
	private TBody m_dataBody;

	/** When the query has 0 results this is set to the div displaying that message. */
	private Div m_errorDiv;

	/** The items that are currently on-screen, to prevent a reload from the model when reused. */
	final private List<TableRowSet<T>> m_visibleItemList = new ArrayList<>();

	/** When set, the table is in "multiselect" mode and shows checkboxes before all rows. */
	private boolean m_multiSelectMode;

	/** When selecting, this is the last index that was used in a select click.. */
	private int m_lastSelectionLocation = -1;

	/** When set this replaces the "no results found" message. */
	@Nullable
	private NodeBase m_emptyMessage;

	/** This will control the display of readonly checkboxes, in case the items are 'not acceptable' but selection is enabled. */
	private boolean m_displayReadonlySelection = true;

	/** When T, the header of the table is always shown, even if the list of results is empty. */
	private boolean m_showHeaderAlways;

	/** When T, rows are not highlighted when table has no selection callbacks on rows. */
	private boolean m_preventRowHighlight;

	@Nonnull
	final private IClicked<TH> m_headerSelectClickHandler = clickednode -> {
		if(isDisabled()) {
			return;
		}
		ISelectionModel<T> sm = getSelectionModel();
		if(null == sm)
			return;
		int ct = sm.getSelectionCount();
		if(0 == ct && sm.isMultiSelect()) {
			sm.selectAll(getModel());
		} else {
			sm.clearSelection();
		}
	};


	public DataTable(@Nonnull ITableModel<T> m, @Nonnull IRowRenderer<T> r) {
		super(m);
		m_rowRenderer = r;
		cInit();
	}

	public DataTable(@Nonnull IRowRenderer<T> r) {
		m_rowRenderer = r;
		cInit();
	}

	public DataTable(@Nonnull ITableModel<T> m) {
		super(m);
		cInit();
	}

	public DataTable() {
		cInit();
	}

	private void cInit() {
		if(DeveloperOptions.getBool("domui.colresizable", true)) {
			m_table.appendCreateJS("WebUI.dataTableResults('" + m_table.getActualID() + "','" + getActualID() + "');");
		}
		setWidth("100%");
	}

	protected void updateBodyClipboardSelection() {
		TBody dataBody = m_dataBody;
		if(null == dataBody)
			return;
		if(isDisableClipboardSelection()) {
			appendJavascript(JavascriptUtil.disableSelection(dataBody));
		}
	}

	@Override
	public void createContent() throws Exception {
		m_dataBody = null;
		m_errorDiv = null;
		addCssClass("ui-dt");
		m_table.setWidth(getWidth());

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

		calcIndices(); 										// Calculate rows to show.

		List<T> list = getPageItems(); 						// Data to show
		if(list.size() == 0) {
			setNoResults();
			return;
		}

		setResults();

		//-- Render the rows.
		ColumnContainer<T> cc = new ColumnContainer<>(this);
		m_visibleItemList.clear();
		int ix = m_six;
		for(T o : list) {
			TableRowSet<T> rowSet = new TableRowSet<>(this, o);
			m_visibleItemList.add(rowSet);
			TR tr = rowSet.getPrimaryRow();
			m_dataBody.add(tr);
			tr.setTestRepeatID("r" + ix);
			cc.setParent(tr);
			renderRow(tr, cc, ix, o);
			ix++;
		}
		ml("createContent rebuilt visibleList after render");
		//if(isDisableClipboardSelection())
		//	appendCreateJS(JavascriptUtil.disableSelection(this)); // Needed to prevent ctrl+click in IE doing clipboard-select, because preventDefault does not work there of course.
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
		//appendJavascript("$('#" + m_table.getActualID() + "').colResizable({postbackSafe: false, onResize: function(tbl) {WebUI.dataTableUpdateWidths(tbl, '" + getActualID() + "');}});");
		//m_table.appendCreateJS("WebUI.dataTableResults('"+ m_table.getActualID() + "','" + getActualID() + "');");

		//-- Render the header.
		THead hd = new THead();
		m_table.add(hd);
		HeaderContainer<T> hc = new HeaderContainer<>(this, hd, "ui-dt-hdr");

		renderHeader(hc);
		if(!hc.hasContent()) {
			hd.remove();
		}

		m_dataBody = new TBody();
		m_table.add(m_dataBody);
		updateBodyClipboardSelection();
	}

	/**
	 * DO NOT OVERRIDE - INTERNAL ONLY - DEPRECATED FOR EXTERNAL USE!!
	 *
	 * Renders the table header. If we're in multi select mode the first column will be
	 * added as a checkbox column. The rest of the columns is delegated to the row
	 * renderer in use.
	 *
	 * @param hc specified header container
	 * @throws Exception
	 */
	@Deprecated
	void renderHeader(@Nonnull HeaderContainer<T> hc) throws Exception {
		//-- Are we rendering a multi-selection?
		if(m_multiSelectMode) {
			TH headerCell = hc.add("");
			headerCell.add(new Img("THEME/dspcb-on.png"));
			headerCell.setTestID("dt_select_all");
			headerCell.setWidth("1%"); //keep selection column with minimal width
			headerCell.setClicked(m_headerSelectClickHandler);
			headerCell.setCssClass("ui-clickable");
		}
		m_rowRenderer.renderHeader(this, hc);
	}

	private void setNoResults() throws Exception {
		if(m_showHeaderAlways)
			setNoResultsWithHeader();
		else
			setNoResultsWithoutHeader();
	}

	/**
	 * Shows an empty data table including header, followed by the no results div.
	 */
	private void setNoResultsWithHeader() throws Exception {
		m_visibleItemList.clear();
		ml("setNoResults visibleList cleared");
		if(m_errorDiv != null)
			return;

		if(m_table != null) {
			m_table.removeAllChildren();
		} else {
			add(m_table = new Table());
		}
		m_dataBody = null;

		//-- Render the header.
		if(!m_table.isAttached())
			add(m_table);
		THead hd = new THead();
		m_table.add(hd);
		HeaderContainer<T> hc = new HeaderContainer<>(this, hd, "ui-dt-hdr");

		renderHeader(hc);
		if(!hc.hasContent()) {
			hd.remove();
		}

		m_dataBody = new TBody();
		m_table.add(m_dataBody);
		updateBodyClipboardSelection();
		renderNoResultsMessage();
	}

	private void renderNoResultsMessage() {
		m_errorDiv = new Div();
		m_errorDiv.setCssClass("ui-dt-nores");

		NodeBase emptyMessage = m_emptyMessage;
		if(null == emptyMessage) {
			m_errorDiv.setText(Msgs.BUNDLE.getString(Msgs.UI_DATATABLE_EMPTY));
		} else {
			m_errorDiv.add(emptyMessage);
		}
		add(m_errorDiv);
	}

	/**
	 * Removes any data table, and presents the "no results found" div.
	 */
	private void setNoResultsWithoutHeader() throws Exception {
		m_visibleItemList.clear();
		ml("setNoResults visibleList cleared");
		if(m_errorDiv != null)
			return;

		if(m_table != null) {
			m_table.removeAllChildren();
			m_table.remove();
			m_dataBody = null;
		}

		renderNoResultsMessage();
	}

	public void setEmptyMessage(@Nullable String message) {
		if(null != message)
			m_emptyMessage = new TextNode(message);
		else
			m_emptyMessage = null;
	}

	public void setEmptyMessage(@Nullable NodeBase node) {
		m_emptyMessage = node;
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
		tr.addCssClass("ui-dt-row");
		//-- Is a rowclick handler needed?
		ISelectionModel<T> sm = getSelectionModel();
		if(m_rowRenderer.getRowClicked() != null || null != sm) {
			//-- Add a click handler to select or pass the rowclicked event.
			cc.getTR().setClicked2(new IClicked2<TR>() {
				@Override
				@SuppressWarnings({"synthetic-access"})
				public void clicked(@Nonnull TR b, @Nonnull ClickInfo clinfo) throws Exception {
					handleRowClick(b, value, clinfo);
				}
			});
			cc.getTR().addCssClass("ui-rowsel");
		} else if(!m_preventRowHighlight) {
			cc.getTR().addCssClass("ui-dt-row-nosel");
		}

		if(sm != null) {
			boolean issel = sm.isSelected(value);
			String clzName = sm.isMultiSelect() ? "mselected" : "selected";
			if(issel)
				tr.addCssClass(clzName);
			else
				tr.removeCssClass(clzName);

			//-- If we're in multiselect mode show the select boxes
			if(m_multiSelectMode) {
				Checkbox cb = createSelectionCheckbox(value, sm);
				TD td = cc.add(cb);
				if(cb.isReadOnly()) {
					td.addCssClass("ui-cur-default");
				} else {
					//it very annoying to target small check box, so we also allow click in cell outside to perform check/uncheck
					hookCheckboxClickToCellToo(td, cb);
				}

				cb.setChecked(issel);
			}
		}
		internalRenderRow(tr, cc, index, value);
	}

	private void hookCheckboxClickToCellToo(TD td, Checkbox cb) {
		td.setClicked2((IClicked2<TD>) (node, clinfo) -> {
			if(!cb.isDisabled()) {
				IClickBase<?> clickHandler = cb.getClicked();
				if(null != clickHandler && clickHandler instanceof IClicked2) {
					cb.setChecked(!cb.isChecked());
					((IClicked2<Checkbox>) clickHandler).clicked(cb, clinfo);
				}
			}
		});
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
	 * @param b
	 * @param instance
	 * @param clinfo
	 * @throws Exception
	 */
	private void handleRowClick(final TR b, final T instance, final ClickInfo clinfo) throws Exception {
		//-- If we have a selection model: check if this is some selecting clicky.
		ISelectionModel<T> selectionModel = getSelectionModel();
		if(selectionModel != null) {
			//-- Treat clicks with ctrl or shift as selection clickies
			if(clinfo.isControl() || clinfo.isShift()) {
				handleSelectClicky(instance, clinfo, null);
				return; // Do NOT fire on selection clickies.
			} else {
				if(!selectionModel.isMultiSelect()) {
					handleSelectClicky(instance, clinfo, null);
				}
			}
		}

		//-- If this has a click handler- fire it.
		ICellClicked<?> rowClicked = m_rowRenderer.getRowClicked();
		if(null != rowClicked)
			((ICellClicked<T>) rowClicked).cellClicked(instance);
	}

	/**
	 * When checkbox itself is clicked, this handles shift stuff.
	 * @param instance
	 * @param checked
	 * @param info
	 * @throws Exception
	 */
	private void selectionCheckboxClicked(T instance, boolean checked, ClickInfo info, @Nonnull Checkbox checkbox) throws Exception {
		handleSelectClicky(instance, info, Boolean.valueOf(checked));
		ISelectionModel<T> sm = getSelectionModel();
		if(null != sm) {
			checkbox.setChecked(sm.isSelected(instance));
		}
	}

	/**
	 * If the specified item is on-screen, this returns the row index inside TBody for that item.
	 * It returns -1 if the thing is not found.
	 * @param item
	 * @return
	 */
	protected int findRowIndex(T item) {
		for(int i = m_visibleItemList.size(); --i >= 0; ) {
			if(item == m_visibleItemList.get(i))
				return i;
		}
		return -1;
	}

	/**
	 * Handle a click that is meant to select/deselect the item(s). It handles ctrl+click as "toggle selection",
	 * and shift+click as "toggle everything between this and the last one".
	 * @param setTo        When null toggle, else set to specific.
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
		int itemindex = getVisibleItemindex(instance);
		if(itemindex == -1)									// Ignore when thingy not found
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
			sl = m_lastSelectionLocation + 1;				// Exclusive
			el = itemindex + 1;
		} else {
			sl = itemindex;
			el = m_lastSelectionLocation;					// Exclusive
		}

		//-- Now toggle all instances, in batches, to prevent loading 1000+ records that cannot be gc'd.
		toggleAllInstances(sm, sl, el);
		m_lastSelectionLocation = -1;
	}

	private void toggleAllInstances(ISelectionModel<T> sm, int sl, int el) throws Exception {
		for(int i = sl; i < el; ) {
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
	}

	private int getVisibleItemindex(@Nonnull T instance) {
		//-- Toggle region. Get the current item's index.
		int itemindex = -1, index = 0;
		for(TableRowSet<T> rowSet : m_visibleItemList) {
			if(MetaManager.areObjectsEqual(rowSet.getInstance(), instance)) {
				itemindex = index;
				break;
			}
			index++;
		}
		return itemindex;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Selection UI update handling.						*/
	/*--------------------------------------------------------------*/
	/**
	 * Updates the "selection" state of the specified local row#.
	 * @param instance
	 * @param tableRowSet
	 * @param on
	 */
	private void updateSelectionChanged(T instance, TableRowSet<T> tableRowSet, boolean on) throws Exception {
		ISelectionModel<T> sm = getSelectionModel();
		if(sm == null)
			throw new IllegalStateException("No selection model!?");
		TR row = tableRowSet.getPrimaryRow();
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
		th.setTestID("dt_select_all");
		th.setWidth("1%");
		headerrow.add(0, th);
		th.setClicked(m_headerSelectClickHandler);
		th.setCssClass("ui-clickable");

		//-- 2. Insert a checkbox in all rows.
		for(int i = 0; i < m_dataBody.getChildCount(); i++) {
			TableRowSet<T> rowSet = m_visibleItemList.get(i);
			TR tr = rowSet.getPrimaryRow();
			TD td = new TD();
			tr.add(0, td);
			td.setRowspan(rowSet.rowCount());

			final Checkbox cb = createSelectionCheckbox(rowSet.getInstance(), getSelectionModel());
			if(cb.isReadOnly()) {
				td.addCssClass("ui-cur-default");
			} else {
				//it very annoying to target small check box, so we also allow click in cell outside to perform check/uncheck
				hookCheckboxClickToCellToo(td, cb);
			}
			td.add(cb);
			cb.setChecked(false);
		}

		fireSelectionUIChanged();
	}

	@Nonnull
	private Checkbox createSelectionCheckbox(@Nonnull final T rowInstance, @Nullable ISelectionModel<T> selectionModel) {
		Checkbox cb = new Checkbox();
		boolean selectable = true;
		if(selectionModel instanceof IAcceptable) {
			selectable = ((IAcceptable<T>) selectionModel).acceptable(rowInstance);
		}
		if(selectable) {
			cb.setClicked2(new IClicked2<Checkbox>() {
				@Override
				public void clicked(@Nonnull Checkbox clickednode, @Nonnull ClickInfo info) throws Exception {
					selectionCheckboxClicked(rowInstance, clickednode.isChecked(), info, clickednode);
				}
			});
		} else {
			cb.setReadOnly(true);
		}
		return cb;
	}

	@Override
	protected void createSelectionUI() throws Exception {
		THead head = m_table.getHead();
		if(null == head)
			throw new IllegalStateException("I've lost my head!?");

		TR headerrow = (TR) head.getChild(0);
		createMultiselectUI(headerrow);
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
		try {
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
			ml("rowAdded before anything: rrow=" + rrow + " index=" + index);
			ColumnContainer<T> cc = new ColumnContainer<T>(this);

			//-- We need a new TableRowSet.
			TableRowSet<T> rowSet = new TableRowSet<T>(this, value);
			DataTableRow<T> tr = rowSet.getPrimaryRow();
			m_visibleItemList.add(rrow, rowSet);

			//-- Locate the insert position for this row and add it to the table before rendering
			int bodyIndex = calculateBodyPosition(rrow);
			m_dataBody.add(bodyIndex, tr);
			cc.setParent(tr);
			tr.setTestRepeatID("r" + index);
			renderRow(tr, cc, index, value);

			ml("rowAdded after adds: rrow=" + rrow + ", index=" + index + ", bodyIndex=" + bodyIndex);

			//-- If we exceed the page size delete the last row.
			if(m_pageSize > 0 && m_visibleItemList.size() > m_pageSize) {
				//-- Delete the last row.
				int lastChildIndex = m_visibleItemList.size() - 1;

				ml("rowAdded removing last item at " + lastChildIndex);
				TableRowSet<T> lastRow = m_visibleItemList.remove(lastChildIndex);
				for(DataTableRow<T> tableRow : lastRow) {
					tableRow.remove();
				}
				ml("rowAdded after pgsz delete visibleSz=" + m_visibleItemList.size());
			}

			if(m_pageSize > 0) {
				while(m_visibleItemList.size() > m_pageSize) {
					int lastChildIndex = m_visibleItemList.size() - 1;
					ml("rowAdded removing last VISIBLE row at " + lastChildIndex);
					m_visibleItemList.remove(lastChildIndex);
				}
				ml("rowAdded after pgsz delete visibleSz=" + m_visibleItemList.size());
			}
			handleOddEven(rrow);
			firePageChanged();
		} catch(Exception x) {
			System.err.println("Last DataTable actions:\n" + m_ml.getData());
			throw x;
		}
	}

	/**
	 * This calculates the starting index position inside the TBody for the data row with
	 * the specified rowIndex. It walks all visibleItems up till the rowIndex, and adds
	 * their rowSize to get the next index.
	 *
	 * @param rowIndex
	 * @return
	 */
	private int calculateBodyPosition(int rowIndex) {
		int position = 0;
		for(int i = 0; i < rowIndex; i++) {
			TableRowSet<T> rowSet = m_visibleItemList.get(i);
			position += rowSet.rowCount();
		}
		return position;
	}

	private void ml(String rest) {
		try {
			TBody dataBody = m_dataBody;
			int sz = dataBody == null ? -1 : dataBody.getChildCount();
			m_ml.add(rest + ": six=" + m_six + ", eix=" + m_eix + ", visibleSz=" + m_visibleItemList.size() + ", bodySz=" + sz);
		} catch(Exception x) {
			m_ml.add("Exception adding to log stack: " + x);
		}
	}

	/**
	 * Delete the row specified. If it is not visible we do nothing. If it is visible we
	 * delete the row. This causes one less row to be shown, so we check if we have a pagesize
	 * set; if so we add a new row at the end IF it is available.
	 *
	 * @see ITableModelListener#rowDeleted(ITableModel, int, Object)
	 */
	@Override
	public void rowDeleted(@Nonnull ITableModel<T> model, int index, @Nonnull T value) throws Exception {
		try {
			if(!isBuilt())
				return;

			//-- We need the indices of the OLD data, so DO NOT RECALCULATE - the model size has changed.
			if(index < m_six || index >= m_eix) {			// Outside visible bounds
				calcIndices();								// Calculate visible nodes
				firePageChanged();
				return;
			}
			int rrow = index - m_six;						// This is the location within the visible items list
			ml("rowDeleted before, index=" + index +", rrow=" + rrow);

			TableRowSet<T> rowSet = m_visibleItemList.remove(rrow);
			for(DataTableRow<T> tableRow : rowSet) {
				tableRow.remove();
			}
			if(m_visibleItemList.size() == 0) {
				calcIndices();								// Calculate visible nodes
				setNoResults();
				firePageChanged();
				return;
			}

			//-- One row gone; must we add one at the end?
			int peix = m_six + m_pageSize - 1;                        // Index of last element on "page"
			if(m_pageSize > 0 && peix < m_eix && peix < getModel().getRows()) {
				ml("rowDelete grow page: peix=" + peix + ", rrow=" + rrow);

				T mi = getModelItem(peix);
				TableRowSet<T> newSet = new TableRowSet<T>(this, mi);
				int lastIndex = m_pageSize - 1;
				m_visibleItemList.add(lastIndex, newSet);

				ColumnContainer<T> cc = new ColumnContainer<>(this);
				DataTableRow<T> tr = newSet.getPrimaryRow();
				cc.setParent(tr);

				int bodyIndex = calculateBodyPosition(lastIndex);
				ml("rowDelete add at body index " + lastIndex);
				m_dataBody.add(lastIndex, tr);
				renderRow(tr, cc, peix, mi);
			}
			calcIndices(); // Calculate visible nodes
			handleOddEven(rrow);
			firePageChanged();
		} catch(IndexOutOfBoundsException x) {
			System.err.println("Last DataTable actions:\n" + m_ml.getData());
			throw new RuntimeException("Bug 7153 rowDelete index error " + x.getMessage() + "\n" + m_ml.getData(), x);
		} catch(Exception x) {
			System.err.println("Last DataTable actions:\n" + m_ml.getData());
			throw x;
		}
	}

	/**
	 * For all VisibleItems, this marks all of their rows as odd/even, starting at the specified index
	 * till the end of the visible range.
	 *
	 * @param index
	 */
	private void handleOddEven(int index) {
		for(int ix = index; ix < m_visibleItemList.size(); ix++) {
			TableRowSet<T> rowSet = m_visibleItemList.get(ix);
			rowSet.markEven((ix & 0x1) == 0);
		}
	}

	/**
	 * Merely force a full redraw of the appropriate row.
	 *
	 * @see ITableModelListener#rowModified(ITableModel, int, Object)
	 */
	@Override
	public void rowModified(@Nonnull ITableModel<T> model, int index, @Nonnull T value) throws Exception {
		if(!isBuilt())
			return;
		if(index < m_six || index >= m_eix)					// Outside visible bounds
			return;
		try {
			int rrow = index - m_six;						// This is the location within the child array

			TableRowSet<T> rowSet = m_visibleItemList.get(rrow);
			for(DataTableRow<T> tableRow : rowSet) {
				tableRow.remove();
			}
			rowSet = new TableRowSet<T>(this, value);
			m_visibleItemList.set(rrow, rowSet);			// Replace with new rowSet

			ml("rowModified: index=" + index + ", rrow=" + rrow);
			TR tr = rowSet.getPrimaryRow();
			int bodyIndex = calculateBodyPosition(rrow);
			m_dataBody.add(bodyIndex, tr);

			ColumnContainer<T> cc = new ColumnContainer<>(this);
			cc.setParent(tr);
			renderRow(tr, cc, index, value);
		} catch(Exception x) {
			System.err.println("Last DataTable actions:\n" + m_ml.getData());
			throw x;
		}
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
		ml("onForceRebuild, visiblesz cleared");
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
	 * @see ISelectionListener#selectionChanged(Object, boolean)
	 */
	@Override
	public void selectionChanged(@Nonnull T row, boolean on) throws Exception {
		//-- Is this a visible row?
		for(TableRowSet<T> tableRowSet : m_visibleItemList) {
			if(MetaManager.areObjectsEqual(row, tableRowSet.getInstance())) {
				updateSelectionChanged(row, tableRowSet, on);
				return;
			}
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Handling selections.								*/
	/*--------------------------------------------------------------*/

	/**
	 * Called when a selection cleared event fires. The underlying model has already been changed. It
	 * tries to see if the row is currently paged in, and if so asks the row renderer to update
	 * it's selection presentation.
	 */
	@Override
	public void selectionAllChanged() throws Exception {
		ISelectionModel<T> sm = getSelectionModel();
		if(sm == null)
			throw new IllegalStateException("Got selection changed event but selection model is empty?");
		//-- Is this a visible row?
		for(TableRowSet<T> tableRowSet : m_visibleItemList) {
			updateSelectionChanged(tableRowSet.getInstance(), tableRowSet, sm.isSelected(tableRowSet.getInstance()));
		}
	}

	public boolean isDisplayReadonlySelection() {
		return m_displayReadonlySelection;
	}

	public void setDisplayReadonlySelection(boolean displayReadonlySelection) {
		if(m_displayReadonlySelection == displayReadonlySelection)
			return;
		m_displayReadonlySelection = displayReadonlySelection;
		forceRebuild();
	}

	/**
	 * When T, the header of the table is always shown, even if the list of results is empty.
	 * @return
	 */
	public boolean isShowHeaderAlways() {
		return m_showHeaderAlways;
	}

	/**
	 * When T, the header of the table is always shown, even if the list of results is empty.
	 * @param showHeaderAlways
	 */
	public void setShowHeaderAlways(boolean showHeaderAlways) {
		m_showHeaderAlways = showHeaderAlways;
	}

	public boolean isPreventRowHighlight() {
		return m_preventRowHighlight;
	}

	/**
	 * When T, rows are not highlighted when table has no selection callbacks on rows.
	 *
	 * @param preventRowHighlight
	 */
	public void setPreventRowHighlight(boolean preventRowHighlight) {
		m_preventRowHighlight = preventRowHighlight;
	}

	/**
	 * UNSTABLE INTERFACE - UNDER CONSIDERATION.
	 * @param dataBody
	 */
	private void setDataBody(@Nonnull TBody dataBody) {
		m_dataBody = dataBody;
		updateBodyClipboardSelection();
	}

	@Nonnull
	private TBody getDataBody() {
		if(null == m_dataBody)
			throw new IllegalStateException("dataBody is still null");
		return m_dataBody;
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

	@Override
	public boolean isMultiSelectionVisible() {
		return m_multiSelectMode;
	}

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

	private void checkVisible(TableRowSet<T> rowSet) {
		if(! m_visibleItemList.contains(rowSet))
			throw new IllegalStateException("The row set is no longer visible");
	}

	public boolean isVisible(TableRowSet<T> rowSet) {
		return m_visibleItemList.contains(rowSet);
	}

	void appendExtraRowAfter(TableRowSet<T> rowSet, DataTableRow<T> newRow, DataTableRow<T> row) {
		checkVisible(rowSet);
		row.appendAfterMe(newRow);
	}

	void appendExtraRowBefore(TableRowSet<T> rowSet, DataTableRow<T> newRow, DataTableRow<T> row) {
		checkVisible(rowSet);
		row.appendBeforeMe(newRow);
	}

	/**
	 * Gets called when column widths have been altered. This retrieves all columns that were changed
	 * and saves the widths so that a next render will reuse the sizes.
	 */
	public void webActionCOLWIDTHS(@Nonnull RequestContextImpl context) throws Exception {
		m_rowRenderer.updateWidths(this, context);
	}
}
