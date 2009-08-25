package to.etc.webapp.qsql;

import java.sql.*;

/**
 * JDBC converter for [String] type.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 25, 2009
 */
class StringType implements ITypeConverter {
	public int accept(JdbcPropertyMeta pm) {
		return 1;
	}

	public Object convertToInstance(ResultSet rs, int index, JdbcPropertyMeta pm) throws Exception {
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
