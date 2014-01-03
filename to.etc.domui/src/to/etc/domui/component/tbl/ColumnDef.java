package to.etc.domui.component.tbl;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.util.*;

public class ColumnDef<T> {
	@Nonnull
	final private Class<T> m_actualClass;

	/** The label text, if needed, to use as the column heading */
	@Nullable
	private String m_columnLabel;

	@Nonnull
	final private ColumnList< ? > m_defList;

	@Nonnull
	final private Class<T> m_columnType;

	@Nonnull
	private SortableType m_sortable = SortableType.UNKNOWN;

	@Nullable
	private ISortHelper m_sortHelper;

	@Nullable
	private String m_width;

	@Nullable
	private String m_propertyName;

	@Nullable
	private String m_cssClass;

	@Nullable
	private String m_headerCssClass;

	@Deprecated
	private int m_displayLength;

	private boolean m_nowrap = true;

	/** If bound to a property: the metamodel for the property. This is null if the column binds to the entire row object. */
	@Nullable
	private PropertyMetaModel<T> m_propertyMetaModel;

	@Nonnull
	private NumericPresentation m_numericPresentation = NumericPresentation.UNKNOWN;

	@Nullable
	private TextAlign m_align;

	@Nullable
	private INodeContentRenderer<T> m_contentRenderer;

	@Nullable
	private ICellClicked<T> m_cellClicked;

	@Nullable
	private String m_renderHint;

	/** @since 2014/1/2 T when this should create an editable component bound to the column's value. */
	private boolean m_editable;

	public <X> ColumnDef(@Nonnull ColumnList< ? > cdl, @Nonnull Class<T> valueClass) {
		m_actualClass = valueClass;
		m_columnType = valueClass;
		m_defList = cdl;
	}

	/**
	 * Create a column definition using metadata for the column.
	 * @param pmm
	 */
	public ColumnDef(@Nonnull ColumnList< ? > cdl, @Nonnull PropertyMetaModel<T> pmm) {
		m_actualClass = pmm.getActualType();
		m_defList = cdl;
		m_columnType = pmm.getActualType();
		setColumnLabel(pmm.getDefaultLabel());
		m_propertyMetaModel = pmm;
		setSortable(pmm.getSortable());
		setPropertyName(pmm.getName());
		setNumericPresentation(pmm.getNumericPresentation());
		if(pmm.getNowrap() == YesNoType.YES)
			setNowrap(true);
	}

	@Nonnull
	public Class<T> getActualClass() {
		return m_actualClass;
	}

	@Nullable
	public PropertyMetaModel<T> getPropertyMetaModel() {
		return m_propertyMetaModel;
	}

	@Nullable
	public String getColumnLabel() {
		return m_columnLabel;
	}

	public void setColumnLabel(@Nullable String columnLabel) {
		label(columnLabel);
	}


	/**
	 * Create an editable component bound to the column's value.
	 * @since 2013/1/2
	 * @return
	 */
	@Nonnull
	public ColumnDef<T> editable() {
		if(m_propertyMetaModel == null)
			throw new IllegalStateException("Cannot edit a row instance");
		m_editable = true;
		return this;
	}

	public boolean isEditable() {
		return m_editable;
	}

	<R> T getColumnValue(@Nonnull R instance) throws Exception {
		PropertyMetaModel<T> pmm = m_propertyMetaModel;
		if(pmm == null)
			return (T) instance;
		else
			return pmm.getValue(instance);
	}

	@Nonnull
	public Class<T> getColumnType() {
		return m_columnType;
	}

	@Nonnull
	public SortableType getSortable() {
		return m_sortable;
	}

	public void setSortable(@Nonnull SortableType sortable) {
		m_sortable = sortable == null ? SortableType.UNKNOWN : sortable;
	}

	@Nullable
	public String getWidth() {
		return m_width;
	}

	public void setWidth(@Nullable String width) {
		width(width);
	}

	@Nullable
	public String getPropertyName() {
		return m_propertyName;
	}

	public void setPropertyName(@Nullable String propertyName) {
		m_propertyName = propertyName;
	}

	@Nullable
	public INodeContentRenderer<T> getContentRenderer() {
		return m_contentRenderer;
	}

	public void setContentRenderer(@Nullable INodeContentRenderer<T> contentRenderer) {
		m_contentRenderer = contentRenderer;
	}

	/**
	 * When set this defines the css class to set on each value cell for this column. Setting this
	 * does NOT set a css class for the header!!
	 * @return
	 */
	@Nullable
	public String getCssClass() {
		return m_cssClass;
	}

	/**
	 * When set this defines the css class to set on each value cell for this column. Setting this
	 * does NOT set a css class for the header!!
	 * @param cssClass
	 */
	public void setCssClass(@Nullable String cssClass) {
		m_cssClass = cssClass;
	}

	/**
	 * When set this defines the css class to set on the header of this column.
	 * @return
	 */
	@Nullable
	public String getHeaderCssClass() {
		return m_headerCssClass;
	}

	/**
	 * When set this defines the css class to set on the header of this column.
	 *
	 * @param headerCssClass
	 */
	public void setHeaderCssClass(@Nullable String headerCssClass) {
		m_headerCssClass = headerCssClass;
	}

