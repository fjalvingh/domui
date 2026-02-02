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
	 * Uses mysql specific code to create a sequence number from a sequence
	 * table.
	 *
	 * @param dbc the connection
	 * @return the id
	 * @throws SQLException if the sequence could not be obtained
	 */
	@Override
	protected int getSequenceID(Connection dbc, String tablename) throws SQLException {

		try {
			return getMysqlSequenceTry(dbc);
		} catch(Exception x) {
		}

		//-- When here the above failed. Try to create the table then retry.
		createMysqlSequence(dbc); // Create the sequence table
		return getMysqlSequenceTry(dbc); // And try once more.
	}

	/**
	 * Returns a sequence number that can be used to create a new PK for a
	 * record in the given table. Sequences are emulated on databases that do
	 * not support 'm.
	 */
	@Override
	protected int getFullSequenceID(Connection dbc, String tablename) throws SQLException {
		return getSequenceID(dbc, tablename);
	}

	/**
	 * Helper to get a sequence number from a table.
	 */
	static private int getMysqlSequenceTry(Connection dbc) throws SQLException {
		try(PreparedStatement ps = dbc.prepareStatement("update sequence set id=LAST_INSERT_ID(id+1)")) {
			ps.executeUpdate();
		}


		try(PreparedStatement ps = dbc.prepareStatement("select last_insert_id()");
			ResultSet rs = ps.executeQuery()) {
			if(!rs.next())
				throw new SQLException("genid Query no results!?");
			return rs.getInt(1);
		}
	}

	/**
	 * Creates the MYSQL sequences table.
	 */
	static private void createMysqlSequence(Connection dbc) {
		try(PreparedStatement ps = dbc.prepareStatement("create table sequence (id integer not null)")) {
			ps.executeUpdate();
		} catch(Exception x) {
			return;
		}
		try(PreparedStatement ps = dbc.prepareStatement("insert into sequence values(1)")) {
			ps.executeUpdate();
		} catch(SQLException x) {
			//-- Ignore
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Blob writing.										*/
	/*--------------------------------------------------------------*/

	/**
	 * Writes a blob to the requested record using the normal setBinaryStream
	 * call. Used for jdbc-compliant databases.
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
			} catch(Exception x) {
				//-- Ignore
			}
		}
	}
}
