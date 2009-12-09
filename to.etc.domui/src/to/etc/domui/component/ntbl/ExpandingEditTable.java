package to.etc.domui.component.ntbl;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.tbl.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
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
public class ExpandingEditTable<T> extends TableModelTableBase<T> implements IHasModifiedIndication {
	private Table m_table = new Table();

	private IRowRenderer<T> m_rowRenderer;

	private IRowEditorFactory<T> m_editorFactory;

	private TBody m_dataBody;

	private boolean m_hideHeader;

	private boolean m_hideIndex;

	private int m_columnCount;

	private boolean m_modifiedByUser;

	private boolean m_disableErrors = true;

	/** By default new rows are edited @ the end; set this to edit @ the start. */
	private boolean m_newAtStart;

	/** When editing a new node, this contains the instance being filled */
	private T m_newInstance;

	/** The TBody which contains the new-editor. */
	private TBody m_newBody;

	private IClicked< ? extends ExpandingEditTable<T>> m_onNew;

	public ExpandingEditTable(@Nonnull Class<T> actualClass, @Nullable IRowRenderer<T> r) {
		super(actualClass);
		m_rowRenderer = r;
		setErrorFence();
	}

	public ExpandingEditTable(@Nonnull Class<T> actualClass, @Nullable ITableModel<T> m, @Nullable IRowRenderer<T> r) {
		super(actualClass, m);
		m_rowRenderer = r;
		setErrorFence();
	}

	/**
	 * Create the structure [(div=self)][ErrorMessageDiv][
	 * @see to.etc.domui.dom.html.NodeBase#createContent()
	 */
	@Override
	public void createContent() throws Exception {
		setCssClass("ui-xdt");
		if(getErrorFence() != null)
			DomApplication.get().getControlBuilder().addErrorFragment(this);

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
			m_columnCount = tr.getChildCount();
			if(!isHideIndex())
				m_columnCount--;
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
	@Nonnull
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
		tr.setUserObject(null);
		renderCollapsedRow(cc, tr, index, value);
	}

	private void renderCollapsedRow(ColumnContainer<T> cc, TR tr, int index, T value) throws Exception {
		cc.setParent(tr);

		if(! isHideIndex()) {
			TD td = cc.add((NodeBase) null);
			createIndexNode(td, index, true);
		}
		m_rowRenderer.renderRow(this, cc, index, value);
		m_columnCount = tr.getChildCount();
		if(!isHideIndex())
			m_columnCount--;
	}

