package to.etc.domui.component.tbl;

import to.etc.domui.component.meta.*;
import to.etc.domui.converter.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.util.*;

import javax.annotation.*;
import java.util.function.Predicate;

final public class ColumnDef<I, T> {
	@Nonnull
	final private Class<T> m_actualClass;

	/** The label text, if needed, to use as the column heading */
	@Nullable
	private String m_columnLabel;

	@Nonnull
	final private ColumnList<I> m_defList;

	@Nonnull
	final private Class<T> m_columnType;

	@Nonnull
	private SortableType m_sortable = SortableType.SORTABLE_ASC;

	@Nullable
	private ISortHelper<?> m_sortHelper;

	/** If this is a column for the entire entity, this name defines the sort property to use if the sort button is pressed */
	@Nullable
	private String m_sortProperty;

	/** Some special width, like "1%" */
	@Nullable
	private String m_width;

	@Nullable
	private String m_propertyName;

	@Nullable
	private String m_cssClass;

	@Nullable
	private String m_headerCssClass;

	/** Set from metadata, specifies the width in characters. */
	private int m_characterWidth;

	private boolean m_nowrap = true;

	/** If bound to a property: the metamodel for the property. This is null if the column binds to the entire row object. */
	@Nullable
	private PropertyMetaModel<T> m_propertyMetaModel;

	@Nonnull
	private NumericPresentation m_numericPresentation = NumericPresentation.UNKNOWN;

	@Nullable
	private TextAlign m_align;

	@Nullable
	private IRenderInto<T> m_contentRenderer;

	@Nullable
	private IConverter<T> m_converter;

	@Nullable
	private ICellClicked<I> m_cellClicked;

	@Nullable
	private String m_renderHint;

	/** @since 2014/1/2 T when this should create an editable component bound to the column's value. */
	private boolean m_editable;

	private IRowControlFactory<I> m_controlFactory;

	@Nullable
	private Predicate<I> m_showCellClickedWhen;

	ColumnDef(@Nonnull ColumnList<I> cdl, @Nonnull Class<T> valueClass) {
		m_actualClass = valueClass;
		m_columnType = valueClass;
		m_defList = cdl;
	}

