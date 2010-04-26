package to.etc.webapp.qsql;

import java.math.*;
import java.sql.*;
import java.util.*;

import to.etc.util.*;

/**
 * EXPERIMENTAL A record read from the database. Stores all (usable) attributes as a map of values.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 26, 2010
 */
public class JdbcAnyRecord {
	private Map<String, Object> m_valueMap = new HashMap<String, Object>();

	private String m_tableName;

	public void initFromRS(String tablename, ResultSetMetaData rsm, ResultSet rs) throws SQLException {
		m_valueMap.clear();
		m_tableName = tablename;
		for(int i = 1, len = rsm.getColumnCount(); i <= len; i++) {
			int type = rsm.getColumnType(i);
			String name = rsm.getColumnName(i);
			switch(type){
				default:
					throw new IllegalStateException("Cannot handle SQLType=" + type + " for column " + tablename + "." + name);
				case Types.NUMERIC:
				case Types.DECIMAL:
				case Types.FLOAT:
				case Types.DOUBLE:
					BigDecimal bd = rs.getBigDecimal(i);
					set(name, bd);
					break;
				case Types.VARCHAR:
				case Types.CHAR:
					set(name, rs.getString(i));
					break;
			}
		}
	}

	public void set(String name, Object bd) {
		if(bd == null)
			m_valueMap.remove(name);
		else
			m_valueMap.put(name.toLowerCase(), bd);
	}

	public Object get(String name) {
		return m_valueMap.get(name.toLowerCase());
	}

	public <T> T getValue(Class<T> type, String name) {
		Object v = get(name);
		if(v == null)
			return null;
		return (T) RuntimeConversions.convertTo(v, type);
	}


}
