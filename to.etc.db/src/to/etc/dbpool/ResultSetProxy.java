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

public class ResultSetProxy implements ResultSet {
	private StatementProxy m_statement;

	/** The wrapped result set, */
	private ResultSet m_rs;

	private final IConnectionEventListener m_statsHandler;

	private final ConnectionProxy m_pc;

	private String m_close_rsn;

	/** Copy of the SQL statement causing this resultset, from statement-proxy (if available) */
	private String m_sql;

	private Object[] m_par;

	private Tracepoint m_allocationLocation;

	/** When collecting statistics, the allocation timestamp */
	long m_ts_allocated;

	/** When collecting statistics, the #of rows fetched through this result set. */
	int m_rowCount;

	/** When collecting statistics, the timestamp of close() demarking the end of the fetch cycle. */
	long m_ts_released;

	long m_ts_executeEnd;

	boolean m_prepared;

	ResultSetProxy(final StatementProxy sp) {
		m_statement = sp;
		m_statsHandler = sp._conn().statsHandler();
		m_pc = sp._conn();
		m_sql = sp.getSQL();		// SQL at time of query,

		if(sp instanceof PreparedStatementProxy) {
			m_par = ((PreparedStatementProxy) sp).internalGetParameters();
		}
		if(sp.pool().c().isLogResultSetLocations()) {
			m_allocationLocation = Tracepoint.create(sp.getSQL());
		}
	}

	void associate(ResultSet rs) {
		m_rs = rs;
	}

	public String internalGetCloseReason() {
		return m_close_rsn;
	}

	public String getSQL() {
		return m_sql;
	}

	public int internalGetRowCount() {
		return m_rowCount;
	}

	public long internalGetFetchDuration() {
		return m_ts_released - m_ts_allocated;
	}

	protected void internalDumpInfo() {
		StringBuilder	sb	= new StringBuilder(512);
		if(m_sql != null) {
			sb.append("ResultSet Query: ").append(m_sql).append("\n");
			if(m_par != null && m_par.length > 0)
				sb.append(BetterSQLException.format(m_par, m_par.length)).append("\n");
		}
		if(m_allocationLocation != null) {
			sb.append("ResultSet was allocated at:\n");
			DbPoolUtil.strStacktraceFiltered(sb, m_allocationLocation.getElements());
		}
		if(sb.length() > 0)
			System.out.println(sb);
	}

	public boolean absolute(final int row) throws SQLException {
		return m_rs.absolute(row);
	}

	public void afterLast() throws SQLException {
		m_rs.afterLast();
	}

	public void beforeFirst() throws SQLException {
		m_rs.beforeFirst();
	}

	public void cancelRowUpdates() throws SQLException {
		m_rs.cancelRowUpdates();
	}

	public void clearWarnings() throws SQLException {
		m_rs.clearWarnings();
	}

	public void close() throws SQLException {
		if(m_rs == null)
			return;
		try {
			m_pc.removeResource(this);
			m_rs.close();
		} finally {
			m_rs = null;
		}
		m_statsHandler.resultSetClosed(m_statement, this);
	}

	/**
	 *  This gets called when the Connection was closed, and it closes the
	 *  resources. This only closes the actual statement and does not remove
	 *  the element from the connection's trace list.
	 */
	public void closedByConnection() throws SQLException {
		if(m_rs != null) {
			m_close_rsn = "Closed because connection was closed";
			System.out.println("---- ResultSet forced CLOSED because connection is closed ----");
			internalDumpInfo();
			DbPoolUtil.dumpLocation("Location of close");
			try {
				m_rs.close();
			} finally {
				m_rs = null;
			}
			m_statsHandler.resultSetClosed(m_statement, this);
		}
	}

	public boolean next() throws SQLException {
		boolean r = m_rs.next();
		if(r) {
//			m_collector.incrementRowCount(this);
			m_rowCount++;
		}
		return r;
	}


	public void deleteRow() throws SQLException {
		m_rs.deleteRow();
	}

	public int findColumn(final String columnName) throws SQLException {
		return m_rs.findColumn(columnName);
	}

	public boolean first() throws SQLException {
		return m_rs.first();
	}

	public Array getArray(final int i) throws SQLException {
		return m_rs.getArray(i);
	}

