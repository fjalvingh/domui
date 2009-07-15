package to.etc.domui.hibernate.generic;

import to.etc.webapp.query.*;

public class HibernateReattachingSource extends QDataContextSourceBase {
	private HibernateSessionMaker m_sessionMaker;

	public HibernateReattachingSource(QEventListenerSet set, HibernateSessionMaker sessionMaker) {
		super(set);
		m_sessionMaker = sessionMaker;
	}

	@Override
	public QDataContext getDataContext() throws Exception {
		return new HibernateReattachingDataContext(this, m_sessionMaker);
	}

	@Override
	public void releaseDataContext(QDataContext dc) {
		HibernaatjeBaseContext q = (HibernaatjeBaseContext) dc;
		q.close();
	}
}
