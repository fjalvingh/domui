package to.etc.domui.hibernate.generic;

import to.etc.webapp.query.*;

/**
 * Source for Hibernate connections.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 25, 2008
 */
public class HibernateDataContextSource implements QDataContextSource {
	private HibernateSessionMaker m_sessionMaker;

	public HibernateDataContextSource(HibernateSessionMaker sessionMaker) {
		m_sessionMaker = sessionMaker;
	}

	public QDataContext getDataContext() throws Exception {
		return new HibernateQDataContext(m_sessionMaker);
	}

	public void releaseDataContext(QDataContext dc) {
		HibernateQDataContext q = (HibernateQDataContext) dc;
		q.close();
	}
}
