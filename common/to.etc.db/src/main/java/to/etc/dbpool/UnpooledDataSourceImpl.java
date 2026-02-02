/*
 * DomUI Java User Interface - shared code
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
package to.etc.dbpool;

import java.io.*;
import java.sql.*;
import java.util.logging.*;

import javax.sql.*;

public class UnpooledDataSourceImpl implements DataSource {
	final private ConnectionPool m_pool;

	UnpooledDataSourceImpl(ConnectionPool p) {
		m_pool = p;
	}

	public Connection getConnection() throws SQLException {
		return m_pool.getConnection(true);
	}

	public Connection getConnection(String username, String password) throws SQLException {
		return m_pool.getUnpooledConnection(username, password);
	}

	public PrintWriter getLogWriter() throws SQLException {
		return null;
	}

	public void setLogWriter(PrintWriter out) throws SQLException {}

	public void setLoginTimeout(int seconds) throws SQLException {}

	public int getLoginTimeout() throws SQLException {
		return 0;
	}

	public boolean isWrapperFor(Class< ? > iface) throws SQLException {
		return false;
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new IllegalStateException("Cannot unwrap to " + iface);
	}

	public ConnectionPool getPool() {
		return m_pool;
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		throw new SQLFeatureNotSupportedException();
	}
}