	/**
	 * Seems nonsense, use width instead.
	 * @return
	 */
	@Deprecated
	public int getDisplayLength() {
		return m_displayLength;
	}

	/**
	 * Seems nonsense, use width instead.
	 * @param displayLength
	 * @return
	 */
	@Deprecated
	public void setDisplayLength(int displayLength) {
		m_displayLength = displayLength;
	}

	public boolean isNowrap() {
		return m_nowrap;
	}

	public void setNowrap(boolean nowrap) {
		m_nowrap = nowrap;
	}

	@Nullable
	public ICellClicked<T> getCellClicked() {
		return m_cellClicked;
	}

	public void setCellClicked(@Nullable ICellClicked<T> cellClicked) {
		m_cellClicked = cellClicked;
	}

	@Nonnull
	public NumericPresentation getNumericPresentation() {
		return m_numericPresentation;
	}

	public void setNumericPresentation(@Nonnull NumericPresentation numericPresentation) {
		m_numericPresentation = numericPresentation;
	}

	@Nullable
	public TextAlign getAlign() {
		return m_align;
	}

	public void setAlign(@Nullable TextAlign align) {
		m_align = align;
	}

	@Nullable
	public String getRenderHint() {
		return m_renderHint;
	}

	public void setRenderHint(@Nullable String renderHint) {
		m_renderHint = renderHint;
	}

	@Nullable
	public ISortHelper getSortHelper() {
		return m_sortHelper;
	}

	public void setSortHelper(@Nullable ISortHelper sortHelper) {
		m_sortHelper = sortHelper;
	}

	@Nonnull
	@Override
	public String toString() {
		return "ColumnDef[" + getPropertyName() + ", type=" + getColumnType() + ", lbl=" + getColumnLabel() + "]";
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Chainable setters.									*/
	/*--------------------------------------------------------------*/

	/**
	 * Set the column header's label.
	 * @param columnLabel
	 * @return
	 */
	@Nonnull
	public ColumnDef<T> label(@Nullable String columnLabel) {
		m_columnLabel = columnLabel;
		return this;
	}

	/**
	 * Set the text align for this column. Defaults depend on the numeric type of the column, if known.
	 * @param align
	 * @return
	 */
	@Nonnull
	public ColumnDef<T> align(@Nonnull TextAlign align) {
		m_align = align;
		return this;
	}

	/**
	 * Set the cell click handler.
	 * @param ck
	 * @return
	 */
	@Nonnull
	public ColumnDef<T> cellClicked(@Nonnull ICellClicked<T> ck) {
		m_cellClicked = ck;
		return this;
	}

	/**
	 * Set the node content renderer.
	 * @param cr
	 * @return
	 */
	@Nonnull
	public ColumnDef<T> renderer(@Nonnull INodeContentRenderer<T> cr) {
		m_contentRenderer = cr;
		return this;
	}

	/**
	 * Set the css class of this column's values.
	 * @param css
	 * @return
	 */
	@Nonnull
	public ColumnDef<T> css(@Nonnull String css) {
		m_cssClass = css;
		return this;
	}

	/**
	 * Set the css class of this column's header.
	 * @param css
	 * @return
	 */
	@Nonnull
	public ColumnDef<T> cssHeader(@Nonnull String css) {
		m_headerCssClass = css;
		return this;
	}

	/**
	 * Make sure this column's contents are wrapped (by default columns added by {@link RowRenderer} are marked as not wrappable.
	 * @return
	 */
	@Nonnull
	public ColumnDef<T> wrap() {
		m_nowrap = false;
		return this;
	}

	/**
	 * Set the column to nowrap.
	 * @return
	 */
	@Nonnull
	public ColumnDef<T> nowrap() {
		m_nowrap = true;
		return this;
	}

	/**
	 * Set the numeric presentation for this column.
	 * @param np
	 * @return
	 */
	@Nonnull
	public ColumnDef<T> numeric(@Nonnull NumericPresentation np) {
		m_numericPresentation = np;
		return this;
	}

	/**
	 * Set the hint for a column.
	 * @param hint
	 * @return
	 */
	@Nonnull
	public ColumnDef<T> hint(@Nonnull String hint) {
		m_renderHint = hint;
		return this;
	}

	/**
	 * Set the default sort order to ascending first.
	 * @return
	 */
	@Nonnull
	public ColumnDef<T> ascending() {
		setSortable(SortableType.SORTABLE_ASC);
		return this;
	}

	/**
	 * Set the default sort order to descending first.
	 * @return
	 */
	@Nonnull
	public ColumnDef<T> descending() {
		setSortable(SortableType.SORTABLE_DESC);
		return this;
	}

	/**
	 * Set this column as the default column to sort on.
	 * @return
	 */
	@Nonnull
	public ColumnDef<T> sortdefault() {
		m_defList.setSortColumn(this);
		return this;
	}

	/**
	 * Set a sort helper to be used for this column.
	 * @param sh
	 * @return
	 */
	@Nonnull
	public ColumnDef<T> sort(@Nonnull ISortHelper sh) {
		m_sortHelper = sh;
		if(m_sortable == SortableType.UNKNOWN)
			m_sortable = SortableType.SORTABLE_ASC;
		return this;
	}

	@Nonnull
	public ColumnDef<T> width(@Nullable String w) {
		m_width = w;
		return this;
	}
}
