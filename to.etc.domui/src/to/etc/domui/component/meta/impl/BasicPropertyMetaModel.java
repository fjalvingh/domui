package to.etc.domui.component.meta.impl;

import to.etc.domui.component.meta.*;
import to.etc.domui.converter.*;

public class BasicPropertyMetaModel {
	static private final PropertyMetaValidator[] NO_VALIDATORS = new PropertyMetaValidator[0];

	private Class< ? extends IConverter< ? >> m_converterClass;

	private IConverter<?> m_bestConverter;

	private SortableType m_sortable = SortableType.UNKNOWN;

	private int m_displayLength = -1;

	private short m_precision = -1;

	private byte m_scale = -1;

	private boolean m_required;

	private String[][] m_viewRoles;

	private String[][] m_editRoles;

	private YesNoType m_readOnly;

	private TemporalPresentationType m_temporal = TemporalPresentationType.UNKNOWN;

	private NumericPresentation m_numericPresentation = NumericPresentation.UNKNOWN;

	private PropertyMetaValidator[] m_validators = NO_VALIDATORS;

	public Class< ? extends IConverter< ? >> getConverterClass() {
		return m_converterClass;
	}

	public void setConverterClass(Class< ? extends IConverter< ? >> converter) {
		m_converterClass = converter;
	}

	public IConverter<?> getBestConverter() {
		return m_bestConverter;
	}

	public void setBestConverter(IConverter<?> bestConverter) {
		m_bestConverter = bestConverter;
	}

	public SortableType getSortable() {
		return m_sortable;
	}

	public void setSortable(SortableType sortable) {
		m_sortable = sortable;
	}

	public int getDisplayLength() {
		return m_displayLength;
	}

	public void setDisplayLength(int displayLength) {
		m_displayLength = displayLength;
	}

	public boolean isRequired() {
		return m_required;
	}

	public void setRequired(boolean required) {
		m_required = required;
	}

	/**
	 * {@inheritDoc}
	 * Returns the roles needed to view (display) this property.
	 * @see to.etc.domui.component.meta.PropertyMetaModel#getViewRoles()
	 */
	public String[][] getViewRoles() {
		return m_viewRoles;
	}

	public void setViewRoles(String[][] viewRoles) {
		m_viewRoles = viewRoles;
	}

	/**
	 * {@inheritDoc}
	 * Returns the roles needed to edit this property.
	 * @see to.etc.domui.component.meta.PropertyMetaModel#getEditRoles()
	 */
	public String[][] getEditRoles() {
		return m_editRoles;
	}

	public void setEditRoles(String[][] editRoles) {
		m_editRoles = editRoles;
	}

	public YesNoType getReadOnly() {
		return m_readOnly;
	}

	public void setReadOnly(YesNoType readOnly) {
		m_readOnly = readOnly;
	}

	public TemporalPresentationType getTemporal() {
		return m_temporal;
	}

	public void setTemporal(TemporalPresentationType temporal) {
		m_temporal = temporal;
	}

	public NumericPresentation getNumericPresentation() {
		return m_numericPresentation;
	}

	public void setNumericPresentation(NumericPresentation numericPresentation) {
		m_numericPresentation = numericPresentation;
	}

	public int getPrecision() {
		return m_precision;
	}

	public void setPrecision(int prec) {
		if(prec < -1 || prec > Short.MAX_VALUE)
			throw new IllegalArgumentException("Precision out of range: " + prec);
		m_precision = (short) prec;
	}

	public int getScale() {
		return m_scale;
	}

	public void setScale(int scale) {
		if(scale < -1 || scale > Byte.MAX_VALUE)
			throw new IllegalArgumentException("Scale out of range: " + scale);
		m_scale = (byte) scale;
	}

	public PropertyMetaValidator[] getValidators() {
		return m_validators;
	}

	public void setValidators(PropertyMetaValidator[] validators) {
		m_validators = validators;
	}
}
