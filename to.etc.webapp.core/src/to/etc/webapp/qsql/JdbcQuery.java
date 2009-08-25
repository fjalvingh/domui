package to.etc.webapp.qsql;

import java.sql.*;
import java.util.*;

import to.etc.webapp.query.*;

/**
 * Encapsulates an actual query.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 25, 2009
 */
public class JdbcQuery<T> {
	private String m_sql;

	private List<IInstanceMaker> m_rowMaker;

	private List<ValSetter> m_valList;

	public JdbcQuery(String sql, List<IInstanceMaker> retrieverList, List<ValSetter> vl) {
		m_sql = sql;
		m_rowMaker = retrieverList;
		m_valList = vl;
	}

	public List<T> query(Connection dbc) throws Exception {
		//-- 1. Create the prepared statement,
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = dbc.prepareStatement(m_sql);
			for(ValSetter vs : m_valList)
				assignValue(ps, vs);

			List<Object> res = new ArrayList<Object>();
			rs = ps.executeQuery();
			while(rs.next()) {
				if(m_rowMaker.size() == 1) {
					res.add(m_rowMaker.get(0).make(rs));
				} else {
					Object[] row = new Object[m_rowMaker.size()];
					for(int i = 0; i < m_rowMaker.size(); i++) {
						row[i] = m_rowMaker.get(i).make(rs);
					}
					res.add(row);
				}
			}
			return (List<T>) res;
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

	private void assignValue(PreparedStatement ps, ValSetter vs) throws Exception {
		vs.getConverter().assignParameter(ps, vs.getIndex(), vs.getProperty(), vs.getValue());
	}

	static public <T> JdbcQuery<T> create(QCriteria<T> q) throws Exception {
		JdbcSQLGenerator qg = new JdbcSQLGenerator();
		qg.visitCriteria(q);
		return (JdbcQuery<T>) qg.getQuery();
	}

	public void dump() {
		System.out.println("SQL: " + m_sql);
	}
}
