/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.webapp.qsql;

import java.math.*;
import java.sql.*;
import java.util.*;

import javax.annotation.*;

import to.etc.util.*;

/**
 * EXPERIMENTAL A record read from the database. Stores all (usable) attributes as a map of values.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 26, 2010
 */
public class JdbcAnyRecord {
	static private final Object NULL_VAL = new Object();

	private Map<String, Object> m_valueMap = new HashMap<String, Object>();

	//	private String m_tableName;

	public void initFromRS(String tablename, ResultSetMetaData rsm, @Nonnull ResultSet rs) throws SQLException {
		if(rs == null)
			throw new IllegalStateException("Null rs not allowed");
		m_valueMap.clear();
		//		m_tableName = tablename;
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

				case Types.ROWID:
					set(name, rs.getString(i));
					break;

				case Types.DATE:
				case Types.TIMESTAMP:
					Timestamp ts = rs.getTimestamp(i);
					if(ts == null)
						set(name, NULL_VAL);
					else
						set(name, new java.util.Date(ts.getTime()));
					break;
				case Types.CLOB:
					Clob clob = rs.getClob(i);
					if(clob == null)
						set(name, NULL_VAL);
					else
						set(name, clob);
					break;
			}
		}
	}

	public void set(String name, Object bd) {
		if(bd == null)
			m_valueMap.put(name.toLowerCase(), NULL_VAL);
		else
			m_valueMap.put(name.toLowerCase(), bd);
	}

	public Object get(String name) {
		Object v = m_valueMap.get(name.toLowerCase());
		if(v == NULL_VAL)
			return null;
		return v;
	}

	public <T> T getValue(Class<T> type, String name) {
		return getValue(type, name, null);
	}

	public <T> T getValue(Class<T> type, String name, T defaultValue) {
		Object v = get(name);
		if(v == NULL_VAL)
			return defaultValue;
		if(v == null)
			return defaultValue; // FIXME Should throw column not found exception.
		return RuntimeConversions.convertTo(v, type);
	}

}
