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

import to.etc.domui.component.controlfactory.*;
import to.etc.domui.component.input.*;
import to.etc.domui.component.meta.*;
import to.etc.domui.converter.*;
import to.etc.domui.util.*;
import to.etc.webapp.nls.*;

/**
 * This is a proxy for an existing PropertyMetaModel for path-based properties. This overrides
 * the Accessor and replaces it with an accessor which walks the path to the target property. In
 * addition this uses extended rules to determine the default label and stuff for the extended
 * property.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 29, 2008
 */
public class PathPropertyMetaModel<T> implements PropertyMetaModel<T>, IValueAccessor<T> {
	private PropertyMetaModel<T> m_original;

	/** The full dotted name from the original source to this synthetic property. */
	private String m_dottedName;

	private PropertyMetaModel< ? >[] m_accessPath;

	public PathPropertyMetaModel(String dottedName, PropertyMetaModel< ? >[] accessPath) {
		m_accessPath = accessPath;
		m_original = (PropertyMetaModel<T>) accessPath[accessPath.length - 1];
		m_dottedName = dottedName;
	}

	@Nonnull
	@Override
	public String toString() {
		return "PathProperty[" + m_dottedName + "@" + m_accessPath[0].getClassModel().toString() + "]";
	}

	@Nonnull
	public List<PropertyMetaModel< ? >> getAccessPath() {
		return Arrays.asList(m_accessPath);
	}

	/**
	 * Calculate the value to get. If any path component is null this returns null, it does not
	 * throw exceptions.
	 *
	 * @see to.etc.domui.util.IValueTransformer#getValue(java.lang.Object)
	 */
	@Override
	public T getValue(Object in) throws Exception {
		Object cv = in;
		for(PropertyMetaModel< ? > pmm : m_accessPath) {
			cv = pmm.getValue(cv);
			if(cv == null)
				return null;
		}
		return (T) cv;
	}

