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

import javax.annotation.*;

import to.etc.domui.component.form.*;
import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.converter.*;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;

/**
 * This describes a normalized expanded display property. This is the base version
 * used for all single properties. When a property refers to another class which must
 * be rendered as multiple columns then the derived class ExpandedDisplayPropertyList
 * is used.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 28, 2008
 */
public class ExpandedDisplayProperty<T> implements PropertyMetaModel<T> {
	private ClassMetaModel m_classModel;

	private IValueAccessor< ? > m_rootAccessor;

	private PropertyMetaModel< ? > m_propertyMeta;

	private Class<T> m_actualType;

	private int m_displayLength;

	@Nonnull
	private YesNoType m_noWrap = YesNoType.UNKNOWN;

	private IConverter<T> m_converter;

	@Nonnull
	private SortableType m_sortableType = SortableType.UNKNOWN;

	private String m_propertyName;

	private String m_renderHint;

	private String m_defaultLabel;

	private IConverter< ? > m_bestConverter;

	/**
	* Constructor for LIST types.
	* @param actual
	* @param displayMeta
	* @param propertyMeta
	* @param accessor
	*/
	protected ExpandedDisplayProperty(Class<T> actual, PropertyMetaModel< ? > propertyMeta, IValueAccessor< ? > accessor) {
		m_actualType = actual;
		m_propertyMeta = propertyMeta;
		m_rootAccessor = accessor;
		if(propertyMeta != null) { // ORDER 1
			m_defaultLabel = propertyMeta.getDefaultLabel();
			m_classModel = propertyMeta.getClassModel();
			if(m_sortableType == SortableType.UNKNOWN)
				setSortable(propertyMeta.getSortable());
			if(m_displayLength <= 0) {
				m_displayLength = propertyMeta.getDisplayLength();
				if(m_displayLength <= 0)
					m_displayLength = propertyMeta.getLength();
			}
			m_noWrap = propertyMeta.getNowrap();
		}
	}

	protected ExpandedDisplayProperty(PropertyMetaModel<T> propertyMeta, IValueAccessor<T> accessor) {
		this((DisplayPropertyMetaModel) null, propertyMeta, accessor);
	}

	protected ExpandedDisplayProperty(DisplayPropertyMetaModel displayMeta, PropertyMetaModel<T> propertyMeta, IValueAccessor<T> accessor) {
		//		m_displayMeta = displayMeta;
		m_propertyMeta = propertyMeta;
		m_rootAccessor = accessor;
		if(propertyMeta != null) { // ORDER 1
			m_defaultLabel = propertyMeta.getDefaultLabel();
			m_classModel = propertyMeta.getClassModel();
			m_actualType = propertyMeta.getActualType();
			m_propertyName = propertyMeta.getName();
			m_converter = propertyMeta.getConverter();
			if(m_sortableType == SortableType.UNKNOWN)
				setSortable(propertyMeta.getSortable());
			if(m_displayLength <= 0) {
				m_displayLength = propertyMeta.getDisplayLength();
				if(m_displayLength <= 0)
					m_displayLength = propertyMeta.getLength();
			}
			m_noWrap = propertyMeta.getNowrap();
		}
		if(displayMeta != null) { // ORDER 2 (overrides propertyMeta)
			if(displayMeta.getConverter() != null)
				m_converter = (IConverter<T>) displayMeta.getConverter();
			if(displayMeta.getSortable() != SortableType.UNKNOWN)
				setSortable(displayMeta.getSortable());
			if(displayMeta.getDisplayLength() > 0)
				m_displayLength = displayMeta.getDisplayLength();
			if(displayMeta.getNoWrap() != YesNoType.UNKNOWN)
				m_noWrap = displayMeta.getNoWrap();

			m_renderHint = displayMeta.getRenderHint();

			String s = displayMeta.getLabel();
			if(s != null)
				m_defaultLabel = s;
		}
		if(m_converter != null)
			m_bestConverter = m_converter;
		else if(propertyMeta != null)
			m_bestConverter = ConverterRegistry.findBestConverter(propertyMeta);
	}

