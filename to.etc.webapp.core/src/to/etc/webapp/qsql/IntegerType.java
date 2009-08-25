package to.etc.webapp.qsql;

import java.sql.*;

import to.etc.util.*;

/**
 * JDBC converter for [int] and [Integer] type.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 25, 2009
 */
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

	public void assignParameter(PreparedStatement ps, int index, JdbcPropertyMeta pm, Object value) throws Exception {
		Integer iv;
		if(value instanceof Integer)
			iv = (Integer) value;
		else
			iv = RuntimeConversions.convertToIntegerWrapper(value);

		//-- If this property has a nullity value and is nullable convert to null if needed.
		if(pm.isNullable() && pm.getActualClass().isPrimitive() && pm.getNullValue() != null) {
			int nv = Integer.parseInt(pm.getNullValue());
			if(nv == iv.intValue()) {
				ps.setNull(index, Types.NUMERIC);
				return;
			}
		}

		ps.setInt(index, iv.intValue());
	}
}
