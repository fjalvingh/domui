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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.input.IQueryManipulator;
import to.etc.domui.component.input.LookupInput;
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.PropertyRelationType;
import to.etc.domui.component.meta.SearchPropertyMetaModel;
import to.etc.domui.component.meta.YesNoType;
import to.etc.domui.util.IComboDataSet;
import to.etc.domui.util.ILabelStringRenderer;
import to.etc.domui.util.IRenderInto;
import to.etc.domui.util.Msgs;
import to.etc.util.PropertyInfo;
import to.etc.webapp.nls.BundleRef;
import to.etc.webapp.nls.NlsContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class DefaultPropertyMetaModel<T> extends BasicPropertyMetaModel<T> implements PropertyMetaModel<T> {
	@NonNull
	private final DefaultClassMetaModel m_classModel;

	@NonNull
	private final IPropertyModelAccessor<T> m_accessor;

	@NonNull
	private final Class<?> m_actualType;

	@NonNull
	private final Type m_genericActualType;

	@Nullable
	private ClassMetaModel m_valueModel;

	@NonNull
	private String m_name;

	//private final PropertyInfo m_descriptor;

	private int m_length = -1;

	private boolean m_primaryKey;

	@NonNull
	private PropertyRelationType m_relationType = PropertyRelationType.NONE;

	private String m_componentTypeHint;

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

	private Class< ? extends IRenderInto<T>> m_comboNodeRenderer;

	@NonNull
	private List<DisplayPropertyMetaModel> m_comboDisplayProperties = Collections.EMPTY_LIST;

	private IQueryManipulator<T> m_queryManipulator;

	/*---- Lookup stuff. ----*/

	/**
	 * Default renderer which renders a lookup field's "field" contents; this is a table which must be filled with
	 * data pertaining to the looked-up item as a single element on the "edit" screen.
	 */
	private Class< ? extends IRenderInto<T>> m_lookupFieldRenderer;

	/**
	 * The default properties to show in a lookup field's instance display.
	 */
	@NonNull
	private List<DisplayPropertyMetaModel> m_lookupFieldDisplayProperties = Collections.EMPTY_LIST;

	/**
	 * The default properties to show in a {@link LookupInput} field's lookup data table.
	 */
	@NonNull
	private List<DisplayPropertyMetaModel> m_lookupFieldTableProperties = Collections.EMPTY_LIST;

	/**
	 * The search properties to use in a {@link LookupInput} field.
	 */
	@NonNull
	private List<SearchPropertyMetaModel> m_lookupFieldSearchProperties = Collections.EMPTY_LIST;

	/**
	 * The keyword search properties to use in a {@link LookupInput} field.
	 */
	@NonNull
	private List<SearchPropertyMetaModel> m_lookupFieldKeySearchProperties = Collections.EMPTY_LIST;

	public DefaultPropertyMetaModel(@NonNull DefaultClassMetaModel classModel, PropertyInfo descriptor, ClassMetaModel valueModel) {
		if(classModel == null)
			throw new IllegalStateException("Cannot be null dude");
		m_accessor = new JavaPropertyAccessor<>(descriptor);
		m_valueModel = valueModel;
		m_classModel = classModel;
		m_name = descriptor.getName();
		m_actualType = descriptor.getActualType();
		m_genericActualType = descriptor.getActualGenericType();
		if(descriptor.getSetter() == null) {
			setReadOnly(YesNoType.YES);
		}
	}

	public DefaultPropertyMetaModel(@NonNull DefaultClassMetaModel classModel, PropertyInfo descriptor) {
		this(classModel, descriptor, null);
	}

	@NonNull
	@Override
	public String getName() {
		return m_name;
	}

	@NonNull
	@Override
	public Class<T> getActualType() {
		return (Class<T>) m_actualType;
	}

	@Override
	public ClassMetaModel getValueModel() {
		return m_valueModel;
		//return MetaManager.findClassMeta(getActualType());
	}

	@Override
	public Type getGenericActualType() {
		return m_genericActualType;
	}

	@Override
	public void setValue(Object targetInstance, T value) throws Exception {
		if(targetInstance == null)
			throw new IllegalStateException("The 'target' instance object is null");
		try {
			m_accessor.setValue(targetInstance, value);
		} catch(InvocationTargetException itx) {
			Throwable c = itx.getCause();
//			System.err.println("(in calling " + setter + " with input object " + target + " and value " + value + ")");
			if(c instanceof Exception) {
				throw (Exception) c;
			} else if(c instanceof Error)
				throw (Error) c;
			else
				throw itx;
		} catch(IllegalArgumentException x) {
			throw new PropertyValueInvalidException(value, targetInstance, this);
		} catch(Exception x) {
//			System.err.println("(in calling " + setter + " with input object " + target + " and value " + value + ")");
			throw x;
		}
	}

	/**
	 * Retrieve the value from this object. If the input object is null
	 * this throws IllegalStateException.
	 */
	@Override
	public T getValue(Object targetInstance) throws Exception {
		try {
			return m_accessor.getValue(targetInstance);
		} catch(InvocationTargetException itx) {
//			System.err.println(itx + " (in calling " + m_descriptor.getGetter() + " with input object " + in + ")");
			Throwable c = itx.getCause();
			if(c instanceof Exception)
				throw (Exception) c;
			else if(c instanceof Error)
				throw (Error) c;
			else
				throw itx;
		} catch(Exception x) {
			try {
				System.err.println(x + " in calling getter for property " + m_name + " with input object " + targetInstance + "(" + m_accessor + ")");
			} catch(Exception xx) {}
			throw x;
		}
	}

	@Override public boolean isReadOnly() {
		return !m_accessor.isMutable() || getReadOnly() == YesNoType.YES;
	}

	@Override
	@NonNull
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
			sb.append(Msgs.uiBoolTrue.getString());
		else if(val == Boolean.FALSE)
			sb.append(Msgs.uiBoolFalse.getString());
		else if(val instanceof Enum< ? >)
			sb.append(((Enum< ? >) val).name());
		else if(val instanceof Boolean) {
			sb.append(((Boolean)val).booleanValue() ? Msgs.uiBoolTrue.getString() : Msgs.uiBoolFalse.getString());
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

	@NonNull
	@Override
	public List<DisplayPropertyMetaModel> getComboDisplayProperties() {
		return m_comboDisplayProperties;
	}

	public void setComboDisplayProperties(@NonNull final List<DisplayPropertyMetaModel> displayProperties) {
		m_comboDisplayProperties = displayProperties;
	}

	@NonNull
	@Override
	public PropertyRelationType getRelationType() {
		return m_relationType;
	}

	public void setRelationType(@NonNull final PropertyRelationType relationType) {
		m_relationType = relationType;
	}

	@NonNull
	@Override
	public ClassMetaModel getClassModel() {
		return m_classModel;
	}

	@Nullable
	@Override
	public Class< ? extends IRenderInto<T>> getComboNodeRenderer() {
		return m_comboNodeRenderer;
	}

	public void setComboNodeRenderer(@Nullable final Class< ? extends IRenderInto<T>> comboNodeRenderer) {
		m_comboNodeRenderer = comboNodeRenderer;
	}

	@Override
	public String toString() {
		return getClassModel().getActualClass().getName() + "." + m_name + "[" + getActualType().getName() + "]";
	}

	@Override
	public Class< ? extends IRenderInto<T>> getLookupSelectedRenderer() {
		return m_lookupFieldRenderer;
	}

	public void setLookupSelectedRenderer(final Class< ? extends IRenderInto<T>> lookupFieldRenderer) {
		m_lookupFieldRenderer = lookupFieldRenderer;
	}

	@NonNull
	@Override
	public List<DisplayPropertyMetaModel> getLookupSelectedProperties() {
		return m_lookupFieldDisplayProperties;
	}

	public void setLookupSelectedProperties(@NonNull final List<DisplayPropertyMetaModel> lookupFieldDisplayProperties) {
		m_lookupFieldDisplayProperties = lookupFieldDisplayProperties;
	}

	@Override
	@NonNull
	public List<DisplayPropertyMetaModel> getLookupTableProperties() {
		return m_lookupFieldTableProperties;
	}

	public void setLookupTableProperties(@NonNull List<DisplayPropertyMetaModel> lookupFieldTableProperties) {
		m_lookupFieldTableProperties = lookupFieldTableProperties;
	}

	@Override
	@NonNull
	public List<SearchPropertyMetaModel> getLookupFieldSearchProperties() {
		return m_lookupFieldSearchProperties;
	}

	public void setLookupFieldSearchProperties(@NonNull List<SearchPropertyMetaModel> lookupFieldSearchProperties) {
		m_lookupFieldSearchProperties = lookupFieldSearchProperties;
	}

	@Override
	@NonNull
	public List<SearchPropertyMetaModel> getLookupFieldKeySearchProperties() {
		return m_lookupFieldKeySearchProperties;
	}

	public void setLookupFieldKeySearchProperties(@NonNull List<SearchPropertyMetaModel> lookupFieldKeySearchProperties) {
		m_lookupFieldKeySearchProperties = lookupFieldKeySearchProperties;
	}

	@Nullable
	@Override
	public String getComponentTypeHint() {
		return m_componentTypeHint;
	}

	public void setComponentTypeHint(@Nullable final String componentTypeHint) {
		m_componentTypeHint = componentTypeHint;
	}

	@Override
	public IQueryManipulator<T> getQueryManipulator() {
		return m_queryManipulator;
	}

	public void setQueryManipulator(IQueryManipulator<T> queryManipulator) {
		m_queryManipulator = queryManipulator;
	}

	/**
	 * This basic implementation returns annotations on the "getter" method of the property, if
	 * available.
	 */
	@Override
	@Nullable
	public <A> A getAnnotation(@NonNull Class<A> annClass) {
		return m_accessor.getAnnotation(annClass);
	}

	/**
	 * This basic implementation returns all annotations on the "getter" method of the property,
	 * if available. It returns the empty list if nothing is found.
	 */
	@Override
	@NonNull
	public List<Object> getAnnotations() {
		return m_accessor.getAnnotations();
	}
}
