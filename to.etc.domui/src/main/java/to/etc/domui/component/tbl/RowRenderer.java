package to.etc.domui.component.tbl;

import kotlin.reflect.KProperty1;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.SortableType;
import to.etc.domui.component.misc.DisplaySpan;
import to.etc.domui.component.ntbl.IRowButtonFactory;
import to.etc.domui.component2.controlfactory.ControlCreatorRegistry;
import to.etc.domui.converter.ConverterRegistry;
import to.etc.domui.converter.IConverter;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.Img;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.Span;
import to.etc.domui.dom.html.TD;
import to.etc.domui.dom.html.TH;
import to.etc.domui.dom.html.TR;
import to.etc.domui.server.DomApplication;
import to.etc.domui.server.RequestContextImpl;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.IRenderInto;
import to.etc.util.StringTool;
import to.etc.webapp.ProgrammerErrorException;
import to.etc.webapp.annotations.GProperty;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * This is the type-safe replacement for the other row renderers which are now deprecated.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 11, 2013
 */
@DefaultNonNull final public class RowRenderer<T> implements IClickableRowRenderer<T> {
	/** Used in DomApplication.setAttribute to set a generic {@link IColumnListener} for all pages. */
	static public final String COLUMN_LISTENER = RowRenderer.class.getCanonicalName() + ".rowl";

	/** The class whose instances we'll render in this table. */
	@Nonnull
	private final Class<T> m_dataClass;

	@Nonnull
	final private ClassMetaModel m_metaModel;

	@Nullable
	private IRowRenderHelper<T> m_helper;

	/** When the definition has completed (the object is used) this is TRUE; it disables all calls that change the definition */
	private boolean m_completed;

	@Nonnull
	private final ColumnList<T> m_columnList;

	@Nullable
	private Img[] m_sortImages;

	@Nullable
	private ICellClicked<T> m_rowClicked;

	@Nullable
	private IRowButtonFactory<T> m_rowButtonFactory;

	@Nullable
	private TableModelTableBase<T> m_tableModelTable;

	@Nullable
	private ITableModel<T> m_lastSortedModel;

	@Nullable
	private ColumnDef<T, ?> m_lastSortedColumn;

	@Nullable
	private Boolean m_lastSortedDirection;

	private Map<ColumnDef<T, ?>, TH> m_columnByThIdMap = new HashMap<>();

	@Nonnull
	private List<TableHeader> m_tableHeaderBeforeList = Collections.EMPTY_LIST;

	@Nonnull
	private List<TableHeader> m_tableHeaderAfterList = Collections.EMPTY_LIST;

	private List<IRowRendered<T>> m_renderListener = new ArrayList<>();

	/** The factor to multiply the #of characters with to get the real em width of a column. */
	private double m_emFactor = 0.65;

	@Nullable
	private IColumnListener<T> m_columnListener;

	public interface IColumnListener<T> {
		void columnsChanged(TableModelTableBase<T> tbl, List<ColumnWidth<T, ?>> newWidths) throws Exception;
	}

	public interface IRowRendered<T> {
		void rowRendered(@Nonnull TR row, @Nonnull T instance);
	}

	public RowRenderer(@Nonnull Class<T> data) {
		this(data, MetaManager.findClassMeta(data));
	}

	public RowRenderer(@Nonnull Class<T> data, @Nonnull ClassMetaModel cmm) {
		m_dataClass = data;
		m_metaModel = cmm;
		m_columnList = new ColumnList<T>(data, m_metaModel);
	}

	/**
	 * Throws an exception if this renderer has been completed and is immutable.
	 */
	private void check() {
		if(m_completed)
			throw new IllegalStateException("Programmer error: This object has been USED and cannot be changed anymore");
	}

	public RowRenderer<T> helper(IRowRenderHelper<T> helper) {
		m_helper = helper;
		return this;
	}

