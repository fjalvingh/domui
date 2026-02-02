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
import java.math.*;
import java.net.*;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.logging.*;

/**
 *	Encapsulates a java.sql.PreparedStatement for NEMA purposes. This class
 *  implements all of the interface, and merely routes all calls to the original
 *  statement.
 *  The close() call is used to remove this from the connection's traced
 *  statement list.
 */
public class PreparedStatementProxy extends StatementProxy implements PreparedStatement {
	private Object[] m_par = new Object[30];

	private int m_maxpar;

	/*--------------------------------------------------------------*/
	/*	CODING:	Changed/intercepted methods..						*/
	/*--------------------------------------------------------------*/

	PreparedStatementProxy(final ConnectionProxy c, final String sql) {
		super(c, sql);
	}

	@Override
	protected void appendQuery(StringBuilder sb) {
		if(getSQL() != null) {
			sb.append("Query: ").append(getSQL()).append("\n");
			if(m_par != null && m_maxpar > 0)
				sb.append(BetterSQLException.format(m_par, m_maxpar)).append("\n");
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	New methods.										*/
	/*--------------------------------------------------------------*/
	/**
	 * Returns the real prepared statement, or throws an error when the statement has
	 * been closed.
	 */
	public PreparedStatement getRealPreparedStatement() {
		return (PreparedStatement) getRealStatement();
	}

	private void _set(int ix, final Object v) {
		if(ix < 1)
			return;
		ix--;
		if(ix >= m_maxpar)
			m_maxpar = ix + 1;
		if(ix >= m_par.length) {
			Object[] nar = new Object[ix + 30];
			System.arraycopy(m_par, 0, nar, 0, m_par.length);
			m_par = nar;
		}
		m_par[ix] = v;
	}

	public Object[] internalGetParameters() {
		Object[] res = new Object[m_maxpar];
		System.arraycopy(m_par, 0, res, 0, m_maxpar);
		return res;
	}

	/**
	 * Override the wrapper to force better exceptions, including the sql parameters.
	 *
	 * @see to.etc.dbpool.StatementProxy#wrap(java.sql.SQLException)
	 */
	@Override
	protected SQLException wrap(final SQLException x) {
		if(pool().c().isPrintExceptions()) {
			System.out.println("----- db: exception in statement -----");
			System.out.println("SQL: " + getSQL());
			System.out.println(BetterSQLException.format(m_par, m_maxpar));
			x.printStackTrace();
		}
		return new BetterSQLException(getSQL(), m_par, m_maxpar, x);
	}

	/*--------------------------------------------------------------*/
	/* CODING: Overrides.                                           */
	/*--------------------------------------------------------------*/
	public ResultSet executeQuery() throws SQLException {
		pool().logExecution(this, ST_QUERY);
		if(LOG.isLoggable(Level.FINE)) {
			LOG.fine("executeQuery(): " + getSQL());
			LOG.fine("parameters:" + BetterSQLException.format(m_par, m_maxpar));
		}
		ResultSetProxy rpx = new ResultSetProxy(this);
		SQLException wx = null;
		try {
			_conn().statsHandler().executePreparedQueryStart(this, rpx);
			rpx.associate(getRealPreparedStatement().executeQuery());
			pool().incOpenRS();
			_conn().addResource(rpx);
			return rpx;
		} catch(SQLException x) {
			wx = wrap(x);
			throw wx;
		} finally {
			_conn().statsHandler().executePreparedQueryEnd(this, wx, rpx);
		}
	}

	public int executeUpdate() throws SQLException {
		pool().logExecution(this, ST_UPDATE);
		if(LOG.isLoggable(Level.FINE)) {
			LOG.fine("executeUpdate(): " + getSQL());
			LOG.fine("parameters:" + BetterSQLException.format(m_par, m_maxpar));
		}
		int rc = -1;
		SQLException wx = null;
		try {
			_conn().statsHandler().executePreparedUpdateStart(this);
			rc = getRealPreparedStatement().executeUpdate();
			return rc;
		} catch(SQLException x) {
			wx = wrap(x);
			throw wx;
		} finally {
			_conn().statsHandler().executePreparedUpdateEnd(this, wx, rc);
		}
	}

	public boolean execute() throws SQLException {
		pool().logExecution(this, ST_EXECUTE);
		if(LOG.isLoggable(Level.FINE))
			LOG.fine("execute called");
		SQLException wx = null;
		boolean res = false;
		try {
			_conn().statsHandler().executeStart(this);
			res = getRealPreparedStatement().execute();
			return res;
		} catch(SQLException x) {
			wx = wrap(x);
			throw wx;
		} finally {
			_conn().statsHandler().executeEnd(this, wx, res);
		}
	}


	/*--------------------------------------------------------------*/
	/* CODING: Simple callthroughs.                                 */
	/*--------------------------------------------------------------*/

	public void setNull(final int parameterIndex, final int sqlType) throws SQLException {
		try {
			_set(parameterIndex, null);
			getRealPreparedStatement().setNull(parameterIndex, sqlType);
		} catch(SQLException x) {
			throw wrap(x);
		}
	}

	public void setBoolean(final int parameterIndex, final boolean x) throws SQLException {
		try {
			_set(parameterIndex, Boolean.valueOf(x));
			getRealPreparedStatement().setBoolean(parameterIndex, x);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setByte(final int parameterIndex, final byte x) throws SQLException {
		try {
			_set(parameterIndex, new Byte(x));
			getRealPreparedStatement().setByte(parameterIndex, x);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setShort(final int parameterIndex, final short x) throws SQLException {
		try {
			_set(parameterIndex, new Short(x));
			getRealPreparedStatement().setShort(parameterIndex, x);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setInt(final int parameterIndex, final int x) throws SQLException {
		try {
			_set(parameterIndex, new Integer(x));
			getRealPreparedStatement().setInt(parameterIndex, x);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setLong(final int parameterIndex, final long x) throws SQLException {
		try {
			_set(parameterIndex, new Long(x));
			getRealPreparedStatement().setLong(parameterIndex, x);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setFloat(final int parameterIndex, final float x) throws SQLException {
		try {
			_set(parameterIndex, new Float(x));
			getRealPreparedStatement().setFloat(parameterIndex, x);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}


	public void setDouble(final int parameterIndex, final double x) throws SQLException {
		try {
			_set(parameterIndex, new Double(x));
			getRealPreparedStatement().setDouble(parameterIndex, x);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setBigDecimal(final int parameterIndex, final BigDecimal x) throws SQLException {
		try {
			_set(parameterIndex, x);
			getRealPreparedStatement().setBigDecimal(parameterIndex, x);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setString(final int parameterIndex, final String x) throws SQLException {
		try {
			_set(parameterIndex, x);
			getRealPreparedStatement().setString(parameterIndex, x);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setBytes(final int parameterIndex, final byte[] x) throws SQLException {
		try {
			_set(parameterIndex, "[bytes]");
			getRealPreparedStatement().setBytes(parameterIndex, x);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setDate(final int parameterIndex, final Date x) throws SQLException {
		try {
			_set(parameterIndex, x);
			getRealPreparedStatement().setDate(parameterIndex, x);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setTime(final int parameterIndex, final Time x) throws SQLException {
		try {
			_set(parameterIndex, x);
			getRealPreparedStatement().setTime(parameterIndex, x);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setTimestamp(final int parameterIndex, final Timestamp x) throws SQLException {
		try {
			_set(parameterIndex, x);
			getRealPreparedStatement().setTimestamp(parameterIndex, x);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setAsciiStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
		try {
			_set(parameterIndex, "[ascii inputstream]");
			getRealPreparedStatement().setAsciiStream(parameterIndex, x, length);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	/**
	 *	@deprecated
	 */
	@Deprecated
	public void setUnicodeStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
		try {
			getRealPreparedStatement().setUnicodeStream(parameterIndex, x, length);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setBinaryStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
		try {
			_set(parameterIndex, "[binary stream]");
			getRealPreparedStatement().setBinaryStream(parameterIndex, x, length);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void clearParameters() throws SQLException {
		try {
			for(int i = m_par.length; --i >= 0;)
				m_par[i] = null;
			getRealPreparedStatement().clearParameters();
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setObject(final int parameterIndex, final Object x, final int targetSqlType, final int scale) throws SQLException {
		try {
			_set(parameterIndex, x);
			getRealPreparedStatement().setObject(parameterIndex, x, targetSqlType, scale);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setObject(final int parameterIndex, final Object x, final int targetSqlType) throws SQLException {
		try {
			_set(parameterIndex, x);
			getRealPreparedStatement().setObject(parameterIndex, x, targetSqlType);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setObject(final int parameterIndex, final Object x) throws SQLException {
		try {
			_set(parameterIndex, x);
			getRealPreparedStatement().setObject(parameterIndex, x);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void addBatch() throws SQLException {
		pool().logExecution(this, true, ST_BATCH);
		if(LOG.isLoggable(Level.FINE)) {
			LOG.fine("addBatch(prepared): " + getSQL());
			LOG.fine("parameters:" + BetterSQLException.format(m_par, m_maxpar));
		}

		try {
			_conn().statsHandler().addBatch(this, getSQL());
			getRealPreparedStatement().addBatch();
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setCharacterStream(final int parameterIndex, final Reader reader, final int length) throws SQLException {
		try {
			getRealPreparedStatement().setCharacterStream(parameterIndex, reader, length);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setRef(final int i, final Ref x) throws SQLException {
		try {
			getRealPreparedStatement().setRef(i, x);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setBlob(final int i, final Blob x) throws SQLException {
		try {
			getRealPreparedStatement().setBlob(i, x);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setClob(final int i, final Clob x) throws SQLException {
		try {
			getRealPreparedStatement().setClob(i, x);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setArray(final int i, final Array x) throws SQLException {
		try {
			getRealPreparedStatement().setArray(i, x);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public ResultSetMetaData getMetaData() throws SQLException {
		try {
			return getRealPreparedStatement().getMetaData();
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setDate(final int parameterIndex, final Date x, final Calendar cal) throws SQLException {
		try {
			_set(parameterIndex, x);
			getRealPreparedStatement().setDate(parameterIndex, x, cal);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setTime(final int parameterIndex, final Time x, final Calendar cal) throws SQLException {
		try {
			_set(parameterIndex, x);
			getRealPreparedStatement().setTime(parameterIndex, x, cal);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setTimestamp(final int parameterIndex, final Timestamp x, final Calendar cal) throws SQLException {
		try {
			_set(parameterIndex, x);
			getRealPreparedStatement().setTimestamp(parameterIndex, x, cal);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setNull(final int paramIndex, final int sqlType, final String typeName) throws SQLException {
		try {
			_set(paramIndex, "[dbnull]");
			getRealPreparedStatement().setNull(paramIndex, sqlType, typeName);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public java.sql.ParameterMetaData getParameterMetaData() throws SQLException {
		try {
			return getRealPreparedStatement().getParameterMetaData();
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setURL(final int id, final URL u) throws SQLException {
		try {
			_set(id, u);
			getRealPreparedStatement().setURL(id, u);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setAsciiStream(int arg0, InputStream arg1, long arg2) throws SQLException {
		try {
			getRealPreparedStatement().setAsciiStream(arg0, arg1, arg2);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setAsciiStream(int arg0, InputStream arg1) throws SQLException {
		try {
			getRealPreparedStatement().setAsciiStream(arg0, arg1);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setBinaryStream(int arg0, InputStream arg1, long arg2) throws SQLException {
		try {
			getRealPreparedStatement().setBinaryStream(arg0, arg1, arg2);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setBinaryStream(int arg0, InputStream arg1) throws SQLException {
		try {
			getRealPreparedStatement().setBinaryStream(arg0, arg1);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setBlob(int arg0, InputStream arg1, long arg2) throws SQLException {
		try {
			getRealPreparedStatement().setBlob(arg0, arg1, arg2);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setBlob(int arg0, InputStream arg1) throws SQLException {
		try {
			getRealPreparedStatement().setBlob(arg0, arg1);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {
		try {
			getRealPreparedStatement().setCharacterStream(arg0, arg1, arg2);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setCharacterStream(int arg0, Reader arg1) throws SQLException {
		try {
			getRealPreparedStatement().setCharacterStream(arg0, arg1);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setClob(int arg0, Reader arg1, long arg2) throws SQLException {
		try {
			getRealPreparedStatement().setClob(arg0, arg1, arg2);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setClob(int arg0, Reader arg1) throws SQLException {
		try {
			getRealPreparedStatement().setClob(arg0, arg1);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}


	public void setNCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {
		try {
			getRealPreparedStatement().setNCharacterStream(arg0, arg1, arg2);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setNCharacterStream(int arg0, Reader arg1) throws SQLException {
		try {
			getRealPreparedStatement().setNCharacterStream(arg0, arg1);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setNClob(int arg0, NClob arg1) throws SQLException {
		try {
			getRealPreparedStatement().setNClob(arg0, arg1);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setNClob(int arg0, Reader arg1, long arg2) throws SQLException {
		try {
			getRealPreparedStatement().setNClob(arg0, arg1, arg2);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setNClob(int arg0, Reader arg1) throws SQLException {
		try {
			getRealPreparedStatement().setNClob(arg0, arg1);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setNString(int arg0, String arg1) throws SQLException {
		try {
			_set(arg0, arg1);
			getRealPreparedStatement().setNString(arg0, arg1);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setRowId(int arg0, RowId arg1) throws SQLException {
		try {
			_set(arg0, arg1);
			getRealPreparedStatement().setRowId(arg0, arg1);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}

	public void setSQLXML(int arg0, SQLXML arg1) throws SQLException {
		try {
			_set(arg0, arg1);
			getRealPreparedStatement().setSQLXML(arg0, arg1);
		} catch(SQLException xx) {
			throw wrap(xx);
		}
	}
}