	/**
	 * Create a column definition using metadata for the column.
	 * @param pmm
	 */
	ColumnDef(@Nonnull ColumnList<I> cdl, @Nonnull PropertyMetaModel<T> pmm) {
		m_actualClass = pmm.getActualType();
		m_defList = cdl;
		m_columnType = pmm.getActualType();
		label(pmm.getDefaultLabel());
		m_propertyMetaModel = pmm;
		SortableType sortable = pmm.getSortable();
		// By default try to sort ascending if sorting is unknown. Use UNSORTABLE to prevent this.
		if(sortable == SortableType.UNKNOWN) {
			sortable = SortableType.SORTABLE_ASC;
		}
		setSortable(sortable);
		setPropertyName(pmm.getName());
		numeric(pmm.getNumericPresentation());
		if(pmm.getNowrap() == YesNoType.YES)
			nowrap();

		/*
		 * jal 20171220 This must not be here: when setting it here we might get both a converter AND a renderer. When calculating
		 * a rendering the code should determine whether it NEEDS a default converter and only use it then.
		 */
		//converter(ConverterRegistry.findBestConverter(pmm));
		width(MetaManager.calculateTextSize(pmm));
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

	@Nullable
	public IConverter<T> getConverter() {
		return m_converter;
	}

	/**
	 * Create an editable component bound to the column's value.
	 * @since 2013/1/2
	 * @return
	 */
	@Nonnull
	public ColumnDef<I, T> editable() {
		if(m_propertyMetaModel == null)
			throw new IllegalStateException("Cannot edit a row instance");
		m_editable = true;
		return this;
	}

	/**
	 * Use to set the property name for a column that wraps the whole record
	 * (a column defined by {@link RowRenderer#column()}. It should be set when a row has
	 * sort defined.
	 *
	 * @param name
	 * @return
	 */
	@Nonnull
	public ColumnDef<I, T> property(@Nonnull String name) {
		if(m_propertyName != null)
			throw new IllegalStateException("The property name is already defined.");
		m_propertyName = name;
		return this;
	}

	public boolean isEditable() {
		return m_editable;
	}

	@Nullable
	public <R> T getColumnValue(@Nonnull R instance) throws Exception {
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

	@Nullable
	public String getWidth() {
		return m_width;
	}

	/**
	 * The requested width in characters, often set from metadata. Only used when it is > 0, and
	 * overridden by {@link #width(String)}.
	 */
	public int getCharacterWidth() {
		return m_characterWidth;
	}

	/**
	 * The requested width in characters, often set from metadata. Only used when it is > 0, and
	 * overridden by {@link #width(String)}.
	 */
	public ColumnDef<I, T> width(int characters) {
		m_characterWidth = characters;
		return this;
	}

	@Nullable
	public String getPropertyName() {
		return m_propertyName;
	}

	public void setPropertyName(@Nullable String propertyName) {
		m_propertyName = propertyName;
	}

	@Nullable
	public IRenderInto<T> getContentRenderer() {
		return m_contentRenderer;
	}

	/**
	 * When set this defines the css class to set on each value cell for this column. Setting this
	 * does NOT set a css class for the header!!
	 */
	@Nullable
	public String getCssClass() {
		return m_cssClass;
	}

	/**
	 * When set this defines the css class to set on the header of this column.
	 * @return
	 */
	@Nullable
	public String getHeaderCssClass() {
		return m_headerCssClass;
	}

	public boolean isNowrap() {
		return m_nowrap;
	}

	@Nullable
	public ICellClicked<I> getCellClicked() {
		return m_cellClicked;
	}

	@Nullable public Predicate<I> getShowCellClickedWhen() {
		return m_showCellClickedWhen;
	}

	@Nonnull
	public NumericPresentation getNumericPresentation() {
		return m_numericPresentation;
	}

	@Nullable
	public TextAlign getAlign() {
		return m_align;
	}

	@Nullable
	public String getRenderHint() {
		return m_renderHint;
	}

	@Nullable
	public ISortHelper<?> getSortHelper() {
		return m_sortHelper;
	}

	@Nullable
	public String getSortProperty() {
		return m_sortProperty;
	}

	public void setSortable(@Nonnull SortableType sortable) {
		m_sortable = sortable == null ? SortableType.UNKNOWN : sortable;
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
	public ColumnDef<I, T> label(@Nullable String columnLabel) {
		m_columnLabel = columnLabel;
		return this;
	}

	/**
	 * Set the text align for this column. Defaults depend on the numeric type of the column, if known.
	 * @param align
	 * @return
	 */
	@Nonnull
	public ColumnDef<I, T> align(@Nonnull TextAlign align) {
		m_align = align;
		return this;
	}

	/**
	 * Set the cell click handler.
	 * @param ck
	 * @return
	 */
	@Nonnull
	public ColumnDef<I, T> cellClicked(@Nullable ICellClicked<I> ck) {
		m_cellClicked = ck;
		return this;
	}

	/**
	 * Set the cell click handler.
	 * @param ck
	 * @return
	 */
	@Nonnull
	public ColumnDef<I, T> cellClicked(@Nullable ICellClicked<I> ck, @Nonnull Predicate<I> showWhen) {
		m_cellClicked = ck;
		m_showCellClickedWhen = showWhen;
		return this;
	}


	/**
	 * Set the node content renderer.
	 * @param cr
	 * @return
	 */
	@Nonnull
	public ColumnDef<I, T> renderer(@Nullable IRenderInto<T> cr) {
		m_contentRenderer = cr;
		return this;
	}

	/**
	 * Set the css class of this column's values.
	 * @param css
	 * @return
	 */
	@Nonnull
	public ColumnDef<I, T> css(@Nonnull String css) {
		m_cssClass = css;
		return this;
	}

	/**
	 * Set the css class of this column's header.
	 * @param css
	 * @return
	 */
	@Nonnull
	public ColumnDef<I, T> cssHeader(@Nonnull String css) {
		m_headerCssClass = css;
		return this;
	}

	/**
	 * Make sure this column's contents are wrapped (by default columns added by {@link RowRenderer} are marked as not wrappable.
	 * @return
	 */
	@Nonnull
	public ColumnDef<I, T> wrap() {
		m_nowrap = false;
		return this;
	}

	/**
	 * Set the column to nowrap.
	 * @return
	 */
	@Nonnull
	public ColumnDef<I, T> nowrap() {
		m_nowrap = true;
		return this;
	}

	/**
	 * Set the numeric presentation for this column.
	 * @param np
	 * @return
	 */
	@Nonnull
	public ColumnDef<I, T> numeric(@Nonnull NumericPresentation np) {
		m_numericPresentation = np;
		return this;
	}

	/**
	 * Set the hint for a column.
	 * @param hint
	 * @return
	 */
	@Nonnull
	public ColumnDef<I, T> hint(@Nonnull String hint) {
		m_renderHint = hint;
		return this;
	}

	/**
	 * Set the default sort order to ascending first.
	 * @return
	 */
	@Nonnull
	public ColumnDef<I, T> ascending() {
		setSortable(SortableType.SORTABLE_ASC);
		return this;
	}

	/**
	 * Set the default sort order to descending first.
	 * @return
	 */
	@Nonnull
	public ColumnDef<I, T> descending() {
		setSortable(SortableType.SORTABLE_DESC);
		return this;
	}

	/**
	 * Set this column as the default column to sort on.
	 * @return
	 */
	@Nonnull
	public ColumnDef<I, T> sortdefault() {
		m_defList.setSortColumn(this);
		return this;
	}

	/**
	 * Set a sort helper to be used for this column. <b>Important:</b> if you just
	 * need to sort on a property consider {@link #sort(String)} instead.
	 * @param sh
	 * @return
	 */
	@Nonnull
	public ColumnDef<I, T> sort(@Nonnull ISortHelper<?> sh) {
		m_sortHelper = sh;
		if(m_sortable == SortableType.UNKNOWN)
			m_sortable = SortableType.SORTABLE_ASC;
		return this;
	}

	/**
	 * For a column that represents the whole entity, this can specify which property of
	 * the entity is to be used to sort on. It prevents having to use an {@link ISortHelper} for simple sorting.
	 *
	 * @param propertyName
	 * @return
	 */
	@Nonnull
	public ColumnDef<I, T> sort(@Nonnull String propertyName) {
		m_sortProperty = propertyName;
		return this;
	}

	@Nonnull
	public ColumnDef<I, T> width(@Nullable String w) {
		m_width = w;
		return this;
	}

	@Nonnull
	public ColumnDef<I, T> converter(@Nullable IConverter<T> converter) {
		m_converter = converter;
		return this;
	}

	/**
	 * Define the control factory to create the control to use to show the column's value.
	 */
	@Nonnull
	public ColumnDef<I, T> factory(@Nullable IRowControlFactory<I> factory) {
		m_controlFactory = factory;
		if(factory != null)
			editable();
		return this;
	}

	/**
	 * Return the control factory to create the control to use to show the column's value.
	 *
	 * @return
	 */
	@Nullable
	public IRowControlFactory<I> getControlFactory() {
		return m_controlFactory;
	}
}
