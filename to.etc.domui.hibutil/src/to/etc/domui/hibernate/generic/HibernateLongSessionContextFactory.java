package to.etc.domui.hibernate.generic;

import java.util.*;

import to.etc.webapp.query.*;

public class HibernateLongSessionContextFactory implements QDataContextFactory {
	protected HibernateSessionMaker m_sessionMaker;

	private QEventListenerSet m_eventSet;

	public HibernateLongSessionContextFactory(QEventListenerSet eventSet, HibernateSessionMaker sessionMaker) {
		m_eventSet = eventSet;
		m_sessionMaker = sessionMaker;
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
	 * @see to.etc.webapp.query.QDataContextFactory#releaseDataContext(to.etc.webapp.query.QDataContext)
	 */
	public void closeDataContext(QDataContext dc) {
		BuggyHibernateBaseContext q = (BuggyHibernateBaseContext) dc;
		q.internalClose();
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContextFactory#getListenerIterator()
	 */
	public Iterator<IQueryListener> getListenerIterator() {
		return m_eventSet.getListenerIterator();
	}
}
