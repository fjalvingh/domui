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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * A generic database access class to hide differences between databases.
 *
 * @author <a href="mailto:jal@bigfoot.com">F. Jalvingh</a>
 * @version 1.0
 */
public class GenericDB {
	static public final BaseDB dbtypeUNKNOWN = new UnknownDB();

	static public final BaseDB dbtypeORACLE = new OracleDB();

	static public final BaseDB dbtypeMYSQL = new MysqlDB();

	static public final BaseDB dbtypePOSTGRESQL = new PostgresDB();

	private GenericDB() {
	}

	/**
	 * Tries to return a database type for the connection passed.
	 * TODO: Need to be replaced with generic accept() in base specific class
	 *
	 * @param dbc the connection to check
	 * @return a dbtype for the connection.
	 */
	static public BaseDB getDbType(Connection dbc) {
		//-- Not a pooled dude.. Try to get a database type from the connection..
		try {
			return getDbTypeByDriverName(dbc.getMetaData().getDriverName());
		} catch(Exception x) {
			return dbtypeUNKNOWN;
		}
	}

	static public BaseDB getDbTypeByDriverName(String dn) {
		dn = dn.toLowerCase();
		//		System.out.println("Reported driver name is "+dn);
		if(dn.indexOf("oracle") != -1)
			return GenericDB.dbtypeORACLE;
		else if(dn.indexOf("mysql") != -1)
			return GenericDB.dbtypeMYSQL;
		else if(dn.indexOf("postgresql") != -1)
			return GenericDB.dbtypePOSTGRESQL;
		else if(dn.indexOf("orac") != -1)
			return GenericDB.dbtypeORACLE;
		return GenericDB.dbtypeUNKNOWN;
	}

	static private BaseDB getBase(BaseDB db) {
		return db;
	}

	static private BaseDB getBase(Connection dbc) {
		return getBase(getDbType(dbc));
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Generic entrypoints.								*/
	/*--------------------------------------------------------------*/
	static public int getFullSequenceID(Connection dbc, String tablename) throws SQLException {
		return getBase(dbc).getFullSequenceID(dbc, tablename);
	}

	/**
	 * Writes a blob to the requested record.
	 *
	 * @parameter is    The stream to write to the blob. If this is null then the
	 * field is set to dbnull.
	 */
	static public void setBlob(Connection dbc, String table, String column, String where, InputStream is, int len) throws Exception {
		getBase(dbc).setBlob(dbc, table, column, where, is, len);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Oracle non-JDBC compliant code...					*/
	/*--------------------------------------------------------------*/

	/**
	 * Get a database script as a resource. The basename should be the base name of the script like "tables.sql". The script
	 * will determine the db type, then first try "tables-[dbtype].sql". If that is not found it will fallback to the basename.
	 */
	@NonNull
	static public String getScriptResource(@NonNull Connection dbc, @NonNull Class<?> clz, @NonNull String baseName) throws Exception {
		BaseDB type = getDbType(dbc);
		String name = type.getName();

		InputStream is = null;
		try {
			String suff = ".sql";
			String root = baseName;

			int pos = baseName.lastIndexOf('.');
			if(pos != -1) {
				suff = baseName.substring(pos);
				root = baseName.substring(0, pos);
			}

			String r = root + "-" + name + suff;
			is = clz.getResourceAsStream(r);
			if(null == is) {
				is = clz.getResourceAsStream(root + suff);
				if(is == null)
					throw new SQLException("The SQL resource file " + baseName + " (relative to " + clz.getName() + ") cannot be found.");
			}
			return readStreamAsString(is, "utf-8");
		} finally {
			try {
				if(is != null)
					is.close();
			} catch(Exception x) {
			}
		}
	}

	static private String readStreamAsString(final InputStream is, final String enc) throws Exception {
		StringBuilder sb = new StringBuilder(128);
		Reader r = new InputStreamReader(is, enc);
		try {
			char[] buf = new char[4096];
			for(; ; ) {
				int ct = r.read(buf);
				if(ct < 0)
					break;
				sb.append(new String(buf, 0, ct));
			}
		} finally {
			r.close();
		}
		return sb.toString();
	}

	/**
	 * Run a script resource with some basic name. Statements in the script must be separated by //
	 */
	static public void runScriptResource(Connection dbc, Class<?> clz, String name, StringBuilder errors) throws Exception {
		String script = getScriptResource(dbc, clz, name);
		String[] ar = script.split("(//)|(--\n)");
		for(String s : ar) {
			runSQL(dbc, s, errors);
		}
	}

	static private boolean isWhiteSpaceOrNbsp(final char c) {
		return c == 0x00a0 || Character.isWhitespace(c);
	}

	static private boolean isAllSpaces(final String s) {
		for(int i = s.length(); --i >= 0; ) {
			if(!isWhiteSpaceOrNbsp(s.charAt(i)))
				return false;
		}
		return true;
	}

	static private void runSQL(Connection dbc, String s, StringBuilder errors) {
		s = s.trim();
		if(isAllSpaces(s))
			return;

		PreparedStatement ps = null;
		try {
			ps = dbc.prepareStatement(s);
			ps.executeUpdate();
			dbc.commit();
		} catch(Exception x) {
			errors.append(s).append("\n").append("ERROR: ").append(x.toString()).append("\n");
		} finally {
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {
			}
			try {
				dbc.rollback();
			} catch(Exception x) {
			}
		}
	}

}
