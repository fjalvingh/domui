package to.etc.domui.hibernate.generic;

import to.etc.webapp.query.*;

public class HibernateLongSessionContextFactory implements QDataContextFactory {
	protected HibernateSessionMaker m_sessionMaker;

	private QEventListenerSet m_eventSet;

	private QQueryHandlerList m_handlers;

	public HibernateLongSessionContextFactory(QEventListenerSet eventSet, HibernateSessionMaker sessionMaker, QQueryHandlerList handlers) {
		m_eventSet = eventSet;
		m_sessionMaker = sessionMaker;
		m_handlers = handlers;
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContextFactory#getDataContext()
	 */
	public QDataContext getDataContext() throws Exception {
		return new HibernateLongSessionContext(this, m_sessionMaker);
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
