package to.etc.dbreplay;

import java.io.*;
import java.math.*;
import java.sql.*;

import javax.annotation.*;

import to.etc.dbpool.*;

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
		} catch(EOFException x) {
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
		for(int i = 0; i < m_parameterAr.length; i++) {
			m_parameterAr[i] = readParameter(r);
		}
	}

	private Object readParameter(DbReplay r) throws Exception {
		int type = r.readByte();
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

}
