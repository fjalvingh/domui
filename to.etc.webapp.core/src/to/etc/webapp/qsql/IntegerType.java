package to.etc.webapp.qsql;

import java.sql.*;

public class IntegerType implements ITypeConverter {
	public int accept(JdbcPropertyMeta pm) {
		return 1;
	}

	public Object convertToInstance(ResultSet rs, int index, JdbcPropertyMeta pm) throws Exception {
		int val = rs.getInt(index);
		if(rs.wasNull()) {
			if(!pm.getActualClass().isPrimitive())
				return null;
			if(pm.getNullValue() == null)
				return Integer.valueOf(0);
			return Integer.valueOf(pm.getNullValue());
		}
		return Integer.valueOf(val);
	}
}
