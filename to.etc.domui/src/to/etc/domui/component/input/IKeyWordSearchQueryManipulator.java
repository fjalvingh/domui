package to.etc.domui.component.input;

import to.etc.webapp.query.*;

/**
 * Allows manipulation of generic key word search query based on predefined searchString parameter. 
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 14 Jan 2010
 */
public interface IKeyWordSearchQueryManipulator<T> {
	/**
	 * Use to manually modify query. In case that query has to be canceled return null.
	 * @param c
	 * @param keyCondition
	 * @return In case that query has to be canceled return null, otherwise return modified query.
	 */
	QCriteria<T> adjustQuery(QCriteria<T> c, String searchString);
}
