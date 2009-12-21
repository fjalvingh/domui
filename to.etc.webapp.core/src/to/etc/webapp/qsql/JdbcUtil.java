package to.etc.webapp.qsql;

import java.sql.*;

import to.etc.util.*;

/**
 * Utility class for JDBC code.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 21, 2009
 */
public class JdbcUtil {
	private JdbcUtil() {}

	static public void setLong(PreparedStatement ps, int index, Long value) throws SQLException {
		if(value == null)
			ps.setNull(index, Types.NUMERIC);
		else
			ps.setLong(index, value.longValue());
	}

	static public void setInteger(PreparedStatement ps, int index, Integer value) throws SQLException {
		if(value == null)
			ps.setNull(index, Types.NUMERIC);
		else
			ps.setInt(index, value.intValue());
	}

	/**
	 * Sets a TIMESTAMP value containing both TIME and DATE values.
	 * @param ps
	 * @param index
	 * @param value
	 * @throws SQLException
	 */
	static public void setTimestamp(PreparedStatement ps, int index, java.util.Date value) throws SQLException {
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
	static public void setDateTruncated(PreparedStatement ps, int index, java.util.Date value) throws SQLException {
		if(value == null)
			ps.setNull(index, Types.TIMESTAMP);
		else {
			value = DateUtil.truncateDate(value);
			ps.setDate(index, new Date(value.getTime()));
		}
	}

	static public void	setString(PreparedStatement ps, int index, String value) throws SQLException {
		if(value == null || value.trim().length() == 0)
			ps.setNull(index, Types.VARCHAR);
		else
			ps.setString(index, value);
	}

	static public void setStringTruncated(PreparedStatement ps, int index, String value, int maxlen) throws SQLException {
		if(value == null || value.trim().length() == 0)
			ps.setNull(index, Types.VARCHAR);
		else {
			int len = value.length();
			if(len > maxlen)
				value = value.substring(0, maxlen);
			ps.setString(index, value);
		}
	}

	static public void setYN(PreparedStatement ps, int index, Boolean value) throws SQLException {
		if(value == null)
			ps.setNull(index, Types.VARCHAR);
		else
			ps.setString(index, value.booleanValue() ? "Y" : "N");
	}

	static public void setFK(PreparedStatement ps, int index, ILongIdentifyable foreigner) throws SQLException {
		if(foreigner == null)
			ps.setNull(index, Types.NUMERIC);
		else if(foreigner.getId() == null)
			throw new IllegalStateException("Reference to foreign object '" + foreigner + "' has a null ID.");
		else {
			ps.setLong(index, foreigner.getId().longValue());
		}
	}
}
