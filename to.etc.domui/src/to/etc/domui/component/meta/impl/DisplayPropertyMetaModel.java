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

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.converter.*;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;

/**
 * Implementation for a Display Property metamodel. The Display Property data overrides the default
 * metadata for a property in a given display context.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 6, 2009
 */
public class DisplayPropertyMetaModel {
	@Nonnull
	final private PropertyMetaModel< ? > m_propertyModel;

	private String m_join;

	private String m_renderHint;

	/**
	 * This is the model of the class that these are <i>in</i>. That is <i>not</i> always the class those properties must be resolved on - that is the <i>target</i> class.
	 */
	private ClassMetaModel m_containedInClass;

	private String m_labelKey;

	private IConverter< ? > m_converter;

	private SortableType m_sortable = SortableType.UNKNOWN;

	/** The index (order) in which all sortable fields should be applied in an initial sort; -1 if there is no default sort. */
	private int m_sortIndex;

	private int m_displayLength = -1;

	public DisplayPropertyMetaModel(@Nonnull PropertyMetaModel< ? > pmm) {
		m_propertyModel = pmm;
		if(null == m_propertyModel)
			throw new IllegalArgumentException("Cannot be null");
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public DisplayPropertyMetaModel(@Nonnull ClassMetaModel cmm, @Nonnull MetaDisplayProperty p) {
		m_containedInClass = cmm;
		m_propertyModel = cmm.findProperty(p.name()); // Creates either a PathPropertyModel or gets a normal one
		if(null == m_propertyModel)
			throw new IllegalStateException("Unknown property " + p.name() + " in " + cmm + " (bad @MetaDisplayProperty)");

		if(!Constants.NO_DEFAULT_LABEL.equals(p.defaultLabel()))
			m_labelKey = p.defaultLabel();
		//		setConverter((p.converterClass() == DummyConverter.class ? null : ConverterRegistry.getConverterInstance(p.converterClass())));
		// 20091123 This kludge below (Raw class cast) is needed because otherwise the JDK compiler pukes on this generics abomination.
		IConverter< ? > c = null;
		if(p.converterClass() != DummyConverter.class)
			c = ConverterRegistry.getConverterInstance((Class) p.converterClass());
		setConverter(c);
		setSortable(p.defaultSortable());
		setDisplayLength(p.displayLength());
		m_join = p.join().equals(Constants.NO_JOIN) ? null : p.join();
//		setReadOnly(p.readOnly());		jal 20101220 Removed, unused and seems silly in table display
//		setRenderHint(p.renderHint());	jal 20101220 Removed, unused and seems silly in table display
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public DisplayPropertyMetaModel(@Nonnull ClassMetaModel cmm, @Nonnull MetaComboProperty p) {
		m_containedInClass = cmm;
		m_propertyModel = cmm.findProperty(p.name()); // Creates either a PathPropertyModel or gets a normal one
		if(null == m_propertyModel)
			throw new IllegalStateException("Unknown property " + p.name() + " in " + cmm + " (bad @MetaComboProperty)");
		//		setConverter((p.converterClass() == DummyConverter.class ? null : ConverterRegistry.getConverterInstance(p.converterClass())));
		// 20091123 This kludge below (Raw class cast) is needed because otherwise the JDK compiler pukes on this generics abomination.
		IConverter< ? > c = null;
		if(p.converterClass() != DummyConverter.class)
			c = ConverterRegistry.getConverterInstance((Class) p.converterClass());
		setConverter(c);
		setSortIndex(p.sortIndex());
		setSortable(p.sortable());
		m_join = p.join().equals(Constants.NO_JOIN) ? null : p.join();
	}

	/**
	 * Converts a list of MetaDisplayProperty annotations into their metamodel equivalents.
	 * @param cmm
	 * @param mar
	 * @return
	 */
	static public List<DisplayPropertyMetaModel> decode(ClassMetaModel cmm, MetaDisplayProperty[] mar) {
		List<DisplayPropertyMetaModel> list = new ArrayList<DisplayPropertyMetaModel>(mar.length);
		for(MetaDisplayProperty p : mar) {
			list.add(new DisplayPropertyMetaModel(cmm, p));
		}
		return list;
	}

	/**
	 * Convert a list of combobox display properties to their metamodel equivalents.
	 * @param cmm
	 * @param mar
	 * @return
	 */
	static public List<DisplayPropertyMetaModel> decode(ClassMetaModel cmm, MetaComboProperty[] mar) {
		List<DisplayPropertyMetaModel> list = new ArrayList<DisplayPropertyMetaModel>(mar.length);
		for(MetaComboProperty p : mar) {
			list.add(new DisplayPropertyMetaModel(cmm, p));
		}
		return list;
	}

	//	/**
	//	 * Returns the property name this pertains to. This can be a property path expression.
	//	 * @return
	//	 */
	//	public String getName() {
	//		return m_name;
	//	}
	//
	//	public void setName(String name) {
	//		m_name = name;
	//	}
	//
	/**
	 * If this is joined display property, this returns the string to put between the joined values. Returns
	 * null for unjoined properties.
	 * @return
	 */
	public String getJoin() {
		return m_join;
	}

	public void setJoin(String join) {
		m_join = join;
	}

	/**
	 * If the label for this display property is overridden this returns the value (not the key) for
	 * the overridden label. If this display property does not override the label it returns null. When
	 * the key does not exist in the bundle this returns the key error string (???+key+???).
	 * @return
	 */
	public String getLabel() {
		if(m_labelKey == null)
			return null;

		return m_containedInClass.getClassBundle().getString(m_labelKey);
	}

	@Nonnull
	public PropertyMetaModel< ? > getProperty() {
		return m_propertyModel;
	}

	/**
	 * Returns the attribute as a string value.
	 * @param root
	 * @return
	 */
	public <X, TT extends IConverter<X>> String getAsString(Object root) throws Exception {
		Object value = getProperty().getValue(root);
		if(getConverter() != null)
			return ((TT) getConverter()).convertObjectToString(NlsContext.getLocale(), (X) value);
		return value == null ? "" : value.toString();
	}

	public String getRenderHint() {
		return m_renderHint;
	}

	public void setRenderHint(String renderHint) {
		m_renderHint = renderHint;
	}

	@Override
	public String toString() {
		return "DisplayPropertyMetaModel[" + getProperty().getName() + "]";
	}

	public IConverter< ? > getConverter() {
		return m_converter;
	}

	public void setConverter(IConverter< ? > converter) {
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

	/**
	 * The index (order) in which all sortable fields should be applied in an initial sort; -1 if there is no default sort.
	 * @return
	 */
	public int getSortIndex() {
		return m_sortIndex;
	}

	public void setSortIndex(int sortIndex) {
		m_sortIndex = sortIndex;
	}
}
