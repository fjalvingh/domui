package to.etc.webapp.qsql;

import java.sql.*;

/**
 * JDBC converter for [String] type.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 25, 2009
 */
class StringType implements ITypeConverter, IJdbcTypeFactory {
	public int accept(JdbcPropertyMeta pm) {
		return pm.getActualClass() == String.class ? 10 : -1;
	}

	@Override
	public ITypeConverter createType(JdbcPropertyMeta pm) {
		return this;
	}

	@Override
	public int columnCount() {
		return 1;
	}

	public Object convertToInstance(ResultSet rs, int index) throws Exception {
		return rs.getString(index);
	}

	public void assignParameter(PreparedStatement ps, int index, JdbcPropertyMeta pm, Object value) throws Exception {
		String s;
		if(value instanceof String)
			s = (String) value;
		else if(value == null)
			s = null;
		else
			s = value.toString();
		ps.setString(index, s);
	}
}
