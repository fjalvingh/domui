package to.etc.domui.component.meta.impl;

import to.etc.domui.component.meta.*;

/**
 * Represents the metadata for a field that can be searched on.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 31, 2009
 */
public class SearchPropertyMetaModelImpl implements SearchPropertyMetaModel {
	private DefaultPropertyMetaModel m_property;

	private boolean m_ignoreCase;

	private int m_order;

	private int m_minLength;

	/**
	 * @see to.etc.domui.component.meta.SearchPropertyMetaModel#getProperty()
	 */
	public DefaultPropertyMetaModel getProperty() {
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
}
