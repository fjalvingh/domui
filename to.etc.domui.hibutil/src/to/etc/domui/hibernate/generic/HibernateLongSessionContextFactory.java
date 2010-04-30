package to.etc.domui.hibernate.generic;

import to.etc.webapp.query.*;

public class HibernateLongSessionContextFactory extends AbstractHibernateContextFactory {
	public HibernateLongSessionContextFactory(QEventListenerSet eventSet, HibernateSessionMaker sessionMaker, QQueryExecutorRegistry handlers) {
		super(eventSet, sessionMaker, handlers);
	}

	public HibernateLongSessionContextFactory(QEventListenerSet eventSet, HibernateSessionMaker sessionMaker) {
		super(eventSet, sessionMaker);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContextFactory#getDataContext()
	 */
	@Override
	public QDataContext getDataContext() throws Exception {
		return new HibernateLongSessionContext(this, getSessionMaker());
	}
}
