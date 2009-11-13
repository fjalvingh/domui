package to.etc.domui.component.tbl;

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
public class SimpleColumnDef {
	/** The label text, if needed, to use as the column heading */
	private String m_columnLabel;

	private Class< ? > m_columnType;

	private SortableType m_sortable;

	private String m_width;

	private String m_propertyName;

	private String m_cssClass;

	private String m_headerCssClass;

	private int m_displayLength;

	private boolean m_nowrap;

	/** The thingy which obtains the column's value (as an object) */
	private IValueTransformer< ? > m_valueTransformer;

	private IConverter< ? > m_valueConverter;

	private NumericPresentation m_numericPresentation;

	private TextAlign m_align;

	private INodeContentRenderer< ? > m_contentRenderer;

	private ICellClicked< ? > m_cellClicked;

	private String m_renderHint;

	public SimpleColumnDef() {}

	/**
	 * Create a column definition using metadata for the column.
	 * @param m
	 */
	public SimpleColumnDef(PropertyMetaModel m) {
		setColumnLabel(m.getDefaultLabel());
		setColumnType(m.getActualType());
		setValueTransformer(m.getAccessor()); // Thing which can obtain the value from the property
		if(m.getBestConverter() != null) {
			setValueConverter(m.getBestConverter());
		} else if(m.getActualType().isEnum() || m.getActualType() == Boolean.TYPE || m.getActualType() == Boolean.class) {
			setValueConverter(ConverterRegistry.findConverter(m.getActualType(), m));
		}
		setSortable(m.getSortable());
		setPropertyName(m.getName());
		setNumericPresentation(m.getNumericPresentation());
	}

	public SimpleColumnDef(ExpandedDisplayProperty m) {
		setColumnLabel(m.getDefaultLabel());
		setColumnType(m.getActualType());
		setValueTransformer(m.getAccessor()); // Thing which can obtain the value from the property
		if(m.getBestConverter() != null) {
			setValueConverter(m.getBestConverter());
		} else if(m.getActualType().isEnum()) {
			setValueConverter(ConverterRegistry.findConverter(m.getActualType(), m));
		}
		setSortable(SortableType.UNSORTABLE); // FIXME From meta pls
		setSortable(m.getSortable());
		setPropertyName(m.getName());
		if(m.getName() == null)
			throw new IllegalStateException("All columns MUST have some name");
		setNumericPresentation(m.getNumericPresentation());
		setRenderHint(m.getRenderHint());
	}

	public String getColumnLabel() {
		return m_columnLabel;
	}

	public void setColumnLabel(String columnLabel) {
		m_columnLabel = columnLabel;
	}

	public Class< ? > getColumnType() {
		return m_columnType;
	}

	public void setColumnType(Class< ? > columnType) {
		m_columnType = columnType;
	}

	public SortableType getSortable() {
		return m_sortable;
	}

	public void setSortable(SortableType sortable) {
		m_sortable = sortable;
	}

	public String getWidth() {
		return m_width;
	}

	public void setWidth(String width) {
		m_width = width;
	}

	public IValueTransformer< ? > getValueTransformer() {
		return m_valueTransformer;
	}

	public void setValueTransformer(IValueTransformer< ? > valueTransformer) {
		m_valueTransformer = valueTransformer;
	}

	public IConverter< ? > getValueConverter() {
		return m_valueConverter;
	}

	public void setValueConverter(IConverter< ? > valueConverter) {
		m_valueConverter = valueConverter;
	}

	public String getPropertyName() {
		return m_propertyName;
	}

	public void setPropertyName(String propertyName) {
		m_propertyName = propertyName;
	}

	public INodeContentRenderer< ? > getContentRenderer() {
		return m_contentRenderer;
	}

	public void setContentRenderer(INodeContentRenderer< ? > contentRenderer) {
		m_contentRenderer = contentRenderer;
	}

	/**
	 * When set this defines the css class to set on each value cell for this column. Setting this
	 * does NOT set a css class for the header!!
	 * @return
	 */
	public String getCssClass() {
		return m_cssClass;
	}

	/**
	 * When set this defines the css class to set on each value cell for this column. Setting this
	 * does NOT set a css class for the header!!
	 * @param cssClass
	 */
	public void setCssClass(String cssClass) {
		m_cssClass = cssClass;
	}

	/**
	 * When set this defines the css class to set on the header of this column.
	 * @return
	 */
	public String getHeaderCssClass() {
		return m_headerCssClass;
	}

	/**
	 * When set this defines the css class to set on the header of this column.
	 *
	 * @param headerCssClass
	 */
	public void setHeaderCssClass(String headerCssClass) {
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

	public ICellClicked< ? > getCellClicked() {
		return m_cellClicked;
	}

	public void setCellClicked(ICellClicked< ? > cellClicked) {
		m_cellClicked = cellClicked;
	}

	public NumericPresentation getNumericPresentation() {
		return m_numericPresentation;
	}

	public void setNumericPresentation(NumericPresentation numericPresentation) {
		m_numericPresentation = numericPresentation;
	}

	public TextAlign getAlign() {
		return m_align;
	}

	public void setAlign(TextAlign align) {
		m_align = align;
	}

	public String getRenderHint() {
		return m_renderHint;
	}

	public void setRenderHint(String renderHint) {
		m_renderHint = renderHint;
	}
}
