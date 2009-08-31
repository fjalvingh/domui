package to.etc.domui.component.tbl;

import java.util.*;

import to.etc.domui.component.tbl.*;
import to.etc.domui.dom.html.*;
import to.etc.webapp.query.*;

public class DefaultQueryHandler<T> implements IQueryHandler<T> {
	private QDataContextFactory m_dcf;

	public DefaultQueryHandler(NodeBase b) {
		m_dcf = QContextManager.getDataContextFactory(b.getPage().getConversation());
	}

	public List<T> query(QCriteria<T> q) throws Exception {
		QDataContext dc = m_dcf.getDataContext();
		try {
			return dc.query(q);
		} finally {
			try {
				dc.close();
			} catch(Exception x) {}
		}
	}
}
