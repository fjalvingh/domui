package to.etc.domui.component.tbl;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.util.*;

import javax.annotation.*;
import java.util.*;

/**
 * @deprecated since there is an issue with table with calculation on various browsers.<br>
 *     Redundant horizontal scrollbars may appear randomly. A workaround for the issue is fixing each column width.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 1/3/15.
 */
@Deprecated
final public class ScrollableDataTable<T> extends SelectableTabularComponent<T> implements ISelectionListener<T>, ISelectableTableComponent<T> {
	static private final boolean DEBUG = true;

	private IRowRenderer<T> m_rowRenderer;

	@Nullable
	private Table m_dataTable;

	/** If a result is visible this is the data table */
	private TBody m_dataBody;

	/** When the query has 0 results this is set to the div displaying that message. */
	private Div m_errorDiv;

	/** The items that are currently on-screen, to prevent a reload from the model when reused. */
	final private List<T> m_visibleItemList = new ArrayList<T>();

	/** When set, the table is in "multiselect" mode and shows checkboxes before all rows. */
	private boolean m_multiSelectMode;

	/** When selecting, this is the last index that was used in a select click.. */
	private int m_lastSelectionLocation = -1;

	/** The last index rendered. */
	private int m_nextIndexToLoad;

	private int m_batchSize = 80;

	private boolean m_allRendered;