	public Array getArray(final String colName) throws SQLException {
		return m_rs.getArray(colName);
	}

	public InputStream getAsciiStream(final int columnIndex) throws SQLException {
		return m_rs.getAsciiStream(columnIndex);
	}

	public InputStream getAsciiStream(final String columnName) throws SQLException {
		return m_rs.getAsciiStream(columnName);
	}

	/**
	 * @deprecated
	 * @see java.sql.ResultSet#getBigDecimal(int, int)
	 */
	@Deprecated
	public BigDecimal getBigDecimal(final int columnIndex, final int scale) throws SQLException {
		return m_rs.getBigDecimal(columnIndex, scale);
	}

	public BigDecimal getBigDecimal(final int columnIndex) throws SQLException {
		return m_rs.getBigDecimal(columnIndex);
	}

	/**
	 * @deprecated
	 * @see java.sql.ResultSet#getBigDecimal(java.lang.String, int)
	 */
	@Deprecated
	public BigDecimal getBigDecimal(final String columnName, final int scale) throws SQLException {
		return m_rs.getBigDecimal(columnName, scale);
	}

	public BigDecimal getBigDecimal(final String columnName) throws SQLException {
		return m_rs.getBigDecimal(columnName);
	}

	public InputStream getBinaryStream(final int columnIndex) throws SQLException {
		return m_rs.getBinaryStream(columnIndex);
	}

	public InputStream getBinaryStream(final String columnName) throws SQLException {
		return m_rs.getBinaryStream(columnName);
	}

	public Blob getBlob(final int i) throws SQLException {
		return m_rs.getBlob(i);
	}

	public Blob getBlob(final String colName) throws SQLException {
		return m_rs.getBlob(colName);
	}

	public boolean getBoolean(final int columnIndex) throws SQLException {
		return m_rs.getBoolean(columnIndex);
	}

	public boolean getBoolean(final String columnName) throws SQLException {
		return m_rs.getBoolean(columnName);
	}

	public byte getByte(final int columnIndex) throws SQLException {
		return m_rs.getByte(columnIndex);
	}

	public byte getByte(final String columnName) throws SQLException {
		return m_rs.getByte(columnName);
	}

	public byte[] getBytes(final int columnIndex) throws SQLException {
		return m_rs.getBytes(columnIndex);
	}

	public byte[] getBytes(final String columnName) throws SQLException {
		return m_rs.getBytes(columnName);
	}

	public Reader getCharacterStream(final int columnIndex) throws SQLException {
		return m_rs.getCharacterStream(columnIndex);
	}

	public Reader getCharacterStream(final String columnName) throws SQLException {
		return m_rs.getCharacterStream(columnName);
	}

	public Clob getClob(final int i) throws SQLException {
		return m_rs.getClob(i);
	}

	public Clob getClob(final String colName) throws SQLException {
		return m_rs.getClob(colName);
	}

	public int getConcurrency() throws SQLException {
		return m_rs.getConcurrency();
	}

	public String getCursorName() throws SQLException {
		return m_rs.getCursorName();
	}

	public Date getDate(final int columnIndex, final Calendar cal) throws SQLException {
		return m_rs.getDate(columnIndex, cal);
	}

	public Date getDate(final int columnIndex) throws SQLException {
		return m_rs.getDate(columnIndex);
	}

	public Date getDate(final String columnName, final Calendar cal) throws SQLException {
		return m_rs.getDate(columnName, cal);
	}

	public Date getDate(final String columnName) throws SQLException {
		return m_rs.getDate(columnName);
	}

	public double getDouble(final int columnIndex) throws SQLException {
		return m_rs.getDouble(columnIndex);
	}

	public double getDouble(final String columnName) throws SQLException {
		return m_rs.getDouble(columnName);
	}

	public int getFetchDirection() throws SQLException {
		return m_rs.getFetchDirection();
	}

	public int getFetchSize() throws SQLException {
		return m_rs.getFetchSize();
	}

	public float getFloat(final int columnIndex) throws SQLException {
		return m_rs.getFloat(columnIndex);
	}

	public float getFloat(final String columnName) throws SQLException {
		return m_rs.getFloat(columnName);
	}

