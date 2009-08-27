package to.etc.dbpool;

import java.io.*;
import java.math.*;
import java.net.*;
import java.sql.*;
import java.sql.Date;
import java.util.*;

/**
 * Encapsulates a java.sql.CallableStatement for NEMA purposes. This class
 * implements all of the interface, and merely routes all calls to the original
 * statement.
 * The close() call is used to remove this from the connection's traced
 * statement list.
 */
public class CallableStatementProxy extends PreparedStatementProxy implements CallableStatement {
	/*--------------------------------------------------------------*/
	/*	CODING:	Changed/intercepted methods..						*/
	/*--------------------------------------------------------------*/
	public CallableStatementProxy(PooledConnection c, CallableStatement st, String sql) {
		super(c, st, sql);
	}

	public CallableStatement getRealCallableStatement() {
		return (CallableStatement) getRealStatement();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Callthroughs...										*/
	/*--------------------------------------------------------------*/
	public void registerOutParameter(int parameterIndex, int sqlType) throws SQLException {
		getRealCallableStatement().registerOutParameter(parameterIndex, sqlType);
	}

	public void registerOutParameter(String parameterIndex, int sqlType) throws SQLException {
		getRealCallableStatement().registerOutParameter(parameterIndex, sqlType);
	}

	public void registerOutParameter(String parameterIndex, int sqlType, int scale) throws SQLException {
		getRealCallableStatement().registerOutParameter(parameterIndex, sqlType, scale);
	}

	public void registerOutParameter(int parameterIndex, int sqlType, int scale) throws SQLException {
		getRealCallableStatement().registerOutParameter(parameterIndex, sqlType, scale);
	}

	public boolean wasNull() throws SQLException {
		return getRealCallableStatement().wasNull();
	}

	public String getString(int parameterIndex) throws SQLException {
		return getRealCallableStatement().getString(parameterIndex);
	}

	public boolean getBoolean(int parameterIndex) throws SQLException {
		return getRealCallableStatement().getBoolean(parameterIndex);
	}

	public byte getByte(int parameterIndex) throws SQLException {
		return getRealCallableStatement().getByte(parameterIndex);
	}

	public short getShort(int parameterIndex) throws SQLException {
		return getRealCallableStatement().getShort(parameterIndex);
	}

	public int getInt(int parameterIndex) throws SQLException {
		return getRealCallableStatement().getInt(parameterIndex);
	}

	public long getLong(int parameterIndex) throws SQLException {
		return getRealCallableStatement().getLong(parameterIndex);
	}

	public float getFloat(int parameterIndex) throws SQLException {
		return getRealCallableStatement().getFloat(parameterIndex);
	}

	public double getDouble(int parameterIndex) throws SQLException {
		return getRealCallableStatement().getDouble(parameterIndex);
	}

	/** @deprecated */
	@Deprecated
	public BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException {
		return getRealCallableStatement().getBigDecimal(parameterIndex, scale);
	}

	public byte[] getBytes(int parameterIndex) throws SQLException {
		return getRealCallableStatement().getBytes(parameterIndex);
	}

	public Date getDate(int parameterIndex) throws SQLException {
		return getRealCallableStatement().getDate(parameterIndex);
	}

	public Date getDate(String parameterIndex) throws SQLException {
		return getRealCallableStatement().getDate(parameterIndex);
	}

	public Time getTime(int parameterIndex) throws SQLException {
		return getRealCallableStatement().getTime(parameterIndex);
	}

	public Time getTime(String parameterIndex) throws SQLException {
		return getRealCallableStatement().getTime(parameterIndex);
	}

	public Timestamp getTimestamp(int parameterIndex) throws SQLException {
		return getRealCallableStatement().getTimestamp(parameterIndex);
	}

	public Timestamp getTimestamp(String parameterIndex) throws SQLException {
		return getRealCallableStatement().getTimestamp(parameterIndex);
	}

	public Object getObject(int parameterIndex) throws SQLException {
		return getRealCallableStatement().getObject(parameterIndex);
	}

	public Object getObject(String parameterIndex) throws SQLException {
		return getRealCallableStatement().getObject(parameterIndex);
	}

	public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
		return getRealCallableStatement().getBigDecimal(parameterIndex);
	}

	public Object getObject(int i, Map map) throws SQLException {
		return getRealCallableStatement().getObject(i, map);
	}

	public Ref getRef(int i) throws SQLException {
		return getRealCallableStatement().getRef(i);
	}

	public Blob getBlob(int i) throws SQLException {
		return getRealCallableStatement().getBlob(i);
	}

	public Clob getClob(int i) throws SQLException {
		return getRealCallableStatement().getClob(i);
	}

	public Array getArray(int i) throws SQLException {
		return getRealCallableStatement().getArray(i);
	}

	public Date getDate(int parameterIndex, Calendar cal) throws SQLException {
		return getRealCallableStatement().getDate(parameterIndex, cal);
	}

	public Time getTime(int parameterIndex, Calendar cal) throws SQLException {
		return getRealCallableStatement().getTime(parameterIndex, cal);
	}

	public Timestamp getTimestamp(int parameterIndex, Calendar cal) throws SQLException {
		return getRealCallableStatement().getTimestamp(parameterIndex, cal);
	}

	public void registerOutParameter(int paramIndex, int sqlType, String typeName) throws SQLException {
		getRealCallableStatement().registerOutParameter(paramIndex, sqlType, typeName);
	}

	public void setNull(String parameterIndex, int sqlType) throws SQLException {
		getRealCallableStatement().setNull(parameterIndex, sqlType);
	}

