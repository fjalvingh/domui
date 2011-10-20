package to.etc.domui.component.input;

import to.etc.webapp.query.*;

/**
 * Custom query factory.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 20 Oct 2011
 */
public interface IQueryFactory<T> {
	/**
	 * Create query
	 * @return
	 */
	QCriteria<T> createQuery();

}
