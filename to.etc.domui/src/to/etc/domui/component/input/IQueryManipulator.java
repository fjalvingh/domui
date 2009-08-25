package to.etc.domui.component.input;

import to.etc.webapp.query.*;

/**
 * Allows manipulation of some generic query after it's creation.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 25, 2009
 */
public interface IQueryManipulator<T> {
	QCriteria<T> adjustQuery(QCriteria<T> c);
}
