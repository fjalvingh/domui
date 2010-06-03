package to.etc.domui.component.tbl;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
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
public class CheckBoxDataTable<T> extends DataTable<T> {
	@Nonnull
	private List<T> m_selectedRows = Collections.EMPTY_LIST;

	@Nullable
	private String m_selectionColTitle;

	@Nullable
	private IValueChanged<CheckBoxDataTable<T>> m_selectionChangedHandler;

	//	public CheckBoxDataTable() {}

	public CheckBoxDataTable(@Nonnull ITableModel<T> m, @Nonnull IRowRenderer<T> r) {
		super(m, r);
	}

	//	public CheckBoxDataTable(IRowRenderer<T> r) {
	//		super(r);
	//	}

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
	public void setModel(ITableModel<T> model) {
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
			m_selectedRows.add(item);
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
	protected void renderHeader(HeaderContainer<T> hc) throws Exception {
		hc.add(getSelectionColTitle() == null ? Msgs.BUNDLE.getString(Msgs.UI_MLUI_COL_TTL) : getSelectionColTitle());
		getRowRenderer().renderHeader(this, hc);
	}

	/**
	 * Add checkbox for selection as first column.
	 * @see to.etc.domui.component.tbl.DataTable#renderRow(to.etc.domui.dom.html.TR, to.etc.domui.component.tbl.ColumnContainer, int, java.lang.Object)
	 */
	@Override
	protected void renderRow(TR tr, ColumnContainer<T> cc, int index, T value) throws Exception {
		TD selectionCell = new TD();

		Checkbox b = new Checkbox();
		b.setClicked(new IClicked<Checkbox>() {
			public void clicked(Checkbox ckb) throws Exception {
				//FIXME: must be done as double change of value to cause changed protected field to be set, otherwise is not rendered properly in HTML response.
				// jal 20091105 Please explain??? The 2nd call is not doing anything right now.... I would understand if the 1st call was ckb.setChecked(ckb.isChecked())...
				ckb.setChecked(!ckb.isChecked());
				ckb.setChecked(!ckb.isChecked());
				handleSelectionChanged(ckb.isChecked(), (T) ckb.getUserObject());
			}
		});
		b.setChecked(getSelectedIndexOf(value) > -1);
		b.setUserObject(value);
		selectionCell.add(b);
		tr.add(selectionCell);
		tr.setUserObject(b);
		tr.addCssClass("ui-rowsel");
		tr.setClicked(new IClicked<TR>() {

			public void clicked(TR row) throws Exception {
				if(row.getUserObject() instanceof Checkbox) {
					Checkbox ckb = (Checkbox) row.getUserObject();
					ckb.setChecked(!((Checkbox) row.getUserObject()).isChecked());
					handleSelectionChanged(ckb.isChecked(), (T) ckb.getUserObject());
				}
			}
		});

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

	public String getSelectionColTitle() {
		return m_selectionColTitle;
	}

	public void setSelectionColTitle(String selectionColTitle) {
		m_selectionColTitle = selectionColTitle;
	}

	public List<T> getSelectedRows() {
		return m_selectedRows;
	}

	public void setSelectionChangedHandler(IValueChanged<CheckBoxDataTable<T>> handler) {
		m_selectionChangedHandler = handler;
	}

	public IValueChanged<CheckBoxDataTable<T>> getSelectionChangedHandler() {
		return m_selectionChangedHandler;
	}

}