	public int getInt(final int columnIndex) throws SQLException {
		return m_rs.getInt(columnIndex);
	}

	public int getInt(final String columnName) throws SQLException {
		return m_rs.getInt(columnName);
	}

	public long getLong(final int columnIndex) throws SQLException {
		return m_rs.getLong(columnIndex);
	}

	public long getLong(final String columnName) throws SQLException {
		return m_rs.getLong(columnName);
	}

	public ResultSetMetaData getMetaData() throws SQLException {
		return m_rs.getMetaData();
	}

	@SuppressWarnings("unchecked")
	public Object getObject(final int arg0, @SuppressWarnings("rawtypes") final Map arg1) throws SQLException {
		return m_rs.getObject(arg0, arg1);
	}

	public Object getObject(final int columnIndex) throws SQLException {
		return m_rs.getObject(columnIndex);
	}

	@SuppressWarnings("unchecked")
	public Object getObject(final String arg0, @SuppressWarnings("rawtypes") final Map arg1) throws SQLException {
		return m_rs.getObject(arg0, arg1);
	}

	public Object getObject(final String columnName) throws SQLException {
		return m_rs.getObject(columnName);
	}

	public Ref getRef(final int i) throws SQLException {
		return m_rs.getRef(i);
	}

	public Ref getRef(final String colName) throws SQLException {
		return m_rs.getRef(colName);
	}

	public int getRow() throws SQLException {
		return m_rs.getRow();
	}

	public short getShort(final int columnIndex) throws SQLException {
		return m_rs.getShort(columnIndex);
	}

	public short getShort(final String columnName) throws SQLException {
		return m_rs.getShort(columnName);
	}

	public Statement getStatement() throws SQLException {
		return m_rs.getStatement();
	}

	public String getString(final int columnIndex) throws SQLException {
		return m_rs.getString(columnIndex);
	}

	public String getString(final String columnName) throws SQLException {
		return m_rs.getString(columnName);
	}

	public Time getTime(final int columnIndex, final Calendar cal) throws SQLException {
		return m_rs.getTime(columnIndex, cal);
	}

	public Time getTime(final int columnIndex) throws SQLException {
		return m_rs.getTime(columnIndex);
	}

	public Time getTime(final String columnName, final Calendar cal) throws SQLException {
		return m_rs.getTime(columnName, cal);
	}

	public Time getTime(final String columnName) throws SQLException {
		return m_rs.getTime(columnName);
	}

	public Timestamp getTimestamp(final int columnIndex, final Calendar cal) throws SQLException {
		return m_rs.getTimestamp(columnIndex, cal);
	}

	public Timestamp getTimestamp(final int columnIndex) throws SQLException {
		return m_rs.getTimestamp(columnIndex);
	}

	public Timestamp getTimestamp(final String columnName, final Calendar cal) throws SQLException {
		return m_rs.getTimestamp(columnName, cal);
	}

	public Timestamp getTimestamp(final String columnName) throws SQLException {
		return m_rs.getTimestamp(columnName);
	}

	public int getType() throws SQLException {
		return m_rs.getType();
	}

	/**
	 * @deprecated
	 * @see java.sql.ResultSet#getUnicodeStream(int)
	 */
	@Deprecated
	public InputStream getUnicodeStream(final int columnIndex) throws SQLException {
		return m_rs.getUnicodeStream(columnIndex);
	}

	/**
	 * @deprecated
	 * @see java.sql.ResultSet#getUnicodeStream(java.lang.String)
	 */
	@Deprecated
	public InputStream getUnicodeStream(final String columnName) throws SQLException {
		return m_rs.getUnicodeStream(columnName);
	}

	public URL getURL(final int columnIndex) throws SQLException {
		return m_rs.getURL(columnIndex);
	}

	public URL getURL(final String columnName) throws SQLException {
		return m_rs.getURL(columnName);
	}

	public SQLWarning getWarnings() throws SQLException {
		return m_rs.getWarnings();
	}

	public void insertRow() throws SQLException {
		m_rs.insertRow();
	}

	public boolean isAfterLast() throws SQLException {
		return m_rs.isAfterLast();
	}

	public boolean isBeforeFirst() throws SQLException {
		return m_rs.isBeforeFirst();
	}

