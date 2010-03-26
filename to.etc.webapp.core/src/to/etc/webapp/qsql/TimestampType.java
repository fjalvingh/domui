package to.etc.webapp.qsql;

import java.sql.*;

import to.etc.util.*;

/**
 * JDBC converter for full Date type.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 25, 2009
 */
public class TimestampType implements ITypeConverter, IJdbcTypeFactory {
	public int accept(JdbcPropertyMeta pm) {
		return pm.getActualClass() == java.util.Date.class ? 10 : -1;
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
		Timestamp ts = rs.getTimestamp(index);
		if(ts == null)
			return null;
		return new Date(ts.getTime()); // Java Date is sheer, utter horror. Prevent the utter stupidity that is the embedded calendar class.
	}

	public void assignParameter(PreparedStatement ps, int index, JdbcPropertyMeta pm, Object value) throws Exception {
		Timestamp ts;

		if(value instanceof Timestamp)
			ts = (Timestamp) value;
		else if(value instanceof java.util.Date) {
			ts = new Timestamp(((java.util.Date) value).getTime());
		} else if(value == null)
			ts = null;
		else {
			java.util.Date dt = (java.util.Date) RuntimeConversions.convertTo(value, java.util.Date.class);
			ts = new Timestamp(dt.getTime());
		}
		ps.setTimestamp(index, ts);
	}
}