	/**
	 * Complete this object if it is not already complete (internal).
	 */
	private void complete(@Nonnull TableModelTableBase<T> tbl) {
		if(isComplete())
			return;
		m_tableModelTable = tbl;

		//-- If we have no columns at all we use a default column list.
		if(getColumnList().size() == 0)
			addDefaultColumns();

		//-- If we have not yet a default sortable column but the model has it - use the model's one.
		if(getSortColumn() == null) {
			String dsp = model().getDefaultSortProperty();
			getColumnList().setDefaultSortColumn(dsp);
		}
		ColumnDef<T, ?> column = getSortColumn();
		if(null != column) {
			setSortDescending(column.getSortable() == SortableType.SORTABLE_DESC);
		}

		String width = tbl.getWidth();
		boolean fullWidth = width != null && width.contains("100%");
		m_completed = true;
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
		for(TableHeader h : m_tableHeaderBeforeList)
			cc.addHeader(false, h);
		for(TableHeader h : m_tableHeaderAfterList)
			cc.addHeader(true, h);

		Img[] sortImages = m_sortImages = new Img[m_columnList.size()];
		int ix = 0;
		final boolean sortablemodel = tbl.getModel() instanceof ISortableTableModel;
		StringBuilder sb = new StringBuilder();

		Map<ColumnDef<T, ?>, String> widthMap = calculateWidths(tbl);

		m_columnByThIdMap.clear();
		for(final ColumnDef<T, ?> cd : m_columnList) {
			TH th;
			String label = cd.getColumnLabel();
			if(!cd.getSortable().isSortable() || !sortablemodel) {
				//-- Just add the span with label, if present. Span is needed to allow styling.
				th = cc.add(new Span(label));
			} else {
				//in order to apply correct positioning, we need to wrap Span around sort indicator image and label
				final Div cellSpan = new Div();
				cellSpan.setCssClass("ui-sortable");
				th = cc.add(cellSpan);
				th.addCssClass("ui-sortable");

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
				final ColumnDef<T, ?> scd = cd;
				th.setClicked((IClicked<TH>) b -> handleSortClick(b, scd));
			}

			String cssClass = cd.getCssClass();
			if(cssClass != null)
				th.addCssClass(cssClass);
			applyNumericCssClass(th, cd);

			th.setWidth(widthMap.get(cd));
			if(cd.isNowrap())
				th.setNowrap(true);

			m_columnByThIdMap.put(cd, th);
			ix++;
		}

		if(getRowButtonFactory() != null)
			cc.add("");
	}

	/**
	 * This calculates the widths.
	 */
	private Map<ColumnDef<T, ?>, String> calculateWidths(TableModelTableBase<T> tbl) {
		Map<ColumnDef<T, ?>, String> map = new HashMap<>();

		String width = tbl.getWidth();
		boolean fullWidth = width != null && width.contains("100%");

		//-- 1. If any width is set with width(String) then we only use that.
		boolean hasAssignedWidth = m_columnList.stream().anyMatch(a -> !StringTool.isBlank(a.getWidth()));
		if(hasAssignedWidth) {
			//-- Just copy all widths.
			m_columnList.forEach(a -> map.put(a, a.getWidth()));
			return map;
		}

		//-- No assignments: use character widths from metadata
		for(int i = 0; i < m_columnList.size(); i++) {
			if(i < m_columnList.size() - 1 || !fullWidth) {
				/*
				 * We skip the last column if we have 100% so that the last column takes the remaining space.
				 * In the other case the real width of the table will be calculated by the columns.
				 */
				ColumnDef<T, ?> cd = m_columnList.get(i);
				int ch = cd.getCharacterWidth();
				if(ch < 0)
					ch = 10;
				long adj = Math.round(ch * getEmFactor() + 1.0);
				map.put(cd, adj + "em");
			}
		}
		return map;
	}

	private void handleSortClick(@Nonnull final NodeBase nb, @Nonnull final ColumnDef<T, ?> scd) throws Exception {
		//-- 1. Is this the same as the "current" sort column? If so toggle the sort order only.
		ColumnDef<T, ?> sortColumn = getSortColumn();
		if(scd == sortColumn) {
			setSortDescending(!isSortDescending());
		} else {
			if(sortColumn != null)
				updateSortImage(sortColumn, "THEME/sort-none.png");

			m_columnList.setSortColumn(scd, scd.getSortable());             // Set the new sort column
		}
		updateSortImage(scd, isSortDescending() ? "THEME/sort-desc.png" : "THEME/sort-asc.png");

		//-- Tell the model to sort.
		TableModelTableBase<T> parent = m_tableModelTable;
		if(null == parent)
			throw new IllegalStateException("Table not defined");

		resort(scd, parent);
	}

	private boolean hasSortChanged(@Nonnull ColumnDef<T, ?> newColumn, @Nonnull TableModelTableBase<T> tableComponent) {
		if(newColumn != m_lastSortedColumn)
			return true;
		ITableModel<T> newModel = tableComponent.getModel();
		if(newModel != m_lastSortedModel)
			return true;
		Boolean direction = m_lastSortedDirection;
		if(direction == null)
			return true;
		return direction.booleanValue() != isSortDescending();
	}

