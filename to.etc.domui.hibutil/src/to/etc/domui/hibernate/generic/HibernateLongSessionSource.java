package to.etc.domui.hibernate.generic;

import to.etc.webapp.query.*;

public class HibernateLongSessionSource extends QDataContextSourceBase {
	protected HibernateSessionMaker m_sessionMaker;

	public HibernateLongSessionSource(QEventListenerSet eventSet, HibernateSessionMaker sessionMaker) {
		super(eventSet);
		m_sessionMaker = sessionMaker;
	}

	@Override
	public QDataContext getDataContext() throws Exception {
		return new HibernateLongSessionContext(this, m_sessionMaker);
	}

	@Override
	public void releaseDataContext(QDataContext dc) {
		HibernaatjeBaseContext q = (HibernaatjeBaseContext) dc;
		q.close();
	}
}
