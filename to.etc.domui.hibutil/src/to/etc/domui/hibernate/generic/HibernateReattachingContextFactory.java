package to.etc.domui.hibernate.generic;

import java.util.*;

import to.etc.webapp.query.*;

/**
 * This is a factory which creates contexts which reattach all their objects when the
 * context is reactivated in a conversation.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 15, 2009
 */
public class HibernateReattachingContextFactory implements QDataContextFactory {
	private HibernateSessionMaker m_sessionMaker;

	private QEventListenerSet m_eventSet;

	public HibernateReattachingContextFactory(QEventListenerSet set, HibernateSessionMaker sessionMaker) {
		m_eventSet = set;
		m_sessionMaker = sessionMaker;
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContextFactory#getDataContext()
	 */
	public QDataContext getDataContext() throws Exception {
		return new HibernateReattachingDataContext(this, m_sessionMaker);
	}

	/**
	 * {@inheritDoc}
	 * @param dc
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
