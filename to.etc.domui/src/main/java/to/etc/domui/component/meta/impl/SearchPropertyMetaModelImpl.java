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

import to.etc.domui.component.meta.ClassMetaModel;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.meta.SearchPropertyMetaModel;

import javax.annotation.Nonnull;

/**
 * Represents the metadata for a field that can be searched on.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 31, 2009
 */
public class SearchPropertyMetaModelImpl implements SearchPropertyMetaModel {
	final private ClassMetaModel m_classModel;

	final private PropertyMetaModel<?> m_property;

	private boolean m_ignoreCase;

	private int m_order;

	private int m_minLength;

	private String m_lookupLabelKey;

	private String m_lookupHintKey;

	private boolean m_popupInitiallyCollapsed;

	private boolean m_popupSearchImmediately;

	public SearchPropertyMetaModelImpl(@Nonnull ClassMetaModel cmm, @Nonnull PropertyMetaModel<?> pmm) {
		m_classModel = cmm;
		m_property = pmm;
	}

	@Override
	public PropertyMetaModel<?> getProperty() {
		return m_property;
	}

	//@Override
	//public synchronized List<PropertyMetaModel< ? >> getPropertyPath() {
	//	if(m_propertyPath == null && m_propertyName != null) {
	//		m_propertyPath = MetaManager.parsePropertyPath(m_classModel, m_propertyName);
	//		if(m_propertyPath.size() == 0)
	//			throw new IllegalStateException("? No path for compound property " + m_propertyName + " in " + m_classModel);
	//	}
	//	return m_propertyPath;
	//}
	//
	//public synchronized void setPropertyPath(List<PropertyMetaModel< ? >> propertyPath) {
	//	m_propertyPath = propertyPath;
	//}

	/**
	 * @see to.etc.domui.component.meta.SearchPropertyMetaModel#isIgnoreCase()
	 */
	@Override
	public boolean isIgnoreCase() {
		return m_ignoreCase;
	}

	public void setIgnoreCase(boolean ignoreCase) {
		m_ignoreCase = ignoreCase;
	}

	/**
	 * @see to.etc.domui.component.meta.SearchPropertyMetaModel#getOrder()
	 */
	@Override
	public int getOrder() {
		return m_order;
	}

	public void setOrder(int order) {
		m_order = order;
	}

	/**
	 * @see to.etc.domui.component.meta.SearchPropertyMetaModel#getMinLength()
	 */
	@Override
	public int getMinLength() {
		return m_minLength;
	}

	public void setMinLength(int minLength) {
		m_minLength = minLength;
	}



	//@Override
	//public synchronized String getPropertyName() {
	//	return m_propertyName;
	//}
	//
	//public synchronized void setPropertyName(String propertyName) {
	//	m_propertyName = propertyName;
	//}

	public String getLookupLabelKey() {
		return m_lookupLabelKey;
	}

	public void setLookupLabelKey(String lookupLabelKey) {
		m_lookupLabelKey = lookupLabelKey;
	}

	@Override
	public String getLookupLabel() {
		if(m_lookupLabelKey == null)
			return null;
		return m_classModel.getClassBundle().getString(m_lookupLabelKey);
	}

	public String getLookupHintKey() {
		return m_lookupHintKey;
	}

	public void setLookupHintKey(String lookupHintKey) {
		m_lookupHintKey = lookupHintKey;
	}

	@Override
	public String getLookupHint() {
		if(m_lookupHintKey == null)
			return null;
		return m_classModel.getClassBundle().getString(m_lookupHintKey);
	}

	/**
	 * @see to.etc.domui.component.meta.SearchPropertyMetaModel#isPopupSearchImmediately()
	 */
	@Override
	public boolean isPopupSearchImmediately() {
		return m_popupSearchImmediately;
	}

	public void setPopupSearchImmediately(boolean v) {
		m_popupSearchImmediately = v;
	}

	/**
	 * @see to.etc.domui.component.meta.SearchPropertyMetaModel#isPopupInitiallyCollapsed()
	 */
	@Override
	public boolean isPopupInitiallyCollapsed() {
		return m_popupInitiallyCollapsed;
	}

	public void setPopupInitiallyCollapsed(boolean v) {
		m_popupInitiallyCollapsed = v;
	}
}
