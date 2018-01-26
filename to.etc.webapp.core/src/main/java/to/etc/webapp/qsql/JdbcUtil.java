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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.util.DateUtil;
import to.etc.util.FileTool;
import to.etc.util.StringTool;
import to.etc.webapp.query.IIdentifyable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Utility class for JDBC code.
 *
 *
 * <h2>Preparation for calling SP's with TYPE xx IS RECORD parameters.</h2>
 * <p>Datadict table ALL_PROCEDURES contains all SP's in packages. The parameters for
 * SPs can be glanced from ALL_ARGUMENTS; something odd so far is that doing a selection:
 * <pre>
 * select * from sys.all_arguments where owner='DECADE' and package_name='GEBRUI' and object_name='LEES100';
 * </pre>
 * returns data that seem to indicate that the SP exists as a 2-parameter version but also an expanded version
 * having all parameter fields.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 21, 2009
 */
final public class JdbcUtil {
	static public final Logger LOG = LoggerFactory.getLogger(JdbcUtil.class);

	private JdbcUtil() {}

	static public void setLong(@Nonnull PreparedStatement ps, int index, Long value) throws SQLException {
		if(value == null)
			ps.setNull(index, Types.NUMERIC);
		else
			ps.setLong(index, value.longValue());
	}

	static public void setInteger(@Nonnull PreparedStatement ps, int index, Integer value) throws SQLException {
		if(value == null)
			ps.setNull(index, Types.NUMERIC);
		else
			ps.setInt(index, value.intValue());
	}

	static public void setDouble(@Nonnull PreparedStatement ps, int index, Double value) throws SQLException {
		if(value == null)
			ps.setNull(index, Types.DOUBLE);
		else
			ps.setDouble(index, value.doubleValue());
	}

	/**
	 * Sets a TIMESTAMP value containing both TIME and DATE values.
	 * @param ps
	 * @param index
	 * @param value
	 * @throws SQLException
	 */
	static public void setTimestamp(@Nonnull PreparedStatement ps, int index, java.util.Date value) throws SQLException {
		if(value == null)
			ps.setNull(index, Types.TIMESTAMP);
		else if(value instanceof Timestamp)
			ps.setTimestamp(index, (Timestamp) value);
		else
			ps.setTimestamp(index, new Timestamp(value.getTime()));
	}

	/**
	 * Sets a <b>truncated</b> date containing <i>only</i> the date part and a zero time.
	 * @param ps
	 * @param index
	 * @param value
	 * @throws SQLException
	 */
	static public void setDateTruncated(@Nonnull PreparedStatement ps, int index, java.util.Date value) throws SQLException {
		if(value == null)
			ps.setNull(index, Types.TIMESTAMP);
		else {
			value = DateUtil.truncateDate(value);
			ps.setDate(index, new java.sql.Date(value.getTime()));
		}
	}

	/**
	 * Sets a date..
	 */
	static public void setDate(@Nonnull PreparedStatement ps, int index, java.util.Date value) throws SQLException {
		if(value == null)
			ps.setNull(index, Types.DATE);
		else {
			ps.setDate(index, new java.sql.Date(value.getTime()));
		}
	}


	static public void setString(@Nonnull PreparedStatement ps, int index, String value) throws SQLException {
		if(value == null || value.trim().length() == 0)
			ps.setNull(index, Types.VARCHAR);
		else
			ps.setString(index, value);
	}

	static public void setStringTruncated(@Nonnull PreparedStatement ps, int index, String value, int maxlen) throws SQLException {
		if(value == null || value.trim().length() == 0)
			ps.setNull(index, Types.VARCHAR);
		else {
			int len = value.length();
			if(len > maxlen)
				value = value.substring(0, maxlen);
			ps.setString(index, value);
		}
	}

	static public void setYN(@Nonnull PreparedStatement ps, int index, Boolean value) throws SQLException {
		if(value == null)
			ps.setNull(index, Types.VARCHAR);
		else
			ps.setString(index, value.booleanValue() ? "Y" : "N");
	}

