package to.etc.webapp.qsql;

import java.sql.*;

public class LongType implements ITypeConverter {
	public int accept(JdbcPropertyMeta pm) {
		return 1;
	}

	public Object convertToInstance(ResultSet rs, int index, JdbcPropertyMeta pm) throws Exception {
		long val = rs.getLong(index);
		if(rs.wasNull()) {
			if(! pm.getActualClass().isPrimitive())
				return null;
			if(pm.getNullValue() == null)
				return Long.valueOf(0);
			return Long.valueOf(pm.getNullValue());
		}
		return Long.valueOf(val);
	}
}
