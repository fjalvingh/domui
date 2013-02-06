package to.etc.webapp.qsql;

import java.sql.*;

import javax.annotation.*;

/**
 * A SQL statement that comes from a {@link SQLBundle}, which can be prepared and executed.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 16, 2011
 */
final public class BundleStatement {
	@Nonnull
	private String m_stmt;

	private int[][] m_parameterar;

	private BundleStatement(String stmt, int[][] nar) {
		m_stmt = stmt;
		m_parameterar = nar;
	}

	@Nonnull
	static BundleStatement create(@Nonnull String sql) {
		//-- Split the statement to get all parameters.
		StringBuilder sb = new StringBuilder();
		int ix = 0;
		int len = sql.length();
		int sqlindex = 1;
		int[][] paramar = new int[100][];
		int[] indexar = new int[paramar.length];
		int topnum = -1;
		while(ix < len) {
			int pos = sql.indexOf('$', ix); // Find next marker
			if(pos == -1) {
				//-- end of thing.
				if(ix < len)
					sb.append(sql.substring(ix)); // Copy remainder of statement
				break;
			}

			//-- Copy string upto marker
			if(ix < pos) {
				sb.append(sql, ix, pos); // Copy just upto the $ sign
				ix = pos;
			}

			//-- If the character before the $ is a \\ we have an escape...
			if(pos > 0 && sql.charAt(pos - 1) == '\\') {
				sb.setLength(sb.length() - 1); // Remove \\ that was copied in
				sb.append('$'); // Append escaped $
				ix++;
				continue;
			}

			//-- We have a real $. It must be followed by digits.
			int num = 0;
			ix++;
			while(ix < len) {
				char c = sql.charAt(ix);
				if(!Character.isDigit(c))
					break;
				num = num * 10 + (c - '0');
				ix++;
			}

			//-- Get the index array for this parameter number
			if(num >= paramar.length)
				throw new IllegalArgumentException("Parameter number $" + num + " is too high, only 0.." + (paramar.length - 1) + " allowed");
			if(num > topnum)
				topnum = num;
			int[] ixar = paramar[num];
			if(ixar == null) {
				ixar = new int[30];
				paramar[num] = ixar;
			}
			int index = indexar[num]++;
			if(index >= ixar.length)
				throw new IllegalArgumentException("Parameter number $" + num + " has too many occurrences, only " + (ixar.length - 1) + " allowed");
			ixar[index] = sqlindex++;
			sb.append("?");
		}

		//-- Finish structures.
		int[][] nar = new int[topnum + 1][]; // Real #of num's used
		for(int i = 0; i < nar.length; i++) {
			int[] ixar = paramar[i];
			if(ixar != null) {
				int[] nixar = new int[indexar[i]]; // Allocate real size
				System.arraycopy(ixar, 0, nixar, 0, indexar[i]);
				nar[i] = nixar;
			}
		}
		return new BundleStatement(sb.toString(), nar);
	}

	/**
	 * Create a preparedStatement from the stored statement, and assign the parameters.
	 * @param dbc
	 * @param parameters
	 * @return
	 * @throws SQLException
	 */
	public PreparedStatement prepare(Connection dbc, Object... parameters) throws SQLException {
		PreparedStatement ps = dbc.prepareStatement(m_stmt);
		boolean ok = false;
		try {
			if(parameters != null && parameters.length > 0)
				assignParameters(ps, parameters);
			ok = true;
			return ps;
		} finally {
			try {
				if(!ok)
					ps.close();
			} catch(Exception x) {}
		}
	}

	/**
	 * Assign the parameters to a prepared statement of this type.
	 * @param ps
	 * @param parameters
	 * @throws SQLException
	 */
	public void assignParameters(PreparedStatement ps, @Nonnull Object... parameters) throws SQLException {
		if(parameters.length < m_parameterar.length)
			throw new SQLException("Incorrect parameter count: you provided " + parameters.length + " but the statement needs " + m_parameterar.length);
		for(int i = 0; i < m_parameterar.length; i++) {
			int[] ixar = m_parameterar[i];
			if(ixar == null)
				continue;
			Object val = parameters[i];
			for(int j = ixar.length; --j >= 0;) {
				JdbcUtil.setParameter(ps, val, ixar[j]);
			}
		}
	}

	/**
	 * Execute the statement as an update statement.
	 * @param dbc
	 * @param parameters
	 * @return
	 * @throws SQLException
	 */
	public int executeUpdate(Connection dbc, Object... parameters) throws SQLException {
		PreparedStatement ps = prepare(dbc, parameters);
		try {
			return ps.executeUpdate();
		} finally {
			try {
				ps.close();
			} catch(Exception x) {}
		}
	}
}
