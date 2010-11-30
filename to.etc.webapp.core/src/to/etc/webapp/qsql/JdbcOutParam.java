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
 * OUT parameter definition when calling oracle function/stored procedure that has OUT params.<BR/>
 * See {@link JdbcUtil#oracleSpCall(Connection, Class, String, Object...)}.
 *
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on Sep 16, 2010
 */
public class JdbcOutParam<T> {
	@Nonnull
	final private Class<T> m_classType;

	@Nullable
	private T m_value;

	public JdbcOutParam(@Nonnull Class<T> classType) {
		m_classType = classType;
	}

	@Nullable
	public T getValue() {
		return m_value;
	}

	public void setValue(@Nullable T value) {
		m_value = value;
	}

	@Nonnull
	public Class<T> getClassType() {
		return m_classType;
	}
}
