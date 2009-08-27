package to.etc.dbpool;

import java.io.*;
import java.math.*;
import java.net.*;
import java.sql.*;
import java.sql.Date;
import java.util.*;

import to.etc.dbpool.stats.*;

public class ResultSetProxy implements ResultSet {
	/** The wrapped result set, */
	private ResultSet m_rs;

	private final InfoCollector m_collector;

	private final PooledConnection m_pc;

	private String m_close_rsn;

	protected ResultSetProxy(final StatementProxy sp, final ResultSet rs) {
		m_collector = sp._conn().collector();
		m_rs = rs;
		m_pc = sp._conn();
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
		m_pc.m_pe.removeResource(m_pc, this);
		m_rs.close();
	}

	/**
	 *  This gets called when the Connection was closed, and it closes the
	 *  resources. This only closes the actual statement and does not remove
	 *  the element from the connection's trace list.
	 */
	public void closedByConnection() throws SQLException {
		if(m_rs != null) {
			m_close_rsn = "Closed because connection was closed";
			System.out.println("ERROR: ResultSet forced CLOSED because connection is closed");
			DbPoolUtil.dumpLocation("Location of close");
			try {
				m_rs.close();
			} finally {
				m_rs = null;
			}
		}
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

	public Object getObject(final int arg0, final Map arg1) throws SQLException {
		return m_rs.getObject(arg0, arg1);
	}

	public Object getObject(final int columnIndex) throws SQLException {
		return m_rs.getObject(columnIndex);
	}

	public Object getObject(final String arg0, final Map arg1) throws SQLException {
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

	public boolean next() throws SQLException {
		boolean r = m_rs.next();
		if(r)
			m_collector.incrementRowCount(this);
		return r;
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


}
