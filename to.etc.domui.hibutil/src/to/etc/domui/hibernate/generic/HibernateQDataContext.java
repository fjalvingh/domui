package to.etc.domui.hibernate.generic;

/**
 * Thingy which implements the generalized datacontext using Hibernate. This version starts a new
 * context for every time we re-attach.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 25, 2008
 */
public class HibernateQDataContext extends HibernaatjeBaseContext {
	HibernateQDataContext(HibernateSessionMaker sessionMaker) {
		super(sessionMaker);
	}
}
