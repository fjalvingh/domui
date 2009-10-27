package to.etc.domui.component.tbl;

import java.util.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;

/**
 * DataTable customized to support multiple selection functionality. Supports accmulation of selection along multiple queries. 
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 26 Oct 2009
 */
public class MultipleSelectionDataTable<T> extends DataTable {

	public MultipleSelectionDataTable(Class<T> dataClass, ITableModel< ? > m, IRowRenderer r) {
		super(m, r);
		m_dataClass = dataClass;
	}

	public MultipleSelectionDataTable(Class<T> dataClass, IRowRenderer r) {
		super(r);
		m_dataClass = dataClass;
	}

	private List<T> m_accumulatedRows = new ArrayList<T>();

	List<Boolean> m_accumulatedSelections = new ArrayList<Boolean>();

	private Class<T> m_dataClass;

	@Override
	public void createContent() throws Exception {
		setCssClass("ui-dt");

		//-- Ask the renderer for a sort order, if applicable
		m_rowRenderer.beforeQuery(this); // ORDER!! BEFORE CALCINDICES or any other call that materializes the result.

		calcIndices(); // Calculate rows to show.

		List< ? > list = getPageItems(); // Data to show

		if(m_accumulatedRows.size() > 0 || list.size() > 0) {
			m_table.removeAllChildren();
			add(m_table);

			//-- Render the header.
			THead hd = new THead();
			m_table.add(hd);
			HeaderContainer hc = new HeaderContainer(this);
			TR tr = new TR();
			tr.setCssClass("ui-dt-hdr");
			hd.add(tr);
			hc.setParent(tr);
			hc.add("Selection");
			m_rowRenderer.renderHeader(this, hc);

			//-- Render loop: add rows && ask the renderer to add columns.
			m_dataBody = new TBody();
			m_table.add(m_dataBody);

			ColumnContainer cc = new ColumnContainer(this);

			for(int index = 0; index < m_accumulatedRows.size(); index++) {
				T accumulatedItem = m_accumulatedRows.get(index);
				tr = new TR();
				m_dataBody.add(tr);
				boolean selected = (m_accumulatedSelections.size() > index ? m_accumulatedSelections.get(index).booleanValue() : false);
				renderAccumulatedItem(tr, cc, accumulatedItem, selected);
			}

			if(m_accumulatedRows.size() > 0) {
				TR splitterRow = createSplitterRow();
				m_dataBody.add(splitterRow);
			}

			int ix = m_six;
			for(Object o : list) {
				//FIXME: unsecures boxing. instanceof can not be used here... 
				T item = (T) o;
				tr = new TR();
				m_dataBody.add(tr);
				cc.setParent(tr);
				Img imgAddToSelection = new Img("THEME/addToSelection.png");
				TD addToSelectionCell = new TD();
				addToSelectionCell.setUserObject(item);
				addToSelectionCell.add(imgAddToSelection);
				addToSelectionCell.setClicked(new IClicked<TD>() {
					public void clicked(TD cell) throws Exception {
						if(cell.getUserObject() != null) {
							accumulateSelection((T) cell.getUserObject());
						}
					}
				});
				tr.add(addToSelectionCell);
				m_rowRenderer.renderRow(this, cc, ix, o);
				ix++;
			}
		}

		if(list.size() == 0) {
			Div error = new Div();
			error.setCssClass("ui-dt-nores");
			error.setText(NlsContext.getGlobalMessage(Msgs.UI_DATATABLE_EMPTY));
			add(error);
		}
	}

	private TR createSplitterRow() {
		assert (m_dataBody.getChildCount() > 0);
		TR headerRow = m_dataBody.getRow(0);
		TR splitterRow = new TR();
		TD splitterCell = new TD();
		splitterCell.setText(" ");
		splitterCell.setCssClass("ui-dt-separator");
		splitterCell.setColspan(headerRow.getChildCount());
		splitterRow.add(splitterCell);
		return splitterRow;
	}

	private void renderAccumulatedItem(TR tr, ColumnContainer cc, T item, boolean selected) throws Exception {
		cc.setParent(tr);
		Checkbox ckb = new Checkbox();
		ckb.setOnValueChanged(new IValueChanged<Checkbox, Boolean>() {
			public void onValueChanged(Checkbox ckb, Boolean value) throws Exception {
				TR row = ckb.getParent(TR.class);
				handleAccumulatedItemRowSelectionChanged(row, value);
			}
		});
		ckb.setChecked(selected);
		TD selectionCell = new TD();
		selectionCell.add(ckb);
		tr.add(selectionCell);
		tr.setUserObject(ckb);
		tr.setClicked(new IClicked<TR>() {

			public void clicked(TR row) throws Exception {
				if(row.getUserObject() instanceof Checkbox) {
					((Checkbox) row.getUserObject()).setChecked(!((Checkbox) row.getUserObject()).isChecked());
				}
			}
		});
		m_rowRenderer.renderRow(this, cc, 0, item);
	}

	protected void handleAccumulatedItemRowSelectionChanged(TR row, Boolean value) {
		int index = m_dataBody.getChildren(TR.class).indexOf(row);
		if(m_accumulatedSelections.size() > index) {
			m_accumulatedSelections.set(index, value);
		}
	}

	public void accumulateSelection(T item) throws Exception {
		if(!m_accumulatedRows.contains(item)) {
			m_accumulatedRows.add(item);
			m_accumulatedSelections.add(Boolean.TRUE);
			TR tr = new TR();
			m_dataBody.add(m_accumulatedRows.size() - 1, tr);
			ColumnContainer cc = new ColumnContainer(this);
			renderAccumulatedItem(tr, cc, item, true);

			if(m_accumulatedRows.size() == 1) {
				TR splitterRow = createSplitterRow();
				m_dataBody.add(m_accumulatedRows.size(), splitterRow);
			}
		}
	}

	/**
	 * Set a new model for this table. This discards the entire presentation and causes a full build at render time.
	 */
	@Override
	public void setModel(ITableModel< ? > model) {
		clearDeselectedAccumulatedRows();
		super.setModel(model);
	}

	private void clearDeselectedAccumulatedRows() {
		for(int index = m_accumulatedSelections.size() - 1; index >= 0; index--) {
			boolean selected = m_accumulatedSelections.get(index).booleanValue();
			if(!selected) {
				m_accumulatedSelections.remove(index);
				if(m_accumulatedRows.size() > index) {
					m_accumulatedRows.remove(index);
				}
			}
		}
	}

	public void handleRowClicked(Page pg, NodeBase tr, T val) throws Exception {
		if(tr instanceof TR && tr.getUserObject() instanceof Checkbox) {
			Checkbox ckb = (Checkbox) tr.getUserObject();
			ckb.setChecked(!ckb.isChecked());
			handleAccumulatedItemRowSelectionChanged((TR) tr, new Boolean(ckb.isChecked()));
		} else {
			accumulateSelection(val);
		}
	}

	public List<T> getAccumulatedResults() {
		List<T> results = new ArrayList<T>();
		for(int index = 0; index < m_accumulatedRows.size(); index++) {
			boolean selected = (m_accumulatedSelections.size() > index ? m_accumulatedSelections.get(index).booleanValue() : false);
			if(selected) {
				results.add(m_accumulatedRows.get(index));
			}
		}
		return results;
	}

}
