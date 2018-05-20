package to.etc.domui.hibernate.jpa;

import org.hibernate.Session;
import org.hibernate.internal.SessionImpl;

import javax.persistence.EntityManager;
import java.sql.Connection;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 10-3-18.
 */
public class JpaConnector {
    /**
     * Get the Hibernate Session object that underlies a JPA connection. Only valid
     * for Hibernate JPA, obviously.
     */
    static public Connection getConnection(EntityManager manager) {
        Session session = manager.unwrap(Session.class);
        return ((SessionImpl) session).connection();
    }

    static public Session getHibernateSession(EntityManager em) {
        Session session = em.unwrap(Session.class);
        return session;
    }

}
