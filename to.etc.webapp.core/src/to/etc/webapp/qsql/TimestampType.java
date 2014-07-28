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
 * JDBC converter for full Date type.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 25, 2009
 */
public class TimestampType implements IJdbcType, IJdbcTypeFactory {
	@Override
	public int accept(JdbcPropertyMeta pm) {
		return pm.getActualClass() == java.util.Date.class ? 10 : -1;
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
		Timestamp ts = rs.getTimestamp(index);
		if(ts == null)
			return null;
		return new Date(ts.getTime()); // Java Date is sheer, utter horror. Prevent the utter stupidity that is the embedded calendar class.
	}

	@Override
	public void assignParameter(PreparedStatement ps, int index, JdbcPropertyMeta pm, Object value) throws Exception {
		Timestamp ts;

		if(value instanceof Timestamp)
			ts = (Timestamp) value;
		else if(value instanceof java.util.Date) {
			ts = new Timestamp(((java.util.Date) value).getTime());
		} else if(value == null)
			ts = null;
		else {
			java.util.Date dt = RuntimeConversions.convertTo(value, java.util.Date.class);
			ts = new Timestamp(dt.getTime());
		}
		ps.setTimestamp(index, ts);
	}
}
