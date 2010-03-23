package to.etc.domui.component.tbl;

import java.util.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * DataTable customized to support multiple selection functionality by rendering checkbox field as first column.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Mar 23, 2010
 */
public class CheckBoxDataTable<T> extends DataTable<T> {

	public CheckBoxDataTable(ITableModel<T> m, IRowRenderer<T> r) {
		super(m, r);
	}

	public CheckBoxDataTable(IRowRenderer<T> r) {
		super(r);
	}

	private List<T> m_selectedRows = Collections.EMPTY_LIST;

	private String m_selectionColTitle;

	private IValueChanged<CheckBoxDataTable<T>> m_selectionChangedHandler;

	@Override
	public void createContent() throws Exception {
		setCssClass("ui-dt");

		//-- Ask the renderer for a sort order, if applicable
		m_rowRenderer.beforeQuery(this); // ORDER!! BEFORE CALCINDICES or any other call that materializes the result.

		calcIndices(); // Calculate rows to show.

		List<T> list = getPageItems(); // Data to show

		if(list.size() > 0) {
			getTable().removeAllChildren();
			add(getTable());

			//-- Render the header.
			THead hd = new THead();
			HeaderContainer<T> hc = new HeaderContainer<T>(this);
			TR tr = new TR();
			tr.setCssClass("ui-dt-hdr");
			hd.add(tr);
			hc.setParent(tr);
			hc.add(getSelectionColTitle() == null ? Msgs.BUNDLE.getString(Msgs.UI_MLUI_COL_TTL) : getSelectionColTitle());
			m_rowRenderer.renderHeader(this, hc);
			getTable().add(hd);

			//-- Render loop: add rows && ask the renderer to add columns.
			setDataBody(new TBody());
			getTable().add(getDataBody());

			ColumnContainer<T> cc = new ColumnContainer<T>(this);

			int ix = m_six;
			for(T item : list) {
				tr = new TR();
				getDataBody().add(tr);
				cc.setParent(tr);
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
				b.setChecked(m_selectedRows.contains(item));
				b.setUserObject(item);
				selectionCell.add(b);
				tr.add(selectionCell);
				tr.setUserObject(b);
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
				m_rowRenderer.renderRow(this, cc, ix, item);
				ix++;
			}
		}

		if(list.size() == 0) {
			Div error = new Div();
			error.setCssClass("ui-dt-nores");
			error.setText(Msgs.BUNDLE.getString(Msgs.UI_DATATABLE_EMPTY));
			add(error);
		}
	}

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

	private void addToSelection(T item) {
		if(m_selectedRows == Collections.EMPTY_LIST) {
			m_selectedRows = new ArrayList<T>();
		}
		if(!m_selectedRows.contains(item)) {
			m_selectedRows.add(item);
		}
	}

	private void removeFromSelection(T item) {
		if(m_selectedRows == Collections.EMPTY_LIST) {
			m_selectedRows = new ArrayList<T>();
		}
		if(m_selectedRows.contains(item)) {
			m_selectedRows.remove(item);
		}
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
