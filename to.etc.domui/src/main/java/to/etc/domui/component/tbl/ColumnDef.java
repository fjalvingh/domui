package to.etc.domui.component.tbl;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.binding.StyleBinder;
import to.etc.domui.component.meta.MetaManager;
import to.etc.domui.component.meta.NumericPresentation;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.SortableType;
import to.etc.domui.component.meta.YesNoType;
import to.etc.domui.converter.IConverter;
import to.etc.domui.dom.css.TextAlign;
import to.etc.domui.util.IRenderInto;
import to.etc.webapp.nls.IBundleCode;
import to.etc.webapp.query.QField;

import java.util.function.Predicate;

final public class ColumnDef<I, T> {
	@NonNull
	final private Class<T> m_actualClass;

	/** The label text, if needed, to use as the column heading */
	@Nullable
	private String m_columnLabel;

	@NonNull
	final private ColumnList<I> m_defList;

	@NonNull
	final private Class<T> m_columnType;

	@NonNull
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

	/** Specifies the max width in characters. */
	private int m_maxCharacterWidth;

	private boolean m_nowrap = true;

	/** If bound to a property: the metamodel for the property. This is null if the column binds to the entire row object. */
	@Nullable
	private PropertyMetaModel<T> m_propertyMetaModel;

	@NonNull
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
	private String m_hint;

	/** @since 2014/1/2 T when this should create an editable component bound to the column's value. */
	private boolean m_editable;

	private IRowControlFactory<I> m_controlFactory;

	@Nullable
	private Predicate<I> m_showCellClickedWhen;

	@Nullable
	private ColumnStyleBindingBuilder<I, T> m_columnStyleBinding;

	private boolean m_rerenderOnBind;

	/**
	 * A property that contains a value hint, which will be shown
	 * as the hover (title=) of the cell when present.
	 */
	@Nullable
	private PropertyMetaModel<String> m_valueHintProperty;

	ColumnDef(@NonNull ColumnList<I> cdl, @NonNull Class<T> valueClass) {
		m_actualClass = valueClass;
		m_columnType = valueClass;
		m_defList = cdl;
	}

