package to.etc.webapp.qsql;

import java.sql.*;

public class EnumAsStringType implements IJdbcType, IJdbcTypeFactory {
	@Override
	public int accept(JdbcPropertyMeta pm) {
		if(Enum.class.isAssignableFrom(pm.getActualClass()))
			return 10;
		return -1;
	}

	@Override
	public IJdbcType createType(JdbcPropertyMeta pm) {
		return this;
	}

	@Override
	public int columnCount() {
		return 1;
	}

	/**
	 * @see to.etc.webapp.qsql.IJdbcType#assignParameter(java.sql.PreparedStatement, int, to.etc.webapp.qsql.JdbcPropertyMeta, java.lang.Object)
	 */
	@Override
	public void assignParameter(PreparedStatement ps, int index, JdbcPropertyMeta pm, Object value) throws Exception {
		Enum< ? > env = (Enum< ? >) value;
		String str = env.name();
		ps.setString(index, str);
	}

	/**
	 * @see to.etc.webapp.qsql.IJdbcType#convertToInstance(java.sql.ResultSet, int, to.etc.webapp.qsql.JdbcPropertyMeta)
	 */
	@Override
	public Object convertToInstance(ResultSet rs, int index, JdbcPropertyMeta pm) throws Exception {
		String val = rs.getString(index);
		if(null == val)
			return null;
		Class<Enum< ? >> clz = (Class<Enum< ? >>) pm.getActualClass();
		for(Enum< ? > en : clz.getEnumConstants()) {
			if(en.name().equals(val))
				return en;
		}
		throw new JdbcConversionException("Could not convert property " + pm + ": value '" + val + "' is not a valid enum value for enum " + pm.getActualClass().getName());
	}
}
