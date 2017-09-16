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

import to.etc.domui.component.meta.NumericPresentation;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.SortableType;
import to.etc.domui.component.meta.YesNoType;
import to.etc.domui.component.meta.impl.ExpandedDisplayProperty;
import to.etc.domui.converter.ConverterRegistry;
import to.etc.domui.converter.IConverter;
import to.etc.domui.converter.IObjectToStringConverter;
import to.etc.domui.dom.css.TextAlign;
import to.etc.domui.util.IRenderInto;
import to.etc.domui.util.IValueTransformer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Contains data for rendering a column in a data table.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 18, 2008
 */
final public class SimpleColumnDef<T> {
	static public final String NOWRAP = "-NOWRAP";

	static public final String WRAP = "-WRAP";

	static public final String DEFAULTSORT = "-DSORT";

	/** The label text, if needed, to use as the column heading */
	@Nullable
	private String m_columnLabel;

	@Nonnull
	final private ColumnDefList< ? > m_defList;

	@Nonnull
	final private Class<T> m_columnType;

	@Nonnull
	private SortableType m_sortable = SortableType.UNKNOWN;

	/**
 	 * If this column is sortable with help from outside code: this defines that helper
	 * which will sort the main model for the table for this column. Watch out: the type
	 * passed to the sort helper is the ROW TYPE, not this-column's type!
	 */
	@Nullable
	private ISortHelper<?> m_sortHelper;

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

	/** When set, specifically define wrap or nowrap. When unset use the default. */
	@Nullable
	private Boolean m_nowrap;

	/** The thingy which obtains the column's value (as an object) */
	@Nullable
	private IValueTransformer<T> m_valueTransformer;

	@Nullable
	private IObjectToStringConverter<T> m_presentationConverter;

	@Nonnull
	private NumericPresentation m_numericPresentation = NumericPresentation.UNKNOWN;

	@Nullable
	private TextAlign m_align;

	@Nullable
	private IRenderInto<T> m_contentRenderer;

	@Nullable
	private ICellClicked<T> m_cellClicked;

	@Nullable
	private String m_renderHint;

	public <X> SimpleColumnDef(@Nonnull ColumnDefList< ? > cdl, @Nonnull Class<T> valueClass) {
		m_columnType = valueClass;
		m_defList = cdl;
	}

	/**
	 * Create a column definition using metadata for the column.
	 * @param pmm
	 */
	public SimpleColumnDef(@Nonnull ColumnDefList< ? > cdl, @Nonnull PropertyMetaModel<T> pmm) {
		m_defList = cdl;
		m_columnType = pmm.getActualType();
		setColumnLabel(pmm.getDefaultLabel());
		setValueTransformer(pmm); 								// Thing which can obtain the value from the property
		setPresentationConverter(ConverterRegistry.findBestConverter(pmm));
		setSortable(pmm.getSortable());
		setPropertyName(pmm.getName());
		setNumericPresentation(pmm.getNumericPresentation());
		if(pmm.getNowrap() == YesNoType.YES)
			setNowrap(Boolean.TRUE);
	}

	public SimpleColumnDef(@Nonnull ColumnDefList< ? > cdl, @Nonnull ExpandedDisplayProperty<T> m) {
		m_defList = cdl;
		m_columnType = m.getActualType();
		setColumnLabel(m.getDefaultLabel());
		setValueTransformer(m); 								// Thing which can obtain the value from the property
		setPresentationConverter(m.getBestConverter());
		setSortable(SortableType.UNSORTABLE); 					// FIXME From meta pls
		setSortable(m.getSortable());
		setPropertyName(m.getName());
		if(m.getName() == null)
			throw new IllegalStateException("All columns MUST have some name");
		setNumericPresentation(m.getNumericPresentation());
		setRenderHint(m.getRenderHint());
		if(m.getDisplayLength() > 0)
			setDisplayLength(m.getDisplayLength());
		if(m.getNowrap() == YesNoType.YES)
			setNowrap(Boolean.TRUE);
	}

	@Nullable
	public String getColumnLabel() {
		return m_columnLabel;
	}

	public void setColumnLabel(@Nullable String columnLabel) {
		label(columnLabel);
	}

