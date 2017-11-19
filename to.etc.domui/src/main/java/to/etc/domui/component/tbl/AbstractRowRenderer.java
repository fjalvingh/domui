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
import to.etc.domui.component.ntbl.*;
import to.etc.domui.converter.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;
import to.etc.webapp.*;
import to.etc.webapp.nls.*;

import javax.annotation.*;

/**
 * Base class for the old and new row renderers, handling most presentation. The configuration is
 * mostly handled by the subclasses.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 15, 2009
 */
public class AbstractRowRenderer<T> implements IClickableRowRenderer<T> {
	/** The class whose instances we'll render in this table. */
	@Nonnull
	private final Class<T> m_dataClass;

	@Nonnull
	final private ClassMetaModel m_metaModel;

	/** When the definition has completed (the object is used) this is TRUE; it disables all calls that change the definition */
	private boolean m_completed;

	@Nonnull
	private final ColumnDefList<T> m_columnList;

	private Img[] m_sortImages;

	private ICellClicked<T> m_rowClicked;

	private IRowButtonFactory<T> m_rowButtonFactory;

	private String m_unknownColumnCaption;

	@Nullable
	private IRowRendered<T> m_onRowRendered;

	private boolean m_sortDescending;

	public interface IRowRendered<T> {
		void rowRendered(@Nonnull TR row, @Nonnull T instance);
	}

	public AbstractRowRenderer(@Nonnull Class<T> data) {
		this(data, MetaManager.findClassMeta(data));
	}

	public AbstractRowRenderer(@Nonnull Class<T> data, @Nonnull ClassMetaModel cmm) {
		m_dataClass = data;
		m_metaModel = cmm;
		m_columnList = new ColumnDefList<T>(data, m_metaModel);
	}

	@Nonnull
	protected ColumnDefList<T> getColumnList() {
		return m_columnList;
	}

	/**
	 * Sets default sort column on row renderer. Overrides property meta model setting if such defines default sort.
	 * @param cd
	 * @param type
	 */
	public void setDefaultSort(@Nonnull SimpleColumnDef< ? > cd, @Nonnull SortableType type) {
		cd.setSortable(type);
		getColumnList().setSortColumn(cd);
	}

	public void setSort(@Nonnull String column, @Nonnull SortableType type) {
		SimpleColumnDef< ? > col = getColumnList().findColumn(column);
		if(null == col)
			return;

		getColumnList().setSortColumn(col);
		col.setSortable(type);
	}

	/**
	 * Returns the metamodel used.
	 * @return
	 */
	@Nonnull
	protected ClassMetaModel model() {
		return m_metaModel;
	}

	/**
	 * Returns the record type being rendered.
	 * @return
	 */
	@Nonnull
	protected Class<?> getActualClass() {
		return m_dataClass;
	}

	/**
	 * Throws an exception if this renderer has been completed and is unmutable.
	 */
	protected void check() {
		if(m_completed)
			throw new IllegalStateException("Programmer error: This object has been USED and cannot be changed anymore");
	}

	/**
	 * Complete this object if it is not already complete (internal).
	 */
	@OverridingMethodsMustInvokeSuper
	protected void complete(@Nonnull final TableModelTableBase<T> tbl) {
		if(!m_completed) {
			SimpleColumnDef< ? > scol = getSortColumn();
			if(scol != null) {
				m_sortDescending = scol.getSortable() == SortableType.SORTABLE_DESC;
			}
		}
		m_completed = true;
	}

	/**
	 * Check if this object is used (completed) and thereby unmodifyable (internal).
	 * @return
	 */
	protected boolean isComplete() {
		return m_completed;
	}

	protected void setSortColumn(@Nullable SimpleColumnDef< ? > cd, @Nullable SortableType type) {
		if(null != cd && null != type)
			cd.setSortable(type);
		m_columnList.setSortColumn(cd);
	}

	@Nullable
	protected SimpleColumnDef< ? > getSortColumn() {
		return m_columnList.getSortColumn();
	}

	/**
	 * The <i>current</i> sorting state of the sort column, as defined by the user's clicking the header.
	 * @return
	 */
	protected boolean isSortDescending() {
		return m_sortDescending;
	}

//	protected void setSortDescending(boolean desc) {
//		m_columnList.setSortDescending(desc);
//	}

	/**
	 * Return the definition for the nth column. You can change the column's definition there.
	 * @param ix
	 * @return
	 */
	@Nonnull
	public SimpleColumnDef< ? > getColumn(final int ix) {
		return m_columnList.get(ix);
	}

