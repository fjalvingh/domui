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
import java.lang.reflect.*;
import java.sql.*;

import javax.annotation.*;
import javax.sql.*;

import org.slf4j.*;

public class OracleDB extends BaseDB {

	public static Logger LOG = LoggerFactory.getLogger(OracleDB.class);

	public OracleDB() {
		super("oracle");
	}

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
	protected void setBlob(Connection dbc, String table, String column, String where, InputStream is, int len) throws SQLException {
		_setBlob(dbc, table, column, where, is, len);
	}

	@Override
	protected void setBlob(Connection dbc, String table, String column, String where, byte[][] data) throws SQLException {
		int len = 0;
		if(data != null) {
			for(byte[] d : data)
				len += d.length;
		}

		_setBlob(dbc, table, column, where, data, len);
	}

	/**
	 *	Writes a blob to the requested record. Works for Oral and MYSQL.
	 *  @parameter is	The stream to write to the blob. If this is null then the
	 *  				field is set to dbnull.
	 */
	private void _setBlob(Connection dbc, String table, String column, String where, Object data, int len) throws SQLException {
		ResultSet rs = null;
		PreparedStatement ps = null;
		OutputStream os = null;
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
			sb.append(data == null ? "null" : "empty_blob()");
			sb.append(" where ");
			sb.append(where);
			ps = dbc.prepareStatement(sb.toString());
			ps.executeUpdate();
			ps.close();
			ps = null;
			if(data == null) {
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
			os = (OutputStream) callObjectMethod(tb, "getBinaryOutputStream");

			//			oracle.sql.BLOB	b	= (oracle.sql.BLOB) tb;
			//			os					= b.getBinaryOutputStream();
			//-- James Gosling is an idiot: we must fucking wrap this exception... Moron.
			try {
				if(data instanceof InputStream)
					streamCopy(os, (InputStream) data);
				else {
					byte[][] in = (byte[][]) data;
					for(int i = 0; i < in.length; i++) {
						os.write(in[i]);
					}
				}
			} catch(IOException x) {
				x.printStackTrace(); // And of course SQLException cannot accept root exception! What a stupid idiots!
				throw new SQLException("IO error in BLOB copy (wrapped): " + x);
			} finally {
				try {
					os.close();
				} catch(Exception x) {}
			}
			os = null;
			ps.close();
			ps = null;
			if(isac)
				dbc.commit(); // Commit if autocommit was on
			okay = true;
		} finally {
			try {
				if(os != null)
					os.close();
			} catch(Exception x) {}
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {}
			try {
				if(ps != null)
					ps.close();
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

	/**
	 * Generic caller of a method using reflection. This prevents us from having
	 * to link to the stupid Oracle driver.
	 * @param src
	 * @param name
	 * @return
	 * @throws Exception
	 */
	static private Object callObjectMethod(Object src, String name) throws SQLException {
		try {
			Method m = src.getClass().getMethod(name, new Class[0]);
			return m.invoke(src, new Object[0]);
		} catch(InvocationTargetException itx) {
			if(itx.getCause() instanceof SQLException)
				throw (SQLException) itx.getCause();
			throw new RuntimeException(itx.getCause().toString(), itx.getCause());
		} catch(Exception x) {
			throw new RuntimeException("Exception calling " + name + " on " + src + ": " + x, x);
		}
	}

	/**
	 * Writes a blob to the requested record. Works for Oral and MYSQL.
	 * @parameter is	The stream to write to the blob. If this is null then the
	 *  				field is set to dbnull.
	 */
	@Override
	protected void setBlob(Connection dbc, String table, String column, String[] pkfields, Object[] key, InputStream is, int len) throws SQLException {
		ResultSet rs = null;
		PreparedStatement ps = null;
		OutputStream os = null;
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
			ps = mkKeyedSQL(dbc, sb, pkfields, key, 1, null);
			ps.executeUpdate();
			ps.close();
			ps = null;
			if(is == null) {
				okay = true;
				return;
			}

			//-- Set to an actual value: make it empty_blob 1st because F****ING oracle does not truncate!(!)
			sb.setLength(0);
			sb.append("select ");
			sb.append(column);
			sb.append(" from ");
			sb.append(table);
			sb.append(" where ");
			ps = mkKeyedSQL(dbc, sb, pkfields, key, 1, "for update of " + column);
			rs = ps.executeQuery();
			if(!rs.next())
				throw new SQLException("Record in table " + table + " not found for BLOB update.");

			//-- Select the blob for update & write,
			Blob tb = rs.getBlob(1);
			os = (OutputStream) callObjectMethod(tb, "getBinaryOutputStream");

			//			oracle.sql.BLOB	b	= (oracle.sql.BLOB) tb;
			//			os					= b.getBinaryOutputStream();
			//-- James Gosling is an idiot: we must fucking wrap this exception... Moron.
			try {
				streamCopy(os, is);
			} catch(IOException x) {
				x.printStackTrace(); // And of course SQLException cannot accept root exception! What a stupid idiots!
				throw new SQLException("IO error in BLOB copy (wrapped): " + x);
			} finally {
				try {
					os.close();
				} catch(Exception x) {}
			}
			os = null;
			ps.close();
			ps = null;
			if(isac)
				dbc.commit(); // Commit if autocommit was on
			okay = true;
		} finally {
			try {
				if(os != null)
					os.close();
			} catch(Exception x) {}
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {}
			try {
				if(ps != null)
					ps.close();
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


	/**
	 *	Writes a blob to the requested record. Works for Oral and MYSQL.
	 *  @parameter is	The stream to write to the blob. If this is null then the
	 *  				field is set to dbnull.
	 */
	protected void setBlob_old(Connection dbc, String table, String column, String where, InputStream is, int len) throws Exception {
		ResultSet rs = null;
		PreparedStatement ps = null;
		OutputStream os = null;
		boolean isac = dbc.getAutoCommit();
		boolean okay = false;

		try {
			//-- The blob cannot be written with autocommit = on.
			if(isac)
				dbc.setAutoCommit(false);

			String s1 = "select " + column + " from " + table + " where " + where + " for update of " + column;

			ps = dbc.prepareStatement(s1);
			rs = ps.executeQuery();
			if(!rs.next())
				throw new SQLException("Record in table " + table + " with key " + where + " not found for BLOB update.");

			Blob tb = rs.getBlob(1);
			if(tb == null || rs.wasNull()) {
				//-- This is a dbnull blob. Was that the goal?
				if(is == null) {
					okay = true;
					return; // YES-> ready.
				}

				//-- No, it was not... Insert an empty blob...
				rs.close();

				PreparedStatement ps2 = dbc.prepareStatement("update " + table + " set " + column + " = empty_blob() where " + where);
				int rc = ps2.executeUpdate();
				if(rc != 1)
					throw new SQLException("Record in table " + table + " with key " + where + " not found 2nd time!?");
				ps2.close();

				//-- Now query again, dude!
				rs = ps.executeQuery(); // Get empty_blob now!
				if(!rs.next())
					throw new SQLException("Record in table " + table + " with key " + where + " not found for BLOB update after I just inserted an empty blob!?.");

				tb = rs.getBlob(1);
			} else if(is == null) // BLOB found but NULL should be set...
			{
				rs.close();
				ps.close();
				PreparedStatement ps2 = dbc.prepareStatement("update " + table + " set " + column + " = null where " + where);
				int rc = ps2.executeUpdate();
				if(rc != 1)
					throw new SQLException("Record in table " + table + " with key " + where + " not found 2nd time!?");
				ps2.close();
				dbc.commit();
				okay = true;
				return;
			}

			//-- Select the blob for update & write,
			os = (OutputStream) callObjectMethod(tb, "getBinaryOutputStream");
			//			oracle.sql.BLOB	b	= (oracle.sql.BLOB) tb;
			//			os					= b.getBinaryOutputStream();
			streamCopy(os, is);
			os.close();
			os = null;
			ps.close();
			ps = null;
			if(isac)
				dbc.commit(); // Commit if autocommit was on
			okay = true;
		} finally {
			try {
				if(os != null)
					os.close();
			} catch(Exception x) {}
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {}
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
			try {
				if(!okay)
					dbc.rollback();
			} catch(Exception x) {}
			try {
				if(dbc.getAutoCommit() != isac)
					dbc.setAutoCommit(isac);
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

	@Override
	public boolean oracleOuterJoin() {
		return true;
	}


	@Override
	protected void setClob(Connection dbc, String table, String column, String where, Reader r) throws Exception {
		PreparedStatement ps = null;
		boolean auc = false;

		try {
			//-- NO AUTOCOMMIT !!!!  It fails all updates!!
			auc = dbc.getAutoCommit();
			if(auc)
				dbc.setAutoCommit(false);

			//-- If reader is null then set the clob field to dbnull.
			if(r == null) {
				ps = dbc.prepareStatement("update " + table + " set " + column + " = null where " + where);
				if(ps.executeUpdate() != 1)
					throw new SQLException("The record cannot be found (update to empty)");
				return;
			}

			//-- Assholes! First ALWAYS insert an empty CLOB because writing a CLOB doesn't reset it's size..
			ps = dbc.prepareStatement("update " + table + " set " + column + " = empty_clob() where " + where);
			if(ps.executeUpdate() != 1)
				throw new SQLException("The record cannot be found (update to empty)");
			ps.close();
			ps = null;

			//-- Now write the empty thing.
			String st = "select " + column + " from " + table + " where " + where + " for update";
			ps = dbc.prepareStatement(st, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			ResultSet rs = ps.executeQuery();
			if(!rs.next())
				throw new SQLException("The record cannot be found (select)");
			Clob clob = rs.getClob(1);
			Writer w = (Writer) callObjectMethod(clob, "getCharacterOutputStream");

			//			oracle.sql.CLOB	c	= (oracle.sql.CLOB) rs.getClob(1);
			//			Writer	w	= c.getCharacterOutputStream();
			char[] buf = new char[4096];
			try {
				int szrd;
				while(0 < (szrd = r.read(buf)))
					w.write(buf, 0, szrd);
				w.close();
			} catch(IOException x) {
				throw new SQLException("IOException on CLOB write: " + x.toString());
			}
			rs.updateRow();
			if(auc) {
				dbc.commit();
				dbc.setAutoCommit(true);
			}
		} finally {
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
			try {
				if(auc)
					dbc.setAutoCommit(true);
			} catch(Exception x) {}

		}
	}

	public static void updateSynonyms(@Nonnull DataSource ds, @Nonnull String owner, String... objectNames) {
//		PreparedStatement ps = null;
//		PreparedStatement ps2 = null;
//		ResultSet rs = null;
//		Connection dbc = null;
//		try {
//			dbc = ds.getConnection();
//			int ct = 0;
//			long ts = System.currentTimeMillis();
//			String objectNamesFilter = null;
//			if(objectNames != null && objectNames.length > 0) {
//				StringBuilder sb = new StringBuilder();
//				for(String object : objectNames) {
//					sb.append("'").append(object).append("',");
//				}
//				sb.setLength(sb.length() - 1);
//				objectNamesFilter = " and o.object_name in (" + sb.toString() + ")";
//			}
//
//			ps = dbc.prepareStatement("select o.object_name from dba_objects o" //
//				+ " where o.owner = '" + owner + "'" //
//				+ " and o.object_type <> 'TYPE'" //
//				+ (objectNamesFilter != null ? objectNamesFilter : "") //
//				+ " and not exists  (" //
//				+ " select 1 from dba_synonyms s" //
//				+ " where s.owner = 'PUBLIC'" //
//				+ " and s.synonym_name = o.object_name " //
//				+ ")" //
//			);
//			rs = ps.executeQuery();
//			while(rs.next()) {
//				String on = rs.getString(1);
//				LOG.info(owner + ": create missing synonym '" + on + "'");
//				try {
//					ps2 = dbc.prepareStatement("create public synonym \"" + on + "\" for " + owner + ".\"" + on + "\"");
//					ps2.executeUpdate();
//				} catch(Exception x) {
//					String message = x.toString().toLowerCase();
//					if(!message.contains("ora-00955"))
//						LOG.error(owner + ": error creating synonym " + on + ": " + x);
//				} finally {
//					FileTool.closeAll(ps2);
//				}
//				ct++;
//			}
//
//			ts = System.currentTimeMillis() - ts;
//			LOG.info(owner + ": created " + ct + " public synonyms in " + StringTool.strDurationMillis(ts));
//		} catch(Exception x) {
//			LOG.error(owner + ": exception while trying to create missing synonyms: " + x);
//			x.printStackTrace();
//		} finally {
//			FileTool.closeAll(rs, ps, ps2, dbc);
//		}
	}

}
