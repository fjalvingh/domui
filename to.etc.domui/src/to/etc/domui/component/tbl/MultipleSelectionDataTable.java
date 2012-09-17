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

import java.util.*;

import javax.annotation.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * DataTable customized to support multiple selection functionality. Supports accmulation of selection along multiple queries.
 * FIXME: vmijic 20110221 - Change implementation later to reuse new multiselction functionality in DataTable.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 26 Oct 2009
 */
public class MultipleSelectionDataTable<T> extends DataTable<T> {

	public MultipleSelectionDataTable(@Nonnull Class<T> dataClass, @Nonnull ITableModel<T> m, @Nonnull IRowRenderer<T> r) {
		super(m, r);
	}

	@Nonnull
	private List<T> m_accumulatedRows = new ArrayList<T>();

	@Nonnull
	List<Boolean> m_accumulatedSelections = new ArrayList<Boolean>();

	//	private Class<T> m_dataClass;

	@Nullable
	private String m_selectionColTitle;

	@Override
	public void createContent() throws Exception {
		setCssClass("ui-dt");

		//-- Ask the renderer for a sort order, if applicable
		getRowRenderer().beforeQuery(this); // ORDER!! BEFORE CALCINDICES or any other call that materializes the result.

		calcIndices(); // Calculate rows to show.

		List<T> list = getPageItems(); // Data to show

		if(m_accumulatedRows.size() > 0 || list.size() > 0) {
			getTable().removeAllChildren();
			Table t = getTable();
			if(null == t)
				throw new IllegalStateException("?");
			add(t);

			//-- Render the header.
			THead hd = new THead();
			HeaderContainer<T> hc = new HeaderContainer<T>(this);
			TR tr = new TR();
			tr.setCssClass("ui-dt-hdr");
			hd.add(tr);
			hc.setParent(tr);
			Img selImg = new Img("THEME/dspcb-on.png");
			selImg.setTitle(getSelectionColTitle() == null ? Msgs.BUNDLE.getString(Msgs.UI_MLUI_COL_TTL) : getSelectionColTitle());
			hc.add(selImg);
			getRowRenderer().renderHeader(this, hc);
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
				tr.add(selectionMarkerCell);

				//-- Is a rowclick handler needed?
				if(getRowRenderer().getRowClicked() != null || null != getSelectionModel()) {
					//-- Add a click handler to select or pass the rowclicked event.
					final TR therow = tr;
					final T theitem = item;
					cc.getTR().setClicked(new IClicked2<TR>() {
						@Override
						@SuppressWarnings({"synthetic-access"})
						public void clicked(@Nonnull TR b, @Nonnull ClickInfo clinfo) throws Exception {
							((ICellClicked<T>) getRowRenderer().getRowClicked()).cellClicked(therow, theitem);
						}
					});
					cc.getTR().addCssClass("ui-rowsel");
				}
				getRowRenderer().renderRow(this, cc, ix, item);
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
			@Override
			public void clicked(Checkbox ckb) throws Exception {
				TR row = ckb.getParent(TR.class);
				handleAccumulatedItemRowSelectionChanged(row, Boolean.valueOf(ckb.isChecked()));
			}
		});
		b.setChecked(selected);
		TD selectionCell = new TD();
		selectionCell.add(b);
		tr.add(selectionCell);
		tr.setUserObject(b);
		tr.setClicked(new IClicked<TR>() {

			@Override
			public void clicked(TR row) throws Exception {
				if(row.getUserObject() instanceof Checkbox) {
					((Checkbox) row.getUserObject()).setChecked(!((Checkbox) row.getUserObject()).isChecked());
				}
			}
		});

		//-- jal: added due to multiselect refactoring. The above setclicked should never have worked???
		if(getRowRenderer().getRowClicked() != null || null != getSelectionModel()) {
			//-- Add a click handler to select or pass the rowclicked event.
			final TR therow = tr;
			final T theitem = item;
			cc.getTR().setClicked(new IClicked2<TR>() {
				@Override
				@SuppressWarnings({"synthetic-access"})
				public void clicked(@Nonnull TR b, @Nonnull ClickInfo clinfo) throws Exception {
					((ICellClicked<T>) getRowRenderer().getRowClicked()).cellClicked(therow, theitem);
				}
			});
			cc.getTR().addCssClass("ui-rowsel");
		}


		getRowRenderer().renderRow(this, cc, index, item);
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
	public void setModel(@Nonnull ITableModel<T> model) {
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

	public void handleRowClicked(NodeBase tr, T val) throws Exception {
		if(tr instanceof TR) {
			if(tr.getUserObject() instanceof Checkbox) {
				Checkbox ckb = (Checkbox) tr.getUserObject();
				if(null == ckb)
					throw new IllegalStateException("No checkbox??");
				ckb.setChecked(!ckb.isChecked());
				handleAccumulatedItemRowSelectionChanged((TR) tr, Boolean.valueOf(ckb.isChecked()));
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

	public void addAllSearchResultsToAccumulatedResults() throws Exception {
		for(T item : getModel().getItems(0, getModel().getRows())) {
			m_accumulatedRows.add(item);
			m_accumulatedSelections.add(Boolean.TRUE);
		}
	}
}
