package to.etc.webapp.qsql;

import java.sql.*;

import to.etc.util.*;

/**
 * JDBC converter for [long] and [Long] type.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 25, 2009
 */
public class LongType implements ITypeConverter, IJdbcTypeFactory {
	public int accept(JdbcPropertyMeta pm) {
		return pm.getActualClass() == long.class || pm.getActualClass() == Long.class ? 10 : -1;
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
		long val = rs.getLong(index);
		if(rs.wasNull())
			return null;
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
