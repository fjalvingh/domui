package to.etc.domui.hibernate.generic;

import to.etc.webapp.query.*;

/**
 * Utterly basic Source for Hibernate connections without any lifecycle management.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 25, 2008
 */
public class HibernateDataContextFactory implements QDataContextFactory {
	private HibernateSessionMaker m_sessionMaker;
	private QEventListenerSet m_eventSet;

	public HibernateDataContextFactory(QEventListenerSet set, HibernateSessionMaker sessionMaker) {
		m_eventSet = set;
		m_sessionMaker = sessionMaker;
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContextFactory#getDataContext()
	 */
	public QDataContext getDataContext() throws Exception {
		return new HibernateQDataContext(this, m_sessionMaker);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContextFactory#closeDataContext(to.etc.webapp.query.QDataContext)
	 */
	public void closeDataContext(QDataContext dc) {
		HibernateQDataContext q = (HibernateQDataContext) dc;
		q.internalClose();
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContextFactory#getEventListeners()
	 */
	public QEventListenerSet getEventListeners() {
		return m_eventSet;
	}
}