	public boolean isFirst() throws SQLException {
		return m_rs.isFirst();
	}

	public boolean isLast() throws SQLException {
		return m_rs.isLast();
	}

	public boolean last() throws SQLException {
		return m_rs.last();
	}

	public void moveToCurrentRow() throws SQLException {
		m_rs.moveToCurrentRow();
	}

	public void moveToInsertRow() throws SQLException {
		m_rs.moveToInsertRow();
	}

	public boolean previous() throws SQLException {
		return m_rs.previous();
	}

	public void refreshRow() throws SQLException {
		m_rs.refreshRow();
	}

	public boolean relative(final int rows) throws SQLException {
		return m_rs.relative(rows);
	}

	public boolean rowDeleted() throws SQLException {
		return m_rs.rowDeleted();
	}

	public boolean rowInserted() throws SQLException {
		return m_rs.rowInserted();
	}

	public boolean rowUpdated() throws SQLException {
		return m_rs.rowUpdated();
	}

	public void setFetchDirection(final int direction) throws SQLException {
		m_rs.setFetchDirection(direction);
	}

	public void setFetchSize(final int rows) throws SQLException {
		m_rs.setFetchSize(rows);
	}

	public void updateArray(final int columnIndex, final Array x) throws SQLException {
		m_rs.updateArray(columnIndex, x);
	}

	public void updateArray(final String columnName, final Array x) throws SQLException {
		m_rs.updateArray(columnName, x);
	}

	public void updateAsciiStream(final int columnIndex, final InputStream x, final int length) throws SQLException {
		m_rs.updateAsciiStream(columnIndex, x, length);
	}

	public void updateAsciiStream(final String columnName, final InputStream x, final int length) throws SQLException {
		m_rs.updateAsciiStream(columnName, x, length);
	}

	public void updateBigDecimal(final int columnIndex, final BigDecimal x) throws SQLException {
		m_rs.updateBigDecimal(columnIndex, x);
	}

	public void updateBigDecimal(final String columnName, final BigDecimal x) throws SQLException {
		m_rs.updateBigDecimal(columnName, x);
	}

	public void updateBinaryStream(final int columnIndex, final InputStream x, final int length) throws SQLException {
		m_rs.updateBinaryStream(columnIndex, x, length);
	}

	public void updateBinaryStream(final String columnName, final InputStream x, final int length) throws SQLException {
		m_rs.updateBinaryStream(columnName, x, length);
	}

	public void updateBlob(final int columnIndex, final Blob x) throws SQLException {
		m_rs.updateBlob(columnIndex, x);
	}

	public void updateBlob(final String columnName, final Blob x) throws SQLException {
		m_rs.updateBlob(columnName, x);
	}

	public void updateBoolean(final int columnIndex, final boolean x) throws SQLException {
		m_rs.updateBoolean(columnIndex, x);
	}

	public void updateBoolean(final String columnName, final boolean x) throws SQLException {
		m_rs.updateBoolean(columnName, x);
	}

	public void updateByte(final int columnIndex, final byte x) throws SQLException {
		m_rs.updateByte(columnIndex, x);
	}

	public void updateByte(final String columnName, final byte x) throws SQLException {
		m_rs.updateByte(columnName, x);
	}

	public void updateBytes(final int columnIndex, final byte[] x) throws SQLException {
		m_rs.updateBytes(columnIndex, x);
	}

	public void updateBytes(final String columnName, final byte[] x) throws SQLException {
		m_rs.updateBytes(columnName, x);
	}

	public void updateCharacterStream(final int columnIndex, final Reader x, final int length) throws SQLException {
		m_rs.updateCharacterStream(columnIndex, x, length);
	}

	public void updateCharacterStream(final String columnName, final Reader reader, final int length) throws SQLException {
		m_rs.updateCharacterStream(columnName, reader, length);
	}

	public void updateClob(final int columnIndex, final Clob x) throws SQLException {
		m_rs.updateClob(columnIndex, x);
	}

	public void updateClob(final String columnName, final Clob x) throws SQLException {
		m_rs.updateClob(columnName, x);
	}

	public void updateDate(final int columnIndex, final Date x) throws SQLException {
		m_rs.updateDate(columnIndex, x);
	}

