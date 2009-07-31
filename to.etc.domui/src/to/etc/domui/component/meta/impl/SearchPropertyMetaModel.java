package to.etc.domui.component.meta.impl;

/**
 * Represents the metadata for a field that can be searched on.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 31, 2009
 */
public class SearchPropertyMetaModel {
	private DefaultPropertyMetaModel m_property;

	private boolean m_ignoreCase;

	private int m_order;

	private int m_minLength;

	/**
	 * The property that is being searched on.
	 * @return
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
	 * When T (default) the search is done in a case-independent way provided we are looking
	 * for some string value.
	 * @return
	 */
	public boolean isIgnoreCase() {
		return m_ignoreCase;
	}

	public void setIgnoreCase(boolean ignoreCase) {
		m_ignoreCase = ignoreCase;
	}

	/**
	 * The order of this search item in the total list of items. This is only used to
	 * set the display order of the items; they will be ordered by ascending [Order;Name].
	 * @return
	 */
	public int getOrder() {
		return m_order;
	}

	public void setOrder(int order) {
		m_order = order;
	}

	/**
	 * To prevent searching over the entire database you can specify a minimum number
	 * of characters that must be present before the search is allowed on this field. This
	 * would prevent huge searches when only a single letter is entered.
	 * @return
	 */
	public int getMinLength() {
		return m_minLength;
	}

	public void setMinLength(int minLength) {
		m_minLength = minLength;
	}
}