	public void setDate(String parameterIndex, Date x) throws SQLException {
		getRealCallableStatement().setDate(parameterIndex, x);
	}

	public void setTime(String parameterIndex, Time x) throws SQLException {
		getRealCallableStatement().setTime(parameterIndex, x);
	}

	public void setTimestamp(String parameterIndex, Timestamp x) throws SQLException {
		getRealCallableStatement().setTimestamp(parameterIndex, x);
	}

	public void setObject(String parameterIndex, Object x, int targetSqlType, int scale) throws SQLException {
		getRealCallableStatement().setObject(parameterIndex, x, targetSqlType, scale);
	}

	public void setObject(String parameterIndex, Object x, int targetSqlType) throws SQLException {
		getRealCallableStatement().setObject(parameterIndex, x, targetSqlType);
	}


	public URL getURL(String s) throws SQLException {
		return getRealCallableStatement().getURL(s);
	}

	public URL getURL(int s) throws SQLException {
		return getRealCallableStatement().getURL(s);
	}

	public Timestamp getTimestamp(String name, java.util.Calendar cal) throws SQLException {
		return getRealCallableStatement().getTimestamp(name, cal);
	}

	public Time getTime(String name, java.util.Calendar cal) throws SQLException {
		return getRealCallableStatement().getTime(name, cal);
	}

	public Date getDate(String name, java.util.Calendar cal) throws SQLException {
		return getRealCallableStatement().getDate(name, cal);
	}

	public Array getArray(String name) throws SQLException {
		return getRealCallableStatement().getArray(name);
	}

	public Clob getClob(String name) throws SQLException {
		return getRealCallableStatement().getClob(name);
	}

	public Blob getBlob(String name) throws SQLException {
		return getRealCallableStatement().getBlob(name);
	}

	public Ref getRef(String name) throws SQLException {
		return getRealCallableStatement().getRef(name);
	}

	public Object getObject(String name, Map map) throws SQLException {
		return getRealCallableStatement().getObject(name, map);
	}

	public BigDecimal getBigDecimal(String name) throws SQLException {
		return getRealCallableStatement().getBigDecimal(name);
	}

	public byte[] getBytes(String name) throws SQLException {
		return getRealCallableStatement().getBytes(name);
	}

	public double getDouble(String name) throws SQLException {
		return getRealCallableStatement().getDouble(name);
	}

	public float getFloat(String name) throws SQLException {
		return getRealCallableStatement().getFloat(name);
	}

	public long getLong(String name) throws SQLException {
		return getRealCallableStatement().getLong(name);
	}

	public int getInt(String name) throws SQLException {
		return getRealCallableStatement().getInt(name);
	}

	public short getShort(String name) throws SQLException {
		return getRealCallableStatement().getShort(name);
	}

	public byte getByte(String name) throws SQLException {
		return getRealCallableStatement().getByte(name);
	}

	public boolean getBoolean(String name) throws SQLException {
		return getRealCallableStatement().getBoolean(name);
	}

	public String getString(String name) throws SQLException {
		return getRealCallableStatement().getString(name);
	}

	public void setNull(String name, int tt, String type) throws SQLException {
		getRealCallableStatement().setNull(name, tt, type);
	}

	public void setTimestamp(String name, Timestamp ts, Calendar cal) throws SQLException {
		getRealCallableStatement().setTimestamp(name, ts, cal);
	}

	public void setTime(String name, Time t, Calendar cal) throws SQLException {
		getRealCallableStatement().setTime(name, t, cal);
	}

	public void setDate(String name, java.sql.Date dt, Calendar cal) throws SQLException {
		getRealCallableStatement().setDate(name, dt, cal);
	}

	public void setCharacterStream(String name, Reader r, int m) throws SQLException {
		getRealCallableStatement().setCharacterStream(name, r, m);
	}

	public void setObject(String name, Object o) throws SQLException {
		getRealCallableStatement().setObject(name, o);
	}

	public void setBinaryStream(String name, InputStream is, int m) throws SQLException {
		getRealCallableStatement().setBinaryStream(name, is, m);
	}

	public void setAsciiStream(String name, InputStream is, int m) throws SQLException {
		getRealCallableStatement().setAsciiStream(name, is, m);
	}

	public void setBytes(String name, byte[] b) throws SQLException {
		getRealCallableStatement().setBytes(name, b);
	}

	public void setString(String name, String v) throws SQLException {
		getRealCallableStatement().setString(name, v);
	}

	public void setBigDecimal(String name, BigDecimal b) throws SQLException {
		getRealCallableStatement().setBigDecimal(name, b);
	}

	public void setDouble(String name, double d) throws SQLException {
		getRealCallableStatement().setDouble(name, d);
	}

	public void setFloat(String name, float f) throws SQLException {
		getRealCallableStatement().setFloat(name, f);
	}

	public void setLong(String name, long l) throws SQLException {
		getRealCallableStatement().setLong(name, l);
	}

	public void setShort(String name, short l) throws SQLException {
		getRealCallableStatement().setShort(name, l);
	}

	public void setInt(String name, int v) throws SQLException {
		getRealCallableStatement().setInt(name, v);
	}

	public void setByte(String name, byte v) throws SQLException {
		getRealCallableStatement().setByte(name, v);
	}

	public void setBoolean(String name, boolean v) throws SQLException {
		getRealCallableStatement().setBoolean(name, v);
	}

	public void setURL(String name, URL u) throws SQLException {
		getRealCallableStatement().setURL(name, u);
	}

	public void registerOutParameter(String name, int m, String v) throws SQLException {
		getRealCallableStatement().registerOutParameter(name, m, v);
	}
}
