package to.etc.webapp.qsql;

import java.sql.*;

public class JdbcConversionException extends RuntimeException {
	public JdbcConversionException(Throwable cause, String message) {
		super(message, cause);
	}

	public JdbcConversionException(String message) {
		super(message);
	}

	public static JdbcConversionException create(Exception x, ResultSet rs, JdbcPropertyMeta pm, int rix) {
		StringBuilder sb = new StringBuilder();
		sb.append("JDBC Data Conversion failed: property ");
		sb.append(pm);
		sb.append(" could not be set from result set value '");

		String lv = "(-)";
		try {
			lv = rs.getString(rix);
		} catch(Exception xx) {}
		sb.append(lv);
		sb.append("' @ index=");
		sb.append(rix);
		sb.append(": ");
		sb.append(x.toString());
		throw new JdbcConversionException(x, sb.toString());
	}
}
