package to.etc.domui.hibernate.generic;

import to.etc.webapp.query.*;

public class HibernateLongSessionContextFactory extends QDataContextFactoryBase {
	protected HibernateSessionMaker m_sessionMaker;

	public HibernateLongSessionContextFactory(QEventListenerSet eventSet, HibernateSessionMaker sessionMaker) {
		super(eventSet);
		m_sessionMaker = sessionMaker;
	}

	@Override
	public QDataContext getDataContext() throws Exception {
		return new HibernateLongSessionContext(this, m_sessionMaker);
	}

	@Override
	public void releaseDataContext(QDataContext dc) {
		BuggyHibernateBaseContext q = (BuggyHibernateBaseContext) dc;
		q.close();
	}
}
