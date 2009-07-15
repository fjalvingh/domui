package to.etc.domui.hibernate.generic;

import to.etc.webapp.query.*;

/**
 * Thingy which implements the generalized datacontext using Hibernate. This version starts a new
 * context for every time we re-attach.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jun 25, 2008
 */
public class HibernateQDataContext extends BuggyHibernateBaseContext {
	HibernateQDataContext(final QDataContextFactory src, final HibernateSessionMaker sessionMaker) {
		super(sessionMaker, src);
	}
}
