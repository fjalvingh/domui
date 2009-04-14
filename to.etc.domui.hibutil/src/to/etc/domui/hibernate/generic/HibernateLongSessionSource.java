package to.etc.domui.hibernate.generic;

import to.etc.webapp.query.*;

public class HibernateLongSessionSource implements QDataContextSource {
	protected HibernateSessionMaker				m_sessionMaker;

	public HibernateLongSessionSource(HibernateSessionMaker sessionMaker) {
		m_sessionMaker = sessionMaker;
	}

	public QDataContext getDataContext() throws Exception {
		return new HibernateLongSessionContext(m_sessionMaker);
	}

	public void releaseDataContext(QDataContext dc) {
		HibernaatjeBaseContext	q = (HibernaatjeBaseContext) dc;
		q.close();
	}
}
