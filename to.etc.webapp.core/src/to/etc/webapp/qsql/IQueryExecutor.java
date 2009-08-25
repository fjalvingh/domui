package to.etc.webapp.qsql;

import java.util.*;

import to.etc.webapp.query.*;

/**
 * Represents a generic way to execute queries using the generic QCriteria and related code.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 25, 2009
 */
public interface IQueryExecutor {
	<T> List<T> executeQuery(QDataContext dc, QCriteria<T> res) throws Exception;
	List<Object[]> query(QSelection< ? > sel) throws Exception;
}
