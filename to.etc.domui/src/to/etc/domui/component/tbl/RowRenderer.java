package to.etc.domui.component.tbl;

import javax.annotation.*;

import to.etc.domui.component.controlfactory.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.component.ntbl.*;
import to.etc.domui.component.tbl.AbstractRowRenderer.IRowRendered;
import to.etc.domui.databinding.observables.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.util.*;
import to.etc.util.*;
import to.etc.webapp.*;
import to.etc.webapp.annotations.*;

/**
 * This is the type-safe replacement for the other row renderers which are now deprecated.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 11, 2013
 */
final public class RowRenderer<T> implements IClickableRowRenderer<T> {
	/** The class whose instances we'll render in this table. */
	@Nonnull
	private final Class<T> m_dataClass;

	@Nonnull
	final private ClassMetaModel m_metaModel;

	/** When the definition has completed (the object is used) this is TRUE; it disables all calls that change the definition */
	private boolean m_completed;

	@Nonnull
	private final ColumnList<T> m_columnList;

	@Nullable
	private Img[] m_sortImages;

	@Nullable
	private ICellClicked< ? > m_rowClicked;

	@Nullable
	private IRowButtonFactory<T> m_rowButtonFactory;

	@Nullable
	private IRowRendered<T> m_onRowRendered;

	public RowRenderer(@Nonnull Class<T> data) {
		this(data, MetaManager.findClassMeta(data));
	}

	public RowRenderer(@Nonnull Class<T> data, @Nonnull ClassMetaModel cmm) {
		m_dataClass = data;
		m_metaModel = cmm;
		m_columnList = new ColumnList<T>(data, m_metaModel);
	}

	/**
	 * Throws an exception if this renderer has been completed and is unmutable.
	 */
	private void check() {
		if(m_completed)
			throw new IllegalStateException("Programmer error: This object has been USED and cannot be changed anymore");
	}

