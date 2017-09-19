package to.etc.dbreplay;

import to.etc.dbpool.ConnectionPool;
import to.etc.dbpool.StatementProxy;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

/**
 * Contains the decoded "image" from the binary record log file.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 31, 2011
 */
class ReplayRecord {
	static public ReplayRecord readRecord(@Nonnull DbReplay r) throws Exception {
		try {
			long magic = r.readLong();
			if(magic != ConnectionPool.STMT_START_MAGIC)
				throw new IOException("Invalid/missing record start marker");
		} catch(EofException x) {
			return null;
		}
		ReplayRecord rr = new ReplayRecord();
		rr.load(r);
		return rr;
	}

	private int m_type;

	private long m_statementTime;

	private int m_connectionId;

	private String m_sql;

	private int m_paramCount;

	private Object[] m_parameterAr;

	private int[] m_parameterType;

	private boolean m_unexecutable;

	/**
	 * Load the entire record.
	 * @param bis
	 * @throws Exception
	 */
	private void load(@Nonnull DbReplay r) throws Exception {
		//-- Read the fixed header.
		int type = r.readByte(); // The record/statement type.
		m_type = type;
		m_statementTime = r.readLong();
		m_connectionId = r.readInt();

		//-- Short frame?
		switch(m_type){
			case StatementProxy.ST_CLOSE:
			case StatementProxy.ST_COMMIT:
			case StatementProxy.ST_ROLLBACK:
				return;
		}

		//-- Long frame containing a statement. Get it,
		m_sql = r.readString();
		m_paramCount = r.readInt();
		if(m_paramCount < 0) // bugfix: log format sometimes contained -1 as param count.
			m_paramCount = 0;
		m_parameterAr = new Object[m_paramCount];
		m_parameterType = new int[m_paramCount];
		for(int i = 0; i < m_parameterAr.length; i++) {
			int ptype = r.readByte();
			m_parameterAr[i] = readParameter(r, ptype);
			m_parameterType[i] = ptype;
		}

		if(m_sql.toLowerCase().contains(" for update"))
			m_unexecutable = true;
	}

	private Object readParameter(DbReplay r, int type) throws Exception {
		switch(type){
			default:
				throw new IOException("Input: unexpected parameter type: " + type + " (" + (char) type + ")");
			case '0':
				return null;
			case 'i':
				return Integer.valueOf(r.readInt());
			case 'l':
				return Long.valueOf(r.readLong());
			case 'B':
				return new BigDecimal(r.readString());
			case 'd':
				return Double.valueOf(r.readString());
			case 'f':
				return Float.valueOf(r.readString());
			case '$':
				return r.readString();
			case 'T':
				long ts = r.readLong();
				return new Timestamp(ts);
			case '?':
				String name = r.readString();
				System.out.println("Unknown parameter type " + name + " in statement - marked as unexecutable");
				m_unexecutable = true;
				return null;
		}
	}

	public void assignParameter(PreparedStatement ps, int index) throws Exception {
		Object v = m_parameterAr[index];
		switch(m_parameterType[index]){
			default:
				throw new IOException("assign: unexpected parameter type: " + m_parameterType[index]);

			case '0':
				ps.setString(index + 1, null);
				break;

			case 'i':
				ps.setInt(index + 1, ((Integer) v).intValue());
				break;

			case 'l':
				ps.setLong(index + 1, ((Long) v).longValue());
				break;

			case 'B':
				ps.setBigDecimal(index + 1, (BigDecimal) v);
				break;

			case 'd':
				ps.setDouble(index + 1, ((Double) v).doubleValue());
				break;

			case 'f':
				ps.setFloat(index + 1, ((Float) v).floatValue());
				break;

			case '$':
				ps.setString(index + 1, (String) v);
				break;

			case 'T':
				ps.setTimestamp(index + 1, (Timestamp) v);
				break;
		}
	}


	public int getType() {
		return m_type;
	}

	public long getStatementTime() {
		return m_statementTime;
	}

	public int getConnectionId() {
		return m_connectionId;
	}

	public String getSql() {
		return m_sql;
	}

	public int getParamCount() {
		return m_paramCount;
	}

	public Object[] getParameterAr() {
		return m_parameterAr;
	}

	public boolean isUnexecutable() {
		return m_unexecutable;
	}

	public int[] getParameterType() {
		return m_parameterType;
	}

	public String getSummary() {
		StringBuilder sb = new StringBuilder();
		sb.append(DbReplay.format(new Date(getStatementTime())));
		sb.append(" @").append(getConnectionId());
		sb.append(" ").append(m_sql);
		return sb.toString();
	}
}
