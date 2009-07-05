package to.etc.domui.hibernate.model;

import org.hibernate.*;

import to.etc.webapp.query.*;

/**
 * Thingy which helps translating generic database stuff to Hibernate specific
 * thingies.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 24, 2008
 */
public class GenericHibernateHandler {
	/**
	 * Translate generalized criteria to Hibernate criteria on a session.
	 *
	 * @param ses
	 * @param qc
	 * @return
	 */
	static public Criteria createCriteria(Session ses, QCriteria< ? > qc) {
		try {
			Criteria c = ses.createCriteria(qc.getBaseClass(), "base");
			qc.visit(new CriteriaCreatingVisitor(c));
			return c;
		} catch(RuntimeException x) {
			throw x;
		} catch(Exception x) {
			throw new RuntimeException(x); // Cannot happen.
		}
	}
}
