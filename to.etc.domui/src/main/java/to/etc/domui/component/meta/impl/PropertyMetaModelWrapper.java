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

import to.etc.domui.component.controlfactory.PropertyControlFactory;
import to.etc.domui.component.input.IQueryManipulator;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.NumericPresentation;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.PropertyMetaValidator;
import to.etc.domui.component.meta.PropertyRelationType;
import to.etc.domui.component.meta.SearchPropertyMetaModel;
import to.etc.domui.component.meta.SortableType;
import to.etc.domui.component.meta.TemporalPresentationType;
import to.etc.domui.component.meta.YesNoType;
import to.etc.domui.converter.IConverter;
import to.etc.domui.util.IComboDataSet;
import to.etc.domui.util.ILabelStringRenderer;
import to.etc.domui.util.IRenderInto;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;

abstract public class PropertyMetaModelWrapper<T> implements PropertyMetaModel<T> {
	private PropertyMetaModel<T> m_parent;

	protected PropertyMetaModelWrapper(PropertyMetaModel<T> parent) {
		m_parent = parent;
	}

	public PropertyMetaModel<T> getWrappedModel() {
		return m_parent;
	}

	/**
	 * WATCH OUT: Should only be used when initializing outside the constructor; should not change after this
	 * has been passed to user code.
	 * @param parent
	 */
	public void setWrappedModel(PropertyMetaModel<T> parent) {
		m_parent = parent;
	}

	@Override
	public T getValue(Object in) throws Exception {
		return m_parent.getValue(in);
	}

	@Override
	public void setValue(Object target, T value) throws Exception {
		m_parent.setValue(target, value);
	}

	@Override
	public @Nonnull Class<T> getActualType() {
		return m_parent.getActualType();
	}

	@Override
	public ClassMetaModel getValueModel() {
		return m_parent.getValueModel();
	}

	@Override
	abstract public @Nonnull ClassMetaModel getClassModel();

	@Override
	public Class< ? extends IComboDataSet< ? >> getComboDataSet() {
		return m_parent.getComboDataSet();
	}

	@Override
	public @Nonnull List<DisplayPropertyMetaModel> getComboDisplayProperties() {
		return m_parent.getComboDisplayProperties();
	}

	@Override
	public Class< ? extends ILabelStringRenderer< ? >> getComboLabelRenderer() {
		return m_parent.getComboLabelRenderer();
	}

	@Override
	public Class< ? extends IRenderInto<T>> getComboNodeRenderer() {
		return m_parent.getComboNodeRenderer();
	}

	@Override
	public String getComponentTypeHint() {
		return m_parent.getComponentTypeHint();
	}

	@Override
	public PropertyControlFactory getControlFactory() {
		return m_parent.getControlFactory();
	}

	@Override
	public IConverter<T> getConverter() {
		return m_parent.getConverter();
	}

	@Override
	public String getDefaultHint() {
		return m_parent.getDefaultHint();
	}

	@Override
	public @Nonnull String getDefaultLabel() {
		return m_parent.getDefaultLabel();
	}

	@Override
	public int getDisplayLength() {
		return m_parent.getDisplayLength();
	}

	@Override
	public String getDomainValueLabel(Locale loc, Object val) {
		return m_parent.getDomainValueLabel(loc, val);
	}

	@Override
	public Object[] getDomainValues() {
		return m_parent.getDomainValues();
	}

	@Override
	public Type getGenericActualType() {
		return m_parent.getGenericActualType();
	}

	@Override
	public int getLength() {
		return m_parent.getLength();
	}

	@Override
	public @Nonnull List<DisplayPropertyMetaModel> getLookupSelectedProperties() {
		return m_parent.getLookupSelectedProperties();
	}

	@Override
	public Class< ? extends IRenderInto<T>> getLookupSelectedRenderer() {
		return m_parent.getLookupSelectedRenderer();
	}

	@Override
	public @Nonnull String getName() {
		return m_parent.getName();
	}

	@Override
	public @Nonnull NumericPresentation getNumericPresentation() {
		return m_parent.getNumericPresentation();
	}

	@Override
	public int getPrecision() {
		return m_parent.getPrecision();
	}

	@Override
	public @Nonnull YesNoType getReadOnly() {
		return m_parent.getReadOnly();
	}

	@Override public boolean isReadOnly() {
		return m_parent.isReadOnly();
	}

	@Override
	public String getRegexpUserString() {
		return m_parent.getRegexpUserString();
	}

	@Override
	public String getRegexpValidator() {
		return m_parent.getRegexpValidator();
	}

	@Override
	public @Nonnull PropertyRelationType getRelationType() {
		return m_parent.getRelationType();
	}

	@Override
	public int getScale() {
		return m_parent.getScale();
	}

	@Override
	public @Nonnull SortableType getSortable() {
		return m_parent.getSortable();
	}

	@Override
	public @Nonnull TemporalPresentationType getTemporal() {
		return m_parent.getTemporal();
	}

	@Override
	public @Nonnull PropertyMetaValidator[] getValidators() {
		return m_parent.getValidators();
	}

	@Override
	public boolean isPrimaryKey() {
		return m_parent.isPrimaryKey();
	}

	@Override
	public boolean isRequired() {
		return m_parent.isRequired();
	}

	@Override
	public boolean isTransient() {
		return m_parent.isTransient();
	}

	@Override
	public @Nonnull List<SearchPropertyMetaModel> getLookupFieldSearchProperties() {
		return m_parent.getLookupFieldSearchProperties();
	}

	@Override
	public @Nonnull List<SearchPropertyMetaModel> getLookupFieldKeySearchProperties() {
		return m_parent.getLookupFieldSearchProperties();
	}

	@Override
	public @Nonnull List<DisplayPropertyMetaModel> getLookupTableProperties() {
		return m_parent.getLookupTableProperties();
	}

	@Override
	public IQueryManipulator<T> getQueryManipulator() {
		return m_parent.getQueryManipulator();
	}

	@Nullable
	@Override
	public <A> A getAnnotation(@Nonnull Class<A> annclass) {
		return m_parent.getAnnotation(annclass);
	}

	@Nonnull
	@Override
	public List<Object> getAnnotations() {
		return m_parent.getAnnotations();
	}

	@Override
	public String[] getColumnNames() {
		return m_parent.getColumnNames();
	}

	@Nonnull
	@Override
	public YesNoType getNowrap() {
		return m_parent.getNowrap();
	}
}
