package to.etc.domui.hibernate.generic;

import org.hibernate.*;

/**
 * Thingy which makes a new Hibernate session.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 26, 2008
 */
public interface HibernateSessionMaker {
	public Session			makeSession() throws Exception;
}
