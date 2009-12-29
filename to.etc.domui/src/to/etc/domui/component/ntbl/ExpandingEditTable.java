package to.etc.domui.component.ntbl;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.component.misc.*;
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

	/** When set this factory is used to create the editor; when null this will create the "default" editor. */
	private IRowEditorFactory<T, ? > m_editorFactory;

	private IRowEditorEvent<T, ? > m_onNewComplete;

	private IRowEditorEvent<T, ? > m_onEditComplete;

	private IRowButtonFactory<T> m_rowButtonFactory;

	TBody m_dataBody;

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

	private NodeContainer m_newEditor;

	private NodeContainer m_emptyDiv;

	private boolean m_enableDeleteButton = true;

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

	private boolean setEmptyDiv() throws Exception {
		if(getModel().getRows() == 0) {
			if(m_emptyDiv != null)
				return true; // Table is empty

			//-- Create the "empty table" message.
			m_emptyDiv = new Div();
			m_emptyDiv.setCssClass("ui-xdt-nores");
			m_emptyDiv.setText(NlsContext.getGlobalMessage(Msgs.UI_DATATABLE_EMPTY));
			add(m_emptyDiv);
			return true;
		}

		if(m_emptyDiv != null) {
			m_emptyDiv.remove();
		}
		return false;
	}

	/**
	 * Create the structure [(div=self)][ErrorMessageDiv][table]
	 * @see to.etc.domui.dom.html.NodeBase#createContent()
	 */
	@Override
	public void createContent() throws Exception {
		setCssClass("ui-xdt");
		if(getErrorFence() != null)
			DomApplication.get().getControlBuilder().addErrorFragment(this);

		//-- Ask the renderer for a sort order, if applicable
		m_rowRenderer.beforeQuery(this); // ORDER!! BEFORE CALCINDICES or any other call that materializes the result.

		m_table.removeAllChildren();
		add(m_table);
		m_dataBody = new TBody();
		m_table.add(m_dataBody);

		if(setEmptyDiv())
			return;

		List<T> list = getPageItems(); // Data to show

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

			//-- Add the header for the action buttons
			hc.add((NodeBase) null);
		}

		//-- Render loop: add rows && ask the renderer to add columns.
		ColumnContainer<T> cc = new ColumnContainer<T>(this);
		RowButtonContainer rc = new RowButtonContainer();
		int ix = 0;
		for(T o : list) {
			TR tr = new TR();
			m_dataBody.add(tr);
			renderCollapsedRow(cc, rc, tr, ix, o);
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
		RowButtonContainer bc = new RowButtonContainer();
		renderCollapsedRow(cc, bc, tr, index, value);
	}

	private void renderCollapsedRow(ColumnContainer<T> cc, RowButtonContainer bc, TR tr, int index, final T value) throws Exception {
		cc.setParent(tr);

		if(! isHideIndex()) {
			TD td = cc.add((NodeBase) null);
			createIndexNode(td, index, true);
		}
		m_rowRenderer.renderRow(this, cc, index, value);
		m_columnCount = tr.getChildCount();
		if(!isHideIndex())
			m_columnCount--;

		//-- Ok, now add the (initially empty) action row
		final TD td = cc.add((NodeBase) null);
		bc.setContainer(td);
		if(getRowButtonFactory() != null) {
			getRowButtonFactory().addButtonsFor(bc, value);
		}

		if(isEnableDeleteButton() && getModel() instanceof IModifyableTableModel< ? >) {
			//-- Render a default "delete" button.
			bc.addConfirmedLinkButton(Msgs.BUNDLE.getString(Msgs.UI_XDT_DELETE), "THEME/btnDelete.png", Msgs.BUNDLE.getString(Msgs.UI_XDT_DELSURE), new IClicked<LinkButton>() {
				@Override
				public void clicked(LinkButton clickednode) throws Exception {
					//vmijic 20091225 delete value, why bother with index? jal 20091229 Because I forgot delete() was part of the model ;-)
					((IModifyableTableModel<T>) getModel()).delete(value);
				}
			});
		}
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
	 * @param bc
	 * @param instance
	 * @param isnew
	 * @throws Exception
	 */
	private NodeContainer createEditor(TD into, RowButtonContainer bc, T instance, boolean isnew) throws Exception {
		if(getEditorFactory() == null)
			throw new IllegalStateException("Auto editor creation not yet supported");

		NodeContainer editor = getEditorFactory().createRowEditor(instance, isnew);
		into.add(editor);
		if(editor.getCssClass() == null)
			editor.setCssClass("ui-xdt-edt"); // 20091221 jal Configuration by exception: provide a reasonable style for simple editors
		into.getParent(TR.class).setUserObject(editor);
		editor.moveModelToControl();
		return editor;
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
		int colspan = getColumnCount();
		td.setColspan(colspan);
		TD atd = tr.addCell();
		RowButtonContainer bc = new RowButtonContainer(atd);

		//-- Add the editor into that,
		T	item	= getModelItem(index);
		createEditor(td, bc, item, false);
	}

	/**
	 * Collapse the row by destroying the editor, if possible.
	 * @param index
	 * @param tr
	 */
	private void collapseRow(int index, TR tr) throws Exception {
		if(tr.getUserObject() == null) // Already collapsed?
			return;
		NodeContainer editor = (NodeContainer) tr.getUserObject();
		T	item	= getModelItem(index);
		if(DomUtil.isModified(editor)) // On collapse pass on modified state
			setModified(true);

		editor.moveControlToModel(); // Phase 1 move data to model;
		if(editor instanceof IEditor) {
			IEditor e = (IEditor) editor;
			if(!e.validate(false))
				return;
		}

		if(getOnEditComplete() != null) {
			if(!((IRowEditorEvent) getOnEditComplete()).onRowChanged(this, editor, item))
				return;
		}

		//-- Done: just re-render the collapsed row
		renderCollapsedRow(index, item);
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
		clearNewEditor();

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
		int colspan = getColumnCount();
		td.setColspan(colspan);
		TD atd = tr.addCell();
		RowButtonContainer bc = new RowButtonContainer(atd);

		//-- Add the editor into that,
		m_newEditor = createEditor(td, bc, instance, true);
		m_newInstance = instance;

		//-- Now add commit/cancel button in action column
		bc.addLinkButton("Add", "THEME/btnConfirm.png", new IClicked<LinkButton>() {
			@Override
			public void clicked(LinkButton clickednode) throws Exception {
				commitNewRow();
			}
		});

		bc.addLinkButton("Cancel", "THEME/btnDelete.png", new IClicked<LinkButton>() {
			@Override
			public void clicked(LinkButton clickednode) throws Exception {
				cancelNew();
			}
		});
	}

	public void cancelNew() {
		if(m_newEditor == null)
			return;
		if(DomUtil.isModified(m_newEditor)) {
			MsgBox.continueCancel(this, "Data is modified, are you sure that you want to cancel?", new IClicked<MsgBox>() {
				@Override
				public void clicked(MsgBox clickednode) throws Exception {
					cancelNewReally();
				}
			});
			return;
		}
		cancelNewReally();
	}

	void cancelNewReally() {
		if(m_newEditor == null)
			return;
		m_newEditor = null;
		m_newBody.remove();
		m_newBody = null;
		m_newInstance = null;
	}

	protected void commitNewRow() throws Exception {
		clearNewEditor();
	}

	private void clearNewEditor() throws Exception {
		if(m_newBody == null)
			return;

		//-- Try to commit, then add;
		m_newEditor.moveControlToModel(); // Move data, exception @ err

		if(m_newEditor instanceof IEditor) {
			IEditor e = (IEditor) m_newEditor;
			if(!e.validate(true))
				return;
		}

		if(getOnNewComplete() != null) {
			if(!((IRowEditorEvent) getOnNewComplete()).onRowChanged(this, m_newEditor, m_newInstance))
				return;
		}

		// jal 20091229 See wiki merge reports - commented out until discussed.
		//		//vmijic 20091225 adding to model has to be done here. onRowChanged is just validation method, it does not add to model.
		//		IModifyableTableModel<T> mtm = (IModifyableTableModel<T>) getModel();
		//		mtm.add(m_newInstance);

		//-- Data move succesful. Move to model proper
		m_newBody.remove(); // Discard editor & stuff
		m_newBody = null;
		m_newInstance = null;
		m_newEditor = null;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	TableModelListener implementation					*/
	/*--------------------------------------------------------------*/
	/**
	 * Called when there are sweeping changes to the model. It forces a complete re-render of the table.
	 */
	public void modelChanged(@Nullable ITableModel<T> model) {
		forceRebuild();
		m_newBody = null;
		m_newEditor = null;
		m_newInstance = null;
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
		setEmptyDiv();
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
		TR row = m_dataBody.getRow(index);
		row.remove();
		updateIndexes(index);
		setEmptyDiv();
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

	/**
	 * Return the editor factory to use to create the row editor. If null we'll use a default editor.
	 * @return
	 */
	public IRowEditorFactory<T, ? extends NodeContainer> getEditorFactory() {
		return m_editorFactory;
	}

	public void setEditorFactory(IRowEditorFactory<T, ? extends NodeContainer> editorFactory) {
		m_editorFactory = editorFactory;
	}

	/**
	 * Returns the button factory to use to add buttons to a row, when needed.
	 * @return
	 */
	@Nullable
	public IRowButtonFactory<T> getRowButtonFactory() {
		return m_rowButtonFactory;
	}

	/**
	 * Returns the button factory to use to add buttons to a row, when needed. When set it disables the automatic rendering of the delete button.
	 * @param rowButtonFactory
	 */
	public void setRowButtonFactory(@Nullable IRowButtonFactory<T> rowButtonFactory) {
		if(rowButtonFactory != null)
			setEnableDeleteButton(false);
		m_rowButtonFactory = rowButtonFactory;
	}

	/**
	 * Set a handler to call when editing a <i>new</i> row in an editable table component after
	 * editing is (somehow) marked as complete. When called the editor's contents has been moved
	 * to the model by using the bindings. This method can be used to check the data for validity
	 * or to check for duplicates, for instance by using {@link MetaManager#hasDuplicates(java.util.List, Object, String)}.
	 *
	 * @return
	 */
	@Nullable
	public IRowEditorEvent<T, ? > getOnNewComplete() {
		return m_onNewComplete;
	}

	/**
	 * Set a handler to call when editing a <i>new</i> row in an editable table component after
	 * editing is (somehow) marked as complete. When called the editor's contents has been moved
	 * to the model by using the bindings. This method can be used to check the data for validity
	 * or to check for duplicates, for instance by using {@link MetaManager#hasDuplicates(java.util.List, Object, String)}.
	 *
	 * @param onNewComplete
	 */
	public void setOnNewComplete(@Nullable IRowEditorEvent<T, ? > onNewComplete) {
		m_onNewComplete = onNewComplete;
	}

	/**
	 * Set a handler to call when editing an <i>existing</i> row in an editable table component after
	 * editing is (somehow) marked as complete. When called the editor's contents has been moved to
	 * the model by using the bindings. This method can be used to check the data for validity or to
	 * check for duplicates, for instance by using {@link MetaManager#hasDuplicates(java.util.List, Object, String)}.
	 *
	 * @return
	 */
	@Nullable
	public IRowEditorEvent<T, ? > getOnEditComplete() {
		return m_onEditComplete;
	}

	/**
	 * Set a handler to call when editing an <i>existing</i> row in an editable table component after
	 * editing is (somehow) marked as complete. When called the editor's contents has been moved to
	 * the model by using the bindings. This method can be used to check the data for validity or to
	 * check for duplicates, for instance by using {@link MetaManager#hasDuplicates(java.util.List, Object, String)}.
	 *
	 * @param onEditComplete
	 */
	public void setOnEditComplete(@Nullable IRowEditorEvent<T, ? > onEditComplete) {
		m_onEditComplete = onEditComplete;
	}

	/**
	 * When T (the default) this component will render a 'delete' linkbutton in the button area after the row, for
	 * <i>every</i> row present. Pressing this button will cause the row to be deleted unconditionally.
	 * <p>This is only very basic functionality. To make it better just disable the this and use {@link ExpandingEditTable#setRowButtonFactory(IRowButtonFactory)}
	 * to define your own method to add buttons; allowing for way more complex interactions.</p>
	 * @return
	 */
	public boolean isEnableDeleteButton() {
		return m_enableDeleteButton;
	}

	public void setEnableDeleteButton(boolean enableDeleteButton) {
		m_enableDeleteButton = enableDeleteButton;
	}
}
