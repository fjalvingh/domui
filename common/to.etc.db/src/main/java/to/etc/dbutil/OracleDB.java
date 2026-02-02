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

import org.eclipse.jdt.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.dbpool.DbPoolUtil;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OracleDB extends BaseDB {

	public static Logger LOG = LoggerFactory.getLogger(OracleDB.class);

	public OracleDB() {
		super("oracle");
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Sequences.											*/
	/*--------------------------------------------------------------*/

	/**
	 * Uses a table sequence to generate a value.
	 *
	 * @param dbc the connection
	 * @return the id
	 * @throws SQLException if the sequence could not be obtained
	 */
	@Override
	protected int getSequenceID(Connection dbc, String tablename) throws SQLException {
		return getFullSequenceID(dbc, tablename + "_sq");
	}

	/**
	 * Uses a table sequence to generate a value.
	 *
	 * @param dbc the connection
	 * @return the id
	 * @throws SQLException if the sequence could not be obtained
	 */
	@Override
	protected int getFullSequenceID(Connection dbc, String seqname) throws SQLException {
		try {
			return trySequenceID(dbc, seqname);
		} catch(Exception x) {
			//-- Ignore
		}

		//-- When here the above failed. Try to create the table then retry.
		createSequence(dbc, seqname); // Create the sequence table
		return trySequenceID(dbc, seqname); // And try once more.
	}

	private void createSequence(Connection dbc, String table) {
		try(PreparedStatement ps = dbc.prepareStatement("create sequence " + table + " start with 1 increment by 1")) {
			ps.executeUpdate();
		} catch(SQLException x) {
			//-- Ignore
		}
	}

	private int trySequenceID(Connection dbc, String tablename) throws SQLException {
		try(PreparedStatement ps = dbc.prepareStatement("select " + tablename + ".nextval from dual"); ResultSet rs = ps.executeQuery()) {
			if(!rs.next())
				throw new SQLException("genid Query no results!?");
			return rs.getInt(1);
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Blob writing.										*/
	/*--------------------------------------------------------------*/

	@Override
	protected void setBlob(Connection dbc, String table, String column, String where, InputStream is, int len) throws SQLException {
		_setBlob(dbc, table, column, where, is, len);
	}

	/**
	 * Writes a blob to the requested record. Works for Oral and MYSQL.
	 *
	 * @parameter is    The stream to write to the blob. If this is null then the
	 * field is set to dbnull.
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
				} catch(Exception x) {
					//-- Ignore
				}
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
			} catch(Exception x) {
				//-- Ignore
			}
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {
				//-- Ignore
			}
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {
				//-- Ignore
			}
			try {
				if(!okay && isac)
					dbc.rollback();
			} catch(Exception x) {
				//-- Ignore
			}
			try {
				if(dbc.getAutoCommit() != isac)
					dbc.setAutoCommit(isac);
			} catch(Exception x) {
				//-- Ignore
			}
		}
	}

	/**
	 * Generic caller of a method using reflection. This prevents us from having
	 * to link to the stupid Oracle driver.
	 */
	static private Object callObjectMethod(Object src, String name) throws SQLException {
		try {
			Method m = src.getClass().getMethod(name);
			return m.invoke(src);
		} catch(InvocationTargetException itx) {
			if(itx.getCause() instanceof SQLException)
				throw (SQLException) itx.getCause();
			throw new SQLException(itx.getCause().toString(), itx.getCause());
		} catch(Exception x) {
			throw new SQLException("Exception calling " + name + " on " + src + ": " + x, x);
		}
	}

	/**
	 * This method creates public synonyms for all objects in a schema (except TYPE objects).
	 */
	public static void updateSynonyms(@NonNull DataSource ds, @NonNull String owner, String... objectNames) {
		PreparedStatement ps = null;
		PreparedStatement ps2 = null;
		ResultSet rs = null;
		Connection dbc = null;
		try {
			dbc = ds.getConnection();

			//-- Drop all invalid synonyms
			ps = dbc.prepareStatement("select owner,object_name from dba_objects where object_type = 'SYNONYM' and status = 'INVALID'");
			rs = ps.executeQuery();
			while(rs.next()) {
				String sowner = rs.getString(1);
				String name = rs.getString(2);

				try(Statement st = dbc.createStatement()) {
					if("PUBLIC".equals(sowner)) {
						st.executeUpdate("drop public synonym \"" + name + "\"");
					} else {
						st.executeUpdate("drop synonym \"" + sowner + "\".\"" + name + "\"");
					}
				} catch(Exception x) {
					System.out.println("Failed to drop synonym " + sowner + "." + name + ": " + x);
				}
			}
			rs.close();
			ps.close();

			//-- Recreate all
			int ct = 0;
			long ts = System.currentTimeMillis();
			String objectNamesFilter = null;
			if(objectNames != null && objectNames.length > 0) {
				StringBuilder sb = new StringBuilder();
				for(String object : objectNames) {
					sb.append("'").append(object).append("',");
				}
				sb.setLength(sb.length() - 1);
				objectNamesFilter = " and o.object_name in (" + sb.toString() + ")";
			}

			ps = dbc.prepareStatement("select o.object_name from dba_objects o" //
				+ " where o.owner = '" + owner + "'" //
				+ " and not (o.object_type in ('TYPE', 'SYNONYM'))" //
				+ (objectNamesFilter != null ? objectNamesFilter : "") //
				+ " and not (object_name like 'SYS@_PLSQL@_%' escape '@')"
				+ " and not exists  (" //
				+ " select 1 from dba_synonyms s" //
				+ " where s.owner = 'PUBLIC'" //
				+ " and s.synonym_name = o.object_name " //
				+ ")" //
			);

			//-- First scan everything in a set, removing duplicate names (like for packages and package bodies, sigh).
			Set<String> todoSet = new HashSet<String>();
			rs = ps.executeQuery();
			while(rs.next()) {
				String res = rs.getString(1);
				if(null != res)
					todoSet.add(res);
			}
			rs.close();
			ps.close();

			//-- Now create synonyms for the set.
			for(String on : todoSet) {
				LOG.info(owner + ": create missing synonym '" + on + "'");
				try {
					ps2 = dbc.prepareStatement("create public synonym \"" + on + "\" for " + owner + ".\"" + on + "\"");
					ps2.executeUpdate();
				} catch(Exception x) {
					String msg = x.toString();
					if(!msg.contains("xxORA-00955")) {                    // jal do not remove this test !@!@!
						System.out.println(owner + ": error creating synonym " + on + ": " + x);
						LOG.error(owner + ": error creating synonym " + on + ": " + x);
					}
				} finally {
					ps2.close();
				}
				ct++;
			}

			ts = System.currentTimeMillis() - ts;
			LOG.info(owner + ": created " + ct + " public synonyms in " + DbPoolUtil.strMillis(ts));
		} catch(Exception x) {
			System.out.println(owner + ": exception while trying to create missing synonyms: " + x);
			LOG.error(owner + ": exception while trying to create missing synonyms: " + x);
			x.printStackTrace();
		} finally {
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {
				//-- Ignore
			}
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {
				//-- Ignore
			}
			try {
				if(ps2 != null)
					ps2.close();
			} catch(Exception x) {
				//-- Ignore
			}
			try {
				if(null != dbc)
					dbc.close();
			} catch(Exception x) {
				//-- Ignore
			}
		}
	}

	/**
	 * Action to execute as the privileged oracle user.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Apr 5, 2011
	 */
	public interface IPrivilegedAction {
		Object execute(@NonNull Connection dbc) throws Exception;
	}

	/**
	 * Execute the action passed using a connection that is logged in as the privileged user of other specified schema.
	 * We do not know the password for this user AND we do not want to store that password: if we do that any change to the
	 * password would cause a problem. The solution implemented here is to use the privileged account and change the other schema user password
	 * to a known value; then we login using that password and obtain connection for execute the action. We restore the original password
	 * immediately after obtaining connection, even before privileged action is executed.
	 *
	 * @param otherSchemaDs DataSource for other schema.
	 * @param otherUserName Username - account that we use to run privileged action - we change its password temporary.
	 * @param defaultDs     DataSource that we use for password manipulations.
	 * @param paction       Privileged action that is executed under otherUserName account directly in otherSchemaDs.
	 */
	static public Object runAsOtherSchemaUser(@NonNull DataSource otherSchemaDs, @NonNull String otherUserName, @NonNull DataSource defaultDs, @NonNull IPrivilegedAction paction) throws Exception {
		Connection otherSchemaConn = null;
		Connection defaultConn = defaultDs.getConnection();
		try {
			otherSchemaConn = allocateConnectionAs(otherSchemaDs, otherUserName, defaultConn);
			return paction.execute(otherSchemaConn);
		} finally {
			try {
				if(otherSchemaConn != null)
					otherSchemaConn.close();
			} catch(Exception x) {
				//-- Ignore
			}

			try {
				if(defaultConn != null)
					defaultConn.close();
			} catch(Exception x) {
				//-- Ignore
			}
		}
	}

	/**
	 * This allocates a connection to another user without that other-user's password, using an initial connection with DBA privileges.
	 */
	@NonNull
	static public Connection allocateConnectionAs(@NonNull DataSource otherSchemaDs, @NonNull String otherUserName, @NonNull Connection sourceConn) throws Exception {
		int phase = 0;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String realhashpass = null;
		String tmppass = "h3rr4lbr4k";
		Connection otherSchemaConn = null;
		try {
			//-- Make extremely sure we're not already the user; if we are abrt.
			ps = sourceConn.prepareStatement("select user from dual");
			rs = ps.executeQuery();
			if(!rs.next())
				throw new SQLException("Cannot get own user id");
			String myuid = rs.getString(1);
			if(myuid == null)
				throw new SQLException("Cannot get own user id");
			if(myuid.equalsIgnoreCase(otherUserName))
				throw new IllegalArgumentException("Trying to get a connection for user=" + otherUserName + " - but you are that user");
			rs.close();
			ps.close();

			//-- Get current hashed password for otherUserName user
			ps = sourceConn.prepareStatement("select password from dba_users where username='" + otherUserName + "'");
			rs = ps.executeQuery();
			if(!rs.next())
				throw new SQLException("The privileged " + otherUserName + " user cannot be found in the data dictionary");
			realhashpass = rs.getString(1);
			if(null == realhashpass) {
				//-- 11g: password always null, try password from sys.user$.
				rs.close();
				ps.close();

				ps = sourceConn.prepareStatement("select password from sys.user$ where name='" + otherUserName + "'");
				rs = ps.executeQuery();
				if(!rs.next())
					throw new SQLException("The privileged " + otherUserName + " user cannot be found in the sys.user$ table");
				realhashpass = rs.getString(1);
				if(null == realhashpass)
					throw new SQLException("Null hash for privileged " + otherUserName + " user?");
			}
			rs.close();
			rs = null;
			ps.close();
			ps = null;

			//-- Alter the password to a known value.
			ps = sourceConn.prepareStatement("alter user " + otherUserName + " identified by " + tmppass);
			ps.executeUpdate();
			phase = 1;
			ps.close();
			ps = null;

			otherSchemaConn = otherSchemaDs.getConnection(otherUserName, tmppass); // FIXME URGENT Need actual security here 8-(
			otherSchemaConn.setAutoCommit(false);

			//-- Immediately restore the original password,
			ps = sourceConn.prepareStatement("alter user " + otherUserName + " identified by values '" + realhashpass + "'");
			ps.executeUpdate();
			ps.close();
			ps = null;
			phase = 0;

			//-- Now execute the other schema command on the otherUserName connection.
			Connection newc = otherSchemaConn;                    // Pass ownership to caller.
			otherSchemaConn = null;
			return newc;
		} finally {
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {
				//-- Ignore
			}
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {
				//-- Ignore
			}

			if(phase == 1) {
				restorePassword(sourceConn, otherUserName, realhashpass);
			}
			try {
				if(otherSchemaConn != null)
					otherSchemaConn.close();
			} catch(Exception x) {
				//-- Ignore
			}
		}
	}

	static private void restorePassword(@NonNull Connection dbc, @NonNull String userName, @NonNull String hash) {
		PreparedStatement ps = null;
		try {
			//-- Immediately restore the original password,
			ps = dbc.prepareStatement("alter user " + userName + " identified by values '" + hash + "'");
			ps.executeUpdate();
		} catch(Exception x) {
			System.out.println("FATAL: Cannot restore the password for the " + userName + " user!!!!");
			x.printStackTrace();
		} finally {
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {
				//-- Ignore
			}
		}
	}

	/**
	 * Set the database session to CHAR or BYTE semantics.
	 */
	static public void setCharSemantics(@NonNull Connection dbc, boolean ischar) throws Exception {
		PreparedStatement ps = null;
		try {
			ps = dbc.prepareStatement("alter session set nls_length_semantics=" + (ischar ? "CHAR" : "BYTE"));
			ps.executeUpdate();
		} finally {
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {
				//-- Ignore
			}
		}
	}

	static private class Pair {
		final private String m_owner;

		final private String m_name;

		public Pair(String owner, String name) {
			m_owner = owner;
			m_name = name;
		}

		public String getOwner() {
			return m_owner;
		}

		public String getName() {
			return m_name;
		}
	}

	/**
	 * Recompile all packages or only invalid packages for the specified schema.
	 */
	static public void recompileAll(@NonNull Connection dbc, @NonNull String schema, boolean invalidsonly, boolean charsemantics) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			OracleDB.setCharSemantics(dbc, charsemantics);

			//-- remove all invalid synonyms and all that refer to pipelined versioned objects (sys_plsql_ ones)
			ps = dbc.prepareStatement("select owner,object_name from dba_objects where object_type = 'SYNONYM' and (object_name like 'SYS_PLSQL_%' or status = 'INVALID')");
			rs = ps.executeQuery();
			List<Pair> all = new ArrayList<Pair>();
			while(rs.next()) {
				all.add(new Pair(rs.getString(1), rs.getString(2)));
			}
			rs.close();
			ps.close();

			for(Pair p : all)
				dropSynonym(dbc, p);

			ps = dbc.prepareStatement("begin dbms_utility.compile_schema(schema=>?, compile_all=>" + (invalidsonly ? "FALSE" : "TRUE") + "); end;");
			ps.setString(1, schema.toUpperCase());
			ps.executeUpdate();
			ps.close();

			//-- 20110113 jal Fix from Leo for the "state of packages" error that occurs regardless of the actual package state
			ps = dbc.prepareStatement("begin dbms_session.reset_package; end;");
			ps.executeUpdate();
			ps.close();
		} finally {
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {
				//-- Ignore
			}
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {
				//-- Ignore
			}
		}
	}

	private static void dropSynonym(@NonNull Connection dbc, @NonNull Pair p) {
		PreparedStatement ps = null;
		try {
			String sql = "PUBLIC".equalsIgnoreCase(p.getOwner()) ? "drop public synonym \"" + p.getName() + "\"" : "drop synonym " + p.getOwner() + ".\"" + p.getName() + "\"";
			ps = dbc.prepareStatement(sql);
			ps.executeUpdate();
		} catch(Exception x) {
			//-- Willfully ignore.
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
