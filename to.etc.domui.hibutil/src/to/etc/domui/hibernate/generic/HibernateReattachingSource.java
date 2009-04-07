package to.etc.domui.hibernate.generic;

import to.etc.domui.util.query.*;
import to.etc.webapp.query.*;

public class HibernateReattachingSource implements QDataContextSource {
	private HibernateSessionMaker				m_sessionMaker;

	public HibernateReattachingSource(HibernateSessionMaker sessionMaker) {
		m_sessionMaker = sessionMaker;
	}

	public QDataContext getDataContext() throws Exception {
		return new HibernateReattachingDataContext(m_sessionMaker);
	}

	public void releaseDataContext(QDataContext dc) {
		HibernaatjeBaseContext	q = (HibernaatjeBaseContext) dc;
		q.close();
	}
}