	/**
	 * Return the #of columns in this renderer.
	 * @return
	 */
	public int getColumnCount() {
		return m_columnList.size();
	}

	/**
	 * Find a column by the property it is displaying. This only works for that kind of columns, and will
	 * not work for any joined columns defined from metadata. If no column exists for the specified property
	 * this will throw an exception.
	 * @param propertyName
	 * @return
	 */
	@Nonnull
	public SimpleColumnDef< ? > getColumnByName(String propertyName) {
		for(SimpleColumnDef< ? > scd : m_columnList) {
			if(propertyName.equals(scd.getPropertyName()))
				return scd;
		}
		throw new ProgrammerErrorException("The property with the name '" + propertyName + "' is undefined in this RowRenderer - perhaps metadata has changed?");
	}

	/**
	 * Quickly set the widths of all columns in the same order as defined.
	 * @param widths
	 */
	public void setColumnWidths(final String... widths) {
		check();
		int ix = 0;
		for(final String s : widths) {
			getColumn(ix++).setWidth(s);
		}
	}

	/**
	 * Convenience method to set the column width; replacement for getColumn(index).setWidth().
	 * @param index
	 * @param width
	 */
	public void setColumnWidth(final int index, final String width) {
		check();
		getColumn(index).setWidth(width);
	}

	/**
	 * Convenience method to set the column's cell renderer; replacement for getColumn(index).setRenderer().
	 * @param index
	 * @param renderer
	 */
	public <T> void setNodeRenderer(final int index, @Nullable final IRenderInto<T> renderer) {
		check();
		((SimpleColumnDef<T>) getColumn(index)).setContentRenderer(renderer);
	}

	/**
	 * Convenience method to get the column's cell renderer; replacement for getColumn(index).getRenderer().
	 * @param index
	 * @return
	 */
	public IRenderInto<?> getNodeRenderer(final int index) {
		return getColumn(index).getContentRenderer();
	}


	/**
	 * When set each row will be selectable (will react when the mouse hovers over it), and when clicked will call this handler.
	 * @return
	 */
	@Override
	@Nullable
	public ICellClicked<T> getRowClicked() {
		return m_rowClicked;
	}

	/**
	 * When set each row will be selectable (will react when the mouse hovers over it), and when clicked will call this handler.
	 * @param rowClicked
	 */
	@Override
	public void setRowClicked(@Nullable final ICellClicked<T> rowClicked) {
		m_rowClicked = rowClicked;
	}

	/**
	 * Get the cell clicked handler for the specified column. Convenience method for getColumn(col).getCellClicked().
	 * @param col
	 * @return
	 */
	@Nullable
	public ICellClicked<?> getCellClicked(final int col) {
		return getColumn(col).getCellClicked();
	}

