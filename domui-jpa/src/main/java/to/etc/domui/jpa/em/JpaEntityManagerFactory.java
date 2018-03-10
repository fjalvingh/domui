package to.etc.domui.jpa.em;

import to.etc.webapp.query.QDataContext;
import to.etc.webapp.query.QDataContextFactory;
import to.etc.webapp.query.QEventListenerSet;
import to.etc.webapp.query.QQueryExecutorRegistry;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 10-3-18.
 */
public class JpaEntityManagerFactory implements QDataContextFactory {
    private final EntityManagerFactory m_emFactory;

    static private QQueryExecutorRegistry m_default = new QQueryExecutorRegistry();

    private QEventListenerSet m_eventSet = new QEventListenerSet();

    private QQueryExecutorRegistry m_handlers = new QQueryExecutorRegistry();


    public JpaEntityManagerFactory(EntityManagerFactory emFactory) {
        m_emFactory = emFactory;
    }

    @Nonnull @Override public QDataContext getDataContext() throws Exception {
        return new JpaDataContext(this);
    }

    @Nonnull @Override public QEventListenerSet getEventListeners() {

        return m_eventSet;
    }

    @Nonnull @Override public QQueryExecutorRegistry getQueryHandlerList() {
        return m_handlers;
    }

    static {
        m_default.register(JpaQueryExecutor.FACTORY);
    }

    public EntityManager createEntityManager() {
        return m_emFactory.createEntityManager();
    }
}