	public void updateDate(final String columnName, final Date x) throws SQLException {
		m_rs.updateDate(columnName, x);
	}

	public void updateDouble(final int columnIndex, final double x) throws SQLException {
		m_rs.updateDouble(columnIndex, x);
	}

	public void updateDouble(final String columnName, final double x) throws SQLException {
		m_rs.updateDouble(columnName, x);
	}

	public void updateFloat(final int columnIndex, final float x) throws SQLException {
		m_rs.updateFloat(columnIndex, x);
	}

	public void updateFloat(final String columnName, final float x) throws SQLException {
		m_rs.updateFloat(columnName, x);
	}

	public void updateInt(final int columnIndex, final int x) throws SQLException {
		m_rs.updateInt(columnIndex, x);
	}

	public void updateInt(final String columnName, final int x) throws SQLException {
		m_rs.updateInt(columnName, x);
	}

	public void updateLong(final int columnIndex, final long x) throws SQLException {
		m_rs.updateLong(columnIndex, x);
	}

	public void updateLong(final String columnName, final long x) throws SQLException {
		m_rs.updateLong(columnName, x);
	}

	public void updateNull(final int columnIndex) throws SQLException {
		m_rs.updateNull(columnIndex);
	}

	public void updateNull(final String columnName) throws SQLException {
		m_rs.updateNull(columnName);
	}

	public void updateObject(final int columnIndex, final Object x, final int scale) throws SQLException {
		m_rs.updateObject(columnIndex, x, scale);
	}

	public void updateObject(final int columnIndex, final Object x) throws SQLException {
		m_rs.updateObject(columnIndex, x);
	}

	public void updateObject(final String columnName, final Object x, final int scale) throws SQLException {
		m_rs.updateObject(columnName, x, scale);
	}

	public void updateObject(final String columnName, final Object x) throws SQLException {
		m_rs.updateObject(columnName, x);
	}

	public void updateRef(final int columnIndex, final Ref x) throws SQLException {
		m_rs.updateRef(columnIndex, x);
	}

	public void updateRef(final String columnName, final Ref x) throws SQLException {
		m_rs.updateRef(columnName, x);
	}

	public void updateRow() throws SQLException {
		m_rs.updateRow();
	}

	public void updateShort(final int columnIndex, final short x) throws SQLException {
		m_rs.updateShort(columnIndex, x);
	}

	public void updateShort(final String columnName, final short x) throws SQLException {
		m_rs.updateShort(columnName, x);
	}

	public void updateString(final int columnIndex, final String x) throws SQLException {
		m_rs.updateString(columnIndex, x);
	}

	public void updateString(final String columnName, final String x) throws SQLException {
		m_rs.updateString(columnName, x);
	}

	public void updateTime(final int columnIndex, final Time x) throws SQLException {
		m_rs.updateTime(columnIndex, x);
	}

	public void updateTime(final String columnName, final Time x) throws SQLException {
		m_rs.updateTime(columnName, x);
	}

	public void updateTimestamp(final int columnIndex, final Timestamp x) throws SQLException {
		m_rs.updateTimestamp(columnIndex, x);
	}

	public void updateTimestamp(final String columnName, final Timestamp x) throws SQLException {
		m_rs.updateTimestamp(columnName, x);
	}

	public boolean wasNull() throws SQLException {
		return m_rs.wasNull();
	}

	/*----------------- Added garbage from JDK6 ------------------------*/
	public int getHoldability() throws SQLException {
		return m_rs.getHoldability();
	}

	public Reader getNCharacterStream(int columnIndex) throws SQLException {
		return m_rs.getNCharacterStream(columnIndex);
	}

	public Reader getNCharacterStream(String columnLabel) throws SQLException {
		return m_rs.getNCharacterStream(columnLabel);
	}

	public NClob getNClob(int columnIndex) throws SQLException {
		return m_rs.getNClob(columnIndex);
	}

	public NClob getNClob(String columnLabel) throws SQLException {
		return m_rs.getNClob(columnLabel);
	}

	public String getNString(int columnIndex) throws SQLException {
		return m_rs.getNString(columnIndex);
	}

	public String getNString(String columnLabel) throws SQLException {
		return m_rs.getNString(columnLabel);
	}

