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
package to.etc.domui.legacy.component.tbl;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.component.tbl.ColumnContainer;
import to.etc.domui.component.tbl.DataTable;
import to.etc.domui.component.tbl.HeaderContainer;
import to.etc.domui.component.tbl.IRowRenderer;
import to.etc.domui.component.tbl.ITableModel;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * 20100323 jal DO NOT USE UNTIL FINISHED.
 * FIXME Needs to properly implement rendering, paging and datamodel changes before it can be used any further.
 *
 * DataTable customized to support multiple selection functionality by rendering checkbox field as first column.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Mar 23, 2010
 */
@Deprecated
public class CheckBoxDataTable<T> extends DataTableOld<T> {
	@Nonnull
	private List<T> m_selectedRows = Collections.EMPTY_LIST;

	@Nonnull
	private List<T> m_disabledRows = Collections.EMPTY_LIST;

	@Nullable
	private String m_selectionColTitle;

	@Nullable
	private IValueChanged<CheckBoxDataTable<T>> m_selectionChangedHandler;

	private boolean m_disableOnRowClick = false;

	//	public CheckBoxDataTable() {}

	public CheckBoxDataTable(@Nonnull ITableModel<T> m, @Nonnull IRowRenderer<T> r) {
		super(m, r);
	}

	//	public CheckBoxDataTable(IRowRenderer<T> r) {
	//		super(r);
	//	}

	private boolean m_readOnly;

	private String m_readOnlyTitle;

	protected void handleSelectionChanged(boolean selected, T item) throws Exception {
		if(selected) {
			addToSelection(item);
		} else {
			removeFromSelection(item);
		}
		if(getSelectionChangedHandler() != null) {
			getSelectionChangedHandler().onValueChanged(this);
		}
	}

	/**
	 * Set a new model for this table. This discards the entire presentation and causes a full build at render time.
	 */
	@Override
	public void setModel(@Nonnull ITableModel<T> model) {
		clearSelection();
		super.setModel(model);
	}

	private void clearSelection() {
		if(m_selectedRows != Collections.EMPTY_LIST) {
			m_selectedRows.clear();
		}
	}

	/**
	 * Set the specified item as SELECTED or DESELECTED.
	 * FIXME 20100404 jal Must update presentation which is currently hard
	 * @param item
	 * @param on
	 */
	public void setSelected(T item, boolean on) {
		if(on)
			addToSelection(item);
		else
			removeFromSelection(item);
	}

	public void setSelected(List<T> items, boolean on) throws Exception {
		for(T item : getModel().getItems(0, getModel().getRows())) {
			if(items.contains(item)) {
				if(on) {
					addToSelection(item);
				} else {
					removeFromSelection(item);
				}
			}
		}
		if(isBuilt()) {
			forceRebuild();
			if(getSelectionChangedHandler() != null) {
				getSelectionChangedHandler().onValueChanged(this);
			}
		}
	}

	public void setDisabled(List<T> items, boolean disable) throws Exception {
		if(m_disabledRows == Collections.EMPTY_LIST) {
			m_disabledRows = new ArrayList<T>();
		}
		for(T item : getModel().getItems(0, getModel().getRows())) {
			for(T toDisable : items) {
				if(MetaManager.areObjectsEqual(item, toDisable)) {
					if(disable && !m_disabledRows.contains(item)) {
						m_disabledRows.add(item);
					}
					if(!disable && m_disabledRows.contains(item)) {
						m_disabledRows.remove(item);
					}
				}
			}
		}
		if(isBuilt()) {
			forceRebuild();
		}
	}

	public void setAllDisabled(boolean allDisabled) throws Exception {
		if(m_disabledRows == Collections.EMPTY_LIST) {
			m_disabledRows = new ArrayList<T>();
		}
		m_disabledRows.clear();
		if(allDisabled) {
			m_disabledRows.addAll(getModel().getItems(0, getModel().getRows()));
		}
		if(isBuilt()) {
			forceRebuild();
		}
	}

	private void addToSelection(T item) {
		if(m_selectedRows == Collections.EMPTY_LIST) {
			m_selectedRows = new ArrayList<T>();
		}
		if(getSelectedIndexOf(item) == -1) {
			m_selectedRows.add(item);
		}
	}

	private void removeFromSelection(T item) {
		if(m_selectedRows == Collections.EMPTY_LIST) {
			m_selectedRows = new ArrayList<T>();
		}
		int index = getSelectedIndexOf(item);
		if(index > -1) {
			m_selectedRows.remove(index);
		}
	}

	public void selectAll() throws Exception {
		if(m_selectedRows == Collections.EMPTY_LIST) {
			m_selectedRows = new ArrayList<T>();
		}
		m_selectedRows.clear();
		for(T item : getModel().getItems(0, getModel().getRows())) {
			if(getDisabledIndexOf(item) < 0) {
				m_selectedRows.add(item);
			}
		}
		if(isBuilt()) {
			forceRebuild();
		}
		if(getSelectionChangedHandler() != null) {
			getSelectionChangedHandler().onValueChanged(this);
		}
	}

	public void deselectAll() throws Exception {
		boolean notifyChange = m_selectedRows.size() > 0;
		if(m_selectedRows != Collections.EMPTY_LIST) {
			m_selectedRows.clear();
		}
		if(isBuilt()) {
			forceRebuild();
		}
		if(notifyChange && getSelectionChangedHandler() != null) {
			getSelectionChangedHandler().onValueChanged(this);
		}
	}

