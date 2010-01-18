package to.etc.domui.component.input;

import to.etc.webapp.query.*;

/**
 * Allows manipulation of generic key word search query based on predefined keyCondition parameter. 
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 14 Jan 2010
 */
public interface IKeyWordSearchQueryManipulator<T> {
	QCriteria<T> adjustQuery(QCriteria<T> c, String keyCondition);
}