	/**
	 * Get the display expansion for a single property. If the property refers to a compound
	 * this will return an {@link ExpandedDisplayPropertyList}.
	 * @param clz
	 * @param property
	 * @return
	 */
	static public ExpandedDisplayProperty< ? > expandProperty(ClassMetaModel cmm, String property) {
		PropertyMetaModel< ? > pmm = cmm.findProperty(property); // Get primary metadata
		if(pmm == null)
			throw new IllegalStateException("Unknown property '" + property + "' on classModel=" + cmm);
		return expandProperty(pmm);
	}

	/**
	 * Get the display expansion for a single property. If the property refers to a compound
	 * this will return an {@link ExpandedDisplayPropertyList}.
	 * @param pmm		The metamodel for the property to expand.
	 */
	static public <X> ExpandedDisplayProperty< ? > expandProperty(PropertyMetaModel<X> pmm) {
		if(pmm == null)
			throw new IllegalArgumentException("Null property???");
		Class< ? > rescl = pmm.getActualType();
		if(!DomUtil.isBasicType(rescl)) {
			ClassMetaModel cmm = MetaManager.findClassMeta(rescl); // Find the property's type metadata (the meta for the class pointed to).

			if(pmm.getLookupTableProperties().size() > 0 || cmm.getTableDisplayProperties().size() > 0) {
				/*
				 * The type pointed to, OR the property itself, has a set-of-columns to use to display the thingy.
				 */
				return expandCompoundProperty(pmm, cmm);
			}
		}

		//-- This is a single property (indivisable). Return it's info.
		return new ExpandedDisplayProperty<X>((DisplayPropertyMetaModel) null, pmm, pmm);
	}

	static public List<ExpandedDisplayProperty< ? >> expandProperties(Class< ? > clz, String[] properties) {
		ClassMetaModel cmm = MetaManager.findClassMeta(clz);
		return expandProperties(cmm, properties);
	}

	static public List<ExpandedDisplayProperty<?>> expandProperties(ClassMetaModel cmm, String[] properties) {
		List<ExpandedDisplayProperty< ? >> res = new ArrayList<ExpandedDisplayProperty< ? >>(properties.length);
		for(String p : properties)
			res.add(expandProperty(cmm, p));
		return res;
	}

	/**
	 * Expands a compound property. If the originating property has a list-of-display-properties
	 * we expand these. The formal expansion strategy is:
	 * <ul>
	 * 	<li>If the core property has a </li>
	 * </ul>
	 *
	 * @param pmm
	 * @param cmm
	 * @return
	 */
	static private <T> ExpandedDisplayProperty< ? > expandCompoundProperty(PropertyMetaModel<T> pmm, ClassMetaModel cmm) {
		List<DisplayPropertyMetaModel> dpl = pmm.getLookupTableProperties(); // Property itself has definition?
		if(dpl.size() == 0) {
			//-- No. Has class-referred-to a default?
			dpl = cmm.getTableDisplayProperties();
			if(dpl.size() == 0) {
				//-- Don't know how to display this. Just use a generic thingy causing a toString().
				return new ExpandedDisplayProperty<T>(pmm, pmm);
			}
		}

		//-- We have a display list! Run the display list expander, using the property accessor as the base accessor.
		List<ExpandedDisplayProperty< ? >> list = expandDisplayProperties(dpl, cmm, pmm);
		return new ExpandedDisplayPropertyList(pmm, pmm, list);
	}