	/**
	 * Add selection column as first column.
	 * @see to.etc.domui.component.tbl.DataTable#renderHeader(to.etc.domui.component.tbl.HeaderContainer)
	 */
	@Override
	protected void renderHeader(@Nonnull HeaderContainer<T> hc) throws Exception {
		hc.add(getSelectionColTitle() == null ? Msgs.BUNDLE.getString(Msgs.UI_MLUI_COL_TTL) : getSelectionColTitle());
		getRowRenderer().renderHeader(this, hc);
	}

	/**
	 * Add checkbox for selection as first column.
	 * @see to.etc.domui.component.tbl.DataTable#renderRow(to.etc.domui.dom.html.TR, to.etc.domui.component.tbl.ColumnContainer, int, java.lang.Object)
	 */
	@Override
	void internalRenderRow(@Nonnull TR tr, @Nonnull ColumnContainer<T> cc, int index, @Nonnull T value) throws Exception {
		TD selectionCell = new TD();

		boolean isDisabled = getDisabledIndexOf(value) > -1;
		Checkbox b = new Checkbox();
		b.setChecked(getSelectedIndexOf(value) > -1);
		if(!isDisabled) {
			b.setClicked(new IClicked<Checkbox>() {
				@Override
				public void clicked(@Nonnull Checkbox ckb) throws Exception {
					//FIXME: must be done as double change of value to cause changed protected field to be set, otherwise is not rendered properly in HTML response.
					// jal 20091105 Please explain??? The 2nd call is not doing anything right now.... I would understand if the 1st call was ckb.setChecked(ckb.isChecked())...
					ckb.setChecked(!ckb.isChecked());
					ckb.setChecked(!ckb.isChecked());
					handleSelectionChanged(ckb.isChecked(), (T) ckb.getUserObject());
				}
			});
		} else {
			b.setDisabled(isDisabled);
		}
		b.setUserObject(value);
		selectionCell.add(b);
		tr.add(selectionCell);
		tr.setUserObject(b);
		if(m_readOnly) {
			b.setReadOnly(true);
			if(m_readOnlyTitle != null) {
				b.setTitle(m_readOnlyTitle);
			}
		} else {
			if(!m_disableOnRowClick && !isDisabled) {
				tr.addCssClass("ui-rowsel");
				tr.setClicked(new IClicked<TR>() {
					@Override
					public void clicked(@Nonnull TR row) throws Exception {
						Object userObject = row.getUserObject();
						if(userObject instanceof Checkbox) {
							Checkbox ckb = (Checkbox) userObject;
							if(null == ckb)
								throw new IllegalStateException("Missing checkbox in userObject?");
							ckb.setChecked(!(ckb.isChecked()));
							handleSelectionChanged(ckb.isChecked(), (T) ckb.getUserObject());
						}
					}
				});
			}
		}

		tr.add(selectionCell);
		getRowRenderer().renderRow(this, cc, index, value);
	}

	private int getSelectedIndexOf(T value) {
		int index = 0;
		for(T item : m_selectedRows) {
			if(MetaManager.areObjectsEqual(value, item)) {
				return index;
			}
			index++;
		}
		return -1;
	}

	private int getDisabledIndexOf(T value) {
		int index = 0;
		for(T item : m_disabledRows) {
			if(MetaManager.areObjectsEqual(value, item)) {
				return index;
			}
			index++;
		}
		return -1;
	}

	public String getSelectionColTitle() {
		return m_selectionColTitle;
	}

	public void setSelectionColTitle(String selectionColTitle) {
		m_selectionColTitle = selectionColTitle;
	}

	public List<T> getSelectedRows() {
		return m_selectedRows;
	}

	public List<T> getDisabledRows() {
		return m_disabledRows;
	}

	public List<T> getUnselectedRows() throws Exception {
		List<T> unselected = new ArrayList<T>();
		for(T item : getModel().getItems(0, getModel().getRows())) {
			if(m_selectedRows.contains(item)) {
				continue;
			}
			unselected.add(item);
		}
		return unselected;
	}

	public void setSelectionChangedHandler(IValueChanged<CheckBoxDataTable<T>> handler) {
		m_selectionChangedHandler = handler;
	}

	public IValueChanged<CheckBoxDataTable<T>> getSelectionChangedHandler() {
		return m_selectionChangedHandler;
	}

	/**
	 * Returns T if the control is currently in readonly mode, which renders readonly checkboxes.
	 * @return
	 */
	public boolean isReadOnly() {
		return m_readOnly;
	}

	/**
	 * Sets checkboxes to readonly or editable mode.
	 * @param ro
	 */
	public void setReadOnly(boolean ro) {
		if(m_readOnly != ro) {
			m_readOnly = ro;
			if(isBuilt()) {
				forceRebuild();
			}
		}
	}

	/**
	 * Returns assigned readonly reason message. That is title on readonly checkbox, if readonly mode is set.
	 * @return
	 */
	public String getReadOnlyTitle() {
		return m_readOnlyTitle;
	}

	/**
	 * Set readonly reason message. That is title on readonly checkbox, if readonly mode is set.
	 * @param readOnlyReason
	 */
	public void setReadOnlyTitle(String readOnlyTitle) {
		if(m_readOnlyTitle != readOnlyTitle) {
			m_readOnlyTitle = readOnlyTitle;
			if(isBuilt()) {
				forceRebuild();
			}
		}
	}

	public boolean isDisableOnRowClick() {
		return m_disableOnRowClick;
	}

	public void setDisableOnRowClick(boolean disableOnRowClick) {
		m_disableOnRowClick = disableOnRowClick;
	}
}

