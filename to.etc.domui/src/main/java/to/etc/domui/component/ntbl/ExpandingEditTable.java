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
package to.etc.domui.component.ntbl;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.component.tbl.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.themes.*;
import to.etc.domui.util.*;

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
	@Nonnull
	private Table m_table = new Table();

	@Nonnull
	private IRowRenderer<T> m_rowRenderer;

	/** When set this factory is used to create the editor; when null this will create the "default" editor. */
	@Nullable
	private IRowEditorFactory<T, ? > m_editorFactory;

	@Nullable
	private IRowEditorEvent<T, ? > m_onRowChangeCompleted;

	@Nullable
	private IRowButtonFactory<T> m_rowButtonFactory;

	@Nullable
	TBody m_dataBody;

	private boolean m_hideHeader;

	private boolean m_hideIndex;

	private int m_columnCount;

	private boolean m_modifiedByUser;

	private boolean m_disableErrors = true;

	/** By default new rows are edited @ the end; set this to edit @ the start. */
	private boolean m_newAtStart;

	/** When editing a new node, this contains the instance being filled */
	@Nullable
	private T m_newInstance;

	/** The TBody which contains the new-editor. */
	@Nullable
	private TBody m_newBody;

	@Nullable
	private NodeContainer m_newEditor;

	@Nullable
	private NodeContainer m_emptyDiv;

	private boolean m_enableDeleteButton = true;

	/**
	 * By default set to true.
	 * Set to false to disable default componet behavior to handle adding of items when no handler is set to getOnRowChangeCompleted.
	 * In case when set to true, component model must be instanceof IModifyableTableModel<T>.
	 */
	private boolean m_enableAddingItems = true;

	/**
	 * By default set to true.
	 * Set to false to disable default componet behavior to enable items expaning and showing editor, i.e. for readonly data presentation.
	 */
	private boolean m_enableExpandItems = true;

	/**
	 * By default set to true.
	 * Set to false to disable items editing when expanded, i.e. for readonly data presentation but when row expanding is in use (showing some row details in expanded view).
	 */
	private boolean m_enableRowEdit = true;

	public ExpandingEditTable(@Nonnull Class<T> actualClass, @Nonnull ITableModel<T> m, @Nonnull IRowRenderer<T> r) {
		super(m);
		m_rowRenderer = r;
		setErrorFence();
	}

	public ExpandingEditTable(@Nonnull ITableModel<T> m, @Nonnull IRowRenderer<T> r) {
		super(m);
		m_rowRenderer = r;
		setErrorFence();
	}

	private boolean setEmptyDiv() throws Exception {
		if(getModel().getRows() == 0) {
			if(m_emptyDiv != null)
				return true; // Table is empty

			//-- Create the "empty table" message.
			Div d = new Div();
			m_emptyDiv = d;
			d.setCssClass("ui-xdt-nores");
			d.setText(Msgs.BUNDLE.getString(Msgs.UI_DATATABLE_EMPTY));
			add(d);
			return true;
		}

		if(m_emptyDiv != null) {
			m_emptyDiv.remove();
		}
		return false;
	}

	@Nonnull
	private TBody getDataBody() {
		if(null != m_dataBody)
			return m_dataBody;
		throw new IllegalStateException("The data body is empty??");
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
			HeaderContainer<T> hc = new HeaderContainer<T>(this, hd, "ui-xdt-hdr");
			if(!isHideIndex()) {
				hc.add((NodeBase) null);
			}

			m_rowRenderer.renderHeader(this, hc);
			if(hc.hasContent())
				m_table.add(hd);

			m_columnCount = hc.row().getChildCount();
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
			getDataBody().add(tr);
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
	private void renderCollapsedRow(int index, @Nonnull T value) throws Exception {
		ColumnContainer<T> cc = new ColumnContainer<T>(this);
		TR tr = (TR) getDataBody().getChild(index);
		tr.removeAllChildren(); // Discard current contents.
		tr.setUserObject(null);
		RowButtonContainer bc = new RowButtonContainer();
		renderCollapsedRow(cc, bc, tr, index, value);
	}

	private void renderCollapsedRow(@Nonnull ColumnContainer<T> cc, @Nonnull RowButtonContainer bc, @Nonnull TR tr, int index, @Nonnull final T value) throws Exception {
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
		IRowButtonFactory<T> bf = getRowButtonFactory();
		if(bf != null) {
			bf.addButtonsFor(bc, value);
		}

		if(isEnableDeleteButton() && getModel() instanceof IModifyableTableModel< ? >) {
			//-- Render a default "delete" button.
			bc.addConfirmedLinkButton(Msgs.BUNDLE.getString(Msgs.UI_XDT_DELETE), "THEME/btnDelete.png", Msgs.BUNDLE.getString(Msgs.UI_XDT_DELSURE), new IClicked<LinkButton>() {
				@Override
				public void clicked(@Nonnull LinkButton clickednode) throws Exception {
					((IModifyableTableModel<T>) getModel()).delete(value);
					DomUtil.setModifiedFlag(ExpandingEditTable.this);
				}
			});
		}
	}

	private void createIndexNode(@Nonnull TD td, final int index, boolean collapsed) {
		Div d = new MsgDiv(Integer.toString(index + 1));
		td.add(d);
		if(isEnableExpandItems()) {
			d.setCssClass(collapsed ? "ui-xdt-ix ui-xdt-clp" : "ui-xdt-ix ui-xdt-exp");

			td.setClicked(new IClicked<TD>() {
				@Override
				public void clicked(@Nonnull TD clickednode) throws Exception {
					toggleExpanded(index);
				}
			});
		}
	}

	/**
	 * When a row is added or deleted all indexes of existing rows after the changed one must change.
	 * @param start
	 */
	private void updateIndexes(int start) {
		if(isHideIndex())
			return;
		for(int ix = start; ix < getDataBody().getChildCount(); ix++) {
			TR tr = (TR) getDataBody().getChild(ix);
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
		if(ix < 0 || ix >= getDataBody().getChildCount())
			return false;
		TR tr = (TR) getDataBody().getChild(ix);
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
	private NodeContainer createEditor(@Nonnull TD into, @Nonnull RowButtonContainer bc, @Nonnull T instance, boolean isnew) throws Exception {
		if(getEditorFactory() == null)
			throw new IllegalStateException("Auto editor creation not yet supported");

		NodeContainer editor = getEditorFactory().createRowEditor(instance, isnew, !m_enableRowEdit);
		into.add(editor);
		if(editor.getCssClass() == null)
			editor.setCssClass("ui-xdt-edt"); // 20091221 jal Configuration by exception: provide a reasonable style for simple editors
		into.getParent(TR.class).setUserObject(editor);
		//		editor.moveModelToControl();
		return editor;
	}

	/**
	 * If the selected row is collapsed it gets expanded; if it is expanded it's values get
	 * moved to the model and if that worked the row gets collapsed.
	 * @param index
	 */
	protected void toggleExpanded(int index) throws Exception {
		if(index < 0 || index >= getDataBody().getChildCount()) // Ignore invalid indices
			return;
		TR tr = (TR) getDataBody().getChild(index);
		if(tr.getUserObject() == null)
			expandRow(index, tr);
		else
			collapseRow(index, tr);
	}

	public void collapseRow(int index) throws Exception {
		if(index < 0 || index >= getDataBody().getChildCount()) // Ignore invalid indices
			return;
		TR tr = (TR) getDataBody().getChild(index);
		collapseRow(index, tr);
	}

	public void expandRow(int index) throws Exception {
		if(index < 0 || index >= getDataBody().getChildCount()) // Ignore invalid indices
			return;
		TR tr = (TR) getDataBody().getChild(index);
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
	@SuppressWarnings({"unchecked"})
	private void collapseRow(int index, @Nonnull TR tr) throws Exception {
		if(tr.getUserObject() == null) // Already collapsed?
			return;
		NodeContainer editor = (NodeContainer) tr.getUserObject();
		if(null == editor)
			throw new IllegalStateException("? No editor in row user object??");
		T	item	= getModelItem(index);
		//vmijic 20100108 not needed since editor itself would pass modified flag through its input fields.
		//				  in case that user didn't chaged any input, then there is no need to raise modified flag.
		if(DomUtil.isModified(editor)) // On collapse pass on modified state
			DomUtil.setModifiedFlag(ExpandingEditTable.this);

		//		editor.moveControlToModel(); // Phase 1 move data to model;
		if(editor instanceof IEditor) {
			IEditor e = (IEditor) editor;
			if(!e.validate(false))
				return;
		}

		IRowEditorEvent<T, ? > onRowChangeCompleted = getOnRowChangeCompleted();
		if(onRowChangeCompleted != null) {
			if(!((IRowEditorEvent<T, NodeContainer>) onRowChangeCompleted).onRowChanged(this, editor, item, false))
				return;
		}

		//-- Done: just re-render the collapsed row
		if(item != null)
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
	public void addNew(@Nonnull T instance) throws Exception {
		if(!(getModel() instanceof IModifyableTableModel< ? >))
			throw new IllegalStateException("The model is not an IModifyableTableModel: use addNew(T, IClicked) instead");
		clearNewEditor();

		//-- Create a new edit body @ the appropriate location.
		TBody newBody = m_newBody = new TBody();
		if(m_newAtStart)
			getTable().add(0, newBody);
		else
			getTable().add(newBody);
		TR tr = newBody.addRow();

		//-- Create row superstructure.
		if(!isHideIndex()) {
			TD td = tr.addCell();
			Div d = new MsgDiv("*");
			td.add(d);
			d.setCssClass("ui-xdt-ix");

			td.setClicked(new IClicked<TD>() {
				@Override
				public void clicked(@Nonnull TD clickednode) throws Exception {
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

		//-- Now add confirm/cancel button in action column
		bc.addLinkButton(Msgs.BUNDLE.getString(Msgs.UI_XDT_CONFIRM), Theme.BTN_CONFIRM, new IClicked<LinkButton>() {
			@Override
			public void clicked(@Nonnull LinkButton clickednode) throws Exception {
				commitNewRow();
			}
		});

		bc.addLinkButton(Msgs.BUNDLE.getString(Msgs.UI_XDT_CANCEL), Theme.BTN_DELETE, new IClicked<LinkButton>() {
			@Override
			public void clicked(@Nonnull LinkButton clickednode) throws Exception {
				cancelNew();
			}
		});
	}

	public void cancelNew() {
		if(m_newEditor == null)
			return;
		if(DomUtil.isModified(m_newEditor)) {
			MsgBox.continueCancel(this, Msgs.BUNDLE.getString(Msgs.UI_XDT_SURE), new IClicked<MsgBox>() {
				@Override
				public void clicked(@Nonnull MsgBox clickednode) throws Exception {
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
		if(m_newBody != null)
			m_newBody.remove();
		m_newBody = null;
		m_newInstance = null;
	}

	protected void commitNewRow() throws Exception {
		clearNewEditor();
	}

	@SuppressWarnings({"unchecked"})
	private void clearNewEditor() throws Exception {
		TBody newBody = m_newBody;
		NodeContainer newEditor = m_newEditor;
		if(newBody == null || newEditor == null)
			return;

		//-- Try to commit, then add;
		//		newEditor.moveControlToModel(); // Move data, exception @ err
		if(newEditor instanceof IEditor) {
			IEditor e = (IEditor) newEditor;
			if(!e.validate(true))
				return;
		}

		T newInstance = m_newInstance;
		if(null == newInstance)
			throw new IllegalStateException("The 'new' instance being edited is null?");
		IRowEditorEvent<T, ? > onRowChangeCompleted = getOnRowChangeCompleted();
		if(onRowChangeCompleted != null) {
			if(!((IRowEditorEvent<T, NodeContainer>) onRowChangeCompleted).onRowChanged(this, newEditor, newInstance, true)) {
				return;
			}
		}
		if(isEnableAddingItems()) {
			if(!(getModel() instanceof IModifyableTableModel< ? >))
				throw new IllegalStateException("model not of expected type IModifyableTableModel<T> : " + getModel().getClass().getName());
			IModifyableTableModel<T> mtm = (IModifyableTableModel<T>) getModel();
			mtm.add(newInstance);
		}

		//-- Data move succesful. Move to model proper
		newBody.remove(); // Discard editor & stuff
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
	@Override
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
	@Override
	public void rowAdded(@Nonnull ITableModel<T> model, int index, @Nonnull T value) throws Exception {
		if(!isBuilt())
			return;

		//-- Sanity
		if(index < 0 || index > getDataBody().getChildCount())
			throw new IllegalStateException("Insane index: " + index);

		//-- Create an insert row && show as collapsed item
		TR tr = new TR();
		getDataBody().add(index, tr);
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
	@Override
	public void rowDeleted(@Nonnull ITableModel<T> model, int index, @Nullable T value) throws Exception {
		if(!isBuilt())
			return;

		//-- Sanity
		if(index < 0 || index >= getDataBody().getChildCount())
			throw new IllegalStateException("Insane index: " + index);

		//-- Remove, and discard any open edit box
		TR row = getDataBody().getRow(index);
		row.remove();
		updateIndexes(index);
		setEmptyDiv();
	}

	/**
	 * When a row is modified we redraw the row in collapsed mode; if it was in edit mode before bad luck.
	 *
	 * @see to.etc.domui.component.tbl.ITableModelListener#rowModified(to.etc.domui.component.tbl.ITableModel, int, java.lang.Object)
	 */
	@Override
	public void rowModified(@Nonnull ITableModel<T> model, int index, @Nonnull T value) throws Exception {
		if(!isBuilt())
			return;
		//-- Sanity
		if(index < 0 || index >= getDataBody().getChildCount())
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
	@Override
	public boolean isModified() {
		return m_modifiedByUser;
	}

	/**
	 * Set or clear the modified by user flag.
	 * @see to.etc.domui.dom.html.IHasModifiedIndication#setModified(boolean)
	 */
	@Override
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
	@Nonnull
	public IRowEditorFactory<T, ? extends NodeContainer> getEditorFactory() {
		if(null != m_editorFactory)
			return m_editorFactory;
		throw new IllegalStateException("editorFactory is not set.");
	}

	public void setEditorFactory(@Nullable IRowEditorFactory<T, ? extends NodeContainer> editorFactory) {
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
	 * Set a handler to call when editing a row in an editable table component after
	 * editing is (somehow) marked as complete. When called the editor's contents has to be handled (f.e. instance should be
	 * added to the model by using the bindings. This method can also be used to check the data for validity
	 * or to check for duplicates, for instance by using {@link MetaManager#hasDuplicates(java.util.List, Object, String)}.
	 *
	 * @return
	 */
	@Nullable
	public IRowEditorEvent<T, ? > getOnRowChangeCompleted() {
		return m_onRowChangeCompleted;
	}

	/**
	 * Set a handler to call when editing a row in an editable table component after
	 * editing is (somehow) marked as complete. When called the editor's contents has to be handled (f.e. instance should be
	 * added to the model by using the bindings. This method can also be used to check the data for validity
	 * or to check for duplicates, for instance by using {@link MetaManager#hasDuplicates(java.util.List, Object, String)}.
	 *
	 * @param onNewComplete
	 */
	public void setOnRowChangeCompleted(@Nullable IRowEditorEvent<T, ? > onRowChangeCompleted) {
		m_onRowChangeCompleted = onRowChangeCompleted;
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

	/**
	 * When set (the default) items are added to the table by calling {@link IModifyableTableModel#add(Object)} when the new row
	 * action executed succesfully.
	 * @return
	 */
	public boolean isEnableAddingItems() {
		return m_enableAddingItems;
	}

	/**
	 * When set items are added to the table by calling {@link IModifyableTableModel#add(Object)} when the new row
	 * action executed succesfully.
	 * @param enableAddingItems
	 */
	public void setEnableAddingItems(boolean enableAddingItems) {
		m_enableAddingItems = enableAddingItems;
	}

	/**
	 * When executed, method would try to collapse all currently expanded rows.
	 * In case that some row can not be collapsed, method would return false, that means that probably data validation has failed on some expanded row.
	 * @return
	 * @throws Exception
	 */
	public boolean collapseAllExpandedRows() throws Exception {
		boolean dataValid = true;
		if(m_newAtStart && m_newBody != null) {
			clearNewEditor();
			dataValid = m_newBody == null;
		}

		if(m_dataBody != null) {
			int index = 0;
			for(TR row : getDataBody().getChildren(TR.class)) {
				if(row.getUserObject() != null && row.getUserObject() instanceof IEditor) {
					collapseRow(index, row);
					//in case that row can be collapsed, editing is successful
					dataValid = dataValid && row.getUserObject() == null;
				}
				index++;
			}
		}

		if(!m_newAtStart && m_newBody != null) {
			clearNewEditor();
			dataValid = dataValid && m_newBody == null;
		}

		return dataValid;
	}

	public boolean isEnableExpandItems() {
		return m_enableExpandItems;
	}

	public void setEnableExpandItems(boolean enableExpandItems) {
		m_enableExpandItems = enableExpandItems;
	}

	public boolean isEnableRowEdit() {
		return m_enableRowEdit;
	}

	public void setEnableRowEdit(boolean enableRowEdit) {
		m_enableRowEdit = enableRowEdit;
	}
}
