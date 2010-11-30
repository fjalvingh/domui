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

import java.lang.reflect.*;
import java.util.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.util.*;
import to.etc.util.*;
import to.etc.webapp.nls.*;

public class DefaultPropertyMetaModel extends BasicPropertyMetaModel implements PropertyMetaModel {
	private final DefaultClassMetaModel m_classModel;

	private final PropertyInfo m_descriptor;

	private PropertyAccessor< ? > m_accessor;

	private int m_length = -1;

	private boolean m_primaryKey;

	private PropertyRelationType m_relationType = PropertyRelationType.NONE;

	private String m_componentTypeHint;

	private String m_renderHint;

	/**
	 * If this class is the UP in a relation this specifies that it must
	 * be shown as a COMBOBOX containing choices. It contains a generator
	 * for the values to show. This is a default for all relations in which
	 * this class is the parent; it can be overridden in individual relations.
	 */
	private Class< ? extends IComboDataSet< ? >> m_comboDataSet;

	/**
	 * When this relation-property is presented as a single field this can contain a class to render
	 * that field as a string.
	 * @return
	 */
	private Class< ? extends ILabelStringRenderer< ? >> m_comboLabelRenderer;

	private Class< ? extends INodeContentRenderer< ? >> m_comboNodeRenderer;

	private List<DisplayPropertyMetaModel> m_comboDisplayProperties = Collections.EMPTY_LIST;

	/*---- Lookup stuff. ----*/

	/**
	 * Default renderer which renders a lookup field's "field" contents; this is a table which must be filled with
	 * data pertaining to the looked-up item as a single element on the "edit" screen.
	 */
	private Class< ? extends INodeContentRenderer< ? >> m_lookupFieldRenderer;

	/**
	 * The default properties to show in a lookup field's instance display.
	 */
	private List<DisplayPropertyMetaModel> m_lookupFieldDisplayProperties = Collections.EMPTY_LIST;

	/**
	 * The default properties to show when the collection is presented as a lookup table.
	 */
	private List<DisplayPropertyMetaModel> m_tableDisplayProperties = Collections.EMPTY_LIST;


	public DefaultPropertyMetaModel(final DefaultClassMetaModel classModel, final PropertyInfo descriptor) {
		if(classModel == null)
			throw new IllegalStateException("Cannot be null dude");
		m_classModel = classModel;
		m_descriptor = descriptor;
	}

	@Override
	public String getName() {
		return m_descriptor.getName();
	}

	@Override
	public Class< ? > getActualType() {
		return m_descriptor.getActualType();
	}

	@Override
	public Type getGenericActualType() {
		Method m = m_descriptor.getGetter();
		return m.getGenericReturnType();
	}

	@Override
	public String getDefaultLabel() {
		return m_classModel.getPropertyLabel(this, NlsContext.getLocale());
	}

	@Override
	public String getDefaultHint() {
		return m_classModel.getPropertyHint(this, NlsContext.getLocale());
	}

	static private final Object[] BOOLS = {Boolean.FALSE, Boolean.TRUE};

	/**
	 * FIXME Needs to be filled in by some kind of factory, not in this thingy directly!!
	 * For enum and boolean property types this returns the possible values for the domain. Booleans
	 * always return Boolean.TRUE and Boolean.FALSE; enums return all enum values.
	 * @return
	 */
	@Override
	public Object[] getDomainValues() {
		if(getActualType() == Boolean.TYPE || getActualType() == Boolean.class) {
			return BOOLS;
		}
		if(Enum.class.isAssignableFrom(getActualType())) {
			Class< ? > ec = getActualType();
			return ec.getEnumConstants();
		}
		return null;
	}

	/**
	 * Get a property-related translation for a domain value for this property.
	 *
	 * @see to.etc.domui.component.meta.PropertyMetaModel#getDomainValueLabel(java.util.Locale, java.lang.Object)
	 */
	@Override
	public String getDomainValueLabel(final Locale loc, final Object val) {
		BundleRef b = m_classModel.getClassBundle();
		StringBuilder sb = new StringBuilder();
		sb.append(getName());
		sb.append(".");
		if(val == Boolean.TRUE)
			sb.append("true");
		else if(val == Boolean.FALSE)
			sb.append("false");
		else if(val instanceof Enum< ? >)
			sb.append(((Enum< ? >) val).name());
		else if(val instanceof Boolean) {
			sb.append(((Boolean)val).booleanValue() ? "true" : "false");
		} else
			throw new IllegalStateException("Property value " + val + " for property " + this + " is not an enumerable or boolean domain (class=" + val.getClass() + ")");
		sb.append(".label");

		return b.findMessage(loc, sb.toString()); // jal 20081201 Do not lie about a resource based name!!
	}

