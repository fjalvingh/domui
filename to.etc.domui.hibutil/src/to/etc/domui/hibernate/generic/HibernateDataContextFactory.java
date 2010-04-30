package to.etc.domui.hibernate.generic;

import to.etc.webapp.query.*;

/**
 * Utterly basic Source for Hibernate connections without any lifecycle management.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 25, 2008
 */
public class HibernateDataContextFactory extends AbstractHibernateContextFactory {
	public HibernateDataContextFactory(QEventListenerSet eventSet, HibernateSessionMaker sessionMaker, QQueryExecutorRegistry handlers) {
		super(eventSet, sessionMaker, handlers);
	}

	public HibernateDataContextFactory(QEventListenerSet eventSet, HibernateSessionMaker sessionMaker) {
		super(eventSet, sessionMaker);
	}

	/**
	 * {@inheritDoc}
	 * @see to.etc.webapp.query.QDataContextFactory#getDataContext()
	 */
	@Override
	public QDataContext getDataContext() throws Exception {
		return new HibernateQDataContext(this, getSessionMaker());
	}
}
