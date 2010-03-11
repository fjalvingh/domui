package to.etc.domui.component.tbl;

import java.util.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.state.*;
import to.etc.webapp.query.*;

/**
 * QD TEMPORARY INTERFACE This class is a handler which allows you to execute
 * a JDBC style query on the same QDataContext used for JPA style queries. When
 * used it allocates a database connection from the QDataContext, then it
 * creates a jdbc query on it and executes.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 31, 2009
 */
public class SameDbJdbcQueryHandler<T> implements IQueryHandler<T> {
	private QDataContextFactory m_dcf;

	public SameDbJdbcQueryHandler(NodeBase b) {
		m_dcf = QContextManager.getDataContextFactory(b.getPage().getConversation());
	}

	public SameDbJdbcQueryHandler(ConversationContext cc) {
		m_dcf = QContextManager.getDataContextFactory(cc);
	}

	/**
	 * FIXME Too verbose and not generalized.
	 * @see to.etc.domui.component.tbl.IQueryHandler#query(to.etc.webapp.query.QCriteria)
	 */
	public List<T> query(QCriteria<T> q) throws Exception {
		QDataContext dc = m_dcf.getDataContext();
		try {
			return dc.query(q);
			//			Connection dbc = dc.getConnection();
			//			JdbcQuery<T> query = JdbcQuery.create(q); // Convert to JDBC query.
			//			return query.query(dbc);
		} finally {
			try {
				dc.close();
			} catch(Exception x) {}
		}
	}
}