	static public void setFK(@Nonnull PreparedStatement ps, int index, @Nullable IIdentifyable< ? extends Number> foreigner) throws SQLException {
		if(foreigner == null)
			ps.setNull(index, Types.NUMERIC);
		else {
			Number id = foreigner.getId();
			if(id == null)
				throw new IllegalStateException("Reference to foreign object '" + foreigner + "' has a null ID.");
			else {
				ps.setLong(index, id.longValue());
			}
		}
	}

	/**
	 * Quick method to select a single value of a given type from the database. Returns null if not found AND if the value was null...
	 * @param connection
	 * @param clz
	 * @param select
	 * @return
	 */
	public static <T> T selectOne(@Nonnull Connection connection, @Nonnull Class<T> clz, @Nonnull String select, @Nonnull Object... params) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = connection.prepareStatement(select);
			setParameters(ps, 1, params);
			logDebug("selectOne", select, params);
			rs = ps.executeQuery();
			if(! rs.next())
				return null;
			Object res = null;
			if(clz == String.class)
				return (T) rs.getString(1);
			else if(clz == Long.class || clz == long.class)
				res = Long.valueOf(rs.getLong(1));
			else if(clz == Integer.class || clz == int.class)
				res = Integer.valueOf(rs.getInt(1));
			else if(clz == Double.class || clz == double.class)
				res = Double.valueOf(rs.getDouble(1));
			else if(clz == BigDecimal.class)
				res = rs.getBigDecimal(1);
			else if(clz == java.util.Date.class) {
				java.sql.Timestamp ts = rs.getTimestamp(1);
				if(ts != null)
					res = new java.util.Date(ts.getTime());
			} else if(clz == Boolean.class || clz == boolean.class)
				res = Boolean.valueOf(rs.getBoolean(1));
			else if(clz == Blob.class)
				res = rs.getBlob(1);
			else if(clz == Clob.class)
				res = rs.getClob(1);
			else
				throw new IllegalStateException("Call error: cannot handle requested return type " + clz);
			if(rs.wasNull())
				return null;
			return (T) res;
		} catch(SQLException x) {
			String msg = x.getMessage();
			if(rs != null && msg != null && msg.contains("internal representation")) {
				String res = "(cannot obtain value)";
				try {
					res = rs.getString(1);
				} catch(Exception xx) {}
				throw new SQLException("Cannot convert '" + res + "' to internal representation " + clz + ": " + x, x);
			}
			throw x;
		} finally {
			FileTool.closeAll(rs, ps);
		}
	}

	/**
	 * Quick method to select a single value of a given type from the database. Returns null if not found AND if the value was null...
	 * @param connection
	 * @param clz
	 * @param select
	 * @return
	 */
	public static <T> List<T> selectSingleColumnList(@Nonnull Connection connection, @Nonnull Class<T> clz, @Nonnull String select, @Nonnull Object... params) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = connection.prepareStatement(select);
			setParameters(ps, 1, params);
			logDebug("selectSingleColumnList", select, params);
			rs = ps.executeQuery();
			List<T> list = new ArrayList<T>();
			while(rs.next()) {
				Object res = null;
				if(clz == String.class)
					res = rs.getString(1);
				else if(clz == Long.class || clz == long.class)
					res = Long.valueOf(rs.getLong(1));
				else if(clz == Integer.class || clz == int.class)
					res = Integer.valueOf(rs.getInt(1));
				else if(clz == java.util.Date.class) {
					java.sql.Timestamp ts = rs.getTimestamp(1);
					if(ts != null)
						res = new java.util.Date(ts.getTime());
				} else if(clz == Boolean.class || clz == boolean.class)
					res = Boolean.valueOf(rs.getBoolean(1));
				else if(clz == Blob.class)
					res = rs.getBlob(1);
				else if(clz == Clob.class)
					res = rs.getClob(1);
				else
					throw new IllegalStateException("Call error: cannot handle requested return type " + clz);
				if(rs.wasNull())
					list.add(null);
				else
					list.add((T) res);
			}
			return list;
		} catch(SQLException x) {
			String msg = x.getMessage();
			if(rs != null && msg != null && msg.contains("internal representation")) {
				String res = "(cannot obtain value)";
				try {
					res = rs.getString(1);
				} catch(Exception xx) {}
				throw new SQLException("Cannot convert '" + res + "' to internal representation " + clz + ": " + x, x);
			}
			throw x;
		} finally {
			FileTool.closeAll(rs, ps);
		}
	}

	public static void setParameters(@Nonnull PreparedStatement ps, int startindex, @Nullable Object[] params) throws SQLException {
		if(params == null)
			return;
		for(int i = 0; i < params.length; i++) {
			Object val = params[i];
			int px = i + startindex;
			if(val instanceof JdbcOutParam< ? >) {
				if(!(ps instanceof CallableStatement)) {
					throw new IllegalArgumentException("expected CallableStatement instead of PreparedStatement for OUT/IN OUT param function/procedure call!");
				}
				((CallableStatement) ps).registerOutParameter(px, calcSQLTypeFor(((JdbcOutParam< ? >) val).getClassType()));
				if(val instanceof JdbcInOutParam< ? >) {
					setParameter(ps, ((JdbcInOutParam< ? >) val).getValue(), px);
				}
			} else {
				setParameter(ps, val, px);
			}
		}
	}

	public static void setParameter(@Nonnull PreparedStatement ps, @Nullable Object val, int px) throws SQLException {
		if(val == null)
			ps.setString(px, null);
		else if(val instanceof String) {
			ps.setString(px, ((String) val));
		} else if(val instanceof Long) {
			ps.setLong(px, ((Long) val).longValue());
		} else if(val instanceof Integer) {
			ps.setInt(px, ((Integer) val).intValue());
		} else if(val instanceof BigDecimal) {
			ps.setBigDecimal(px, (BigDecimal) val);
		} else if(val instanceof Double) {
			ps.setDouble(px, ((Double) val).doubleValue());
		} else if(val instanceof java.sql.Timestamp) {
			ps.setTimestamp(px, (java.sql.Timestamp) val);
		} else if(val instanceof java.util.Date) {
			ps.setTimestamp(px, new Timestamp(((java.util.Date) val).getTime()));
		} else if(val instanceof Boolean) {
			ps.setBoolean(px, ((Boolean) val).booleanValue());
		} else if(val instanceof RowId) {
			ps.setRowId(px, (RowId) val);
		} else if(val.getClass().getName().endsWith(".ROWID")) { // Oracle sucks at standards.
			ps.setObject(px, val);
		} else if(val instanceof Blob) {
			ps.setBlob(px, ((Blob) val));
		} else if(val instanceof Clob) {
			ps.setClob(px, ((Clob) val));
		} else
			throw new IllegalStateException("Call error: unknown SQL parameter of type " + val.getClass());
	}

	public static void readParameters(@Nonnull PreparedStatement ps, int startindex, @Nullable Object[] params) throws SQLException {
		if(params == null)
			return;
		for(int i = 0; i < params.length; i++) {
			Object val = params[i];
			if(val instanceof JdbcOutParam< ? >) {
				if(!(ps instanceof CallableStatement)) {
					throw new IllegalArgumentException("expected CallableStatement instead of PreparedStatement for OUT/IN OUT param function/procedure call!");
				}
				setOutParamValue((CallableStatement) ps, startindex + i, ((JdbcOutParam< ? >) val).getClassType(), (JdbcOutParam< ? >) val);
			}
		}
	}

	static public List<JdbcAnyRecord> queryAny(@Nonnull Connection dbc, @Nonnull String select, Object... parameters) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = dbc.prepareStatement(select);
			setParameters(ps, 1, parameters);
			logDebug("queryAny", select, parameters);
			rs = ps.executeQuery();
			return queryAny(select, rs);
		} finally {
			FileTool.closeAll(rs, ps);
		}
	}

	static public List<JdbcAnyRecord> queryAny(String tblname, ResultSet rs) throws SQLException {
		List<JdbcAnyRecord> l = new ArrayList<JdbcAnyRecord>();
		ResultSetMetaData md = rs.getMetaData();
		while(rs.next()) {
			JdbcAnyRecord a = new JdbcAnyRecord();
			a.initFromRS(tblname, md, rs);
			l.add(a);
		}
		return l;
	}

	static public JdbcAnyRecord queryAnyOne(@Nonnull Connection dbc, @Nonnull String select, Object... parameters) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = dbc.prepareStatement(select);
			setParameters(ps, 1, parameters);
			logDebug("queryAnyOne", select, parameters);
			rs = ps.executeQuery();
			return queryAnyOne(select, rs);
		} finally {
			FileTool.closeAll(rs, ps);
		}
	}

	static public JdbcAnyRecord queryAnyOne(@Nonnull String tblname, @Nonnull ResultSet rs) throws SQLException {
		if(!rs.next())
			return null;
		ResultSetMetaData md = rs.getMetaData();
		JdbcAnyRecord a = new JdbcAnyRecord();
		a.initFromRS(tblname, md, rs);
		if(rs.next())
			throw new SQLException("Got >1 result for queryAnyOne");
		return a;
	}

	static private int calcSQLTypeFor(@Nonnull Class< ? > rt) {
		if(rt == String.class)
			return Types.VARCHAR;
		else if(rt == Integer.class || rt == int.class || rt == Long.class || rt == long.class || rt == BigDecimal.class || rt == Double.class || rt == double.class)
			return Types.NUMERIC;
		else if(rt == Date.class) {
			return Types.DATE;
		} else
			throw new IllegalStateException("Call error: cannot get SQLType for java type=" + rt);
	}

	private static void appendSPParameters(@Nonnull StringBuilder sb, @Nonnull List<Object> pars, @Nonnull Object[] args) {
		//-- Handle parameters, and handle boolean arguments, sigh.
		for(int i = 0; i < args.length; i++) {
			Object val = args[i];
			if(i > 0)
				sb.append(',');
			if(val instanceof Boolean) {
				sb.append(((Boolean) val).booleanValue() ? "true" : "false");
			} else {
				sb.append("?");
				pars.add(val);
			}
		}
	}

	public static boolean executeStatement(@Nonnull Connection dbc, @Nonnull String sql, Object... args) throws SQLException {
		PreparedStatement ps = null;
		try {
			ps = dbc.prepareStatement(sql);
			setParameters(ps, 1, args);
			logDebug("executeStatement", sql, args);
			return ps.execute();
		} finally {
			FileTool.closeAll(ps);
		}
	}

	/**
	 * Provides interface to execute SQL as {@link CallableStatement} that is UPDATING DATA and also possibly return some values.
	 * Out values should be specified as {@link JdbcOutParam}.
	 * In case that intention is to just execute CallableStatement that does not explicitly do updates (i.e. to execute stored procedure), please use
	 * {@link JdbcUtil#executeStatement} or {@link JdbcUtil#oracleSpCall} methods.
	 *
	 * @param dbc
	 * @param sql i.e. "begin insert into table1(colA, colB, colC) values (?,?,?) returning colId into ? ; end;"
	 * @param args
	 * @return T in case update changed any data, otherwise F.
	 * @throws SQLException
	 */
	public static boolean executeUpdatingCallableStatement(@Nonnull Connection dbc, @Nonnull String sql, @Nullable Object... args) throws SQLException {
		sql = sql.trim();
		if(!StringTool.strStartsWithIgnoreCase(sql, "begin") || !StringTool.strEndsWithIgnoreCase(sql, "end;")) {
			StringBuilder response = new StringBuilder();
			response.append("Expected sql that starts with \"begin\" and ends with \"end;\", for example : begin insert into table1(colA, colB, colC) values (?,?,?) returning colId into ? ; end;\n");
			response.append("Found sql: ").append(sql).append("\n");
			response
				.append("In case that intention is to just execute CallableStatement that does not explicitly do updates (i.e. to execute stored procedure), please use {@link JdbcUtil#executeStatement} or {@link JdbcUtil#oracleSpCall} methods.");
			throw new IllegalArgumentException(response.toString());
		}
		CallableStatement cs = null;
		try {
			cs = dbc.prepareCall(sql);
			setParameters(cs, 1, args);
			logDebug("executeUpdatingCallableStatement", sql, args);
			if(cs.executeUpdate() == 1) {
				readParameters(cs, 1, args);
				return true;
			} else {
				return false;
			}
		} finally {
			FileTool.closeAll(cs);
		}
	}

	/**
	 * Provides interface to call Oracle stored procedures and functions.
	 * In case that called function returns boolean use {@link JdbcUtil#oracleSpCallReturningBool(Connection, String, Object...)}
	 * Support all three type of parameters:<BR/>
	 * <UL>
	 * <LI>OUT params: use {@link JdbcOutParam}</LI>
	 * <LI>IN OUT params: use {@link JdbcInOutParam}</LI>
	 * <LI>IN params: use simple java type instancies</LI>
	 * </UL>
	 * @param <T> Oracle function return value type
	 * @param con Db connection
	 * @param rtype Oracle function return type
	 * @param sp Stored procedure / function name
	 * @param args Stored procedure / function parameters
	 * @return Oracle function return value in case of rtype != Void.class
	 * @throws SQLException
	 */
	static public <T> T oracleSpCall(@Nonnull Connection con, @Nonnull Class<T> rtype, @Nonnull String sp, @Nonnull Object... args) throws SQLException {
		if(rtype == Boolean.class || rtype == boolean.class) {
			return (T) Boolean.valueOf(oracleSpCallReturningBool(con, sp, args));
		}

		StringBuilder sb = new StringBuilder();
		sb.append("begin ");
		int startix = 1;
		if(rtype != null && rtype != Void.class) {
			sb.append("? := ");
			startix = 2;
		}
		List<Object> pars = new ArrayList<Object>(args.length);
		sb.append(sp).append('(');
		appendSPParameters(sb, pars, args);
		sb.append(");");
		sb.append("end;");
		String stmt = sb.toString();

		//-- Call the SP
		CallableStatement ps = null;
		try {
			ps = con.prepareCall(stmt);
			if(startix != 1)
				ps.registerOutParameter(1, calcSQLTypeFor(rtype));
			setParameters(ps, startix, pars.toArray());

			logDebug("oracleSpCall", stmt, pars);
			ps.execute();
			readParameters(ps, startix, args);

			if(startix != 1) {
				return readPsValue(ps, 1, rtype);
			} else {
				return null;
			}
		} finally {
			FileTool.closeAll(ps);
		}
	}

	/**
	 * Similar as {@link JdbcUtil#oracleSpCall(Connection, Class, String, Object...)},
	 * adjusted to handle returning of oracle boolean type properly.
	 * @param con
	 * @param sp
	 * @param args For hanlding IN/OUT/IN OUT params see {@link JdbcUtil#oracleSpCall(Connection, Class, String, Object...)}
	 * @return
	 * @throws SQLException
	 */
	public static boolean oracleSpCallReturningBool(@Nonnull Connection con, @Nonnull String sp, @Nonnull Object... args) throws SQLException {
		StringBuilder sb = new StringBuilder();
		sb.append("declare l_res number; ");
		sb.append("begin if(").append(sp).append("(");
		List<Object> pars = new ArrayList<Object>(args.length);
		appendSPParameters(sb, pars, args);
		sb.append(")) then l_res := 1; else l_res := 0; end if; ? := l_res;");
		sb.append("end;");
		String stmt = sb.toString();

		//-- Call the SP
		CallableStatement ps = null;
		try {
			ps = con.prepareCall(stmt);
			setParameters(ps, 1, pars.toArray());
			ps.registerOutParameter(pars.size() + 1, Types.NUMERIC);
			logDebug("oracleSpCallReturningBool", stmt, pars);
			ps.execute();
			readParameters(ps, 1, pars.toArray());
			int res = ps.getInt(pars.size() + 1);
			return res != 0;
		} finally {
			FileTool.closeAll(ps);
		}
	}

	private static <T> void setOutParamValue(@Nonnull CallableStatement ps, int index, @Nonnull Class<T> rtype, @Nonnull JdbcOutParam< ? > pOutParam) throws SQLException {
		JdbcOutParam<T> outParam = (JdbcOutParam<T>) pOutParam;
		outParam.setValue(readPsValue(ps, index, rtype));
	}

	private static <T> T readPsValue(@Nonnull CallableStatement ps, int index, @Nonnull Class<T> rtype) throws SQLException {
		if(rtype == String.class) {
			return (T) ps.getString(index);
		} else if(rtype == Integer.class || rtype == int.class) {
			return (T) (Integer.valueOf(ps.getInt(index)));
		} else if(rtype == Long.class || rtype == long.class) {
			return (T) (Long.valueOf(ps.getLong(index)));
		} else if(rtype == Double.class || rtype == double.class) {
			return (T) (Double.valueOf(ps.getDouble(index)));
		} else if(rtype == BigDecimal.class) {
			return (T) ps.getBigDecimal(index);
		} else if(rtype == Blob.class) {
			return (T) ps.getBlob(index);
		} else if(rtype == Clob.class) {
			return (T) ps.getClob(index);
		} else if(rtype == Date.class) {
			java.sql.Timestamp ts = ps.getTimestamp(index);
			if(ts != null)
				return (T) new java.util.Date(ts.getTime());
			return null;
		} else {
			throw new IllegalStateException("Call error: cannot get out parameter for result java type=" + rtype);
		}
	}

	/**
	 * Check if the specified table's record (identified by schemaName, tableName and it's primary key) has child records. If so, return the
	 * name of the first table that contains those child records. If no child records are found (meaning it should be safe to delete this
	 * record) then this method returns null.
	 *
	 * <p>This method is only suitable for relations without compound keys, and the key's value must be representable and convertible by
	 * JDBC to and from String. The method takes care of cascading, allowing a delete if child records are present with delete-cascade
	 * rule, and it checks if those delete-cascaded records are deleteable themselves recursively.</p>
	 *
	 * @param dbc
	 * @param schemaName
	 * @param tableName
	 * @param primaryKey
	 * @return
	 * @throws Exception
	 */
	@Nullable
	static public String hasChildRecords(@Nonnull Connection dbc, @Nullable String schemaName, @Nonnull String tableName, @Nonnull String primaryKey) throws Exception {
		DatabaseMetaData dmd = dbc.getMetaData();
		ResultSet rs = null;
		ResultSet rs2 = null;
		PreparedStatement ps = null;
		try {
			//-- Find all of my child relations.
			rs = dmd.getExportedKeys(dbc.getCatalog(), null == schemaName ? dbc.getSchema() : schemaName.toUpperCase(), tableName.toUpperCase());
			if(!rs.next()) {
				rs.close();
				rs = dmd.getExportedKeys(dbc.getCatalog(), null == schemaName ? dbc.getSchema() : schemaName, tableName);
				if(!rs.next())
					return null;
			}

			do {
				String pkColumn = rs.getString("PKCOLUMN_NAME");
				String fkTable = rs.getString("FKTABLE_NAME");
				String fkColumn = rs.getString("FKCOLUMN_NAME");
				int cascade = rs.getInt("DELETE_RULE");
				int keyseq = rs.getInt("KEY_SEQ");
				if(keyseq > 1)
					throw new IllegalStateException("This method cannot be used for compound-key relations: relation " + tableName + " --< " + fkTable);

				if(DatabaseMetaData.importedKeyCascade == cascade) {
					//-- This cascades the children. Check if any of the children have undeleteable items, recursively. First collect all childs.
					List<String>	fklist = new ArrayList<String>();
					ps = dbc.prepareStatement("select "+pkColumn+" from "+fkTable+" where "+fkColumn+"=?");
					ps.setString(1, primaryKey);
					rs2 = ps.executeQuery();
					while(rs2.next()) {
						fklist.add(rs2.getString(1));
					}
					rs2.close();
					ps.close();

					//-- Now check all children.
					for(String apk : fklist) {
						String tbl = hasChildRecords(dbc, schemaName, fkTable, apk);
						if(null != tbl)
							return tbl;
					}
				} else {
					//-- Not cascading. Does this relation have child records?
					ps = dbc.prepareStatement("select 1 from " + fkTable + " where " + fkColumn + "=?");
					ps.setString(1, primaryKey);
					rs2 = ps.executeQuery();
					if(rs2.next()) {
						return fkTable;									// Has dependent record in this table -> exit.
					}
					rs2.close();
					ps.close();
				}
			} while(rs.next());
			return null;
		} finally {
			FileTool.closeAll(rs2, rs, ps);
		}
	}

	private static void logDebug(@Nonnull String sourceMethod, @Nonnull String sql, Object[] params) {
		if(LOG.isDebugEnabled()) {
			LOG.debug(sourceMethod + ": " + sql);
			logDebugParams(params);
		}
	}

	private static void logDebug(@Nonnull String sourceMethod, @Nonnull String sql, List<Object> params) {
		if(LOG.isDebugEnabled()) {
			logDebug(sourceMethod, sql, params.toArray());
		}
	}

	private static void logDebugParams(Object[] params) {
		int i = 0;
		for(Object val : params) {
			LOG.debug(i + ": " + getDebugParamValue(val));
			i++;
		}
	}

	private static String getDebugParamValue(Object val) {
		if(val == null)
			return "null";
		else if(val instanceof String) {
			return "[String] '" + val + "'";
		} else if(val instanceof Long) {
			return "[Long] " + ((Long) val).longValue();
		} else if(val instanceof Integer) {
			return "[Integer] " + ((Integer) val).intValue();
		} else if(val instanceof BigDecimal) {
			return "[BigDecimal] " + ((BigDecimal) val).doubleValue();
		} else if(val instanceof Double) {
			return "[Double] " + ((Double) val).doubleValue();
		} else if(val instanceof java.sql.Timestamp) {
			return "[Timestamp] " + val;
		} else if(val instanceof java.util.Date) {
			return "[Date] " + val;
		} else if(val instanceof Boolean) {
			return "[Boolean] " + ((Boolean) val).booleanValue();
		} else if(val instanceof RowId) {
			return "[RowId] " + val;
		} else if(val.getClass().getName().endsWith(".ROWID")) { // Oracle sucks at standards.
			return "[Class.ROWID] " + val;
		} else if(val instanceof Blob) {
			return "[Blob]";
		} else if(val instanceof Clob) {
			return "[Clob]";
		} else if(val instanceof JdbcOutParam< ? >) {
			return "[JdbcOutParam] class = " + ((JdbcOutParam< ? >) val).getClass();
		} else if(val instanceof JdbcInOutParam< ? >) {
			return "[JdbcInParam] " + getDebugParamValue(((JdbcOutParam< ? >) val).getValue());
		} else {
			return "[unknown type!]";
		}
	}

	@Nullable
	public static Long readLong(@Nonnull ResultSet rs, @Nonnull String colName) throws SQLException {
		Long value = rs.getLong(colName);
		if (rs.wasNull()){
			return null;
		}
		return value;
	}

	@Nullable
	public static Integer readInt(@Nonnull ResultSet rs, @Nonnull String colName) throws SQLException {
		Integer value = rs.getInt(colName);
		if (rs.wasNull()){
			return null;
		}
		return value;
	}

	@Nullable
	public static java.util.Date readDate(@Nonnull ResultSet rs, @Nonnull String colName) throws SQLException {
		return DateUtil.sqlToUtilDate(rs.getDate(colName));
	}

	@Nullable
	public static java.util.Date readDate(@Nonnull ResultSet rs, int index) throws SQLException {
		return DateUtil.sqlToUtilDate(rs.getDate(index));
	}


	@Nullable
	public static java.util.Date readTimestamp(@Nonnull ResultSet rs, int colIndex) throws SQLException {
		Timestamp ts = rs.getTimestamp(colIndex);
		return null == ts ? null : new java.util.Date(ts.getTime());
	}

}
