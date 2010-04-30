package to.etc.domui.hibernate.generic;

import to.etc.webapp.query.*;

abstract public class AbstractHibernateContextFactory implements QDataContextFactory {
	private HibernateSessionMaker m_sessionMaker;

	private QEventListenerSet m_eventSet;

	private QQueryExecutorRegistry m_handlers;

	static private QQueryExecutorRegistry m_default = new QQueryExecutorRegistry();

	static {
		m_default.register(HibernateQueryExecutor.FACTORY);
	}

	public AbstractHibernateContextFactory(QEventListenerSet eventSet, HibernateSessionMaker sessionMaker, QQueryExecutorRegistry handlers) {
		m_eventSet = eventSet;
		m_sessionMaker = sessionMaker;
		m_handlers = handlers;
	}

	public AbstractHibernateContextFactory(QEventListenerSet eventSet, HibernateSessionMaker sessionMaker) {
		m_eventSet = eventSet;
		m_sessionMaker = sessionMaker;
		m_handlers = m_default;
	}

	protected HibernateSessionMaker getSessionMaker() {
		return m_sessionMaker;
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContextFactory#getDataContext()
	 */
	abstract public QDataContext getDataContext() throws Exception;

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContextFactory#getEventListeners()
	 */
	public QEventListenerSet getEventListeners() {
		return m_eventSet;
	}

	@Override
	public QQueryExecutorRegistry getQueryHandlerList() {
		return m_handlers;
	}
}