	/**
	 * Enter with a list of display thingies; returns the fully-expanded list of thingeridoos.
	 * @param dpl
	 * @param cmm
	 * @param rootAccessor
	 * @return
	 */
	static public List<ExpandedDisplayProperty< ? >> expandDisplayProperties(List<DisplayPropertyMetaModel> dpl, ClassMetaModel cmm, IValueAccessor< ? > rootAccessor) {
		if(rootAccessor == null)
			rootAccessor = new IdentityAccessor<Object>();
		List<ExpandedDisplayProperty< ? >> res = new ArrayList<ExpandedDisplayProperty< ? >>(dpl.size());
		List<DisplayPropertyMetaModel> joinList = null;
		for(DisplayPropertyMetaModel dpm : dpl) {
			if(dpm.getJoin() != null) {
				if(joinList == null)
					joinList = new ArrayList<DisplayPropertyMetaModel>();
				joinList.add(dpm);
				continue;
			}

			//-- If we have a joinlist left handle that before handling the new property,
			if(joinList != null) {
				joinList.add(dpm);
				res.add(createJoinedProperty(cmm, joinList, rootAccessor));
				joinList = null;
				continue;
			}

			//-- Handle this (normal) property. Is this a DOTTED one?
			PropertyMetaModel< ? > pmm = dpm.getProperty();
			Class< ? > clz = pmm.getActualType();
			List<DisplayPropertyMetaModel> subdpl = pmm.getLookupTableProperties(); // Has defined sub-properties?
			ClassMetaModel pcmm = findCompoundClassModel(clz);
			if(subdpl.size() == 0 && pcmm != null) {
				//-- Has target-class a list of properties?
				subdpl = pcmm.getTableDisplayProperties();
			}

			//-- FIXME Handle embedded
			SubAccessor<Object, Object> sacc = new SubAccessor<Object, Object>((IValueAccessor<Object>) rootAccessor, (IValueAccessor<Object>) pmm);
			if(subdpl.size() != 0) {
				/*
				 * The property here is a COMPOUND property. Explicitly create a subthing for it,
				 */
				List<ExpandedDisplayProperty< ? >> xlist = expandDisplayProperties(subdpl, pcmm, sacc);
				res.add(new ExpandedDisplayPropertyList(pmm, pmm, xlist));
				continue;
			}

			//-- Not a compound type-> add normal expansion (one column)
			ExpandedDisplayProperty<Object> xdp = new ExpandedDisplayProperty<Object>(dpm, (PropertyMetaModel<Object>) pmm, sacc);
			res.add(xdp);
		}

		//-- If we have a joinlist left handle that before exiting.
		if(joinList != null) {
			res.add(createJoinedProperty(cmm, joinList, rootAccessor));
		}
		return res;
	}

	static private ClassMetaModel findCompoundClassModel(Class< ? > clz) {
		if(DomUtil.isBasicType(clz)) // Simple classes have no model
			return null;
		ClassMetaModel cmm = MetaManager.findClassMeta(clz);
		return cmm;
		//		return cmm.getTableDisplayProperties().size() == 0 ? null : cmm;
	}

