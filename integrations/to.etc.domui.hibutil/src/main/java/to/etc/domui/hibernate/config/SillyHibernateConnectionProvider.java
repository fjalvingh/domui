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
package to.etc.domui.hibernate.config;

import java.sql.*;
import java.util.*;

import javax.sql.*;

import org.hibernate.*;
import org.hibernate.connection.*;

/**
 * Only reason for existence is to provide a DataSource to hibernate dynamically. This class
 * gets instantiated by name by Hibernate config; it will then obtain a datasource from the
 * already-initialized {@link HibernateConfigurator} static member variable.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 30, 2010
 */
final public class SillyHibernateConnectionProvider implements ConnectionProvider {
	private DataSource m_ds;

	public SillyHibernateConnectionProvider() {
		m_ds = HibernateConfigurator.getDataSource();
	}

    @Override
	public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
	public Connection getConnection() throws SQLException {
		Connection dbc = m_ds.getConnection();
    	dbc.setAutoCommit(false);
        return dbc;
    }

    @Override
	public void configure(Properties props) throws HibernateException {
		//-- Useless.
    }

    @Override
	public void closeConnection(Connection conn) throws SQLException {
        conn.close();
    }

    @Override
	public void close() throws HibernateException {
    }
}
