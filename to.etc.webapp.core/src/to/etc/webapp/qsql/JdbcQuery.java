package to.etc.webapp.qsql;

import java.sql.*;
import java.util.*;

/**
 * Encapsulates an actual query.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 25, 2009
 */
public class JdbcQuery {
	private String m_sql;

	private List<IInstanceMaker> m_rowMaker;

	public JdbcQuery(String sql, List<IInstanceMaker> retrieverList) {
		m_sql = sql;
		m_rowMaker = retrieverList;
	}

	public List< ? > query(Connection dbc) throws Exception {
		//-- 1. Create the prepared statement,
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = dbc.prepareStatement(m_sql);
			//-- FIXME Bind parameters for where

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
			return res;
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

}