	<R> T getColumnValue(@Nonnull R instance) throws Exception {
		IValueTransformer<T> valueTransformer = getValueTransformer();
		if(valueTransformer == null)
			return (T) instance;
		else
			return valueTransformer.getValue(instance);
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
	public IValueTransformer<T> getValueTransformer() {
		return m_valueTransformer;
	}

	public void setValueTransformer(@Nullable IValueTransformer<T> valueTransformer) {
		m_valueTransformer = valueTransformer;
	}

	/**
	 * Returns the optional converter to use to convert raw object values to some presentation string value.
	 * @return
	 */
	@Nullable
	public IObjectToStringConverter<T> getPresentationConverter() {
		return m_presentationConverter;
	}

	public void setPresentationConverter(@Nullable IConverter<T> valueConverter) {
		m_presentationConverter = valueConverter;
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

	public void setContentRenderer(@Nullable IRenderInto<T> contentRenderer) {
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

	@Nullable
	public Boolean isNowrap() {
		return m_nowrap;
	}

	public void setNowrap(@Nullable Boolean nowrap) {
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

	/**
	 * If this column is sortable with help from outside code: this defines that helper
	 * which will sort the main model for the table for this column. Watch out: the type
	 * passed to the sort helper is the ROW TYPE, not this-column's type!
	 * @return
	 */
	@Nullable
	public ISortHelper<?> getSortHelper() {
		return m_sortHelper;
	}

	public void setSortHelper(@Nullable ISortHelper<?> sortHelper) {
		m_sortHelper = sortHelper;
	}

	@Nonnull
	@Override
	public String toString() {
		return "SimpleColumnDef[" + getPropertyName() + ", type=" + getColumnType() + ", lbl=" + getColumnLabel() + "]";
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
	public SimpleColumnDef<T> label(@Nullable String columnLabel) {
		m_columnLabel = columnLabel;
		return this;
	}

	/**
	 * Set the text align for this column. Defaults depend on the numeric type of the column, if known.
	 * @param align
	 * @return
	 */
	@Nonnull
	public SimpleColumnDef<T> align(@Nonnull TextAlign align) {
		m_align = align;
		return this;
	}

	/**
	 * Set the cell click handler.
	 * @param ck
	 * @return
	 */
	@Nonnull
	public SimpleColumnDef<T> cellClicked(@Nonnull ICellClicked<T> ck) {
		m_cellClicked = ck;
		return this;
	}

	/**
	 * Set the node content renderer.
	 * @param cr
	 * @return
	 */
	@Nonnull
	public SimpleColumnDef<T> renderer(@Nonnull IRenderInto<T> cr) {
		m_contentRenderer = cr;
		return this;
	}

	/**
	 * Set the css class of this column's values.
	 * @param css
	 * @return
	 */
	@Nonnull
	public SimpleColumnDef<T> css(@Nonnull String css) {
		m_cssClass = css;
		return this;
	}

	/**
	 * Set the css class of this column's header.
	 * @param css
	 * @return
	 */
	@Nonnull
	public SimpleColumnDef<T> cssHeader(@Nonnull String css) {
		m_headerCssClass = css;
		return this;
	}

	/**
	 * Make sure this column's contents are wrapped (by default columns added by {@link RowRenderer} are marked as not wrappable.
	 * @return
	 */
	@Nonnull
	public SimpleColumnDef<T> wrap() {
		m_nowrap = Boolean.FALSE;
		return this;
	}

	/**
	 * Set the column to nowrap.
	 * @return
	 */
	@Nonnull
	public SimpleColumnDef<T> nowrap() {
		m_nowrap = Boolean.TRUE;
		return this;
	}

	/**
	 * Set the numeric presentation for this column.
	 * @param np
	 * @return
	 */
	@Nonnull
	public SimpleColumnDef<T> numeric(@Nonnull NumericPresentation np) {
		m_numericPresentation = np;
		return this;
	}

	/**
	 * Set a column value-to-string converter to be used.
	 * @param c
	 * @return
	 */
	@Nonnull
	public SimpleColumnDef<T> converter(@Nonnull IObjectToStringConverter<T> c) {
		m_presentationConverter = c;
		return this;
	}

	/**
	 * Set the hint for a column.
	 * @param hint
	 * @return
	 */
	@Nonnull
	public SimpleColumnDef<T>	hint(@Nonnull String hint) {
		m_renderHint = hint;
		return this;
	}

	/**
	 * Set the default sort order to ascending first.
	 * @return
	 */
	@Nonnull
	public SimpleColumnDef<T>	ascending() {
		setSortable(SortableType.SORTABLE_ASC);
		return this;
	}

	/**
	 * Set the default sort order to descending first.
	 * @return
	 */
	@Nonnull
	public SimpleColumnDef<T>	descending() {
		setSortable(SortableType.SORTABLE_DESC);
		return this;
	}

	/**
	 * Set this column as the default column to sort on.
	 * @return
	 */
	@Nonnull
	public SimpleColumnDef<T>	sortdefault() {
		m_defList.setSortColumn(this);
		return this;
	}

	/**
	 * Set a sort helper to be used for this column.
	 * @param sh
	 * @return
	 */
	@Nonnull
	public SimpleColumnDef<T>	sort(@Nonnull ISortHelper<T> sh) {
		m_sortHelper = sh;
		if(m_sortable == SortableType.UNKNOWN)
			m_sortable = SortableType.SORTABLE_ASC;
		return this;
	}

	/**
	 * Set a value transformer to convert this column value into something else.
	 * @param vt
	 * @return
	 */
	@Nonnull
	public SimpleColumnDef<T>	transform(@Nonnull IValueTransformer<T> vt) {
		m_valueTransformer = vt;
		return this;
	}

	@Nonnull
	public SimpleColumnDef<T> width(@Nullable String w) {
		m_width = w;
		return this;
	}
}
