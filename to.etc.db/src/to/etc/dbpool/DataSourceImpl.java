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

/**
 * Pooled or unpooled datasource implementation using a connection pool. It
 * only implements the getConnection method; the rest of the silly crap is
 * ignored.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 31, 2006
 */
final public class DataSourceImpl implements DataSource {
	final private ConnectionPool m_pool;

	DataSourceImpl(ConnectionPool p) {
		m_pool = p;
	}

	public ConnectionPool getPool() {
		return m_pool;
	}

	public Connection getConnection() throws SQLException {
		return m_pool.getConnection(false);
	}

	public Connection getConnection(String username, String password) throws SQLException {
		if(!m_pool.c().getUid().equalsIgnoreCase(username))
			throw new SQLException("Bad user ID or password.");
		return getConnection();
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
		if(DataSourceImpl.class == iface)
			return (T) this;
		if(DataSource.class == iface)
			return (T) this;

		throw new IllegalStateException("Cannot unwrap to " + iface);
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		throw new SQLFeatureNotSupportedException();
	}
}