	private void createIndexNode(TD td, final int index, boolean collapsed) {
		Div d = new Div(Integer.toString(index + 1));
		td.add(d);
		d.setCssClass(collapsed ? "ui-xdt-ix ui-xdt-clp" : "ui-xdt-ix ui-xdt-exp");

		td.setClicked(new IClicked<TD>() {
			@Override
			public void clicked(TD clickednode) throws Exception {
				toggleExpanded(index);
			}
		});
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

	/*--------------------------------------------------------------*/
	/*	CODING:	Expanding and collapsing.							*/
	/*--------------------------------------------------------------*/
	/**
	 * Returns T if this row is expanded (seen by looking at the UserObect which contains
	 * the edit node if expanded).
	 * @param ix
	 * @return
	 */
	private boolean isExpanded(int ix) {
		if(ix < 0 || ix >= m_dataBody.getChildCount())
			return false;
		TR tr = (TR) m_dataBody.getChild(ix);
		return tr.getUserObject() != null;
	}

	/**
	 * Create the editor into the specified node, ready for editing the instance.
	 * @param into
	 * @param instance
	 * @throws Exception
	 */
	private void createEditor(NodeContainer into, T instance) throws Exception {
		if(getEditorFactory() == null)
			throw new IllegalStateException("Auto editor creation not yet supported");
		getEditorFactory().createRowEditor(into, instance);
		into.moveModelToControl(); // Ensure items are moved

	}

	/**
	 * If the selected row is collapsed it gets expanded; if it is expanded it's values get
	 * moved to the model and if that worked the row gets collapsed.
	 * @param index
	 */
	protected void toggleExpanded(int index) throws Exception {
		if(index < 0 || index >= m_dataBody.getChildCount()) // Ignore invalid indices
			return;
		TR tr = (TR) m_dataBody.getChild(index);
		if(tr.getUserObject() == null)
			expandRow(index, tr);
		else
			collapseRow(index, tr);
	}

	public void collapseRow(int index) throws Exception {
		if(index < 0 || index >= m_dataBody.getChildCount()) // Ignore invalid indices
			return;
		TR tr = (TR) m_dataBody.getChild(index);
		collapseRow(index, tr);
	}

	public void expandRow(int index) throws Exception {
		if(index < 0 || index >= m_dataBody.getChildCount()) // Ignore invalid indices
			return;
		TR tr = (TR) m_dataBody.getChild(index);
		expandRow(index, tr);
	}

	private int	getColumnCount() {
		return m_columnCount;
	}

	/**
	 * Expand the specified row: destroy the collapsed content, then insert an editor there.
	 * @param index
	 * @param tr
	 */
	private void expandRow(int index, TR tr) throws Exception {
		if(tr.getUserObject() != null) // Already expanded?
			return;

		//-- Remove the current contents of the row, then add back the index cell if needed.
		tr.removeAllChildren();
		if(!isHideIndex()) {
			TD td = tr.addCell();
			createIndexNode(td, index, false);
		}

		//-- Create a single big cell that will contain the editor.
		TD td = tr.addCell();
		td.setCssClass("ui-xdt-edt");
		int colspan = getColumnCount();
		td.setColspan(colspan);
		tr.setUserObject(td);

		//-- Add the editor into that,
		T	item	= getModelItem(index);
		createEditor(td, item);
		td.moveModelToControl();
	}

	/**
	 * Collapse the row by destroying the editor, if possible.
	 * @param index
	 * @param tr
	 */
	private void collapseRow(int index, TR tr) throws Exception {
		if(tr.getUserObject() == null) // Already collapsed?
			return;
		TD	editor = (TD) tr.getUserObject();
		T	item	= getModelItem(index);
		if(DomUtil.isModified(editor)) // On collapse pass on modified state
			setModified(true);
		validateEditor(editor, item); // Move data to model, abort on input error

		//-- Done: just re-render the collapsed row
		renderCollapsedRow(index, item);
	}

	private void validateEditor(TD editor, T item) throws Exception {
		editor.moveControlToModel();
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	New-row editor mode.								*/
	/*--------------------------------------------------------------*/

	/**
	 * Make the control enter ADD NEW mode for the specified instance. The previous
	 * NEW row, if present, is commited if possible; if that fails we exit with an
	 * exception. After that we create a new edit row at the end or the start of the
	 * table and edit it.
	 * This version uses an {@link IModifyableTableModel} to do the actual adding
	 * of the item to the model.
	 * @param instance
	 */
	public void addNew(T instance) throws Exception {
		if(!(getModel() instanceof IModifyableTableModel< ? >))
			throw new IllegalStateException("The model is not an IModifyableTableModel: use addNew(T, IClicked) instead");
		addNew(instance, null);
	}

	/**
	 * Make the control enter ADD NEW mode for the specified instance. The previous
	 * NEW row, if present, is commited if possible; if that fails we exit with an
	 * exception. After that we create a new edit row at the end or the start of the
	 * table and edit it.
	 * @param instance
	 * @param cl		The handler to call after the row should be commited. This handler should add the row
	 * 					to the tablemodel.
	 */
	public void addNew(T instance, IClicked< ? extends ExpandingEditTable<T>> cl) throws Exception {
		clearNewEditor();
		m_onNew = cl;

		//-- Create a new edit body @ the appropriate location.
		m_newBody = new TBody();
		if(m_newAtStart)
			getTable().add(0, m_newBody);
		else
			getTable().add(m_newBody);
		TR tr = m_newBody.addRow();

		//-- Create row superstructure.
		if(!isHideIndex()) {
			TD td = tr.addCell();
			Div d = new Div("*");
			td.add(d);
			d.setCssClass("ui-xdt-ix ui-xdt-new");

			td.setClicked(new IClicked<TD>() {
				@Override
				public void clicked(TD clickednode) throws Exception {
					commitNewRow();
				}
			});
		}

		//-- Create a single big cell that will contain the editor.
		TD td = tr.addCell();
		td.setCssClass("ui-xdt-edt");
		int colspan = getColumnCount();
		td.setColspan(colspan);
		tr.setUserObject(td);

		//-- Add the editor into that,
		createEditor(td, instance);
		td.moveModelToControl();
		m_newInstance = instance;
	}

	protected void commitNewRow() throws Exception {
		clearNewEditor();
	}

	private void clearNewEditor() throws Exception {
		if(m_newBody == null)
			return;

		//-- Try to commit, then add;
		m_newBody.moveControlToModel(); // Move data, exception @ err

		//-- If no new click listener is present try to add it ourselves.
		if(m_onNew != null) {
			((IClicked) m_onNew).clicked(this); // On exception leave input as-is to allow fixing the problem.
		} else {
			IModifyableTableModel<T> mtm = (IModifyableTableModel<T>) getModel();
			mtm.add(m_newInstance);
		}

		//-- Data move succesful. Move to model proper
		m_newBody.remove(); // Discard editor & stuff
		m_newBody = null;
		m_newInstance = null;
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
		//		TR row = (TR) m_dataBody.removeChild(index); // Discard this one;
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
	/*	CODING:	IHasModifiedIndication impl							*/
	/*--------------------------------------------------------------*/
	/**
	 * Returns the modified-by-user flag.
	 * @see to.etc.domui.dom.html.IHasModifiedIndication#isModified()
	 */
	public boolean isModified() {
		return m_modifiedByUser;
	}

	/**
	 * Set or clear the modified by user flag.
	 * @see to.etc.domui.dom.html.IHasModifiedIndication#setModified(boolean)
	 */
	public void setModified(boolean as) {
		m_modifiedByUser = as;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Sillyness.											*/
	/*--------------------------------------------------------------*/
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

	public IRowEditorFactory<T> getEditorFactory() {
		return m_editorFactory;
	}

	public void setEditorFactory(IRowEditorFactory<T> editorFactory) {
		m_editorFactory = editorFactory;
	}

	/**
	 * When set to T this control no longer shows errors.
	 * @param on
	 */
	public void setDisableErrors(boolean on) {
		if(m_disableErrors == on)
			return;
		m_disableErrors = on;
		//		if(on) {
		//			setErrorFence();
		//		} else {
		//			setErrorFence(null);
		//		}
		forceRebuild();
	}

	/**
	 * Returns T if this control's error handling has been disabled, causing the
	 * parent to handle errors instead of showing the errors in the control.
	 * @return
	 */
	public boolean isDisableErrors() {
		return m_disableErrors;
	}

	/**
	 * By default new rows are edited @ the end; set this to edit @ the start.
	 */
	public boolean isNewAtStart() {
		return m_newAtStart;
	}

	public void setNewAtStart(boolean newAtStart) {
		m_newAtStart = newAtStart;
	}


}
