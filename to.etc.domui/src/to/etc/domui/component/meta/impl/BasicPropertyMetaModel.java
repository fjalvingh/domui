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
package to.etc.domui.component.meta.impl;

import javax.annotation.*;

import to.etc.domui.component.form.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.converter.*;

public class BasicPropertyMetaModel<T> {
	static public final String[] NO_NAMES = new String[0];

	static private final PropertyMetaValidator[] NO_VALIDATORS = new PropertyMetaValidator[0];

	private IConverter<T> m_converter;

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

	private String m_regexpValidator;

	private String m_regexpUserString;

	/** T if marked as @Transient */
	private boolean m_transient;

	private ControlFactory m_controlFactory;

	@Nonnull
	private String[] m_columnNames = NO_NAMES;

	public IConverter<T> getConverter() {
		return m_converter;
	}

	public void setConverter(IConverter<T> converter) {
		m_converter = converter;
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

	public String getRegexpValidator() {
		return m_regexpValidator;
	}

	public void setRegexpValidator(String regexpValidator) {
		m_regexpValidator = regexpValidator;
	}

	public String getRegexpUserString() {
		return m_regexpUserString;
	}

	public void setRegexpUserString(String regexpUserString) {
		m_regexpUserString = regexpUserString;
	}

	public boolean isTransient() {
		return m_transient;
	}

	public void setTransient(boolean transient1) {
		m_transient = transient1;
	}

	public ControlFactory getControlFactory() {
		return m_controlFactory;
	}

	public void setControlFactory(ControlFactory controlFactory) {
		m_controlFactory = controlFactory;
	}

	@Nonnull
	public String[] getColumnNames() {
		return m_columnNames;
	}

	public void setColumnNames(@Nonnull String[] columnNames) {
		if(null == columnNames)
			throw new IllegalArgumentException("Cannot accept null");
		m_columnNames = columnNames;
	}
}
