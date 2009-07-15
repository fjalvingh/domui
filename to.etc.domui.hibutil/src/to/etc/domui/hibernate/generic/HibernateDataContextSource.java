package to.etc.domui.hibernate.generic;

import to.etc.webapp.query.*;

/**
 * Sample Source for Hibernate connections.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 25, 2008
 */
public class HibernateDataContextSource extends QDataContextSourceBase {
	private HibernateSessionMaker m_sessionMaker;

	public HibernateDataContextSource(HibernateSessionMaker sessionMaker) {
		m_sessionMaker = sessionMaker;
	}

	@Override
	public QDataContext getDataContext() throws Exception {
		return new HibernateQDataContext(m_sessionMaker);
	}

	@Override
	public void releaseDataContext(QDataContext dc) {
		HibernateQDataContext q = (HibernateQDataContext) dc;
		q.close();
	}
}
