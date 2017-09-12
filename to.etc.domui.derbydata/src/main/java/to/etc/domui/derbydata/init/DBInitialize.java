package to.etc.domui.derbydata.init;

import java.io.*;
import java.sql.*;

import javax.sql.*;

import to.etc.lexer.*;
import to.etc.util.*;

public class DBInitialize {
	/**
	 * Load the database, if it is empty.
	 * @throws Exception
	 */
	static public void fillDatabase(DataSource ds) throws Exception {
		Connection dbc = ds.getConnection();
		InputStream is = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String last = "Unknown";
		try {
			try {
				//	-- Is there data in the DB?
				ps = dbc.prepareStatement("select count(*) from Track");
				rs = ps.executeQuery();
				if(!rs.next())
					throw new IllegalStateException("No data");

				if(rs.getLong(1) != 0)
					return;
			} catch(Exception x) {
				System.out.println("init: the database is empty. Loading data.");
			}
			if(rs != null)
				rs.close();
			if(ps != null)
				ps.close();

			is = DBInitialize.class.getResourceAsStream("/resources/database/CreateDB.sql");
			if(is == null)
				throw new IllegalStateException("Cannot locate demo database load script.");
			Reader r = new InputStreamReader(is, "utf-8");
			ReaderTokenizerBase rsb = new ReaderTokenizerBase("CreateDB.sql", r);
			rsb.setReturnWhitespace(true);
			rsb.setReturnNewline(true);
			rsb.setKeepQuotes(true);
			StringBuilder sb = new StringBuilder();
			int count = 0;
			for(;;) {
				sb.setLength(0);
				if(!scanStatement(sb, rsb))
					break;
				//				System.out.println("SQL: " + sb.toString());
				last = sb.toString();
				ps = dbc.prepareStatement(last);
				ps.executeUpdate();
				ps.close();
				if(count % 1000 == 0)
					System.out.println("....statement " + count);
				count++;

			}
		} catch(Exception x) {
			System.err.println("SQL Statement failed:\n" + last + "\n" + x);
			throw x;
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
				if(null != is)
					is.close();
			} catch(Exception x) {}
			try {
				dbc.close();
			} catch(Exception x) {}
		}
	}

	static private String replaceDate(String instr) {
		String in = instr.substring(1, instr.length() - 1);

		String[] spl = in.split("\\/");
		if(spl.length != 3)
			return instr;
		for(int i = 0; i < 3; i++) {
			if(!StringTool.isNumber(spl[i]))
				return instr;
		}

		//-- It is a date. Reformat yyyy/mm/dd to yyyy-mm-dd.
		return "'" + in.replace('/', '-') + "'";
	}

	/**
	 * Split SQL script into statements.
	 * @param sb
	 * @param rsb
	 * @return
	 */
	static private boolean scanStatement(StringBuilder sb, ReaderTokenizerBase rsb) throws Exception {
		sb.setLength(0);
		for(;;) {
			int t = rsb.nextToken();
			if(t == -1)
				return sb.length() != 0;

			if(t == ';')
				return true;
			else if(t == '\n' || t == ' ' || t == '\t') {
				if(sb.length() > 0)
					sb.append(' ');
			} else if(t == '\r')
				;
			else if(t == ReaderScannerBase.T_STRING) {
				//-- Is this a DATE format?
				sb.append(replaceDate(rsb.getCopied()));
			} else
				sb.append(rsb.getCopied());
		}
	}


}
