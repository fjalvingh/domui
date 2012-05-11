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

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.component.meta.impl.*;
import to.etc.domui.converter.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.util.*;

/**
 * Contains data for rendering a column in a data table.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 18, 2008
 */
final public class SimpleColumnDef {
	/** The label text, if needed, to use as the column heading */
	@Nullable
	private String m_columnLabel;

	@Nullable
	private Class< ? > m_columnType;

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

	private int m_displayLength;

	private boolean m_nowrap;

	/** The thingy which obtains the column's value (as an object) */
	@Nullable
	private IValueTransformer< ? > m_valueTransformer;

	@Nullable
	private IObjectToStringConverter< ? > m_presentationConverter;

	@Nonnull
	private NumericPresentation m_numericPresentation = NumericPresentation.UNKNOWN;

	@Nullable
	private TextAlign m_align;

	@Nullable
	private INodeContentRenderer< ? > m_contentRenderer;

	@Nullable
	private ICellClicked< ? > m_cellClicked;

	@Nullable
	private String m_renderHint;

	public SimpleColumnDef() {}

	/**
	 * Create a column definition using metadata for the column.
	 * @param m
	 */
	public SimpleColumnDef(@Nonnull PropertyMetaModel< ? > m) {
		setColumnLabel(m.getDefaultLabel());
		setColumnType(m.getActualType());
		setValueTransformer(m); // Thing which can obtain the value from the property
		setPresentationConverter(ConverterRegistry.findBestConverter(m));
		setSortable(m.getSortable());
		setPropertyName(m.getName());
		setNumericPresentation(m.getNumericPresentation());
		if(m.getNowrap() == YesNoType.YES)
			setNowrap(true);
	}

	public SimpleColumnDef(@Nonnull ExpandedDisplayProperty< ? > m) {
		setColumnLabel(m.getDefaultLabel());
		setColumnType(m.getActualType());
		setValueTransformer(m); // Thing which can obtain the value from the property
		setPresentationConverter(m.getBestConverter());
		setSortable(SortableType.UNSORTABLE); // FIXME From meta pls
		setSortable(m.getSortable());
		setPropertyName(m.getName());
		if(m.getName() == null)
			throw new IllegalStateException("All columns MUST have some name");
		setNumericPresentation(m.getNumericPresentation());
		setRenderHint(m.getRenderHint());
		if(m.getDisplayLength() > 0)
			setDisplayLength(m.getDisplayLength());
		if(m.getNowrap() == YesNoType.YES)
			setNowrap(true);
	}

	@Nullable
	public String getColumnLabel() {
		return m_columnLabel;
	}

	public void setColumnLabel(@Nullable String columnLabel) {
		m_columnLabel = columnLabel;
	}

	@Nullable
	public Class< ? > getColumnType() {
		return m_columnType;
	}

	public void setColumnType(@Nullable Class< ? > columnType) {
		m_columnType = columnType;
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
		m_width = width;
	}

	@Nullable
	public IValueTransformer< ? > getValueTransformer() {
		return m_valueTransformer;
	}

	public void setValueTransformer(@Nullable IValueTransformer< ? > valueTransformer) {
		m_valueTransformer = valueTransformer;
	}

	/**
	 * Returns the optional converter to use to convert raw object values to some presentation string value.
	 * @return
	 */
	@Nullable
	public IObjectToStringConverter< ? > getPresentationConverter() {
		return m_presentationConverter;
	}

	public void setPresentationConverter(@Nullable IConverter< ? > valueConverter) {
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
	public INodeContentRenderer< ? > getContentRenderer() {
		return m_contentRenderer;
	}

	public void setContentRenderer(@Nullable INodeContentRenderer< ? > contentRenderer) {
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

	public int getDisplayLength() {
		return m_displayLength;
	}

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
	public ICellClicked< ? > getCellClicked() {
		return m_cellClicked;
	}

	public void setCellClicked(@Nullable ICellClicked< ? > cellClicked) {
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
		return "SimpleColumnDef[" + getPropertyName() + ", type=" + getColumnType() + ", lbl=" + getColumnLabel() + "]";
	}
}
