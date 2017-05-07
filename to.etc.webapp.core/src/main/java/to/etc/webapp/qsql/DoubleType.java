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

import java.sql.*;

import to.etc.util.*;

/**
 * JDBC converter for [double] and [Double] type.
 *
 * @author <a href="mailto:dprica@execom.eu">Darko Prica</a>
 * Created on 21 Oct 2009
 */
public class DoubleType implements IJdbcType, IJdbcTypeFactory {
	@Override
	public int accept(JdbcPropertyMeta pm) {
		return pm.getActualClass() == double.class || pm.getActualClass() == Double.class ? 10 : -1;
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
	 * @see to.etc.webapp.qsql.IJdbcType#convertToInstance(java.sql.ResultSet, int, to.etc.webapp.qsql.JdbcPropertyMeta)
	 */
	@Override
	public Object convertToInstance(ResultSet rs, int index, JdbcPropertyMeta pm) throws Exception {
		double val = rs.getDouble(index);
		if(rs.wasNull())
			return null;
		return Double.valueOf(val);
	}
}
