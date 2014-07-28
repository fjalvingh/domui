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
 * JDBC converter for [long] and [Long] type.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 25, 2009
 */
public class LongType implements IJdbcType, IJdbcTypeFactory {
	@Override
	public int accept(JdbcPropertyMeta pm) {
		return pm.getActualClass() == long.class || pm.getActualClass() == Long.class ? 10 : -1;
	}

	@Override
	public IJdbcType createType(JdbcPropertyMeta pm) {
		return this;
	}

	@Override
	public int columnCount() {
		return 1;
	}

	@Override
	public Object convertToInstance(ResultSet rs, int index, JdbcPropertyMeta pm) throws Exception {
		long val = rs.getLong(index);
		if(rs.wasNull())
			return null;
		return Long.valueOf(val);
	}

	@Override
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
