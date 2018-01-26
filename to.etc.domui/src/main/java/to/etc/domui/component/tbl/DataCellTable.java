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

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.util.*;

import javax.annotation.*;
import java.util.*;

/**
 * A table-related component which shows all "rows" in a single cell instead of
 * in a table. This results in an w x h grid where each cell in the grid contains
 * a single data item. Example is a photo album's index page.
 *
 * 20110107 vmijic -> made generic
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 13, 2008
 */
public class DataCellTable<T> extends PageableTabularComponentBase<T> implements ISelectionListener<T>, ISelectableTableComponent<T> {
	private int m_rows = 3, m_columns = 3;

	@Nonnull
	final private Table m_table = new Table();

	private TBody m_dataBody;

	private boolean m_renderEmptyCells;

	private boolean m_renderEmptyRows;

	/** The specified ComboRenderer used. */
	private IRenderInto<T> m_contentRenderer;

	private IRenderInto<T> m_actualContentRenderer;

	private Class< ? extends IRenderInto<T>> m_contentRendererClass;

	@Nonnull
	final private Map<T, Div> m_visibleMap = new HashMap<T, Div>();

	public DataCellTable(@Nonnull ITableModel<T> model) {
		super(model);
	}

	public int getRows() {
		return m_rows;
	}

	public void setRows(int rows) {
		if(rows == m_rows)
			return;
		m_rows = rows;
		rebuild();
	}

	public int getColumns() {
		return m_columns;
	}

	public void setColumns(int columns) {
		if(m_columns == columns)
			return;
		m_columns = columns;
		rebuild();
	}

	@Override
	protected int getPageSize() {
		return m_columns * m_rows;
	}

	public boolean isRenderEmptyCells() {
		return m_renderEmptyCells;
	}

	/**
	 * When set to T, empty cells (cells that have no data content, usually present at the
	 * end of the table in the last row) will be rendered by calling the INodeContentRenderer
	 * with a null object value. The default is to simply render empty (invisible) cells for
	 * the missing data items.
	 *
	 * @param renderEmptyCells
	 */
	public void setRenderEmptyCells(boolean renderEmptyCells) {
		m_renderEmptyCells = renderEmptyCells;
	}

	@Nullable
	public IRenderInto<T> getContentRenderer() {
		return m_contentRenderer;
	}

	public void setContentRenderer(@Nullable IRenderInto<T> contentRenderer) {
		m_contentRenderer = contentRenderer;
	}

	@Nullable
	public Class< ? extends IRenderInto<T>> getContentRendererClass() {
		return m_contentRendererClass;
	}

	public void setContentRendererClass(@Nullable Class< ? extends IRenderInto<T>> contentRendererClass) {
		m_contentRendererClass = contentRendererClass;
	}

	public boolean isRenderEmptyRows() {
		return m_renderEmptyRows;
	}

	public void setRenderEmptyRows(boolean renderEmptyRows) {
		m_renderEmptyRows = renderEmptyRows;
	}

	private void rebuild() {
		forceRebuild();
	}