	/**
	 * This creates a joined property: a list of properties joined together in a string, separated by the join
	 * string.
	 *
	 * @param dpl
	 * @param accessor
	 * @return
	 */
	static private ExpandedDisplayProperty<String> createJoinedProperty(ClassMetaModel cmm, List<DisplayPropertyMetaModel> dpl, IValueAccessor< ? > accessor) {
		//-- Create a paired list of PropertyMetaModel thingies
		List<PropertyMetaModel< ? >> plist = new ArrayList<PropertyMetaModel< ? >>(dpl.size());
		for(DisplayPropertyMetaModel dm : dpl) {
			PropertyMetaModel< ? > pm = dm.getProperty();
			plist.add(pm);
		}
		return new JoinedDisplayProperty(dpl, plist, accessor);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Accessing info on the expanded property.			*/
	/*--------------------------------------------------------------*/

	@Override
	public Class<T> getActualType() {
		return m_actualType;
	}

	@Override
	final public void setValue(Object target, Object value) throws Exception {
		throw new IllegalStateException("Expanded properties cannot be set to a value");
	}

	@Override
	public T getValue(Object in) throws Exception {
		return ((IValueAccessor<T>) m_rootAccessor).getValue(in);
	}

	@Override
	public ClassMetaModel getValueModel() {
		return null;
	}

	/**
	 * Returns null always; this seems reasonable for a type like this.
	 * @see to.etc.domui.component.meta.PropertyMetaModel#getGenericActualType()
	 */
	@Override
	public Type getGenericActualType() {
		return null;
	}

	@Override
	public String getDefaultLabel() {
		return m_defaultLabel;
	}

	@Override
	public IConverter<T> getConverter() {
		return m_converter;
	}

	@Override
	public int getDisplayLength() {
		return m_displayLength;
	}

	//	public void setActualType(Class< ? > actualType) {
	//		m_actualType = actualType;
	//	}

	public void setDisplayLength(int displayLength) {
		m_displayLength = displayLength;
	}

	@Override
	public SortableType getSortable() {
		if(m_sortableType == null)
			throw new IllegalStateException("?? Sortable may never be null??");
		return m_sortableType;
	}

	public void setSortable(SortableType sortableType) {
		if(sortableType == null)
			throw new IllegalStateException("?? Sortable may never be null??");
		m_sortableType = sortableType;
	}

	@Override
	public String getName() {
		return m_propertyName;
	}

	public void setName(String propertyName) {
		m_propertyName = propertyName;
	}

	public IConverter< ? > getBestConverter() {
		return m_bestConverter;
	}

	public String getPresentationString(Object root) throws Exception {
		Object colval = getValue(root);
		String s;
		if(colval == null)
			s = "";
		else {
			//-- FIXME Need to think about generic, non-user-specified properties.
			if(getBestConverter() != null)
				s = ((IObjectToStringConverter<Object>) getBestConverter()).convertObjectToString(NlsContext.getLocale(), colval);
			else
				s = colval.toString();
		}
		return s;
	}

	static public List<ExpandedDisplayProperty< ? >> flatten(List<ExpandedDisplayProperty< ? >> in) {
		List<ExpandedDisplayProperty< ? >> res = new ArrayList<ExpandedDisplayProperty< ? >>(in.size() + 10);
		flatten(res, in);
		return res;
	}

	static private void flatten(List<ExpandedDisplayProperty< ? >> res, List<ExpandedDisplayProperty< ? >> in) {
		for(ExpandedDisplayProperty< ? > xd : in)
			flatten(res, xd);
	}

	static public void flatten(List<ExpandedDisplayProperty<?>> res, ExpandedDisplayProperty<?> xd) {
		if(xd instanceof ExpandedDisplayPropertyList)
			flatten(res, ((ExpandedDisplayPropertyList) xd).getChildren());
		else
			res.add(xd);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	PropertyMetaModel proxy.							*/
	/*--------------------------------------------------------------*/
	/**
	 * This returns the ClassMetaModel for the ROOT of this property(!).
	 * @see to.etc.domui.component.meta.PropertyMetaModel#getClassModel()
	 */
	@Override
	public ClassMetaModel getClassModel() {
		return m_classModel;
	}

	@Override
	public Class< ? extends IComboDataSet< ? >> getComboDataSet() {
		return m_propertyMeta != null ? m_propertyMeta.getComboDataSet() : null;
	}

	@Override
	public List<DisplayPropertyMetaModel> getComboDisplayProperties() {
		return m_propertyMeta == null ? Collections.EMPTY_LIST : m_propertyMeta.getComboDisplayProperties();
	}

	@Override
	public Class< ? extends ILabelStringRenderer< ? >> getComboLabelRenderer() {
		return m_propertyMeta == null ? null : m_propertyMeta.getComboLabelRenderer();
	}

	@Override
	public Class< ? extends INodeContentRenderer< ? >> getComboNodeRenderer() {
		return m_propertyMeta == null ? null : m_propertyMeta.getComboNodeRenderer();
	}

	@Override
	public String getComponentTypeHint() {
		return m_propertyMeta == null ? null : m_propertyMeta.getComponentTypeHint();
	}

	//	public Class< ? extends IConverter> getConverterClass() {
	//		return m_converterClass;
	//	}

	@Override
	public String getDefaultHint() {
		return m_propertyMeta == null ? null : m_propertyMeta.getDefaultHint();
	}

	@Override
	public String getDomainValueLabel(Locale loc, Object val) {
		return m_propertyMeta == null ? null : m_propertyMeta.getDomainValueLabel(loc, val);
	}

	@Override
	public Object[] getDomainValues() {
		return m_propertyMeta == null ? null : m_propertyMeta.getDomainValues();
	}

	@Override
	public String[][] getEditRoles() {
		return m_propertyMeta == null ? null : m_propertyMeta.getEditRoles();
	}

	@Override
	public int getLength() {
		return m_propertyMeta == null ? -1 : m_propertyMeta.getLength();
	}

	@Override
	public List<DisplayPropertyMetaModel> getLookupSelectedProperties() {
		return m_propertyMeta == null ? Collections.EMPTY_LIST : m_propertyMeta.getLookupSelectedProperties();
	}

	@Override
	public List<SearchPropertyMetaModel> getLookupFieldSearchProperties() {
		return m_propertyMeta == null ? Collections.EMPTY_LIST : m_propertyMeta.getLookupFieldSearchProperties();
	}

	@Override
	public List<SearchPropertyMetaModel> getLookupFieldKeySearchProperties() {
		return m_propertyMeta == null ? Collections.EMPTY_LIST : m_propertyMeta.getLookupFieldKeySearchProperties();
	}

	@Override
	public List<DisplayPropertyMetaModel> getLookupTableProperties() {
		return m_propertyMeta == null ? Collections.EMPTY_LIST : m_propertyMeta.getLookupTableProperties();
	}

	@Override
	public Class< ? extends INodeContentRenderer< ? >> getLookupSelectedRenderer() {
		return m_propertyMeta == null ? null : m_propertyMeta.getLookupSelectedRenderer();
	}

	@Override
	public int getPrecision() {
		return m_propertyMeta == null ? -1 : m_propertyMeta.getPrecision();
	}

	@Override
	public YesNoType getReadOnly() {
		return m_propertyMeta == null ? YesNoType.UNKNOWN : m_propertyMeta.getReadOnly();
	}

	@Override
	public PropertyRelationType getRelationType() {
		return m_propertyMeta == null ? PropertyRelationType.NONE : m_propertyMeta.getRelationType();
	}

	@Override
	public int getScale() {
		return m_propertyMeta == null ? -1 : m_propertyMeta.getScale();
	}

	//	public SortableType getSortable() {
	//		return m_propertyMeta == null ? null : m_propertyMeta.getS;
	//	}

	@Override
	public ControlFactory getControlFactory() {
		return m_propertyMeta == null ? null : m_propertyMeta.getControlFactory();
	}

	@Override
	public TemporalPresentationType getTemporal() {
		return m_propertyMeta == null ? TemporalPresentationType.UNKNOWN : m_propertyMeta.getTemporal();
	}

	@Override
	public NumericPresentation getNumericPresentation() {
		return m_propertyMeta == null ? NumericPresentation.UNKNOWN : m_propertyMeta.getNumericPresentation();
	}

	@Nonnull
	@Override
	public PropertyMetaValidator[] getValidators() {
		return m_propertyMeta == null ? NO_VALIDATORS : m_propertyMeta.getValidators();
	}

	@Override
	public String[][] getViewRoles() {
		return m_propertyMeta == null ? null : m_propertyMeta.getViewRoles();
	}

	@Override
	public boolean isPrimaryKey() {
		return m_propertyMeta == null ? false : m_propertyMeta.isPrimaryKey();
	}

	@Override
	public boolean isRequired() {
		return m_propertyMeta == null ? false : m_propertyMeta.isRequired();
	}

	@Override
	public boolean isTransient() {
		return m_propertyMeta == null ? true : m_propertyMeta.isTransient();
	}

	@Nonnull
	@Override
	public YesNoType getNowrap() {
		return m_noWrap;
	}

	public String getRenderHint() {
		return m_renderHint;
	}

	public void setRenderHint(String renderHint) {
		m_renderHint = renderHint;
	}

	@Override
	public String toString() {
		return "ExpandedDisplayProperty[" + m_propertyName + "]";
	}

	@Override
	public String getRegexpUserString() {
		return null;
	}

	@Override
	public String getRegexpValidator() {
		return null;
	}

	/**
	 * Expanded properties do not have annotations. This returns null always.
	 * @see to.etc.domui.component.meta.PropertyMetaModel#getAnnotation(java.lang.Class)
	 */
	@Override
	@Nullable
	public <A> A getAnnotation(Class<A> annclass) {
		return null;
	}

	/**
	 * Expanded properties do not have annotations. This returns the empty list always.
	 * @see to.etc.domui.component.meta.PropertyMetaModel#getAnnotations()
	 */
	@Override
	public List<Object> getAnnotations() {
		return Collections.emptyList();
	}

	/**
	 * Returns empty by definition.
	 * @see to.etc.domui.component.meta.PropertyMetaModel#getColumnNames()
	 */
	@Nonnull
	@Override
	public String[] getColumnNames() {
		return BasicPropertyMetaModel.NO_NAMES;
	}

	@Override
	public IQueryManipulator<T> getQueryManipulator() {
		return null;
	}
}
