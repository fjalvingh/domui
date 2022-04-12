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
import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.MetaComboProperty;
import to.etc.domui.component.meta.MetaDisplayProperty;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.SortableType;
import to.etc.domui.component.meta.YesNoType;
import to.etc.domui.converter.ConverterRegistry;
import to.etc.domui.converter.DummyConverter;
import to.etc.domui.converter.IConverter;
import to.etc.domui.util.Constants;
import to.etc.webapp.nls.NlsContext;

/**
 * Implementation for a Display Property metamodel. The Display Property data overrides the default
 * metadata for a property in a given display context.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 6, 2009
 */
public class DisplayPropertyMetaModel {
	@NonNull
	final private PropertyMetaModel< ? > m_propertyModel;

	private String m_join;

	private String m_renderHint;

	/**
	 * This is the model of the class that these are <i>in</i>. That is <i>not</i> always the class those properties must be resolved on - that is the <i>target</i> class.
	 */
	private ClassMetaModel m_containedInClass;

	private String m_labelKey;

	private IConverter< ? > m_converter;

	private SortableType m_sortable = SortableType.UNSORTABLE;

	/** The index (order) in which all sortable fields should be applied in an initial sort; -1 if there is no default sort. */
	private int m_sortIndex;

	private int m_displayLength = -1;

	@NonNull
	private YesNoType m_noWrap = YesNoType.UNKNOWN;

	public DisplayPropertyMetaModel(@NonNull PropertyMetaModel< ? > pmm) {
		m_propertyModel = pmm;
		if(null == m_propertyModel)
			throw new IllegalArgumentException("Cannot be null");
	}

	public DisplayPropertyMetaModel(@NonNull ClassMetaModel cmm, @NonNull MetaDisplayProperty p) {
		m_containedInClass = cmm;

		PropertyMetaModel< ? > pmm = cmm.findProperty(p.name());		// Creates either a PathPropertyModel or gets a normal one
		if(null == pmm)
			throw new IllegalStateException("Unknown property " + p.name() + " in " + cmm + " (bad @MetaDisplayProperty)");
		m_propertyModel = pmm;

		if(!Constants.NO_DEFAULT_LABEL.equals(p.defaultLabel()))
			m_labelKey = p.defaultLabel();
		//		setConverter((p.converterClass() == DummyConverter.class ? null : ConverterRegistry.getConverterInstance(p.converterClass())));
		// 20091123 This kludge below (Raw class cast) is needed because otherwise the JDK compiler pukes on this generics abomination.
		IConverter< ? > c = null;
		if(p.converterClass() != DummyConverter.class)
			c = createconv(p.converterClass());
		setConverter(c);

		/*
		 * Sortable: if the root property is unsortable or the display property is unsortable then do not allow sort.
		 * Otherwise, if display property is unknown prefer the pmm's sorting, otherwise use the display property.
		 */
		SortableType sortable = pmm.getSortable();
		if(sortable != SortableType.UNSORTABLE) {
			SortableType ds = p.defaultSortable();
			if(ds == SortableType.UNSORTABLE) {
				sortable = SortableType.UNSORTABLE;
			} else if(ds != SortableType.UNKNOWN) {
				sortable = ds;
			}
		}

		setSortable(sortable);
		setDisplayLength(p.displayLength());
		setNoWrap(p.noWrap());
		m_join = p.join().equals(Constants.NO_JOIN) ? null : p.join();
//		setReadOnly(p.readOnly());		jal 20101220 Removed, unused and seems silly in table display
//		setRenderHint(p.renderHint());	jal 20101220 Removed, unused and seems silly in table display
	}

	public DisplayPropertyMetaModel(@NonNull ClassMetaModel cmm, @NonNull MetaComboProperty p) {
		m_containedInClass = cmm;

		PropertyMetaModel< ? > pmm = cmm.findProperty(p.name());		// Creates either a PathPropertyModel or gets a normal one
		if(null == pmm)
			throw new IllegalStateException("Unknown property " + p.name() + " in " + cmm + " (bad @MetaComboProperty)");
		m_propertyModel = pmm;

		//		setConverter((p.converterClass() == DummyConverter.class ? null : ConverterRegistry.getConverterInstance(p.converterClass())));
		// 20091123 This kludge below (Raw class cast) is needed because otherwise the JDK compiler pukes on this generics abomination.
		IConverter< ? > c = null;
		if(p.converterClass() != DummyConverter.class)
			c = createconv(p.converterClass());
		setConverter(c);
		setSortIndex(p.sortIndex());
		setSortable(p.sortable());
		m_join = p.join().equals(Constants.NO_JOIN) ? null : p.join();
	}

	/**
	 * Idiocy to prevent generics problem.
	 * @param clz
	 * @return
	 */
	@NonNull
	static private <T> IConverter<T> createconv(@NonNull Class< ? > clz) {
		return ConverterRegistry.getConverterInstance((Class< ? extends IConverter<T>>) clz);
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

	@NonNull
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

	@NonNull
	public YesNoType getNoWrap() {
		return m_noWrap;
	}

	public void setNoWrap(@NonNull YesNoType noWrap) {
		m_noWrap = noWrap;
	}
}