	/**
	 * Create a column definition using metadata for the column.
	 */
	ColumnDef(@NonNull ColumnList<I> cdl, @NonNull PropertyMetaModel<T> pmm) {
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

	@NonNull
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
	 */
	@NonNull
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
	 */
	@NonNull
	public ColumnDef<I, T> property(@NonNull String name) {
		if(m_propertyName != null)
			throw new IllegalStateException("The property name is already defined.");
		m_propertyName = name;
		return this;
	}

	public boolean isEditable() {
		return m_editable;
	}

	@Nullable
	public <R> T getColumnValue(@NonNull R instance) throws Exception {
		PropertyMetaModel<T> pmm = m_propertyMetaModel;
		if(pmm == null)
			return (T) instance;
		else
			return pmm.getValue(instance);
	}

	@NonNull
	public Class<T> getColumnType() {
		return m_columnType;
	}

	@NonNull
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

	/**
	 * The requested max width in characters.
	 * It would ensure that column never grows over that value in em units.
	 * In case that text content is larger, it would automatically add whole content text as cell hover.
	 */
	public int getMaxCharacterWidth() {
		return m_maxCharacterWidth;
	}

	/**
	 * The requested max width in characters.
	 * It would ensure that column never grows over that value in em units.
	 * In case that text content is larger, it would automatically add whole content text as cell hover.
	 */
	public ColumnDef<I, T> maxWidth(int characters) {
		m_maxCharacterWidth = characters;
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

	@Nullable
	public Predicate<I> getShowCellClickedWhen() {
		return m_showCellClickedWhen;
	}

	@NonNull
	public NumericPresentation getNumericPresentation() {
		return m_numericPresentation;
	}

	@Nullable
	public TextAlign getAlign() {
		return m_align;
	}

	@Nullable
	public String getHint() {
		return m_hint;
	}

	@Nullable
	public ISortHelper<?> getSortHelper() {
		return m_sortHelper;
	}

	@Nullable
	public String getSortProperty() {
		return m_sortProperty;
	}

	public void setSortable(@NonNull SortableType sortable) {
		m_sortable = sortable == null ? SortableType.UNKNOWN : sortable;
	}

	@NonNull
	@Override
	public String toString() {
		return "ColumnDef[" + getPropertyName() + ", type=" + getColumnType() + ", lbl=" + getColumnLabel() + "]";
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Chainable setters.									*/
	/*--------------------------------------------------------------*/

	@NonNull
	public ColumnDef<I, T> label(IBundleCode code) {
		return label(code.getString());
	}

	/**
	 * Set the column header's label.
	 */
	@NonNull
	public ColumnDef<I, T> label(@Nullable String columnLabel) {
		m_columnLabel = columnLabel;
		return this;
	}

	/**
	 * Set the text align for this column. Defaults depend on the numeric type of the column, if known.
	 */
	@NonNull
	public ColumnDef<I, T> align(@NonNull TextAlign align) {
		m_align = align;
		return this;
	}

	/**
	 * Set the cell click handler.
	 */
	@NonNull
	public ColumnDef<I, T> cellClicked(@Nullable ICellClicked<I> ck) {
		m_cellClicked = ck;
		return this;
	}

	/**
	 * Set the cell click handler.
	 */
	@NonNull
	public ColumnDef<I, T> cellClicked(@Nullable ICellClicked<I> ck, @NonNull Predicate<I> showWhen) {
		m_cellClicked = ck;
		m_showCellClickedWhen = showWhen;
		return this;
	}

	/**
	 * Set the node content renderer.
	 */
	@NonNull
	public ColumnDef<I, T> renderer(@Nullable IRenderInto<T> cr) {
		m_contentRenderer = cr;
		return this;
	}

	/**
	 * Set the css class of this column's values.
	 */
	@NonNull
	public ColumnDef<I, T> css(@NonNull String css) {
		m_cssClass = css;
		return this;
	}

	/**
	 * Set the css class of this column's header.
	 */
	@NonNull
	public ColumnDef<I, T> cssHeader(@NonNull String css) {
		m_headerCssClass = css;
		return this;
	}

	/**
	 * Make sure this column's contents are wrapped (by default columns added by {@link RowRenderer} are marked as not wrappable.
	 */
	@NonNull
	public ColumnDef<I, T> wrap() {
		m_nowrap = false;
		return this;
	}

	/**
	 * Set the column to nowrap.
	 */
	@NonNull
	public ColumnDef<I, T> nowrap() {
		m_nowrap = true;
		return this;
	}

	/**
	 * Set the numeric presentation for this column.
	 */
	@NonNull
	public ColumnDef<I, T> numeric(@NonNull NumericPresentation np) {
		m_numericPresentation = np;
		return this;
	}

	/**
	 * Set the hint for a column as localized code.
	 */
	@NonNull
	public ColumnDef<I, T> hint(IBundleCode code) {
		return hint(code.getString());
	}

	/**
	 * Set the hint for a column.
	 */
	@NonNull
	public ColumnDef<I, T> hint(@NonNull String hint) {
		m_hint = hint;
		return this;
	}

	/**
	 * Set the default sort order to ascending first.
	 */
	@NonNull
	public ColumnDef<I, T> ascending() {
		setSortable(SortableType.SORTABLE_ASC);
		return this;
	}

	/**
	 * Set the default sort order to descending first.
	 */
	@NonNull
	public ColumnDef<I, T> descending() {
		setSortable(SortableType.SORTABLE_DESC);
		return this;
	}

	/**
	 * Set this column as the default column to sort on.
	 */
	@NonNull
	public ColumnDef<I, T> sortdefault() {
		m_defList.setSortColumn(this);
		return this;
	}

	/**
	 * Set a sort helper to be used for this column. <b>Important:</b> if you just
	 * need to sort on a property consider {@link #sort(String)} instead.
	 */
	@NonNull
	public ColumnDef<I, T> sort(@NonNull ISortHelper<?> sh) {
		m_sortHelper = sh;
		if(m_sortable == SortableType.UNKNOWN)
			m_sortable = SortableType.SORTABLE_ASC;
		return this;
	}

	/**
	 * For a column that represents the whole entity, this can specify which property of
	 * the entity is to be used to sort on. It prevents having to use an {@link ISortHelper} for simple sorting.
	 */
	@NonNull
	public ColumnDef<I, T> sort(@NonNull String propertyName) {
		m_sortProperty = propertyName;
		return this;
	}

	public ColumnDef<I, T> sort(@NonNull QField<I, ?> field) {
		m_sortProperty = field.getName();
		return this;
	}

	@NonNull
	public ColumnDef<I, T> width(@Nullable String w) {
		m_width = w;
		return this;
	}

	@NonNull
	public ColumnDef<I, T> converter(@Nullable IConverter<T> converter) {
		m_converter = converter;
		return this;
	}

	/**
	 * Define the control factory to create the control to use to show the column's value.
	 */
	@NonNull
	public ColumnDef<I, T> factory(@Nullable IRowControlFactory<I> factory) {
		m_controlFactory = factory;
		if(factory != null)
			editable();
		return this;
	}

	public boolean isRerenderOnBind() {
		return m_rerenderOnBind;
	}

	public ColumnDef<I, T> rerenderOnBind() {
		m_rerenderOnBind = true;
		return this;
	}

	public ColumnDef<I, T> rerenderOnBind(boolean yes) {
		m_rerenderOnBind = yes;
		return this;
	}

	/**
	 * Return the control factory to create the control to use to show the column's value.
	 */
	@Nullable
	public IRowControlFactory<I> getControlFactory() {
		return m_controlFactory;
	}

	@NonNull
	ColumnList<I> getColumnList() {
		return m_defList;
	}

	public ColumnStyleBindingBuilder<I, T> styleBinding(StyleBinder style) {
		ColumnStyleBindingBuilder<I, T> csb = new ColumnStyleBindingBuilder<I, T>(this, style);
		return csb;
	}

	void styleBindingComplete(ColumnStyleBindingBuilder<I, T> csb) {
		m_columnStyleBinding = csb;
	}

	@Nullable
	public ColumnStyleBindingBuilder<I, T> getColumnStyleBinding() {
		return m_columnStyleBinding;
	}

	@Nullable
	public PropertyMetaModel<String> getValueHintProperty() {
		return m_valueHintProperty;
	}

	/**
	 * A property that contains a value hint, which will be shown
	 * as the hover (title=) of the cell when present.
	 */
	public ColumnDef<I, T> valueHint(QField<I, String> property) {
		m_valueHintProperty = m_defList.model().findProperty(property);
		return this;
	}
}