	@Override
	public int getLength() {
		return m_length;
	}

	public void setLength(int length) {
		m_length = length;
	}

	/**
	 * The thingy to access the property generically.
	 * @see to.etc.domui.component.meta.PropertyMetaModel#getAccessor()
	 */
	@Override
	public IValueAccessor< ? > getAccessor() {
		return m_accessor;
	}

	public void setAccessor(PropertyAccessor< ? > accessor) {
		m_accessor = accessor;
	}

	@Override
	public boolean isPrimaryKey() {
		return m_primaryKey;
	}

	public void setPrimaryKey(final boolean primaryKey) {
		m_primaryKey = primaryKey;
	}

	@Override
	public Class< ? extends IComboDataSet< ? >> getComboDataSet() {
		return m_comboDataSet;
	}

	public void setComboDataSet(final Class< ? extends IComboDataSet< ? >> comboDataSet) {
		m_comboDataSet = comboDataSet;
	}

	@Override
	public Class< ? extends ILabelStringRenderer< ? >> getComboLabelRenderer() {
		return m_comboLabelRenderer;
	}

	public void setComboLabelRenderer(final Class< ? extends ILabelStringRenderer< ? >> comboLabelRenderer) {
		m_comboLabelRenderer = comboLabelRenderer;
	}

	@Override
	public List<DisplayPropertyMetaModel> getComboDisplayProperties() {
		return m_comboDisplayProperties;
	}

	public void setComboDisplayProperties(final List<DisplayPropertyMetaModel> displayProperties) {
		m_comboDisplayProperties = displayProperties;
	}

	@Override
	public PropertyRelationType getRelationType() {
		return m_relationType;
	}

	public void setRelationType(final PropertyRelationType relationType) {
		m_relationType = relationType;
	}

	@Override
	public ClassMetaModel getClassModel() {
		return m_classModel;
	}

	@Override
	public Class< ? extends INodeContentRenderer< ? >> getComboNodeRenderer() {
		return m_comboNodeRenderer;
	}

	public void setComboNodeRenderer(final Class< ? extends INodeContentRenderer< ? >> comboNodeRenderer) {
		m_comboNodeRenderer = comboNodeRenderer;
	}

	@Override
	public List<DisplayPropertyMetaModel> getTableDisplayProperties() {
		return m_tableDisplayProperties;
	}

	public void setTableDisplayProperties(final List<DisplayPropertyMetaModel> tableDisplayProperties) {
		m_tableDisplayProperties = tableDisplayProperties;
	}

	@Override
	public String toString() {
		return getClassModel().getActualClass().getName() + "." + m_descriptor.getName() + "[" + getActualType().getName() + "]";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class< ? extends INodeContentRenderer< ? >> getLookupFieldRenderer() {
		return m_lookupFieldRenderer;
	}

	public void setLookupFieldRenderer(final Class< ? extends INodeContentRenderer< ? >> lookupFieldRenderer) {
		m_lookupFieldRenderer = lookupFieldRenderer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<DisplayPropertyMetaModel> getLookupFieldDisplayProperties() {
		return m_lookupFieldDisplayProperties;
	}

	public void setLookupFieldDisplayProperties(final List<DisplayPropertyMetaModel> lookupFieldDisplayProperties) {
		m_lookupFieldDisplayProperties = lookupFieldDisplayProperties;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getComponentTypeHint() {
		return m_componentTypeHint;
	}

	public void setComponentTypeHint(final String componentTypeHint) {
		m_componentTypeHint = componentTypeHint;
	}

	public String getRenderHint() {
		return m_renderHint;
	}

	public void setRenderHint(final String renderHint) {
		m_renderHint = renderHint;
	}
}
