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

	private int m_start, m_limit;

	public JdbcQuery(String sql, List<IInstanceMaker> retrieverList, List<ValSetter> vl, int start, int limit) {
		m_sql = sql;
		m_rowMaker = retrieverList;
		m_valList = vl;
		m_start = start;
		m_limit = limit;
	}

	public List< ? > query(QDataContext dc) throws Exception {
		System.out.println("jdbc: " + m_sql);

		//-- 1. Create the prepared statement,
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = dc.getConnection().prepareStatement(m_sql);
			for(ValSetter vs : m_valList)
				assignValue(ps, vs);

			List<Object> res = new ArrayList<Object>();
			rs = ps.executeQuery();
			int rownum = 0;
			while(rs.next()) {
				if(rownum >= m_start) {
					if(m_limit > 0 && res.size() >= m_limit)
						break;

					if(m_rowMaker.size() == 1) {
						res.add(m_rowMaker.get(0).make(dc, rs));
					} else {
						Object[] row = new Object[m_rowMaker.size()];
						for(int i = 0; i < m_rowMaker.size(); i++) {
							row[i] = m_rowMaker.get(i).make(dc, rs);
						}
						res.add(row);
					}
				}
				rownum++;
			}
			return res;
		} catch(Exception x) {
			QDbException dx = QDbException.findTranslation(x);
			if(dx != null)
				throw dx;
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

	static public <T> JdbcQuery<T> create(QSelection<T> q) throws Exception {
		JdbcSQLGenerator qg = new JdbcSQLGenerator();
		qg.visitSelection(q);
		return (JdbcQuery<T>) qg.getQuery();
	}


	public void dump() {
		System.out.println("SQL: " + m_sql);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Helper implementations for QDataContext's			*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @param <T>
	 * @param clz
	 * @param pk
	 * @return
	 * @throws Exception
	 */
	static public <T> T find(QDataContext dc, Class<T> clz, Object pk) throws Exception {
		//-- Ohh, the joys of generalization ;-)
		JdbcClassMeta jcm = JdbcMetaManager.getMeta(clz);
		JdbcPropertyMeta pkpm = jcm.getPrimaryKey();
		if(pkpm == null)
			throw new IllegalStateException("No primary key defined on " + clz);

		QCriteria<T> qc = QCriteria.create(clz);
		qc.eq(pkpm.getName(), pk);
		return queryOne(dc, qc);
	}

	/**
	 * Get an instance; this will return an instance by first trying to load it; if that fails
	 * it will create one but only fill the PK. Use is questionable though.
	 * @see to.etc.webapp.query.QDataContext#getInstance(java.lang.Class, java.lang.Object)
	 */
	static public <T> T getInstance(QDataContext dc, Class<T> clz, Object pk) throws Exception {
		JdbcClassMeta jcm = JdbcMetaManager.getMeta(clz);
		JdbcPropertyMeta pkpm = jcm.getPrimaryKey();
		if(pkpm == null)
			throw new IllegalStateException("No primary key defined on " + clz);

		QCriteria<T> qc = QCriteria.create(clz);
		qc.eq(pkpm.getName(), pk);
		T v = queryOne(dc, qc);
		if(v != null)
			return v;

		//-- Try to create one. Just die on failure.
		v = clz.newInstance();
		pkpm.getPi().getSetter().invoke(v, pk);
		return v;
	}

	static public <T> List<T> query(QDataContext dc, QCriteria<T> q) throws Exception {
		JdbcQuery<T> query = JdbcQuery.create(q); // Convert to JDBC query.
		return (List<T>) query.query(dc);
	}

	static public List<Object[]> query(QDataContext dc, QSelection< ? > sel) throws Exception {
		JdbcQuery< ? > query = JdbcQuery.create(sel); // Convert to JDBC query.
		return (List<Object[]>) query.query(dc);
	}

	static public <T> T queryOne(QDataContext dc, QCriteria<T> q) throws Exception {
		List<T> res = query(dc, q);
		if(res.size() == 0)
			return null;
		else if(res.size() == 1)
			return res.get(0);
		throw new IllegalStateException("The criteria-query " + q + " returns " + res.size() + " results instead of one");
	}

	static public Object queryOne(QDataContext dc, QSelection< ? > q) throws Exception {
		List<Object[]> res = query(dc, q);
		if(res.size() == 0)
			return null;
		else if(res.size() == 1)
			return res.get(0);
		throw new IllegalStateException("The criteria-query " + q + " returns " + res.size() + " results instead of one");
	}


}
