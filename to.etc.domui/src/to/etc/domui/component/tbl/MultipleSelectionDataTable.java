package to.etc.domui.component.tbl;

import java.util.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * DataTable customized to support multiple selection functionality. Supports accmulation of selection along multiple queries.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 26 Oct 2009
 */
public class MultipleSelectionDataTable<T> extends DataTable<T> {

	public MultipleSelectionDataTable(Class<T> dataClass, ITableModel<T> m, IRowRenderer<T> r) {
		super(m, r);
		//		m_dataClass = dataClass;
	}

	public MultipleSelectionDataTable(Class<T> dataClass, IRowRenderer<T> r) {
		super(r);
		//		m_dataClass = dataClass;
	}

	private List<T> m_accumulatedRows = new ArrayList<T>();

	List<Boolean> m_accumulatedSelections = new ArrayList<Boolean>();

	//	private Class<T> m_dataClass;

	private String m_selectionColTitle;

	@Override
	public void createContent() throws Exception {
		setCssClass("ui-dt");

		//-- Ask the renderer for a sort order, if applicable
		m_rowRenderer.beforeQuery(this); // ORDER!! BEFORE CALCINDICES or any other call that materializes the result.

		calcIndices(); // Calculate rows to show.

		List<T> list = getPageItems(); // Data to show

		if(m_accumulatedRows.size() > 0 || list.size() > 0) {
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

			for(int index = 0; index < m_accumulatedRows.size(); index++) {
				T accumulatedItem = m_accumulatedRows.get(index);
				tr = new TR();
				getDataBody().add(tr);
				boolean selected = (m_accumulatedSelections.size() > index ? m_accumulatedSelections.get(index).booleanValue() : false);
				renderAccumulatedItem(tr, cc, accumulatedItem, selected, index);
			}

			if(m_accumulatedRows.size() > 0) {
				TR splitterRow = createSplitterRow();
				getDataBody().add(splitterRow);
			}

			int ix = m_six;
			for(T item : list) {
				tr = new TR();
				getDataBody().add(tr);
				cc.setParent(tr);
				TD selectionMarkerCell = new TD();
				if(!m_accumulatedRows.contains(item)) {
					Img imgAddToSelection = new Img("THEME/addToSelection.png");
					selectionMarkerCell.add(imgAddToSelection);
				} else {
					selectionMarkerCell.add(" ");
				}
				selectionMarkerCell.setUserObject(item);
				/*				selectionMarkerCell.setClicked(new IClicked<TD>() {
									public void clicked(TD cell) throws Exception {
										if(cell.getUserObject() != null) {
											accumulateSelection((T) cell.getUserObject());
										}
									}
								});*/
				tr.add(selectionMarkerCell);
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

	private TR createSplitterRow() {
		if(getDataBody().getChildCount() == 0) {
			throw new IllegalStateException("Missing childs!");
		}
		TR headerRow = getDataBody().getRow(0);
		TR splitterRow = new TR();
		TD splitterCell = new TD();
		splitterCell.setText(" ");
		splitterCell.setCssClass("ui-dt-separator");
		splitterCell.setColspan(headerRow.getChildCount());
		splitterRow.add(splitterCell);
		return splitterRow;
	}

	private void renderAccumulatedItem(TR tr, ColumnContainer<T> cc, T item, boolean selected, int index) throws Exception {
		cc.setParent(tr);
		Checkbox b = new Checkbox();
		b.setClicked(new IClicked<Checkbox>() {
			public void clicked(Checkbox ckb) throws Exception {
				//FIXME: must be done as double change of value to cause changed protected field to be set, otherwise is not rendered properly in HTML response.
				// jal 20091105 Please explain??? The 2nd call is not doing anything right now.... I would understand if the 1st call was ckb.setChecked(ckb.isChecked())...
				ckb.setChecked(!ckb.isChecked());
				ckb.setChecked(!ckb.isChecked());
				TR row = ckb.getParent(TR.class);
				handleAccumulatedItemRowSelectionChanged(row, new Boolean(ckb.isChecked()));
			}
		});
		b.setChecked(selected);
		TD selectionCell = new TD();
		selectionCell.add(b);
		tr.add(selectionCell);
		tr.setUserObject(b);
		tr.setClicked(new IClicked<TR>() {

			public void clicked(TR row) throws Exception {
				if(row.getUserObject() instanceof Checkbox) {
					((Checkbox) row.getUserObject()).setChecked(!((Checkbox) row.getUserObject()).isChecked());
				}
			}
		});
		m_rowRenderer.renderRow(this, cc, index, item);
	}

	protected void handleAccumulatedItemRowSelectionChanged(TR row, Boolean value) {
		int index = getDataBody().getChildren(TR.class).indexOf(row);
		if(m_accumulatedSelections.size() > index) {
			m_accumulatedSelections.set(index, value);
		}
	}

	public void accumulateSelection(TR row, T item) throws Exception {
		if(!m_accumulatedRows.contains(item)) {
			m_accumulatedRows.add(item);
			m_accumulatedSelections.add(Boolean.TRUE);
			TR tr = new TR();
			getDataBody().add(m_accumulatedRows.size() - 1, tr);
			ColumnContainer<T> cc = new ColumnContainer<T>(this);
			renderAccumulatedItem(tr, cc, item, true, m_accumulatedRows.size() - 1);

			if(m_accumulatedRows.size() == 1) {
				TR splitterRow = createSplitterRow();
				getDataBody().add(m_accumulatedRows.size(), splitterRow);
			}

			if(row.getChildren(TD.class).size() == 0) {
				throw new IllegalStateException("Missing row childs!");
			}
			TD selectionMarkerCell = row.getChildren(TD.class).get(0);
			selectionMarkerCell.removeAllChildren();
			selectionMarkerCell.add(" ");
		}
	}

	/**
	 * Set a new model for this table. This discards the entire presentation and causes a full build at render time.
	 */
	@Override
	public void setModel(ITableModel<T> model) {
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
		if(tr instanceof TR) {
			if(tr.getUserObject() instanceof Checkbox) {
				Checkbox ckb = (Checkbox) tr.getUserObject();
				ckb.setChecked(!ckb.isChecked());
				handleAccumulatedItemRowSelectionChanged((TR) tr, new Boolean(ckb.isChecked()));
			} else {
				accumulateSelection((TR) tr, val);
			}
		} else {
			throw new IllegalStateException("expected TR: " + tr);
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

	public String getSelectionColTitle() {
		return m_selectionColTitle;
	}

	public void setSelectionColTitle(String selectionColTitle) {
		m_selectionColTitle = selectionColTitle;
	}

}
