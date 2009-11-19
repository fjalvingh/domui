package to.etc.dbpool;

import java.sql.*;

/**
 * Created on Jul 8, 2003
 * @author jal
 */
public class BetterSQLException extends SQLException {
	private static final long serialVersionUID = 3916681942006359065L;

	public BetterSQLException(String sql, SQLException rootcause) {
		super(format(sql, rootcause, null, 0));
		this.initCause(rootcause);
	}

	public BetterSQLException(String sql, Object[] par, int ct, SQLException rootcause) {
		super(format(sql, rootcause, par, ct));
		this.initCause(rootcause);
	}

	@Override
	public int getErrorCode() {
		if(getCause() != null) {
			return ((SQLException) getCause()).getErrorCode();
		}
		return 0;
	}

	@Override
	public String getSQLState() {
		if(getCause() != null) {
			return ((SQLException) getCause()).getSQLState();
		}
		return null;
	}

	static private String format(String sql, SQLException rootcause, Object[] par, int ct) {
		StringBuilder sb = new StringBuilder(128);
		sb.append(rootcause.toString());
		sb.append("\n\nSQL: ");
		sb.append(sql);
		sb.append('\n');
		if(par != null && ct > 0) {
			sb.append("Parameters:\n");
			for(int i = 0; i < ct; i++) {
				sb.append("#");
				sb.append(i + 1);
				sb.append(":");
				Object o = par[i];
				if(o == null)
					sb.append("[null]\n");
				else {
					sb.append(o.getClass().getName());
					sb.append(":");
					sb.append(o.toString());
					sb.append("\n");
				}
				if(sb.length() > 8192) {
					sb.append(".... (truncated)...\n");
					break;
				}
			}
		}

		return sb.toString();
	}

	static public String format(Object[] par, int ct) {
		StringBuilder sb = new StringBuilder(128);
		if(par != null && ct > 0) {
			sb.append("Parameters:\n");
			for(int i = 0; i < ct; i++) {
				sb.append("#");
				sb.append(i + 1);
				sb.append(":");
				Object o = par[i];
				if(o == null)
					sb.append("[null]\n");
				else {
					sb.append(o.getClass().getName());
					sb.append(":");
					sb.append(o.toString());
					sb.append("\n");
				}
				if(sb.length() > 8192) {
					sb.append(".... (truncated)...\n");
					break;
				}
			}
		}

		return sb.toString();
	}
}
