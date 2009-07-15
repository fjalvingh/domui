package to.etc.domui.hibernate.generic;

import to.etc.webapp.query.*;

public class HibernateReattachingContextFactory extends QDataContextFactoryBase {
	private HibernateSessionMaker m_sessionMaker;

	public HibernateReattachingContextFactory(QEventListenerSet set, HibernateSessionMaker sessionMaker) {
		super(set);
		m_sessionMaker = sessionMaker;
	}

	@Override
	public QDataContext getDataContext() throws Exception {
		return new HibernateReattachingDataContext(this, m_sessionMaker);
	}

	@Override
	public void releaseDataContext(QDataContext dc) {
		BuggyHibernateBaseContext q = (BuggyHibernateBaseContext) dc;
		q.close();
	}
}
