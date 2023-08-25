package to.etc.webapp.qsql;

import to.etc.util.WrappedException;
import to.etc.webapp.query.QDataContext;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-08-2023.
 */
public class JdbcObjectStream<T> implements Iterator<T> {
	private final JdbcQuery<T> m_query;

	private final QDataContext m_dc;

	private final PreparedStatement m_ps;

	private final ResultSet m_rs;

	private final int m_start;

	private int m_nextRowIndex = 0;

	private int m_rowsRead;

	public JdbcObjectStream(JdbcQuery<T> query, QDataContext dc, PreparedStatement ps, ResultSet rs) {
		m_query = query;
		m_dc = dc;
		m_ps = ps;
		m_rs = rs;
		m_start = query.getStart() < 0 ? 0 : -1;
	}

	@Override
	public boolean hasNext() {
		try {
			if(m_query.getLimit() > 0 && m_rowsRead >= m_query.getLimit())
				return false;
			while(m_nextRowIndex < m_query.getStart()) {
				if(! m_rs.next())
					return false;
				m_nextRowIndex++;
			}
			return m_rs.next();
		} catch(Exception x) {
			throw WrappedException.wrap(x);
		}
	}

	@Override
	public T next() {
		try {
			if(m_rs.isAfterLast() || m_rs.isBeforeFirst())
				throw new IllegalStateException("No next available");
			return m_query.readRecord(m_dc, m_rs);
		} catch(Exception x) {
			throw WrappedException.wrap(x);
		}
	}
}
