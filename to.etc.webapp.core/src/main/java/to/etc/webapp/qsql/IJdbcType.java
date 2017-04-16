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

import javax.annotation.*;

/**
 * JDBC to java type converter, used to convert column values to Java objects and v.v.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 25, 2009
 */
interface IJdbcType {
	/**
	 * Returns the #of columns occupied by this type.
	 * @return
	 */
	int columnCount();

	/**
	 * Must convert the value at the specified location of the result set to the type represented by this type.
	 * @param rs
	 * @param index
	 * @param pm
	 * @return
	 * @throws Exception
	 */
	Object convertToInstance(@Nonnull ResultSet rs, int index, @Nonnull JdbcPropertyMeta pm) throws Exception;

	void assignParameter(PreparedStatement ps, int index, JdbcPropertyMeta pm, Object value) throws Exception;
}
