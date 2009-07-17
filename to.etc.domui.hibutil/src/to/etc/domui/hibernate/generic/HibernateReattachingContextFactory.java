package to.etc.domui.hibernate.generic;

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
	 * @see to.etc.webapp.query.QDataContextFactory#getEventListeners()
	 */
	public QEventListenerSet getEventListeners() {
		return m_eventSet;
	}
}