	private void resort(@Nonnull ColumnDef<T, ?> scd, TableModelTableBase<T> parent) throws Exception {
		if(!hasSortChanged(scd, parent))
			return;

		ISortHelper<T> sortHelper = (ISortHelper<T>) scd.getSortHelper();
		if(sortHelper != null) {
			sortHelper.adjustSort(parent.getModel(), isSortDescending());    // Tell the helper to sort
		} else {
			final ISortableTableModel stm = (ISortableTableModel) parent.getModel();
			String propertyName = scd.getSortProperty();
			if(null == propertyName)
				propertyName = scd.getPropertyName();

			stm.sortOn(propertyName, isSortDescending());
		}
		m_lastSortedDirection = Boolean.valueOf(isSortDescending());
		m_lastSortedModel = parent.getModel();
		m_lastSortedColumn = scd;
	}

	private void updateSortImage(@Nonnull final ColumnDef<T, ?> scd, @Nonnull final String img) {
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
	 */
	@Override
	public void beforeQuery(@Nonnull final TableModelTableBase<T> tbl) throws Exception {
		complete(tbl);
		if(!(tbl.getModel() instanceof ISortableTableModel)) {
			return;
		}

		ColumnDef<T, ?> scol = getSortColumn();
		if(scol == null)
			return;

		//-- Tell the model to sort.
		resort(scol, tbl);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Actual rendering: a row.							*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 */
	@Override
	public void renderRow(@Nonnull final TableModelTableBase<T> tbl, @Nonnull final ColumnContainer<T> cc, final int index, @Nonnull final T instance) throws Exception {
		IRowRenderHelper<T> helper = m_helper;
		if(null != helper)
			helper.setRow(instance);

		for(final ColumnDef<T, ?> cd : m_columnList) {
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

		for(IRowRendered<T> rrl : m_renderListener) {
			rrl.rowRendered(cc.getTR(), instance);
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
	protected <X> void renderColumn(@Nonnull final TableModelTableBase<T> tbl, @Nonnull final ColumnContainer<T> cc, final int index, @Nonnull final T instance, @Nonnull final ColumnDef<T, X> cd) throws Exception {
		TD cell = cc.add((NodeBase) null);
		String cssClass = cd.getCssClass();
		if(cssClass != null)
			cell.addCssClass(cssClass);

		if(cd.isNowrap())
			cell.setNowrap(true);

		//-- If a cell clicked thing is present attach it to the td
		if(cd.getCellClicked() != null) {
			Predicate<T> predicate = cd.getShowCellClickedWhen();
			if(null == predicate || predicate.test(instance)) {
				/*
				 * FIXME For now I add a separate instance of the handler to every cell. A single instance is OK too,
				 * provided it can calculate the row and cell data from the TR it is attached to.
				 */
				cell.setClicked((IClicked<TD>) b -> {
					ICellClicked<Object> clicked = (ICellClicked<Object>) cd.getCellClicked();
					if(null != clicked)
						clicked.cellClicked(instance);
				});
				cell.addCssClass("ui-cellsel");
			}
		}

		if(cd.getAlign() != null)
			cell.setTextAlign(cd.getAlign());
		else if(cssClass != null) {
			cell.addCssClass(cssClass);
		}

		//-- Render the value, in whatever way. The value is bound to the model so that updates cause a render.
		IRenderInto<X> contentRenderer = cd.getContentRenderer();
		IConverter<X> cellConverter = cd.getConverter();
		PropertyMetaModel<X> pmm = cd.getPropertyMetaModel();
		if(cd.isEditable() || cd.getControlFactory() != null) {
			if(null != contentRenderer)
				throw new IllegalStateException("A column cannot be editable if you assign your own renderer to it: handle the editing inside the renderer yourself.");
			if(null != cellConverter)
				throw new IllegalStateException("A column cannot be editable if you assign a converter to it: handle the conversion inside the renderer yourself.");
			renderEditable(tbl, cd, cell, instance);
		} else if(pmm != null) {
			//-- Bind the property to a display control.
			IConverter<X> converter = cellConverter;
			if(null == converter) {
				converter = ConverterRegistry.findBestConverter(pmm);
			}
			DisplaySpan<X> ds = new DisplaySpan<>(pmm.getActualType(), null);
			cell.add(ds);
			ds.bind().to(instance, pmm);                    // Bind value to model
			if(null != contentRenderer) {
				// Bind the display control and let it render through the content renderer, enabling binding
				ds.setRenderer(new IRenderInto<X>() {
					/**
					 * Wrap the renderer so we can pass the "instance" to it.
					 */
					@Override
					public void render(@Nonnull NodeContainer node, @Nullable X object) throws Exception {
						contentRenderer.renderOpt(node, object); //, instance);
					}
				});
			} else {
				if(converter != null) {
					ds.setConverter(converter);
				}
			}
			applyNumericCssClass(cell, cd);
		} else if(contentRenderer != null) {
			//-- No property but a content renderer -> let it take care of binding itself as we cannot.
			X value = cd.getColumnValue(instance);
			contentRenderer.renderOpt(cell, value);
		} else {
			throw new IllegalStateException("? Don't know how to render " + cd);
		}
	}

	private void applyNumericCssClass(NodeContainer cell, ColumnDef<T, ?> cd) {
		if(cd.getConverter() != null)
			return;
		PropertyMetaModel<?> pmm = cd.getPropertyMetaModel();
		if(pmm != null && pmm.getConverter() != null)
			return;

		//NumericPresentation np = cd.getNumericPresentation();
		if(! Number.class.isAssignableFrom(DomUtil.getBoxedForPrimitive(cd.getActualClass()))) {
			return;
		}

		cell.addCssClass("ui-numeric");
	}

	/**
	 * Render an editable component for the thingy, and bind it to the item value.
	 * @param tbl
	 * @param cell
	 * @param instance
	 */
	private <X, C extends NodeBase & IControl<X>> void renderEditable(@Nonnull TableModelTableBase<T> tbl, @Nonnull ColumnDef<T, X> cd, @Nonnull TD cell, @Nonnull T instance) throws Exception {
		PropertyMetaModel<X> pmm = cd.getPropertyMetaModel();
		if(null == pmm)
			throw new IllegalStateException("Cannot render edit value for row type");

		IRowControlFactory<T> cf = cd.getControlFactory();
		C control;
		if(cf != null) {
			control = (C) cf.createControl(instance);

			//FIXME We need to bind the control to the INSTANCE if that was passed 8-/

		} else {
			ControlCreatorRegistry reg = DomApplication.get().getControlCreatorRegistry();
			control = reg.createControl(pmm, null);
			//ControlBuilder cb = DomApplication.get().getControlBuilder();
			//control = cb.createControlFor(pmm, true, null).getFormControl();
		}
		control.bind().to(instance, pmm);
		cell.add(control);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Setters and getters.								*/
	/*--------------------------------------------------------------*/

	public double getEmFactor() {
		return m_emFactor;
	}

	/**
	 * The factor to multiply the #of characters with to get the real em width of a column; defaults to 0.6.
	 */
	public RowRenderer<T> emFactor(double factor) {
		m_emFactor = factor;
		return this;
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
	public void setDefaultSort(@Nonnull ColumnDef<T, ?> cd, @Nonnull SortableType type) {
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
	protected Class<?> getActualClass() {
		return m_dataClass;
	}

	/**
	 * Check if this object is used (completed) and thereby unmodifyable (internal).
	 * @return
	 */
	protected boolean isComplete() {
		return m_completed;
	}

	protected void setSortColumn(@Nullable ColumnDef<T, ?> cd, @Nullable SortableType type) {
		m_columnList.setSortColumn(cd, type);
	}

	@Nullable
	protected ColumnDef<T, ?> getSortColumn() {
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
	public ColumnDef<T, ?> getColumn(final int ix) {
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
	public ColumnDef<T, ?> getColumnByName(String propertyName) {
		for(ColumnDef<T, ?> scd : m_columnList) {
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
			getColumn(ix++).width(s);
		}
	}

	/**
	 * Convenience method to set the column width; replacement for getColumn(index).setWidth().
	 * @param index
	 * @param width
	 */
	public void setColumnWidth(final int index, final String width) {
		check();
		getColumn(index).width(width);
	}

	/**
	 * Convenience method to set the column's cell renderer; replacement for getColumn(index).setRenderer().
	 * @param index
	 * @param renderer
	 */
	public <C> void setNodeRenderer(final int index, @Nullable final IRenderInto<C> renderer) {
		check();
		((ColumnDef<T, C>) getColumn(index)).renderer(renderer);
	}

	/**
	 * Convenience method to get the column's cell renderer; replacement for getColumn(index).getRenderer().
	 * @param index
	 * @return
	 */
	@Nullable
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
		getColumn(col).cellClicked(cellClicked);
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
	public <V> ColumnDef<T, V> column(@Nonnull Class<V> type, @Nonnull @GProperty String property) {
		return getColumnList().column(type, property);
	}

	/**
	 * This adds a column on the specified property, but has no idea about the real type. It can be used as long
	 * as that type is not needed.
	 * @param property
	 * @return
	 */
	@Nonnull
	public ColumnDef<T, ?> column(@Nonnull String property) {
		return getColumnList().column(property);
	}

	public <F> ColumnDef<T, F> column(@Nonnull KProperty1<T, F> property) {
		return getColumnList().column(property);
	}

	public <A, B> ColumnDef<T, B> column(@Nonnull KProperty1<T, A> property1, @Nonnull KProperty1<A, B> property2) {
		return getColumnList().column(property1, property2);
	}

	public <A, B, C> ColumnDef<T, C> column(@Nonnull KProperty1<T, A> property1, @Nonnull KProperty1<A, B> property2, @Nonnull KProperty1<B, C> property3) {
		return getColumnList().column(property1, property2, property3);
	}

	//public ColumnDef<T, ?> column(@Nonnull KProperty1<?, ?> property1, @Nonnull KProperty1<?, ?> property2) {
	//	throw new IllegalStateException();
	//	//return getColumnList().column(property1, property2);
	//}

	/**
	 * Add a column which gets referred the row element instead of a column element. This is normally used together with
	 * @return
	 */
	@Nonnull
	public ColumnDef<T, T> column() {
		return getColumnList().column();
	}

	public RowRenderer<T> addRenderListener(@Nonnull IRowRendered<T> listener) {
		m_renderListener.add(listener);
		return this;
	}

	public void addHeaderBefore(@Nonnull TableHeader header) {
		if(m_tableHeaderBeforeList.size() == 0)
			m_tableHeaderBeforeList = new ArrayList<>(2);
		m_tableHeaderBeforeList.add(header);
	}

	public void addHeaderAfter(@Nonnull TableHeader header) {
		if(m_tableHeaderAfterList.size() == 0)
			m_tableHeaderAfterList = new ArrayList<>(2);
		m_tableHeaderAfterList.add(header);
	}

	public final static class ColumnWidth<T, I> {
		private final ColumnDef<T, I> m_column;

		final private int m_index;

		final private String m_width;

		public ColumnWidth(ColumnDef<T, I> column, int index, String width) {
			m_column = column;
			m_index = index;
			m_width = width;
		}

		public ColumnDef<T, I> getColumn() {
			return m_column;
		}

		public int getIndex() {
			return m_index;
		}

		public String getWidth() {
			return m_width;
		}
	}


	/**
	 * Called when the column size has been changed by the user, this stores the new sizes
	 * in the column to be sure they are re-rendered with the same widths when paging
	 * or re-rendering. In addition it calls the store method if configured.
	 *
	 * @param context
	 */
	@Override public void updateWidths(@Nonnull TableModelTableBase<T> tbl, @Nonnull RequestContextImpl context) throws Exception {
		//-- Get the CSS widths for all heads.
		List<ColumnWidth<T, ?>> list = new ArrayList<>();
		int index = 0;
		for(ColumnDef<T, ?> cd : getColumnList()) {
			TH th = m_columnByThIdMap.get(cd);
			if(null != th) {
				String cw = context.getParameter("column_" + th.getActualID());
				if(null != cw) {
					th.unchanged(() -> th.setWidth(cw));								// Update TH
					list.add(new ColumnWidth<>(cd, index, cw));
				}
			}
			index++;
		}

		//-- If a listener is there delegate there, else delegate to the page.
		IColumnListener<T> listener = getColumnListener();
		if(null != listener) {
			listener.columnsChanged(tbl, list);
		} else {
			listener = (IColumnListener<T>) DomApplication.get().getAttribute(COLUMN_LISTENER);
			if(null != listener) {
				listener.columnsChanged(tbl, list);
			}
		}
	}

	@Nullable public IColumnListener<T> getColumnListener() {
		return m_columnListener;
	}

	public void setColumnListener(@Nullable IColumnListener<T> columnListener) {
		m_columnListener = columnListener;
	}
}
