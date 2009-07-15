package to.etc.domui.hibernate.generic;

import to.etc.webapp.query.*;

public class HibernateLongSessionSource extends QDataContextSourceBase {
	protected HibernateSessionMaker m_sessionMaker;

	public HibernateLongSessionSource(HibernateSessionMaker sessionMaker) {
		m_sessionMaker = sessionMaker;
	}

	@Override
	public QDataContext getDataContext() throws Exception {
		return new HibernateLongSessionContext(m_sessionMaker);
	}

	@Override
	public void releaseDataContext(QDataContext dc) {
		HibernaatjeBaseContext q = (HibernaatjeBaseContext) dc;
		q.close();
	}
}
