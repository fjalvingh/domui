package to.etc.domui.hibernate.jpa;

import to.etc.webapp.query.QContextManager;

import javax.persistence.EntityManagerFactory;

/**
 * Initializes the Hibernate JPA interface for DomUI.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-5-18.
 */
final public class HibernateJpaConfigurator {
	private HibernateJpaConfigurator() {
	}

	static public void initialize(EntityManagerFactory emf) {
		JpaEntityManagerFactory jef = new JpaEntityManagerFactory(emf);
		QContextManager.setImplementation(QContextManager.DEFAULT, jef);
	}
}
