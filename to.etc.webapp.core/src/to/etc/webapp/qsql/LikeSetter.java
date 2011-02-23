package to.etc.webapp.qsql;

import java.sql.*;

class LikeSetter implements IQValueSetter {
	private int m_index;

	private String m_value;

	private IJdbcType m_converter;

	private JdbcPropertyMeta m_property;

	public LikeSetter(int index, String like, JdbcPropertyMeta pm) {
		m_index = index;
		m_value = like;
		m_property = pm;
	}

	public JdbcPropertyMeta getProperty() {
		return m_property;
	}

	public int getIndex() {
		return m_index;
	}

	/**
	 * @see to.etc.webapp.qsql.IQValueSetter#assign(java.sql.PreparedStatement)
	 */
	@Override
	public void assign(PreparedStatement ps) throws Exception {
		ps.setString(getIndex(), m_value);
	}
}
