package to.etc.domui.hibernate.generic;

import to.etc.webapp.query.*;

public class HibernateReattachingSource extends QDataContextSourceBase {
	private HibernateSessionMaker m_sessionMaker;

	public HibernateReattachingSource(HibernateSessionMaker sessionMaker) {
		m_sessionMaker = sessionMaker;
	}

	@Override
	public QDataContext getDataContext() throws Exception {
		return new HibernateReattachingDataContext(m_sessionMaker);
	}

	@Override
	public void releaseDataContext(QDataContext dc) {
		HibernaatjeBaseContext q = (HibernaatjeBaseContext) dc;
		q.close();
	}
}
