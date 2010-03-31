package to.etc.webapp.qsql;

/**
 * Holds a value and a location (result set) to set a value.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 25, 2009
 */
class ValSetter {
	private int m_index;

	private Object m_value;

	private IJdbcType m_converter;

	private JdbcPropertyMeta m_property;

	public ValSetter(int index, Object value, IJdbcType converter, JdbcPropertyMeta pm) {
		m_index = index;
		m_value = value;
		m_converter = converter;
		m_property = pm;
	}

	public JdbcPropertyMeta getProperty() {
		return m_property;
	}
	public int getIndex() {
		return m_index;
	}

	public Object getValue() {
		return m_value;
	}

	public IJdbcType getConverter() {
		return m_converter;
	}
}
