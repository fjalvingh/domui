package to.etc.dbutil;

import java.io.*;
import java.sql.*;

public class OranxoDB extends BaseDB {
	public OranxoDB() {}

	/**
	 * Returns a SQL statement that is the cheapest way to check the validity of a connection.
	 * @return
	 */
	@Override
	protected String getCheckString() {
		return "select 1 from dual";
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Sequences.											*/
	/*--------------------------------------------------------------*/
	/**
	 * Uses a table sequence to generate a value.
	 * @param dbc			the connection
	 * @return				the id
	 * @throws SQLException	if the sequence could not be obtained
	 */
	@Override
	protected int getSequenceID(Connection dbc, String tablename) throws SQLException {
		return getFullSequenceID(dbc, tablename + "_sq");
	}

	/**
	 * Uses a table sequence to generate a value.
	 * @param dbc			the connection
	 * @return				the id
	 * @throws SQLException	if the sequence could not be obtained
	 */
	@Override
	protected int getFullSequenceID(Connection dbc, String seqname) throws SQLException {
		try {
			return trySequenceID(dbc, seqname);
		} catch(Exception x) {}

		//-- When here the above failed. Try to create the table then retry.
		createSequence(dbc, seqname); // Create the sequence table
		return trySequenceID(dbc, seqname); // And try once more.
	}

	private void createSequence(Connection dbc, String table) {
		PreparedStatement ps = null;
		try {
			ps = dbc.prepareStatement("create sequence " + table + " start with 1 increment by 1");
			ps.executeUpdate();
		} catch(SQLException x) {} finally {
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
		}
	}

	private int trySequenceID(Connection dbc, String tablename) throws SQLException {
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			ps = dbc.prepareStatement("select " + tablename + ".nextval from dual");
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

	/*--------------------------------------------------------------*/
	/*	CODING:	Blob writing.										*/
	/*--------------------------------------------------------------*/
	@Override
	protected void setBlob(Connection dbc, String table, String column, String[] pkfields, Object[] key, InputStream is) throws SQLException {
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
				ps.setBinaryStream(1, is, Integer.MAX_VALUE);
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
	 *	Writes a blob to the requested record.
	 *  @parameter is	The stream to write to the blob. If this is null then the
	 *  				field is set to dbnull.
	 */
	@Override
	protected void setBlob(Connection dbc, String table, String column, String where, InputStream is) throws SQLException {
		ResultSet rs = null;
		PreparedStatement ps = null;
		PreparedStatement ps2 = null;
		boolean isac = dbc.getAutoCommit();
		boolean okay = false;

		try {
			//-- The blob cannot be written with autocommit = on.
			if(isac)
				dbc.setAutoCommit(false);

			//-- Set to NULL or DBNULL,
			StringBuilder sb = new StringBuilder(64);
			sb.append("update ");
			sb.append(table);
			sb.append(" set ");
			sb.append(column);
			sb.append(" = ");
			sb.append(is == null ? "null" : "empty_blob()");
			sb.append(" where ");
			sb.append(where);
			ps = dbc.prepareStatement(sb.toString());
			ps.executeUpdate();
			ps.close();
			ps = null;
			if(is == null) {
				okay = true;
				return;
			}

			//-- Set to an actual value: make it empty_blob 1st because F****ING oracle does not truncate!(!)
			String s1 = "select " + column + " from " + table + " where " + where + " for update of " + column;
			ps = dbc.prepareStatement(s1);
			rs = ps.executeQuery();
			if(!rs.next())
				throw new SQLException("Record in table " + table + " with key " + where + " not found for BLOB update.");

			//-- Select the blob for update & write,
			Blob tb = rs.getBlob(1);
			ps2 = dbc.prepareCall("{call DBMS_LOB.WRITEAPPEND( ?, ?, ? )}");
			ps2.setBlob(1, tb);
			int szrd;
			byte[] buf = new byte[8192];

			//-- James Gosling is an idiot: we must fucking wrap this exception... Moron.
			try {
				while(-1 != (szrd = (is.read(buf)))) {
					ps2.setInt(2, szrd);
					ps2.setBytes(3, buf);
					ps2.execute();
				}
			} catch(IOException x) {
				x.printStackTrace(); // And of course SQLException cannot accept root exception! What a stupid idiots!
				throw new SQLException("IO error in BLOB copy (wrapped): " + x);
			}
			if(isac)
				dbc.commit(); // Commit if autocommit was on
			okay = true;
		} finally {
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {}
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
			try {
				if(ps2 != null)
					ps2.close();
			} catch(Exception x) {}
			try {
				if(!okay && isac)
					dbc.rollback();
			} catch(Exception x) {}
			try {
				if(dbc.getAutoCommit() != isac)
					dbc.setAutoCommit(isac);
			} catch(Exception x) {}
		}
	}

	@Override
	protected void setClob(Connection dbc, String table, String column, String where, Reader r) throws Exception {
		GenericDB.dbtypeORACLE.setClob(dbc, table, column, where, r);
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
		InputStream is = rs.getBinaryStream(col);
		if(is == null)
			return null;
		return new InputStreamReader(is);
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
		InputStream is = rs.getBinaryStream(col);
		if(is == null)
			return null;
		return new InputStreamReader(is);
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
		return rs.getBinaryStream(col);
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
		return rs.getBinaryStream(col);
	}

	@Override
	public boolean oracleOuterJoin() {
		return true;
	}
}
