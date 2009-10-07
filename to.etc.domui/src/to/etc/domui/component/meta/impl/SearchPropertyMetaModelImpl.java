package to.etc.domui.component.meta.impl;

import to.etc.domui.component.meta.*;
import to.etc.webapp.*;

/**
 * Represents the metadata for a field that can be searched on.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 31, 2009
 */
public class SearchPropertyMetaModelImpl implements SearchPropertyMetaModel {
	private DefaultClassMetaModel m_classModel;

	private String m_propertyName;

	private PropertyMetaModel m_property;

	private boolean m_ignoreCase;

	private int m_order;

	private int m_minLength;

	public SearchPropertyMetaModelImpl(DefaultClassMetaModel cmm) {
		m_classModel = cmm;
	}

	/**
	 * Returns the property model for the attached property. This value is usually
	 * set when the @SearchProperty is defined on a property because at that time
	 * the actual property is known. But for @MetaSearch properties the actual
	 * property cannot be set at metadata creation time because they can refer
	 * to <i>other</i> class models. For these properties we do the lookup here
	 * the first time it gets referenced.
	 *
	 * @see to.etc.domui.component.meta.SearchPropertyMetaModel#getProperty()
	 */
	public synchronized PropertyMetaModel getProperty() {
		if(m_property == null && m_propertyName != null) {
			m_property = m_classModel.findProperty(m_propertyName);
			if(m_property == null)
				throw new ProgrammerErrorException("MetaModel error: the search property '" + m_propertyName + "' cannot be located on class=" + m_classModel);
		}
		return m_property;
	}

	/**
	 * The property that is being searched on.
	 *
	 * @param property
	 */
	public void setProperty(DefaultPropertyMetaModel property) {
		m_property = property;
	}

	/**
	 * @see to.etc.domui.component.meta.SearchPropertyMetaModel#isIgnoreCase()
	 */
	public boolean isIgnoreCase() {
		return m_ignoreCase;
	}

	public void setIgnoreCase(boolean ignoreCase) {
		m_ignoreCase = ignoreCase;
	}

	/**
	 * @see to.etc.domui.component.meta.SearchPropertyMetaModel#getOrder()
	 */
	public int getOrder() {
		return m_order;
	}

	public void setOrder(int order) {
		m_order = order;
	}

	/**
	 * @see to.etc.domui.component.meta.SearchPropertyMetaModel#getMinLength()
	 */
	public int getMinLength() {
		return m_minLength;
	}

	public void setMinLength(int minLength) {
		m_minLength = minLength;
	}

	public String getPropertyName() {
		return m_propertyName;
	}

	public void setPropertyName(String propertyName) {
		m_propertyName = propertyName;
	}
}