	@Nonnull
	private IRenderInto<T> calculateContentRenderer(@Nullable Object val) {
		if(m_actualContentRenderer != null)
			return m_actualContentRenderer;
		if(m_contentRenderer != null)
			return (m_actualContentRenderer = m_contentRenderer);
		if(m_contentRendererClass != null)
			return (m_actualContentRenderer = DomApplication.get().createInstance(m_contentRendererClass));
		throw new IllegalStateException("Missing INodeContentRenderer on " + this);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Content (re)renderers.								*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @see to.etc.domui.dom.html.NodeBase#createContent()
	 */
	@Override
	public void createContent() throws Exception {
		setCssClass("ui-dct");
		calcIndices(); // Calculate rows to show.

		//-- If we've nothing to show- show nothing beautifully.
		List<T> list = getPageItems(); // Data to show
		if(list.size() == 0) {
			Div error = new Div();
			error.setCssClass("ui-dct-nores");
			error.setText(Msgs.BUNDLE.getString(Msgs.UI_DATATABLE_EMPTY));
			add(error);
			return;
		}
		m_table.removeAllChildren();
		add(m_table);
		TBody body = m_dataBody = new TBody();
		m_table.add(body);

		//-- Row- and column renderer thingerydoo.
		m_visibleMap.clear();
		int index = 0;
		for(int row = 0; row < getRows(); row++) {
			//-- Create the next row of cells,
			TR tr = new TR();
			body.add(tr);

			//-- Do all columns,
			for(int col = 0; col < getColumns(); col++) {
				TD td = new TD();
				tr.add(td);
				td.setValign(TableVAlign.TOP);

				Div seldiv = new Div();
				td.add(seldiv);
				seldiv.setCssClass("ui-dct-item");


				T value = null;
				if(index >= list.size()) {
					if(!isRenderEmptyCells()) {
						//-- Empty cell node
						seldiv.setCssClass("ui-dct-empty");
						continue;
					}
				} else
					value = list.get(index);

				//-- Call the renderer
				if(null != value) {
					m_visibleMap.put(value, seldiv);
					renderCell(seldiv, value);
				}
				index++;
			}

			//-- This-row has completed. Are there more cells?
			if(index >= list.size()) {
				if(!isRenderEmptyRows())
					break;
			}
		}
	}

	private boolean hasSelectionModel() {
		return getSelectionModel() != null;
	}


	private void renderCell(@Nonnull final Div td, @Nonnull final T value) throws Exception {
		boolean selected = false;
		ISelectionModel<T> sm = getSelectionModel();
		if(null != sm) {
			selected = sm.isSelected(value);
			td.addCssClass("ui-clickable");
			td.setClicked(new IClicked<Div>() {
				@Override
				public void clicked(@Nonnull Div clickednode) throws Exception {
					handleSelectionClick(td, value);
				}
			});
		}

		IRenderInto<T> r = calculateContentRenderer(value);
		r.render(td, value); //, Boolean.valueOf(selected));

		if(selected)
			td.setCssClass("ui-dct-selected");
	}

	private void handleSelectionClick(@Nonnull Div td, @Nonnull T value) throws Exception {
		ISelectionModel<T> sm = getSelectionModel();
		if(null == sm)
			throw new IllegalStateException("SelectionModel is null??");
		boolean nvalue = !sm.isSelected(value);
		sm.setInstanceSelected(value, nvalue);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	TableModelListener interface implementation.		*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @see to.etc.domui.component.tbl.ITableModelListener#modelChanged(to.etc.domui.component.tbl.ITableModel)
	 */
	@Override
	public void modelChanged(@Nullable ITableModel<T> model) {
		rebuild();
	}

	@Override
	public void rowAdded(@Nonnull ITableModel<T> model, int index, @Nonnull T value) throws Exception {
	}

	@Override
	public void rowDeleted(@Nonnull ITableModel<T> model, int index, @Nonnull T value) throws Exception {
	}

	@Override
	public void rowModified(@Nonnull ITableModel<T> model, int index, @Nonnull T value) throws Exception {
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
		for(Map.Entry<T, Div> me : m_visibleMap.entrySet()) {
			if(MetaManager.areObjectsEqual(me.getKey(), row)) {
				Div d = me.getValue();
				if(on)
					d.addCssClass("ui-dct-selected");
				else
					d.removeCssClass("ui-dct-selected");
				return;
			}
		}
	}

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

		for(T item : m_visibleMap.keySet()) {
			selectionChanged(item, sm.isSelected(item));
		}
	}

	@Override
	public boolean isMultiSelectionVisible() {
		ISelectionModel<T> sm = getSelectionModel();
		return sm != null && sm.isMultiSelect();
	}

	@Override
	protected void createSelectionUI() throws Exception {}

}
