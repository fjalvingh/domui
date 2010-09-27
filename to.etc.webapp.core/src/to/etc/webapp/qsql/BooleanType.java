package to.etc.webapp.qsql;

import java.sql.*;

import to.etc.util.*;

/**
 * JDBC converter for [boolean] and [Boolean] type.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Sep 24, 2010
 */
public class BooleanType implements IJdbcType, IJdbcTypeFactory {
	public int accept(JdbcPropertyMeta pm) {
		return pm.getActualClass() == boolean.class || pm.getActualClass() == Boolean.class ? 10 : -1;
	}

	@Override
	public IJdbcType createType(JdbcPropertyMeta pm) {
		return this;
	}

	@Override
	public int columnCount() {
		return 1;
	}

	public Object convertToInstance(ResultSet rs, int index) throws Exception {
		boolean val = rs.getBoolean(index);
		if(rs.wasNull())
			return null;
		return Boolean.valueOf(val);
	}

	public void assignParameter(PreparedStatement ps, int index, JdbcPropertyMeta pm, Object value) throws Exception {
		Boolean iv;
		if(value instanceof Boolean)
			iv = (Boolean) value;
		else
			iv = RuntimeConversions.convertToBooleanWrapper(value);

		//-- If this property has a nullity value and is nullable convert to null if needed.
		if(pm.isNullable() && pm.getActualClass().isPrimitive() && pm.getNullValue() != null) {
			boolean nv = Boolean.parseBoolean(pm.getNullValue());
			if(nv == iv.booleanValue()) {
				ps.setNull(index, Types.BOOLEAN);
				return;
			}
		}

		ps.setBoolean(index, iv.booleanValue());
	}
}
