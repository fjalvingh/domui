package to.etc.domui.component.meta.impl;

public class SearchPropertyMetaModel {
	private DefaultPropertyMetaModel m_property;

	private boolean m_ignoreCase;

	private int m_order;

	private int m_minLength;

	public DefaultPropertyMetaModel getProperty() {
		return m_property;
	}

	public void setProperty(DefaultPropertyMetaModel property) {
		m_property = property;
	}

	public boolean isIgnoreCase() {
		return m_ignoreCase;
	}

	public void setIgnoreCase(boolean ignoreCase) {
		m_ignoreCase = ignoreCase;
	}

	public int getOrder() {
		return m_order;
	}

	public void setOrder(int order) {
		m_order = order;
	}

	public int getMinLength() {
		return m_minLength;
	}

	public void setMinLength(int minLength) {
		m_minLength = minLength;
	}
}
