package to.etc.webapp.qsql;

import java.sql.*;

import to.etc.util.*;

/**
 * JDBC converter for [long] and [Long] type.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 25, 2009
 */
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

	public void assignParameter(PreparedStatement ps, int index, JdbcPropertyMeta pm, Object value) throws Exception {
		Long iv;
		if(value instanceof Long)
			iv = (Long) value;
		else
			iv = RuntimeConversions.convertToLongWrapper(value);

		//-- If this property has a nullity value and is nullable convert to null if needed.
		if(pm.isNullable() && pm.getActualClass().isPrimitive() && pm.getNullValue() != null) {
			long nv = Long.parseLong(pm.getNullValue());
			if(nv == iv.longValue()) {
				ps.setNull(index, Types.NUMERIC);
				return;
			}
		}

		ps.setLong(index, iv.longValue());
	}
}
