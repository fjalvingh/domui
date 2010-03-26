package to.etc.webapp.qsql;

import java.sql.*;

import to.etc.util.*;

/**
 * JDBC converter for [double] and [Double] type.
 *
 * @author <a href="mailto:dprica@execom.eu">Darko Prica</a>
 * Created on 21 Oct 2009
 */
public class DoubleType implements ITypeConverter, IJdbcTypeFactory {
	public int accept(JdbcPropertyMeta pm) {
		return pm.getActualClass() == double.class || pm.getActualClass() == Double.class ? 10 : -1;
	}

	@Override
	public ITypeConverter createType(JdbcPropertyMeta pm) {
		return this;
	}

	@Override
	public int columnCount() {
		return 1;
	}

	/**
	 * @see to.etc.webapp.qsql.ITypeConverter#assignParameter(java.sql.PreparedStatement, int, to.etc.webapp.qsql.JdbcPropertyMeta, java.lang.Object)
	 */
	public void assignParameter(PreparedStatement ps, int index, JdbcPropertyMeta pm, Object value) throws Exception {
		Double doubleValue;
		if(value instanceof Double) {
			doubleValue = (Double) value;
		} else {
			doubleValue = RuntimeConversions.convertToDoubleWrapper(value);
		}

		//-- If this property has a nullity value and is nullable convert to null if needed.
		if(pm.isNullable() && pm.getActualClass().isPrimitive() && pm.getNullValue() != null) {
			double nullValue = Double.parseDouble(pm.getNullValue());
			if(nullValue == doubleValue.doubleValue()) {
				ps.setNull(index, Types.DOUBLE);
				return;
			}
		}

		ps.setDouble(index, doubleValue.doubleValue());
	}

	/**
	 * @see to.etc.webapp.qsql.ITypeConverter#convertToInstance(java.sql.ResultSet, int, to.etc.webapp.qsql.JdbcPropertyMeta)
	 */
	public Object convertToInstance(ResultSet rs, int index) throws Exception {
		double val = rs.getDouble(index);
		if(rs.wasNull())
			return null;
		return Double.valueOf(val);
	}
}
