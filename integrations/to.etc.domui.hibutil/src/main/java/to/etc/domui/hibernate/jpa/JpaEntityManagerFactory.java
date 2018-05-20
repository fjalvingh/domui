package to.etc.domui.hibernate.jpa;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.webapp.query.QDataContext;
import to.etc.webapp.query.QDataContextFactory;
import to.etc.webapp.query.QEventListenerSet;
import to.etc.webapp.query.QQueryExecutorRegistry;

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
        m_handlers.register(JpaQueryExecutor.FACTORY);
    }

    @NonNull @Override public QDataContext getDataContext() throws Exception {
        return new JpaDataContext(this);
    }

    @NonNull @Override public QEventListenerSet getEventListeners() {

        return m_eventSet;
    }

    @NonNull @Override public QQueryExecutorRegistry getQueryHandlerList() {
        return m_handlers;
    }

    static {
        m_default.register(JpaQueryExecutor.FACTORY);
    }

    public EntityManager createEntityManager() {
        return m_emFactory.createEntityManager();
    }
}