	@Override
	public void setValue(Object target, T value) throws Exception {
		Object cv = target;
		for(PropertyMetaModel< ? > pmm : m_accessPath) {
			if(pmm == m_original) { // Reached last segment?
				//-- Actually set a value now
				((IValueAccessor<T>) pmm).setValue(cv, value);
				return;
			}

			cv = pmm.getValue(cv);
			if(cv == null)
				throw new IllegalStateException("In path '" + m_dottedName + "', property '" + pmm.getName() + "' in classModel=" + pmm.getClassModel() + " is null - cannot set a value!!");
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Overrides for labels and hints						*/
	/*--------------------------------------------------------------*/
	/*
	 * We use an extended mechanism to determine text resources for pathbased
	 * properties. This allows overriding of remote properties in the reached
	 * class from within the property file of the root source.
	 * The mechanism is as follows: start to locate the full path name starting
	 * from the current location in the class' bundle.
	 */
	/**
	 *
	 * @see to.etc.domui.component.meta.PropertyMetaModel#getDefaultHint()
	 */
	@Override
	@Nullable
	public String getDefaultHint() {
		return locateProperty("hint");
	}

	/**
	 *
	 * @see to.etc.domui.component.meta.PropertyMetaModel#getDefaultLabel()
	 */
	@Override
	@Nonnull
	public String getDefaultLabel() {
		String toReturn = locateProperty("label");
		return toReturn == null? getName() : toReturn;
	}

	/**
	 * Walk the access path to locate a label/hint for the specified property. For
	 * the property a.b.c it must try the following:
	 * <pre>
	 * a.b.c.label, a.b.*.label, a.*.*.label, a.*.label
	 * Then move to bundle for b, and try:
	 * b.c.label, b.*.label
	 * And finally move to c and try c.label.
	 * </pre>
	 *
	 * @param type
	 * @return
	 */
	@Nullable
	private String locateProperty(String type) {
		Locale loc = NlsContext.getLocale();
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < m_accessPath.length - 1; i++) {
			PropertyMetaModel< ? > pmm = m_accessPath[i];
			BundleRef br = pmm.getClassModel().getClassBundle(); // Current target-path
			String v = attemptProperty(sb, br, i, type, loc);
			if(v != null)
				return v;
		}
		//-- Last resort...
		sb.setLength(0);
		sb.append(m_original.getName());
		sb.append('.');
		sb.append(type);
		BundleRef cb = m_original.getClassModel().getClassBundle();
		if(null != cb) {
			String s = cb.findMessage(loc, sb.toString());
			if(s != null)
				return s;
		}
		return null;
	}

	/**
	 * Starting at the specified level, generate all of the pertinent names and try for
	 * a resource string on 'm.
	 *
	 * @param sb
	 * @param br
	 * @param i
	 * @param type
	 * @param loc
	 * @return
	 */
	private String attemptProperty(StringBuilder sb, BundleRef br, int startix, String type, Locale loc) {
		//-- 1. Try a.b.c, a.b.*, a.*.*
		for(int i = m_accessPath.length; i > startix; i--) {
			sb.setLength(0);
			for(int j = startix; j < m_accessPath.length; j++) {
				if(sb.length() > 0)
					sb.append('.');
				if(j >= i)
					sb.append('*');
				else
					sb.append(m_accessPath[j].getName());
			}

			//-- Gotta name...
			sb.append('.');
			sb.append(type);
			String k = sb.toString();
			//			System.out.println("k=" + k);
			String s = br.findMessage(loc, k);
			if(s != null)
				return s;
		}

		//		//-- try a.b.c.d.e, a.b.c.d.*, a.b.c.*.*, a.b.c.*, a.b.*.*, a.b.*,
		//		for(int i = m_accessPath.length; i > startix; i--) {
		//
		//
		//		}
		//
		// TODO

		return null;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Silly proxies.										*/
	/*--------------------------------------------------------------*/
	@Override
	public @Nonnull Class<T> getActualType() {
		return m_original.getActualType();
	}

	@Override
	public ClassMetaModel getValueModel() {
		return m_original.getValueModel();
	}

	@Override
	public Type getGenericActualType() {
		return m_original.getGenericActualType();
	}

	@Override
	public @Nonnull ClassMetaModel getClassModel() {
		return m_original.getClassModel();
	}

	@Override
	public Class< ? extends IComboDataSet< ? >> getComboDataSet() {
		return m_original.getComboDataSet();
	}

	@Override
	public @Nonnull List<DisplayPropertyMetaModel> getComboDisplayProperties() {
		return m_original.getComboDisplayProperties();
	}

	@Override
	public Class< ? extends ILabelStringRenderer< ? >> getComboLabelRenderer() {
		return m_original.getComboLabelRenderer();
	}

	@Override
	public Class< ? extends IRenderInto<T>> getComboNodeRenderer() {
		return m_original.getComboNodeRenderer();
	}

	@Override
	public String getComponentTypeHint() {
		return m_original.getComponentTypeHint();
	}

	@Override
	public IConverter<T> getConverter() {
		return m_original.getConverter();
	}

	@Override
	public int getDisplayLength() {
		return m_original.getDisplayLength();
	}

	@Override
	public String getDomainValueLabel(Locale loc, Object val) {
		return m_original.getDomainValueLabel(loc, val);
	}

	@Override
	public Object[] getDomainValues() {
		return m_original.getDomainValues();
	}

	@Override
	public int getLength() {
		return m_original.getLength();
	}

	@Override
	public @Nonnull List<DisplayPropertyMetaModel> getLookupSelectedProperties() {
		return m_original.getLookupSelectedProperties();
	}

	@Override
	public Class< ? extends IRenderInto<T>> getLookupSelectedRenderer() {
		return m_original.getLookupSelectedRenderer();
	}

	@Override
	public @Nonnull List<SearchPropertyMetaModel> getLookupFieldSearchProperties() {
		return m_original.getLookupFieldSearchProperties();
	}

	@Override
	public @Nonnull List<SearchPropertyMetaModel> getLookupFieldKeySearchProperties() {
		return m_original.getLookupFieldKeySearchProperties();
	}

	@Override
	public @Nonnull List<DisplayPropertyMetaModel> getLookupTableProperties() {
		return m_original.getLookupTableProperties();
	}

	@Override
	public @Nonnull String getName() {
		return m_dottedName;
	}

	@Override
	public int getPrecision() {
		return m_original.getPrecision();
	}

	@Override
	public @Nonnull YesNoType getReadOnly() {
		return m_original.getReadOnly();
	}

	@Override
	public @Nonnull PropertyRelationType getRelationType() {
		return m_original.getRelationType();
	}

	@Override
	@Nonnull
	public YesNoType getNowrap() {
		return m_original.getNowrap();
	}

	@Override
	public int getScale() {
		return m_original.getScale();
	}

	@Override
	public @Nonnull SortableType getSortable() {
		return m_original.getSortable();
	}

	@Override
	public @Nonnull TemporalPresentationType getTemporal() {
		return m_original.getTemporal();
	}

	@Override
	public @Nonnull NumericPresentation getNumericPresentation() {
		return m_original.getNumericPresentation();
	}

	@Nonnull
	@Override
	public PropertyMetaValidator[] getValidators() {
		return m_original.getValidators();
	}

	@Override
	public boolean isPrimaryKey() {
		return m_original.isPrimaryKey();
	}

	@Override
	public boolean isRequired() {
		return m_original.isRequired();
	}

	@Override
	public boolean isTransient() {
		return m_original.isTransient();
	}

	@Override
	public String getRegexpUserString() {
		return m_original.getRegexpUserString();
	}

	@Override
	public String getRegexpValidator() {
		return m_original.getRegexpValidator();
	}

	@Override
	public PropertyControlFactory getControlFactory() {
		return m_original.getControlFactory();
	}

	@Override
	public <A> A getAnnotation(@Nonnull Class<A> annclass) {
		return m_original.getAnnotation(annclass);
	}

	@Override
	public @Nonnull List<Object> getAnnotations() {
		return m_original.getAnnotations();
	}

	@Nonnull
	@Override
	public String[] getColumnNames() {
		return m_original.getColumnNames();
	}

	@Override
	@Nullable
	public IQueryManipulator<T> getQueryManipulator() {
		return m_original.getQueryManipulator();
	}

	@Override public boolean isReadOnly() {
		return m_original.isReadOnly();
	}
}
