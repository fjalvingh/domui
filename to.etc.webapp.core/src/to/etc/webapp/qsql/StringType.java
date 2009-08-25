package to.etc.webapp.qsql;

import java.sql.*;

class StringType implements ITypeConverter {
	public int accept(JdbcPropertyMeta pm) {
		return 1;
	}

	public Object convertToInstance(ResultSet rs, int index, JdbcPropertyMeta pm) throws Exception {
		return rs.getString(index);
	}
}