	/**
	 * Complete this object if it is not already complete (internal).
	 */
	private void complete(@Nonnull TableModelTableBase<T> tbl) {
		if(isComplete())
			return;

		//-- If we have no columns at all we use a default column list.
		if(getColumnList().size() == 0)
			addDefaultColumns();

		//-- If we have not yet a default sortable column but the model has it - use the model's one.
		if(getSortColumn() == null) {
			String dsp = model().getDefaultSortProperty();
			getColumnList().setDefaultSortColumn(dsp);
		}

		getColumnList().assignPercentages();				// Calculate widths
		m_completed = true;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Actual rendering: the header.						*/
	/*--------------------------------------------------------------*/
	/**
	 * Render the header for a table, using the simple column metadata provided. This renders a rich
	 * header, containing column labels, sort boxes and the like.
	 *
	 * @see to.etc.domui.component.tbl.IRowRenderer#renderHeader(to.etc.domui.component.tbl.HeaderContainer)
	 */
	@Override
	public void renderHeader(@Nonnull final TableModelTableBase<T> tbl, @Nonnull final HeaderContainer<T> cc) throws Exception {
		Img[] sortImages = m_sortImages = new Img[m_columnList.size()];
		int ix = 0;
		final boolean sortablemodel = tbl.getModel() instanceof ISortableTableModel;
		StringBuilder sb = new StringBuilder();

		for(final ColumnDef< ? > cd : m_columnList) {
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
					img.setSrc(m_columnList.isSortDescending() ? "THEME/sort-desc.png" : "THEME/sort-asc.png");
				} else {
					img.setSrc("THEME/sort-none.png");
				}
				sortImages[ix] = img;

				// Add the label;
				if(!StringTool.isBlank(label))
					cellSpan.add(new Span(label));
				final ColumnDef< ? > scd = cd;
				th.setClicked(new IClicked<TH>() {
					@Override
					public void clicked(final @Nonnull TH b) throws Exception {
						handleSortClick(b, scd);
					}
				});

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

	private void handleSortClick(@Nonnull final NodeBase nb, @Nonnull final ColumnDef< ? > scd) throws Exception {
		//-- 1. Is this the same as the "current" sort column? If so toggle the sort order only.
		ColumnDef< ? > sortColumn = getSortColumn();
		if(scd == sortColumn) {
			setSortDescending(!isSortDescending());
		} else {
			if(sortColumn != null)
				updateSortImage(sortColumn, "THEME/sort-none.png");

			m_columnList.setSortColumn(scd, scd.getSortable());			 // Set the new sort column
		}
		updateSortImage(scd, isSortDescending() ? "THEME/sort-desc.png" : "THEME/sort-asc.png");

		//-- Tell the model to sort.
		DataTable< ? > parent = nb.getParent(DataTable.class);
		if(scd.getSortHelper() != null) {
			//-- A sort helper is needed.
			final IProgrammableSortableModel stm = (IProgrammableSortableModel) parent.getModel();
			stm.sortOn(scd.getSortHelper(), isSortDescending());
		} else {
			final ISortableTableModel stm = (ISortableTableModel) parent.getModel();
			stm.sortOn(scd.getPropertyName(), isSortDescending());
		}
	}

	private void updateSortImage(@Nonnull final ColumnDef< ? > scd, @Nonnull final String img) {
		Img[] sortImages = m_sortImages;
		if(sortImages == null)
			return;
		final int index = m_columnList.indexOf(scd);
		if(index == -1)
			throw new IllegalStateException("?? Cannot find sort column!?");
		sortImages[index].setSrc(img);
	}

	/**
	 * This gets called by the data table component just before it starts to render
	 * a new page. When called the query has not yet been done and nothing is rendered
	 * for this object. This exposes the actual model that will be used during the rendering
	 * process and allows this component to define sorting, if needed.
	 *
	 * @see to.etc.domui.component.tbl.IRowRenderer#beforeQuery(to.etc.domui.component.tbl.DataTable)
	 */
	@Override
	public void beforeQuery(@Nonnull final TableModelTableBase<T> tbl) throws Exception {
		complete(tbl);
		if(!(tbl.getModel() instanceof ISortableTableModel)) {
			//			m_sortableModel = false;
			return;
		}

		//		m_sortableModel = true;
		ColumnDef< ? > scol = getSortColumn();
		if(scol == null)
			return;

		//-- Tell the model to sort.
		if(scol.getSortHelper() != null) {
			//-- A sort helper is needed.
			final IProgrammableSortableModel stm = (IProgrammableSortableModel) tbl.getModel();
			stm.sortOn(scol.getSortHelper(), isSortDescending());
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
	 * @see to.etc.domui.component.tbl.IRowRenderer#renderRow(to.etc.domui.component.tbl.ColumnContainer, int, java.lang.Object)
	 */
	@Override
	public void renderRow(@Nonnull final TableModelTableBase<T> tbl, @Nonnull final ColumnContainer<T> cc, final int index, @Nonnull final T instance) throws Exception {
		for(final ColumnDef< ? > cd : m_columnList) {
			renderColumn(tbl, cc, index, instance, cd);
		}

		//-- If a button factory is attached give it the opportunity to add buttons.
		IRowButtonFactory<T> rbf = getRowButtonFactory();
		if(rbf != null) {
			TD td = cc.add((NodeBase) null);
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
	protected <X> void renderColumn(@Nonnull final TableModelTableBase<T> tbl, @Nonnull final ColumnContainer<T> cc, final int index, @Nonnull final T instance, @Nonnull final ColumnDef<X> cd) throws Exception {
		TD cell = cc.add((NodeBase) null);
		String cssClass = cd.getCssClass();
		if(cssClass != null)
			cell.addCssClass(cssClass);

		if(cd.isNowrap())
			cell.setNowrap(true);

		//-- If a cell clicked thing is present attach it to the td
		if(cd.getCellClicked() != null) {
			/*
			 * FIXME For now I add a separate instance of the handler to every cell. A single instance is OK too,
			 * provided it can calculate the row and cell data from the TR it is attached to.
			 */
			cell.setClicked(new IClicked<TD>() {
				@Override
				public void clicked(final @Nonnull TD b) throws Exception {
					ICellClicked<Object> clicked = (ICellClicked<Object>) cd.getCellClicked();
					if(null != clicked)
						clicked.cellClicked(b, instance);
				}
			});
			cell.addCssClass("ui-cellsel");
		}

		if(cd.getAlign() != null)
			cell.setTextAlign(cd.getAlign());
		else if(cssClass != null) {
			cell.addCssClass(cssClass);
		}

		//-- Render the value, in whatever way

		//-- Start rendering.
		INodeContentRenderer<X> contentRenderer = cd.getContentRenderer();
		if(null != contentRenderer) {
			if(cd.isEditable()) {
				throw new IllegalStateException("A column cannot be editable if you assign your own renderer to it: handle the editing inside the renderer yourself.");
			}

			//-- Using a content renderer means you need to take care of everything related to rendering yourself.
			X colval = cd.getColumnValue(instance);
			contentRenderer.renderNodeContent(tbl, cell, colval, instance);
		} else if(cd.isEditable()) {
			renderEditable(tbl, cd, cell, instance);
		} else if(instance instanceof IObservableEntity) {
			PropertyMetaModel<X> pmm = cd.getPropertyMetaModel();
			if(null == pmm)
				throw new IllegalStateException("Cannot render display value for row type of " + cd.getColumnLabel());
			DisplaySpan<X> dv = new DisplaySpan<X>(cd.getActualClass());
			cell.getBindingContext().unibind(instance, pmm.getName(), dv, "value");
			cell.add(dv);
		} else {
			X value = cd.getColumnValue(instance);
			if(null != value) {
				cell.add(new DisplaySpan<X>(value));
			}
		}
	}

	/**
	 * Render an editable component for the thingy, and bind it to the item value.
	 * @param tbl
	 * @param cell
	 * @param instance
	 */
	private <X, C extends NodeBase & IControl<X>> void renderEditable(@Nonnull TableModelTableBase<T> tbl, @Nonnull ColumnDef<X> cd, @Nonnull TD cell, @Nonnull T instance) throws Exception {
		if(!(instance instanceof IObservableEntity))
			throw new IllegalStateException("The instance type " + instance.getClass().getName() + "' is not an Observable entity; I need one to be able to bind to it's properties");
		PropertyMetaModel<X> pmm = cd.getPropertyMetaModel();
		if(null == pmm)
			throw new IllegalStateException("Cannot render edit value for row type");

		IControlFactory<X> cf = cd.getControlFactory();
		C control;
		if(cf != null) {
			control = (C) cf.createControl(pmm);

			//FIXME We need to bind the control to the INSTANCE if that was passed 8-/

		} else {
			ControlBuilder cb = DomApplication.get().getControlBuilder();
			control = cb.createControlFor(pmm, true, null).getFormControl();
		}
		cell.add(control);
		cell.getBindingContext().joinbinding(instance, pmm.getName(), control, "value");
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Setters and getters.								*/
	/*--------------------------------------------------------------*/

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

	/**
	 * Add all of the columns as defined by the metadata to the list.
	 */
	public void addDefaultColumns() {
		getColumnList().addDefaultColumns();
	}

	@Nonnull
	private ColumnList<T> getColumnList() {
		return m_columnList;
	}

	/**
	 * Sets default sort column on row renderer. Overrides property meta model setting if such defines default sort.
	 * @param cd
	 * @param type
	 */
	public void setDefaultSort(@Nonnull ColumnDef< ? > cd, @Nonnull SortableType type) {
		getColumnList().setSortColumn(cd, type);
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
	protected Class< ? > getActualClass() {
		return m_dataClass;
	}

	/**
	 * Check if this object is used (completed) and thereby unmodifyable (internal).
	 * @return
	 */
	protected boolean isComplete() {
		return m_completed;
	}

	protected void setSortColumn(@Nullable ColumnDef< ? > cd, @Nullable SortableType type) {
		m_columnList.setSortColumn(cd, type);
	}

	@Nullable
	protected ColumnDef< ? > getSortColumn() {
		return m_columnList.getSortColumn();
	}

	protected boolean isSortDescending() {
		return m_columnList.isSortDescending();
	}

	protected void setSortDescending(boolean desc) {
		m_columnList.setSortDescending(desc);
	}

	/**
	 * Return the definition for the nth column. You can change the column's definition there.
	 * @param ix
	 * @return
	 */
	@Nonnull
	public ColumnDef< ? > getColumn(final int ix) {
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
	public ColumnDef< ? > getColumnByName(String propertyName) {
		for(ColumnDef< ? > scd : m_columnList) {
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
	public <T> void setNodeRenderer(final int index, @Nullable final INodeContentRenderer<T> renderer) {
		check();
		((ColumnDef<T>) getColumn(index)).setContentRenderer(renderer);
	}

	/**
	 * Convenience method to get the column's cell renderer; replacement for getColumn(index).getRenderer().
	 * @param index
	 * @return
	 */
	public INodeContentRenderer< ? > getNodeRenderer(final int index) {
		return getColumn(index).getContentRenderer();
	}


	/**
	 * When set each row will be selectable (will react when the mouse hovers over it), and when clicked will call this handler.
	 * @return
	 */
	@Override
	@Nullable
	public ICellClicked< ? > getRowClicked() {
		return m_rowClicked;
	}

	/**
	 * When set each row will be selectable (will react when the mouse hovers over it), and when clicked will call this handler.
	 * @param rowClicked
	 */
	@Override
	public <V> void setRowClicked(@Nullable final ICellClicked<V> rowClicked) {
		m_rowClicked = rowClicked;
	}

	/**
	 * Get the cell clicked handler for the specified column. Convenience method for getColumn(col).getCellClicked().
	 * @param col
	 * @return
	 */
	@Nullable
	public ICellClicked< ? > getCellClicked(final int col) {
		return getColumn(col).getCellClicked();
	}

	/**
	 * Set the cell clicked handler for the specified column. Convenience method for getColumn(col).setCellClicked().
	 * @param col
	 * @param cellClicked
	 */
	@Override
	public <T> void setCellClicked(final int col, @Nullable final ICellClicked<T> cellClicked) {
		((ColumnDef<T>) getColumn(col)).setCellClicked(cellClicked);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Typesafe definition delegates.						*/
	/*--------------------------------------------------------------*/
	/**
	 * Add and return the column definition for a column on the specified property. Because Java still has no
	 * first-class properties (sigh) you need to pass in the property's type to get a typeful column. If you
	 * do not need a typeful column use {@link #column(String)}.
	 * @param type
	 * @param property
	 * @return
	 */
	@Nonnull
	public <T> ColumnDef<T> column(@Nonnull Class<T> type, @Nonnull @GProperty String property) {
		return getColumnList().column(type, property);
	}

	/**
	 * This adds a column on the specified property, but has no idea about the real type. It can be used as long
	 * as that type is not needed.
	 * @param property
	 * @return
	 */
	@Nonnull
	public ColumnDef< ? > column(@Nonnull String property) {
		return getColumnList().column(property);
	}

	/**
	 * Add a column which gets referred the row element instead of a column element. This is normally used together with
	 * @return
	 */
	@Nonnull
	public ColumnDef<T> column() {
		return getColumnList().column();
	}
}