	@Nonnull
	final private IClicked<TH> m_headerSelectClickHandler = new IClicked<TH>() {
		@Override
		public void clicked(@Nonnull TH clickednode) throws Exception {
			if (isDisabled()){
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
		}
	};

	private boolean m_redrawn;


	public ScrollableDataTable(@Nonnull ITableModel<T> m, @Nonnull IRowRenderer<T> r) {
		super(m);
		m_rowRenderer = r;
	}

	public ScrollableDataTable(@Nonnull IRowRenderer<T> r) {
		m_rowRenderer = r;
	}

	public ScrollableDataTable(@Nonnull ITableModel<T> m) {
		super(m);
	}

	public ScrollableDataTable() {}

	@Override
	public void createContent() throws Exception {
		m_dataTable = null;
		m_dataBody = null;
		m_errorDiv = null;
		m_allRendered = false;
		addCssClass("ui-dt");
		setOverflow(Overflow.AUTO);
		m_nextIndexToLoad = 0;

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

		if(getModel().getRows() == 0) {
			setNoResults();
			return;
		}

		setResults();
		loadMoreData();
		if(isDisableClipboardSelection())
			appendCreateJS(JavascriptUtil.disableSelection(this)); // Needed to prevent ctrl+click in IE doing clipboard-select, because preventDefault does not work there of course.
		if(m_redrawn) {
			appendJavascript("WebUI.scrollableTableReset('" + getActualID() + "','" + tbl().getActualID() + "');");
		} else {
			appendCreateJS("WebUI.initScrollableTable('" + getActualID() + "','" + tbl().getActualID() + "');");
			m_redrawn = true;
		}
	}

	@Nonnull
	private Table tbl() {
		Table t = m_dataTable;
		if(null == t)
			throw new IllegalStateException("Access to table while unbuilt?");
		return t;
	}

	private void loadMoreData() throws Exception {
		if(m_allRendered) {
			System.err.println("domui: ScrollableDataTable got unexpected loadMoreData");
			return;
		}
		int rows = getModel().getRows();
		if(m_nextIndexToLoad >= rows) {
			System.err.println("domui: ScrollableDataTable got unexpected loadMoreData and allrendered is false!?");
			return;
		}

		//-- Get the next batch
		int six = m_nextIndexToLoad;
		int eix = six + m_batchSize;
		if(eix > rows)
			eix = rows;

		List<T> list = getModel().getItems(six, eix);

		//-- Render the rows.
		ColumnContainer<T> cc = new ColumnContainer<T>(this);
		int ix = six;
		for(T o : list) {
			m_visibleItemList.add(o);
			TR tr = new TR();
			m_dataBody.add(tr);
			tr.setTestRepeatID("r" + ix);
			cc.setParent(tr);
			renderRow(tr, cc, ix, o);
			ix++;
		}
		m_nextIndexToLoad = eix;
		if(ix >= getModel().getRows()) {
			renderFinalRow();
		}
		if(DEBUG)
			System.out.println("rendered till "+ m_nextIndexToLoad);
	}

	private void rerender() throws Exception {
		if(! isBuilt() || m_dataBody == null)
			return;
		m_nextIndexToLoad = 0;
		m_dataBody.removeAllChildren();
		m_allRendered = false;
		m_visibleItemList.clear();
		loadMoreData();
		appendJavascript("WebUI.scrollableTableReset('" + getActualID() + "','" + tbl().getActualID() + "');");
	}

	private void renderFinalRow() {
		TBody dataBody = m_dataBody;
		if(null == dataBody)
			throw new IllegalStateException("No data body?");
		if(dataBody.getChildCount() > 0) {
			TR row = dataBody.getRow(dataBody.getChildCount()-1);
			row.setSpecialAttribute("lastRow", "true");
		}

		m_allRendered = true;
	}

	private List<T> getPageItems() throws Exception {
		return getModel().getItems(0, m_nextIndexToLoad);
	}

	@SuppressWarnings("deprecation")
	private void setResults() throws Exception {
		if(m_errorDiv != null) {
			m_errorDiv.remove();
			m_errorDiv = null;
		}
		if(m_dataBody != null)
			return;
		Table dataTable = m_dataTable = new Table();
		add(dataTable);
		dataTable.setCssClass("ui-dt-ovflw-tbl");

		//-- Render the header.
		THead hd = new THead();
		dataTable.add(hd);
		HeaderContainer<T> hc = new HeaderContainer<T>(this, hd, "ui-dt-hdr");

		renderHeader(hc);
		if(!hc.hasContent()) {
			hd.remove();
		}

		m_dataBody = new TBody();
		dataTable.add(m_dataBody);
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
	private void renderHeader(@Nonnull HeaderContainer<T> hc) throws Exception {
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


	/**
	 * Removes any data table, and presents the "no results found" div.
	 */
	private void setNoResults() {
		m_visibleItemList.clear();
		if(m_errorDiv != null)
			return;

		Table dataTable = m_dataTable;
		if(dataTable != null) {
			dataTable.removeAllChildren();
			dataTable.remove();
			m_dataBody = null;
			m_dataTable = null;
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
			cc.getTR().setClicked2(new IClicked2<TR>() {
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
			Checkbox cb = createSelectionCheckbox(value, sm);
			TD td = cc.add(cb);
			if(cb.isReadOnly()) {
				td.addCssClass("ui-cur-default");
			}

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
	private void internalRenderRow(@Nonnull final TR tr, @Nonnull ColumnContainer<T> cc, int index, @Nonnull final T value) throws Exception {
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
		ISelectionModel<T> selectionModel = getSelectionModel();
		if(selectionModel != null) {
			//-- Treat clicks with ctrl or shift as selection clickies
			if(clinfo.isControl() || clinfo.isShift()) {
				handleSelectClicky(instance, clinfo, null);
				return; // Do NOT fire on selection clickies.
			} else {
				if(! selectionModel.isMultiSelect()) {
					handleSelectClicky(instance, clinfo, null);
				}
			}
		}

		//-- If this has a click handler- fire it.
		ICellClicked< ? > rowClicked = m_rowRenderer.getRowClicked();
		if(null != rowClicked)
			((ICellClicked<T>) rowClicked).cellClicked(instance);
	}

	/**
	 * When checkbox itself is clicked, this handles shift stuff.
	 * @param instance
	 * @param checked
	 * @param info
	 * @param clickednode
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
		if(itemindex == -1) 						// Ignore when thingy not found
			return;

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
		THead head = tbl().getHead();
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
			final T instance = m_visibleItemList.get(i);
			TR tr = (TR) m_dataBody.getChild(i);
			TD td = new TD();
			tr.add(0, td);

			Checkbox cb = createSelectionCheckbox(instance, getSelectionModel());
			if(cb.isReadOnly()) {
				td.addCssClass("ui-cur-default");
			}
			td.add(cb);
			cb.setChecked(false);
		}

		fireSelectionUIChanged();
	}

	@Override
	protected void createSelectionUI() throws Exception {
		THead head = tbl().getHead();
		if(null == head)
			throw new IllegalStateException("I've lost my head!?");

		TR headerrow = (TR) head.getChild(0);
		createMultiselectUI(headerrow);
	}

	@Override
	public boolean isMultiSelectionVisible() {
		return m_multiSelectMode;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	ITableModelListener implementation					*/
	/*--------------------------------------------------------------*/
	/**
	 * Called when there are sweeping changes to the model. It forces a complete re-render of the table.
	 */
	@Override
	public void modelChanged(@Nullable ITableModel<T> model) throws Exception {
		rerender();
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
		calcIndices(); 								// Calculate visible nodes
		if(DEBUG)
			System.out.println("dd: add@ "+index+", eix="+ m_nextIndexToLoad);
		if(index < 0 || (index >= m_nextIndexToLoad && m_nextIndexToLoad >= m_batchSize)) { 			// Outside visible bounds & no need to load more
			firePageChanged();
			return;
		}

		//-- What relative row?
		setResults();
		int rrow = index; 							// This is the location within the child array
		ColumnContainer<T> cc = new ColumnContainer<>(this);
		TR tr = new TR();
		m_dataBody.add(rrow, tr);
		cc.setParent(tr);
		tr.setTestRepeatID("r" + index);
		renderRow(tr, cc, index, value);
		m_visibleItemList.add(rrow, value);

		//-- Do we need to increase the nextIndexToLoad?
		if(m_nextIndexToLoad < m_batchSize) {
			if(m_visibleItemList.size() > m_nextIndexToLoad) {
				m_nextIndexToLoad = m_visibleItemList.size();
				if(DEBUG)
					System.out.println("dd: nextIndexToLoad set to "+m_nextIndexToLoad);
			}
		} else if(m_visibleItemList.size() > m_nextIndexToLoad && m_nextIndexToLoad >= m_batchSize) {
			//-- Delete the last row.
			int delindex = m_visibleItemList.size() - 1;
			m_visibleItemList.remove(delindex);
			m_dataBody.removeChild(delindex);
		}

		handleOddEven(rrow);
		firePageChanged();
	}

	/**
	 * Delete the row specified. If it is not visible we do nothing. If it is visible we
	 * delete the row. This causes one less row to be shown, so we check if we have a pagesize
	 * set; if so we add a new row at the end IF it is available.
	 *
	 * @see to.etc.domui.component.tbl.ITableModelListener#rowDeleted(to.etc.domui.component.tbl.ITableModel, int, Object)
	 */
	@Override
	public void rowDeleted(@Nonnull ITableModel<T> model, int index, @Nonnull T value) throws Exception {
		if(!isBuilt())
			return;

		//-- We need the indices of the OLD data, so DO NOT RECALCULATE - the model size has changed.
		if(DEBUG)
			System.out.println("dd: delete index="+index+", eix="+ m_nextIndexToLoad);
		if(index < 0 || index >= m_nextIndexToLoad) { 			// Outside visible bounds
			calcIndices(); 							// Calculate visible nodes
			firePageChanged();
			return;
		}
		int rrow = index; 							// This is the location within the child array
		m_dataBody.removeChild(rrow); 				// Discard this one;
		m_visibleItemList.remove(rrow);
		if(m_dataBody.getChildCount() == 0) {
			calcIndices(); 							// Calculate visible nodes
			setNoResults();
			firePageChanged();
			return;
		}

		//-- One row gone; must we add one at the end?
		if(index < m_nextIndexToLoad && m_nextIndexToLoad <= getModel().getRows()) {
			ColumnContainer<T> cc = new ColumnContainer<T>(this);
			TR tr = new TR();
			cc.setParent(tr);

			T mi = getModelItem(m_nextIndexToLoad -1);	// Because of delete the item to show has become "visible" in the model @ the last index
			if(DEBUG)
				System.out.println("dd: Add item#"+ m_nextIndexToLoad +" @ "+(m_nextIndexToLoad -1));
			m_dataBody.add(m_nextIndexToLoad -1, tr);
			renderRow(tr, cc, m_nextIndexToLoad -1, mi);
			m_visibleItemList.add(m_nextIndexToLoad -1, mi);
		}
		if(m_nextIndexToLoad > getModel().getRows()) {
			m_nextIndexToLoad = getModel().getRows();
			if(DEBUG)
				System.out.println("dd: decrement size of loaded data eix="+ m_nextIndexToLoad);
		}
		while(m_visibleItemList.size() > m_nextIndexToLoad) {
			m_visibleItemList.remove(m_visibleItemList.size() - 1);
		}

		if(DEBUG)
			System.out.println("dd: sizes "+getModel().getRows()+" "+m_dataBody.getChildCount()+", "+m_visibleItemList.size()+", eix="+ m_nextIndexToLoad);
		calcIndices(); 								// Calculate visible nodes
		handleOddEven(rrow);
		firePageChanged();
	}

	private void calcIndices() {}

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
		if(index < 0 || index >= m_nextIndexToLoad) 	// Outside visible bounds
			return;
		int rrow = index; 								// This is the location within the child array
		TR tr = (TR) m_dataBody.getChild(rrow); 		// The visible row there
		tr.removeAllChildren(); 						// Discard current contents.
		m_visibleItemList.set(rrow, value);

		ColumnContainer<T> cc = new ColumnContainer<T>(this);
		cc.setParent(tr);
		renderRow(tr, cc, index, value);
	}

	public void setTableWidth(@Nullable String w) {
		tbl().setTableWidth(w);
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
		for(int i = 0; i < m_visibleItemList.size(); i++) {
			T item = m_visibleItemList.get(i);
			updateSelectionChanged(item, i, sm.isSelected(item));
		}
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

	@Override public void componentHandleWebAction(@Nonnull RequestContextImpl ctx, @Nonnull String action) throws Exception {
		if("LOADMORE".equals(action)) {
			loadMoreData();
			return;
		}

		super.componentHandleWebAction(ctx, action);
	}

	/**
	 * The number of records to load every time new data is needed. Should be enough to provide (a bit more than) an entire page of data.
	 * @return
	 */
	public int getBatchSize() {
		return m_batchSize;
	}

	public void setBatchSize(int batchSize) {
		m_batchSize = batchSize;
	}
}