	public RowId getRowId(int columnIndex) throws SQLException {
		return m_rs.getRowId(columnIndex);
	}

	public RowId getRowId(String columnLabel) throws SQLException {
		return m_rs.getRowId(columnLabel);
	}

	public SQLXML getSQLXML(int columnIndex) throws SQLException {
		return m_rs.getSQLXML(columnIndex);
	}

	public SQLXML getSQLXML(String columnLabel) throws SQLException {
		return m_rs.getSQLXML(columnLabel);
	}

	public boolean isClosed() throws SQLException {
		return m_rs.isClosed();
	}

	public boolean isWrapperFor(Class< ? > iface) throws SQLException {
		return m_rs.isWrapperFor(iface);
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		return m_rs.unwrap(iface);
	}

	public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
		m_rs.updateAsciiStream(columnIndex, x, length);
	}

	public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
		m_rs.updateAsciiStream(columnIndex, x);
	}

	public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
		m_rs.updateAsciiStream(columnLabel, x, length);
	}

	public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
		m_rs.updateAsciiStream(columnLabel, x);
	}

	public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
		m_rs.updateBinaryStream(columnIndex, x, length);
	}

	public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
		m_rs.updateBinaryStream(columnIndex, x);
	}

	public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
		m_rs.updateBinaryStream(columnLabel, x, length);
	}

	public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
		m_rs.updateBinaryStream(columnLabel, x);
	}

	public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
		m_rs.updateBlob(columnIndex, inputStream, length);
	}

	public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
		m_rs.updateBlob(columnIndex, inputStream);
	}

	public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
		m_rs.updateBlob(columnLabel, inputStream, length);
	}

	public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
		m_rs.updateBlob(columnLabel, inputStream);
	}

	public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		m_rs.updateCharacterStream(columnIndex, x, length);
	}

	public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
		m_rs.updateCharacterStream(columnIndex, x);
	}

	public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		m_rs.updateCharacterStream(columnLabel, reader, length);
	}

	public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
		m_rs.updateCharacterStream(columnLabel, reader);
	}

	public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
		m_rs.updateClob(columnIndex, reader, length);
	}

	public void updateClob(int columnIndex, Reader reader) throws SQLException {
		m_rs.updateClob(columnIndex, reader);
	}

	public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
		m_rs.updateClob(columnLabel, reader, length);
	}

	public void updateClob(String columnLabel, Reader reader) throws SQLException {
		m_rs.updateClob(columnLabel, reader);
	}

	public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		m_rs.updateNCharacterStream(columnIndex, x, length);
	}

	public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
		m_rs.updateNCharacterStream(columnIndex, x);
	}

	public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		m_rs.updateNCharacterStream(columnLabel, reader, length);
	}

	public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
		m_rs.updateNCharacterStream(columnLabel, reader);
	}

	public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
		m_rs.updateNClob(columnIndex, nClob);
	}

	public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
		m_rs.updateNClob(columnIndex, reader, length);
	}

	public void updateNClob(int columnIndex, Reader reader) throws SQLException {
		m_rs.updateNClob(columnIndex, reader);
	}

	public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
		m_rs.updateNClob(columnLabel, nClob);
	}

	public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
		m_rs.updateNClob(columnLabel, reader, length);
	}

	public void updateNClob(String columnLabel, Reader reader) throws SQLException {
		m_rs.updateNClob(columnLabel, reader);
	}

	public void updateNString(int columnIndex, String nString) throws SQLException {
		m_rs.updateNString(columnIndex, nString);
	}

	public void updateNString(String columnLabel, String nString) throws SQLException {
		m_rs.updateNString(columnLabel, nString);
	}

	public void updateRowId(int columnIndex, RowId x) throws SQLException {
		m_rs.updateRowId(columnIndex, x);
	}

	public void updateRowId(String columnLabel, RowId x) throws SQLException {
		m_rs.updateRowId(columnLabel, x);
	}

	public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
		m_rs.updateSQLXML(columnIndex, xmlObject);
	}

	public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
		m_rs.updateSQLXML(columnLabel, xmlObject);
	}

	@Override
	public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
		return m_rs.getObject(columnIndex, type);
	}

	@Override
	public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
		return m_rs.getObject(columnLabel, type);
	}
}