	/**
	 * Set the cell clicked handler for the specified column. Convenience method for getColumn(col).setCellClicked().
	 * @param col
	 * @param cellClicked
	 */
	@Override
	public void setCellClicked(final int col, @Nullable final ICellClicked<T> cellClicked) {
		((SimpleColumnDef<T>) getColumn(col)).setCellClicked(cellClicked);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Actual rendering: the header.						*/
	/*--------------------------------------------------------------*/
	/**
	 * Render the header for a table, using the simple column metadata provided. This renders a rich
	 * header, containing column labels, sort boxes and the like.
	 *
	 */
	@Override
	public void renderHeader(@Nonnull final TableModelTableBase<T> tbl, @Nonnull final HeaderContainer<T> cc) throws Exception {

		m_sortImages = new Img[m_columnList.size()];
		int ix = 0;
		final boolean sortablemodel = tbl.getModel() instanceof ISortableTableModel;
		StringBuilder sb = new StringBuilder();

		for(final SimpleColumnDef< ? > cd : m_columnList) {
			TH th;
			String label = cd.getColumnLabel();
			if(!cd.getSortable().isSortable() || !sortablemodel) {
				//-- Just add the label, if present,
				th = cc.add(label);
			} else {
				//in order to apply correct positioning, we need to wrap Span around sort indicator image and label
				final Div cellSpan = new Div();
				cellSpan.setCssClass("ui-sortable");
				th = cc.add(cellSpan);
				th.setCssClass("ui-sortable");

				//-- Add the sort order indicator: a single image containing either ^, v or both.
				final Img img = new Img();
				cellSpan.add(img);

				if(cd == getSortColumn()) {
					img.setSrc(m_sortDescending ? "THEME/sort-desc.png" : "THEME/sort-asc.png");
				} else {
					img.setSrc("THEME/sort-none.png");
				}
				m_sortImages[ix] = img;

				// Add the label;
				if(label == null || label.trim().length() == 0)
					label = getUnknownColumnCaption();
				cellSpan.add(new Span(label));
				final SimpleColumnDef< ? > scd = cd;
				th.setClicked(new IClicked<TH>() {
					@Override
					public void clicked(final @Nonnull TH b) throws Exception {
						handleSortClick(b, scd);
					}
				});

				//-- Experimental: set a calculated test ID
				String lbl = cd.getPropertyName();
				if(null == lbl)
					lbl = label;
				if(null == lbl || lbl.length() == 0)
					lbl = Integer.toString(m_columnList.indexOf(cd));
				th.setCalculcatedId("H-" + lbl, tbl.calcTestID());
			}
			if(cd.getHeaderCssClass() != null) {
				sb.setLength(0);
				if(th.getCssClass() != null) {
					sb.append(th.getCssClass());
					sb.append(' ');
				}
				sb.append(cd.getHeaderCssClass());
				th.setCssClass(sb.toString());
			}

			th.setWidth(cd.getWidth());
			ix++;
		}

		if(getRowButtonFactory() != null)
			cc.add("");
	}

	void handleSortClick(@Nonnull final NodeBase nb, @Nonnull final SimpleColumnDef< ? > scd) throws Exception {
		//-- 1. Is this the same as the "current" sort column? If so toggle the sort order only.
		SimpleColumnDef< ? > sortColumn = getSortColumn();
		if(scd == sortColumn) {
			m_sortDescending = !m_sortDescending;
		} else {
			if(sortColumn != null)
				updateSortImage(sortColumn, "THEME/sort-none.png");

			m_columnList.setSortColumn(scd);			 // Set the new sort column
			m_sortDescending = scd.getSortable() == SortableType.SORTABLE_DESC;
		}
		updateSortImage(scd, isSortDescending() ? "THEME/sort-desc.png" : "THEME/sort-asc.png");

		//-- Tell the model to sort.
		DataTable<T> parent = nb.getParent(DataTable.class);
		sortModel(parent, scd);
	}

	private void updateSortImage(@Nonnull final SimpleColumnDef< ? > scd, @Nonnull final String img) {
		final int index = m_columnList.indexOf(scd);
		if(index == -1)
			throw new IllegalStateException("?? Cannot find sort column!?");
		Img sortimg = m_sortImages[index];
		if(null == sortimg) {
			//-- The code has decided this was not sortable after all, even though the column was marked as such. Ignore updating the sort image. Fixes 44131.
			return;
		}
		sortimg.setSrc(img);
	}

	/**
	 * This gets called by the data table component just before it starts to render
	 * a new page. When called the query has not yet been done and nothing is rendered
	 * for this object. This exposes the actual model that will be used during the rendering
	 * process and allows this component to define sorting, if needed.
	 */
	@Override
	public void beforeQuery(@Nonnull final TableModelTableBase<T> tbl) throws Exception {
		complete(tbl);
		if(!(tbl.getModel() instanceof ISortableTableModel)) {
			//			m_sortableModel = false;
			return;
		}

		//		m_sortableModel = true;
		SimpleColumnDef< ? > scol = getSortColumn();
		if(scol == null)
			return;

		//-- Tell the model to sort.
		sortModel(tbl, scol);
	}

	private void sortModel(@Nonnull TableModelTableBase<T> tbl, SimpleColumnDef<?> scol) throws Exception {
		ISortHelper<T> sortHelper = (ISortHelper<T>) scol.getSortHelper();
		if(sortHelper != null) {
			sortHelper.adjustSort(tbl.getModel(), isSortDescending());
		} else {
			final ISortableTableModel stm = (ISortableTableModel) tbl.getModel();
			stm.sortOn(scol.getPropertyName(), isSortDescending());
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Actual rendering: a row.							*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 */
	@Override
	public void renderRow(@Nonnull final TableModelTableBase<T> tbl, @Nonnull final ColumnContainer<T> cc, final int index, @Nonnull final T instance) throws Exception {
		for(final SimpleColumnDef< ? > cd : m_columnList) {
			renderColumn(tbl, cc, index, instance, cd);
		}

		//-- If a button factory is attached give it the opportunity to add buttons.
		IRowButtonFactory<T> rbf = getRowButtonFactory();
		if(rbf != null) {
			TD td = cc.add((NodeBase) null);
			td.setNowrap(true);
			cc.getRowButtonContainer().setContainer(td);
			rbf.addButtonsFor(cc.getRowButtonContainer(), instance);
		}

		//-- Toggle odd/even indicator
		if((index & 1) == 0) {
			cc.getTR().removeCssClass("ui-odd");
			cc.getTR().addCssClass("ui-even");
		} else {
			cc.getTR().removeCssClass("ui-even");
			cc.getTR().addCssClass("ui-odd");
		}

		IRowRendered<T> rr = getOnRowRendered();
		if(null != rr) {
			rr.rowRendered(cc.getTR(), instance);
		}
	}

	/**
	 * Render a single column fully.
	 * @param tbl
	 * @param cc
	 * @param index
	 * @param instance
	 * @param cd
	 * @throws Exception
	 */
	protected <X> void renderColumn(@Nonnull final TableModelTableBase<T> tbl, @Nonnull final ColumnContainer<T> cc, final int index, @Nonnull final T instance, @Nonnull final SimpleColumnDef<X> cd)
		throws Exception {
		//-- If a value transformer is known get the column value, else just use the instance itself (case when Renderer is used)
		X colval;
		IValueTransformer<X> valueTransformer = cd.getValueTransformer();
		if(valueTransformer == null)
			colval = (X) instance;
		else
			colval = valueTransformer.getValue(instance);

		//-- Is a node renderer used?
		TD cell;
		String cssClass = cd.getCssClass();
		IRenderInto<X> contentRenderer = cd.getContentRenderer();
		if(null != contentRenderer) {
			cell = cc.add((NodeBase) null); 					// Add the new row
			if(cssClass != null)
				cell.addCssClass(cssClass);
			contentRenderer.renderOpt(cell, colval);
			//contentRenderer.renderOpt(tbl, cell, colval, instance);
		} else {
			String s;
			if(colval == null)
				s = null;
			else {
				IObjectToStringConverter< ? > presentationConverter = cd.getPresentationConverter();
				if(presentationConverter != null)
					s = ((IConverter<X>) presentationConverter).convertObjectToString(NlsContext.getLocale(), colval);
				else
					s = String.valueOf(colval);
			}

			if(s == null)
				cell = cc.add((NodeBase) null);
			else
				cell = cc.add(s);
			if(cssClass != null)
				cell.addCssClass(cssClass);
		}
		Boolean nw = cd.isNowrap();
		if((nw == null && cd.getDisplayLength() == 0) || (nw != null && nw.booleanValue()))
			cell.setNowrap(true);

		//-- If a cellclicked thing is present attach it to the td
		final ICellClicked< ? > cellClicked = cd.getCellClicked();
		if(cellClicked != null) {
			/*
			 * FIXME For now I add a separate instance of the handler to every cell. A single instance is OK too,
			 * provided it can calculate the row and cell data from the TR it is attached to.
			 */
			cell.setClicked(new IClicked<TD>() {
				@Override
				public void clicked(final @Nonnull TD b) throws Exception {
					ICellClicked<Object> clicked = (ICellClicked<Object>) cd.getCellClicked();
					if(null != clicked)
						clicked.cellClicked(instance);
				}
			});
			cell.addCssClass("ui-cellsel");
		}

		if(cd.getAlign() != null)
			cell.setTextAlign(cd.getAlign());
		else if(cssClass != null) {
			cell.addCssClass(cssClass);
		}

		//-- Assign a testID
		String label = cd.getColumnLabel();
		String lbl = cd.getPropertyName();
		if(null == lbl)
			lbl = label;
		if(null == lbl || lbl.length() == 0)
			lbl = Integer.toString(m_columnList.indexOf(cd));
		cell.setCalculcatedId("C-" + lbl, tbl.calcTestID());
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Setters and getters.								*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @return
	 */
	@Nullable
	public IRowButtonFactory<T> getRowButtonFactory() {
		return m_rowButtonFactory;
	}

	public void setRowButtonFactory(@Nullable IRowButtonFactory<T> rowButtonFactory) {
		m_rowButtonFactory = rowButtonFactory;
	}

	public void setUnknownColumnCaption(@Nullable String unknownColumnCaption) {
		m_unknownColumnCaption = unknownColumnCaption;
	}

	@Nonnull
	public String getUnknownColumnCaption() {
		return m_unknownColumnCaption == null ? "" : m_unknownColumnCaption;
	}

	/**
	 * Sets a handler that gets called every time a row is rendered.
	 * @return
	 */
	@Nullable
	public IRowRendered<T> getOnRowRendered() {
		return m_onRowRendered;
	}

	public void setOnRowRendered(@Nullable IRowRendered<T> onRowRendered) {
		m_onRowRendered = onRowRendered;
	}


}
