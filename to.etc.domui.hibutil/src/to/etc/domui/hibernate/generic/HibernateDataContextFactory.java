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

	private QQueryHandlerList m_handlers;

	public HibernateDataContextFactory(QEventListenerSet set, HibernateSessionMaker sessionMaker, QQueryHandlerList list) {
		m_eventSet = set;
		m_sessionMaker = sessionMaker;
		m_handlers = list;
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
	 * @see to.etc.webapp.query.QDataContextFactory#getEventListeners()
	 */
	public QEventListenerSet getEventListeners() {
		return m_eventSet;
	}

	@Override
	public QQueryHandlerList getQueryHandlerList() {
		return m_handlers;
	}
}
