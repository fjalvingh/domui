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
package to.etc.dbutil;

import java.io.*;
import java.sql.*;

public class MysqlDB extends BaseDB {
	public MysqlDB() {
		super("mysql");
	}

	/**
	 * Returns a SQL statement that is the cheapest way to check the validity of a connection.
	 * @return
	 */
	@Override
	protected String getCheckString() {
		return "select 1";
	}


	/**
	 * Uses mysql specific code to create a sequence number from a sequence
	 * table.
	 * @param dbc			the connection
	 * @return				the id
	 * @throws SQLException	if the sequence could not be obtained
	 */
	@Override
	protected int getSequenceID(Connection dbc, String tablename) throws SQLException {
		try {
			return getMysqlSequenceTry(dbc);
		} catch(Exception x) {}

		//-- When here the above failed. Try to create the table then retry.
		createMysqlSequence(dbc); // Create the sequence table
		return getMysqlSequenceTry(dbc); // And try once more.
	}

	/**
	 * Returns a sequence number that can be used to create a new PK for a
	 * record in the given table. Sequences are emulated on databases that do
	 * not support 'm.
	 *
	 * @param dbc
	 * @param tablename
	 * @return
	 * @throws SQLException
	 */
	@Override
	protected int getFullSequenceID(Connection dbc, String tablename) throws SQLException {
		return getSequenceID(dbc, tablename);
	}

	/**
	 * Helper to get a sequence number from a table.
	 * @param dbc
	 * @return					a number
	 * @throws SQLException
	 */
	static private int getMysqlSequenceTry(Connection dbc) throws SQLException {
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			ps = dbc.prepareStatement("update sequence set id=LAST_INSERT_ID(id+1)");
			ps.executeUpdate();
			ps.close();
			ps = null;
			ps = dbc.prepareStatement("select last_insert_id()");
			rs = ps.executeQuery();
			if(!rs.next())
				throw new SQLException("genid Query no results!?");
			return rs.getInt(1);
		} finally {
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {}
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
		}
	}


	/**
	 * Creates the MYSQL sequences table.
	 * @param dbc
	 * @throws SQLException
	 */
	static private void createMysqlSequence(Connection dbc) {
		PreparedStatement ps = null;
		try {
			ps = dbc.prepareStatement("create table sequence (id integer not null)");
			ps.executeUpdate();
			ps.close();
			ps = dbc.prepareStatement("insert into sequence values(1)");
			ps.executeUpdate();
		} catch(SQLException x) {} finally {
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Blob writing.										*/
	/*--------------------------------------------------------------*/
	@Override
	protected void setBlob(Connection dbc, String table, String column, String[] pkfields, Object[] key, InputStream is, int len) throws SQLException {
		PreparedStatement ps = null;
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("update ");
			sb.append(table);
			sb.append(" set ");
			sb.append(column);
			sb.append('=');
			if(is == null)
				sb.append("null");
			else
				sb.append('?');
			sb.append(" where ");
			ps = mkKeyedSQL(dbc, sb, pkfields, key, is == null ? 1 : 2, null);
			if(is != null)
				ps.setBinaryStream(1, is, len);
			int rc = ps.executeUpdate();
			if(rc != 1)
				throw new SQLException("Record in table " + table + " not found for BLOB update.");
		} finally {
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
		}
	}

	/**
	 *	Writes a blob to the requested record using the normal setBinaryStream
	 *  call. Used for jdbc-compliant databases.
	 */
	@Override
	protected void setBlob(Connection dbc, String table, String column, String where, InputStream is, int len) throws SQLException {
		PreparedStatement ps = null;
		try {
			if(is == null)
				ps = dbc.prepareStatement("update " + table + " set " + column + " = null where " + where);
			else {
				ps = dbc.prepareStatement("update " + table + " set " + column + " = ? where " + where);
				ps.setBinaryStream(1, is, len);
			}
			int rc = ps.executeUpdate();
			if(rc != 1)
				throw new SQLException("Record in table " + table + " with key " + where + " not found for BLOB update.");
		} finally {
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
		}
	}

	/**
	 *	Writes a blob to the requested record using the normal setBinaryStream
	 *  call. Used for jdbc-compliant databases.
	 */
	@Override
	protected void setClob(Connection dbc, String table, String column, String where, Reader r) throws Exception {
		PreparedStatement ps = null;
		try {
			if(r == null)
				ps = dbc.prepareStatement("update " + table + " set " + column + " = null where " + where);
			else {
				ps = dbc.prepareStatement("update " + table + " set " + column + " = ? where " + where);
				ps.setCharacterStream(1, r, Integer.MAX_VALUE);
			}
			int rc = ps.executeUpdate();
			if(rc != 1)
				throw new SQLException("Record in table " + table + " with key " + where + " not found for BLOB update.");
		} finally {
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Getting streams/readers from a resultset.			*/
	/*--------------------------------------------------------------*/
	/**
	 * Returns a Reader from the blob (clob) column specified.
	 * @param rs
	 * @param col
	 * @return
	 * @throws Exception
	 */
	@Override
	protected Reader getLobReader(Connection dbc, ResultSet rs, int col) throws Exception {
		Blob b = rs.getBlob(col);
		if(b == null)
			return null; // Null blob.
		return new InputStreamReader(b.getBinaryStream());
	}

	/**
	 * Returns a Reader from the blob (clob) column specified.
	 * @param rs
	 * @param col
	 * @return
	 * @throws Exception
	 */
	@Override
	protected Reader getLobReader(Connection dbc, ResultSet rs, String col) throws Exception {
		Blob b = rs.getBlob(col);
		if(b == null)
			return null; // Null blob.
		return new InputStreamReader(b.getBinaryStream());
	}

	/**
	 * Returns an InputStream from the blob (clob) column specified.
	 * @param rs
	 * @param col
	 * @return
	 * @throws Exception
	 */
	@Override
	protected InputStream getLobStream(Connection dbc, ResultSet rs, int col) throws Exception {
		Blob b = rs.getBlob(col);
		if(b == null)
			return null; // Null blob.
		return b.getBinaryStream();
	}

	/**
	 * Returns an InputStream from the blob (clob) column specified.
	 * @param rs
	 * @param col
	 * @return
	 * @throws Exception
	 */
	@Override
	protected InputStream getLobStream(Connection dbc, ResultSet rs, String col) throws Exception {
		Blob b = rs.getBlob(col);
		if(b == null)
			return null; // Null blob.
		return b.getBinaryStream();
	}


}
