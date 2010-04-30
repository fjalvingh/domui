package to.etc.domui.hibernate.generic;

import to.etc.webapp.query.*;

/**
 * This is a factory which creates contexts which reattach all their objects when the
 * context is reactivated in a conversation.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 15, 2009
 */
public class HibernateReattachingContextFactory extends AbstractHibernateContextFactory {
	public HibernateReattachingContextFactory(QEventListenerSet eventSet, HibernateSessionMaker sessionMaker, QQueryExecutorRegistry handlers) {
		super(eventSet, sessionMaker, handlers);
	}

	public HibernateReattachingContextFactory(QEventListenerSet eventSet, HibernateSessionMaker sessionMaker) {
		super(eventSet, sessionMaker);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContextFactory#getDataContext()
	 */
	@Override
	public QDataContext getDataContext() throws Exception {
		return new HibernateReattachingDataContext(this, getSessionMaker());
	}
}
